package com.pokebros.android.pokemononline;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.launcher.DragController;
import com.android.launcher.DragLayer;
import com.android.launcher.PokeDragIcon;
import com.pokebros.android.pokemononline.battle.Battle;
import com.pokebros.android.pokemononline.battle.BattleMove;
import com.pokebros.android.pokemononline.battle.Type;
import com.pokebros.android.pokemononline.poke.BattlePoke;
import com.pokebros.android.pokemononline.poke.ShallowBattlePoke;
import com.pokebros.android.pokemononline.poke.ShallowShownPoke;
import com.pokebros.android.pokemononline.poke.UniqueID;
import com.pokebros.android.pokemononline.poke.PokeEnums.Gender;
import com.pokebros.android.pokemononline.poke.PokeEnums.Status;

import de.marcreichelt.android.RealViewSwitcher;

public class BattleActivity extends Activity {
	public enum BattleDialog {
		RearrangeTeam,
		ConfirmForfeit, 
		OppDynamicInfo, 
		MyDynamicInfo,
		MoveInfo
	}

	public final static int SWIPE_TIME_THRESHOLD = 100;
    final static String pkgName = "com.pokebros.android.pokemononline";
    
	DragLayer mDragLayer;
	
	RealViewSwitcher realViewSwitcher;
	RelativeLayout battleView;
	TextProgressBar[] hpBars = new TextProgressBar[2];
	TextView[] currentPokeNames = new TextView[2];
	TextView[] currentPokeLevels = new TextView[2];
	ImageView[] currentPokeGenders = new ImageView[2];
	ImageView[] currentPokeStatuses = new ImageView[2];
	
	TextView[] attackNames = new TextView[4];
	TextView[] attackPPs = new TextView[4];
	RelativeLayout[] attackLayouts = new RelativeLayout[4];
	
	TextView[] timers = new TextView[2];
	TextView[] pokeListNames = new TextView[6];
	TextView[] pokeListItems = new TextView[6];
	TextView[] pokeListAbilities = new TextView[6];
	TextView[] pokeListHPs = new TextView[6];
	ImageView[] pokeListIcons = new ImageView[6];
	
	PokeDragIcon[] myArrangePokeIcons = new PokeDragIcon[6];
	ImageView[] oppArrangePokeIcons = new ImageView[6];
	
	RelativeLayout[] pokeListButtons = new RelativeLayout[6];
	TextView[][] pokeListMovePreviews = new TextView[6][4];
	TextView infoView;
	ScrollView infoScroll;
	TextView[] names = new TextView[2];
	ImageView[][] pokeballs = new ImageView[2][6];
	ImageView[] pokeSprites = new ImageView[2];

	RelativeLayout struggleLayout;
	LinearLayout attackRow1;
	LinearLayout attackRow2;
	
	Battle battle = null;
	
	BattleMove lastClickedMove;
	
	Resources resources;
	public NetworkService netServ = null;
	int me, opp;
	
	class HpAnimator implements Runnable {
		int i, goal;
		boolean finished;

		public void setGoal(int i, int goal) {
			this.i = i;
			this.goal = goal;
			finished = false;
		}
		
		public void run() {
			while(goal < hpBars[i].getProgress()) {
				runOnUiThread(new Runnable() {
					public void run() {
						hpBars[i].incrementProgressBy(-1);
						hpBars[i].setText(hpBars[i].getProgress() + "%");
						checkHpColor();
					}
				});
				try {
					Thread.sleep(40);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}
			}
			while(goal > hpBars[i].getProgress()) {
				runOnUiThread(new Runnable() {
					public void run() {
						hpBars[i].incrementProgressBy(1);
						hpBars[i].setText(hpBars[i].getProgress() + "%");
						checkHpColor();
					}
				});
				try {
					Thread.sleep(40);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}
			}
			
			synchronized (battle) {
				battle.notify();
			}
		}

		public void setHpBarToGoal() {
			runOnUiThread(new Runnable() {
				public void run() {
					hpBars[i].setProgress(goal);
					hpBars[i].setText(hpBars[i].getProgress() + "%");
					checkHpColor();
				}
			});
		}
		
		void checkHpColor() {
			runOnUiThread(new Runnable() {
				public void run() {
					int progress = hpBars[i].getProgress();
					Rect bounds = hpBars[i].getProgressDrawable().getBounds();
					if(progress > 50)
						hpBars[i].setProgressDrawable(resources.getDrawable(R.drawable.green_progress));
					else if(progress <= 50 && progress > 20)
						hpBars[i].setProgressDrawable(resources.getDrawable(R.drawable.yellow_progress));
					else
						hpBars[i].setProgressDrawable(resources.getDrawable(R.drawable.red_progress));
					hpBars[i].getProgressDrawable().setBounds(bounds);
					// XXX the hp bars won't display properly unless I do this. Spent many hours trying
					// to figure out why
					int increment = (hpBars[i].getProgress() == 100) ? -1 : 1;
					hpBars[i].incrementProgressBy(increment);
					hpBars[i].incrementProgressBy(-1 * increment);
				}
			});
		}
	};
	
	public HpAnimator hpAnimator = new HpAnimator();
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	System.out.println("BattleActivity Created");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.battle_pokeviewer);

        bindService(new Intent(BattleActivity.this, NetworkService.class), connection,
        		Context.BIND_AUTO_CREATE);
        
        resources = getResources();
        realViewSwitcher = (RealViewSwitcher)findViewById(R.id.battlePokeSwitcher);
        
        for(int i = 0; i < 4; i++) {
        	attackNames[i] = (TextView)findViewById(resources.getIdentifier("attack" + (i+1) + "Name", "id", pkgName));
        	attackPPs[i] = (TextView)findViewById(resources.getIdentifier("attack" + (i+1) + "PP", "id", pkgName));
        	attackLayouts[i] = (RelativeLayout)findViewById(resources.getIdentifier("attack" + (i+1) + "Layout", "id", pkgName));
        	attackLayouts[i].setOnClickListener(battleListener);
        	attackLayouts[i].setOnLongClickListener(moveListener);
        }
        for(int i = 0; i < 6; i++) {
        	pokeListNames[i] = (TextView)findViewById(resources.getIdentifier("pokename" + (i+1), "id", pkgName));
        	pokeListHPs[i] = (TextView)findViewById(resources.getIdentifier("hp" + (i+1), "id", pkgName));
        	pokeListItems[i] = (TextView)findViewById(resources.getIdentifier("item" + (i+1), "id", pkgName));
        	pokeListAbilities[i] = (TextView)findViewById(resources.getIdentifier("ability" + (i+1), "id", pkgName));
        	pokeListButtons[i] = (RelativeLayout)findViewById(resources.getIdentifier("pokeViewLayout" + (i+1), "id", pkgName));
        	pokeListButtons[i].setOnClickListener(battleListener);
        	pokeListIcons[i] = (ImageView)findViewById(resources.getIdentifier("pokeViewIcon" + (i+1), "id", pkgName));
        	
        	for(int j = 0; j < 4; j++)
        		pokeListMovePreviews[i][j] = (TextView)findViewById(resources.getIdentifier("p" + (i+1) + "_attack" + (j+1), "id", pkgName));
        }
        
        infoView = (TextView)findViewById(R.id.infoWindow);
        infoScroll = (ScrollView)findViewById(R.id.infoScroll);
        battleView = (RelativeLayout)findViewById(R.id.battleScreen);
        
    	struggleLayout = (RelativeLayout)findViewById(R.id.struggleLayout);
    	attackRow1 = (LinearLayout)findViewById(R.id.attackRow1);
    	attackRow2 = (LinearLayout)findViewById(R.id.attackRow2);
    	
    	struggleLayout.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			netServ.socket.sendMessage(battle.constructAttack((byte)-1), Command.BattleMessage); // This is how you struggle
    		}
    	});
    }
	
	private Handler handler = new Handler();
    
	private Runnable updateTimeTask = new Runnable() {
		public void run() {
			for(int i = 0; i < 2; i++) {
				int seconds;
				if (battle.ticking[i]) {
					long millis = SystemClock.uptimeMillis()
					- battle.startingTime[i];
					seconds = battle.remainingTime[i] - (int) (millis / 1000);
				}
				else
					seconds = battle.remainingTime[i];

				if(seconds < 0) seconds = 0;
				else if(seconds > 300) seconds = 300;

				int minutes = (seconds / 60);
				seconds = seconds % 60;
				timers[i].setText(String.format("%02d:%02d", minutes, seconds));
			}
			handler.postDelayed(this, 200);
		}
	};
	
	public void setHpBarTo(final int i, final int goal) {
		hpAnimator.setGoal(i, goal);
		hpAnimator.setHpBarToGoal();
	}
	
	public void animateHpBarTo(final int i, final int goal) {
		hpAnimator.setGoal(i, goal);
		new Thread(hpAnimator).start();
	}
	
	public void updateBattleInfo(boolean scroll) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (!netServ.hasBattle())
					return;
				synchronized (battle.histDelta) {
					infoView.append(battle.histDelta);
					if (battle.histDelta.length() != 0 || true) {
						infoScroll.post(new Runnable() {
							public void run() {
								infoScroll.smoothScrollTo(0, infoView.getMeasuredHeight());
							}
						});
					}
					infoScroll.invalidate();
					battle.hist.append(battle.histDelta);
					battle.histDelta.clear();
				}
			}
		});
	}
	
	public void updatePokes(byte player) {
		if (player == me)
			updateMyPoke();
		else
			updateOppPoke();
	}
	
	public void updatePokeballs() {
		runOnUiThread(new Runnable() {
			public void run() {
				for (int i = 0; i < 2; i++) {
					for (int j = 0; j < 6; j++) {
						pokeballs[i][j].setImageResource(resources.getIdentifier("status" + battle.pokes[i][j].status(), "drawable", pkgName));
					}
				}
			}
		});
	}

	private Drawable getSprite(ShallowBattlePoke poke, boolean front) {
        String res;
        
        if (battle.shouldShowPreview || poke.status() == Status.Koed.poValue())
        	res = "empty_sprite";
        else if (poke.sub)
        	res = (front ? "sub_front" : "sub_back");
        else {
        	UniqueID uID;
        	if (poke.specialSprites.isEmpty())
        		uID = poke.uID;
        	else
        		uID = poke.specialSprites.peek();
        	if (uID.pokeNum < 0)
        		res = "empty_sprite";
        	else {
	        	res = "p" + uID.pokeNum + (uID.subNum == 0 ? "" : "_" + uID.subNum) +
	        			(front ? "_front" : "_back");
	        	if (poke.gender != Gender.Female.ordinal())
	        		res = res + (poke.shiny ? "s" : "");
	        	else {
	        		if (resources.getIdentifier(res + "f", "drawable", pkgName) == 0)
	        			// No special female sprite
	        			res = res + (poke.shiny ? "s" : "");
	        		else
	        			res = res + "f" + (poke.shiny ? "s" : "");
	        	}
        	}
        }
        System.out.println("SPRITE: " + res);
        return resources.getDrawable(resources.getIdentifier(res, "drawable", pkgName));
	}

	public void updateCurrentPokeListEntry() {
		runOnUiThread(new Runnable() {
			public void run() {
				synchronized(battle) {
					BattlePoke battlePoke = battle.myTeam.pokes[0];
					pokeListHPs[0].setText(battlePoke.currentHP +
							"/" + battlePoke.totalHP);
				}
				// TODO: Status ailments and stuff
			}
		});
	}

	public void updateMovePP(final int moveNum) {
		runOnUiThread(new Runnable() {
			public void run() {
				BattleMove move = battle.displayedMoves[moveNum];
				attackPPs[moveNum].setText("PP " + move.currentPP + "/" + move.totalPP);
			}
		});
	}
	public void updateMyPoke() {
		runOnUiThread(new Runnable() {
			public void run() {
				ShallowBattlePoke poke = battle.currentPoke(me);
				// Load correct moveset and name
				if(poke != null) {
					currentPokeNames[me].setText(poke.rnick);
					currentPokeLevels[me].setText("Lv. " + poke.level);
					currentPokeGenders[me].setImageResource(resources.getIdentifier("battle_gender" + poke.gender, "drawable", pkgName));
					currentPokeStatuses[me].setImageResource(resources.getIdentifier("battle_status" + poke.status(), "drawable", pkgName));
					setHpBarTo(me, poke.lifePercent);
					BattlePoke battlePoke = battle.myTeam.pokes[0];
			        for(int i = 0; i < 4; i++) {
			        	BattleMove move = battle.displayedMoves[i];
			        	updateMovePP(i);
			        	attackNames[i].setText(move.toString());
			        	String type;
			        	if (move.num == 237)
			        		type = Type.values()[battlePoke.hiddenPowerType()].toString();
			        	else
			        		type = move.getTypeString();
			        	type = type.toLowerCase();
			        	attackLayouts[i].setBackgroundResource(resources.getIdentifier(type + "_type_button",
					      		"drawable", pkgName));
			        }
		        	pokeSprites[me].setImageDrawable(getSprite(poke, false));
				}
			}
		});
		updateTeam();
	}
	
	public void updateOppPoke() {
		runOnUiThread(new Runnable() {
			public void run() {
					ShallowBattlePoke poke = battle.currentPoke(opp);
					// Load correct moveset and name
					if(poke != null) {
						currentPokeNames[opp].setText(poke.rnick);
						currentPokeLevels[opp].setText("Lv. " + poke.level);
						currentPokeGenders[opp].setImageResource(resources.getIdentifier("battle_gender" + poke.gender, "drawable", pkgName));
						currentPokeStatuses[opp].setImageResource(resources.getIdentifier("battle_status" + poke.status(), "drawable", pkgName));
						setHpBarTo(opp, poke.lifePercent);
						pokeSprites[opp].setImageDrawable(getSprite(poke, true));
					}
				}
		});		
	}
	
	public void updateButtons() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (!checkStruggle()) {
					for (int i = 0; i < 4; i++) {
						if (battle.allowAttack && !battle.clicked) {
							setAttackButtonEnabled(i, battle.allowAttacks[i]);
						}
						else {
							setAttackButtonEnabled(i, false);
						}
					}
				}
				for(int i = 0; i < 6; i++) {
					if (battle.myTeam.pokes[i].status() != Status.Koed.poValue() && !battle.clicked)
						setPokeListButtonEnabled(i, battle.allowSwitch);
					else
						setPokeListButtonEnabled(i, false);
				}
			}
		});
	}
	
	public boolean checkStruggle() {
		// This method should hide moves, show the button if necessary and return whether it showed the button
		boolean struggle = battle.shouldStruggle;
		if(struggle) {
			attackRow1.setVisibility(View.GONE);
			attackRow2.setVisibility(View.GONE);
			struggleLayout.setVisibility(View.VISIBLE);
		}
		else {
			attackRow1.setVisibility(View.VISIBLE);
			attackRow2.setVisibility(View.VISIBLE);
			struggleLayout.setVisibility(View.GONE);
		}
		return struggle;
	}
	
	private Drawable getIcon(UniqueID uid) {
		int resID = resources.getIdentifier("pi" + uid.pokeNum +
				(uid.subNum == 0 ? "" : "_" + uid.subNum) +
				"_icon", "drawable", pkgName);
		if (resID == 0)
			resID = resources.getIdentifier("pi" + uid.pokeNum + "_icon",
					"drawable", pkgName);
		return resources.getDrawable(resID);
	}
	
	public void updateTeam() {
		runOnUiThread(new Runnable() {
			public void run() {
				for (int i = 0; i < 6; i++) {
					BattlePoke poke = battle.myTeam.pokes[i];
					pokeListIcons[i].setImageDrawable(getIcon(poke.uID));
					pokeListNames[i].setText(poke.nick);
					pokeListHPs[i].setText(poke.currentHP +
							"/" + poke.totalHP);
					pokeListItems[i].setText(poke.itemString);
					pokeListAbilities[i].setText(poke.abilityString);
					for (int j = 0; j < 4; j++) {
						pokeListMovePreviews[i][j].setText(poke.moves[j].toString());
						pokeListMovePreviews[i][j].setShadowLayer((float)1, 1, 1, resources.getColor(battle.allowSwitch && !battle.clicked ? R.color.poke_text_shadow_enabled : R.color.poke_text_shadow_disabled));
			        	String type;
			        	if (poke.moves[j].num == 237)
			        		type = Type.values()[poke.hiddenPowerType()].toString();
			        	else
			        		type = poke.moves[j].getTypeString();
			        	type = type.toLowerCase();
			        	pokeListMovePreviews[i][j].setBackgroundResource(resources.getIdentifier(type + "_type_button",
					      		"drawable", pkgName));
					}
				}
			}
		});
	}

	public void switchToPokeViewer() {
		runOnUiThread(new Runnable() {
			public void run() {
				realViewSwitcher.snapToScreen(1);
			}
		});
	}
	
	public void onResume() {
		// XXX we might want more stuff here
		super.onResume();
		if (battle != null)
			checkRearrangeTeamDialog();
	}
	
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ = ((NetworkService.LocalBinder)service).getService();
			netServ.herp();
			battle = netServ.battle;
			if (!netServ.hasBattle()) {
	    		startActivity(new Intent(BattleActivity.this, ChatActivity.class));
				finish();
				return;
			}
			netServ.showNotification(BattleActivity.class, "Battle");

			battleView.setBackgroundResource(resources.getIdentifier("bg" + battle.background, "drawable", pkgName));
			
			// Set the UI to display the correct info
	        me = battle.me;
	        opp = battle.opp;
	        // We don't know which timer is which until the battle starts,
	        // so set them here.
	        timers[me] = (TextView)findViewById(R.id.timerB);
	        timers[opp] = (TextView)findViewById(R.id.timerA);

	        names[me] = (TextView)findViewById(R.id.nameB);
	        names[opp] = (TextView)findViewById(R.id.nameA);

	        for (int i = 0; i < 6; i++) {
		        pokeballs[me][i] = (ImageView)findViewById(resources.getIdentifier("pokeball" + (i + 1) + "B", "id", pkgName));
		        pokeballs[opp][i] = (ImageView)findViewById(resources.getIdentifier("pokeball" + (i + 1) + "A", "id", pkgName));
	        }
	        updatePokeballs();
	        
	        names[me].setText(battle.players[me].nick());
	        names[opp].setText(battle.players[opp].nick());

	        hpBars[me] = (TextProgressBar)findViewById(R.id.hpBarB);
	        hpBars[opp] = (TextProgressBar)findViewById(R.id.hpBarA);
	        
	        currentPokeNames[me] = (TextView)findViewById(R.id.currentPokeNameB);
	        currentPokeNames[opp] = (TextView)findViewById(R.id.currentPokeNameA);

	        currentPokeLevels[me] = (TextView)findViewById(R.id.currentPokeLevelB);
	        currentPokeLevels[opp] = (TextView)findViewById(R.id.currentPokeLevelA);
	        
	        currentPokeGenders[me] = (ImageView)findViewById(R.id.currentPokeGenderB);
	        currentPokeGenders[opp] = (ImageView)findViewById(R.id.currentPokeGenderA);
	        
	        currentPokeStatuses[me] = (ImageView)findViewById(R.id.currentPokeStatusB);
	        currentPokeStatuses[opp] = (ImageView)findViewById(R.id.currentPokeStatusA);
	        
	        pokeSprites[me] = (ImageView)findViewById(R.id.pokeSpriteB);
	        pokeSprites[opp] = (ImageView)findViewById(R.id.pokeSpriteA);
	        for(int i = 0; i < 2; i++)
	        	pokeSprites[i].setOnLongClickListener(spriteListener);
	        
	        
	        infoView.setOnLongClickListener(new OnLongClickListener() {
				public boolean onLongClick(View view) {
			        final EditText input = new EditText(BattleActivity.this);
					new AlertDialog.Builder(BattleActivity.this)
					.setTitle("Battle Chat")
					.setMessage("Send Battle Message")
					.setView(input)
					.setPositiveButton("Send", new DialogInterface.OnClickListener() {			
						public void onClick(DialogInterface i, int j) {
							String message = input.getText().toString();
							if (message.length() > 0) {
								Baos msg = new Baos();
								msg.putInt(BattleActivity.this.battle.bID);
								msg.putString(message);
					    		netServ.socket.sendMessage(msg, Command.BattleChat);
							}
						}
					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
						}
					}).show();
					return false;
				}
			});

	        // Don't set battleActivity until after we've finished
	        // getting UI elements. Otherwise there's a race condition if Battle
	        // wants to update one of our UI elements we haven't gotten yet.
	        synchronized(battle) {
	        	netServ.battleActivity = BattleActivity.this;
	        }

	        // Load scrollback
	        infoView.setText(battle.hist);
	        updateBattleInfo(true);
	    	
	    	// Prompt a UI update of the pokemon
	        updateMyPoke();
	        updateOppPoke();
	        
	        // Enable or disable buttons
	        updateButtons();
	        
	    	// Start timer updating
	        handler.postDelayed(updateTimeTask, 100);
	        
			checkRearrangeTeamDialog();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			netServ.battleActivity = null;
			netServ = null;
		}
	};
	
	public void end() {
		runOnUiThread(new Runnable() { public void run() { BattleActivity.this.finish(); } } );
	}
  
    @Override
    public void onDestroy() {
    	unbindService(connection);
    	super.onDestroy();
    }

    public OnTouchListener dialogListener = new OnTouchListener() {
    	public boolean onTouch(View v, MotionEvent e) {
    		int id = v.getId();
    		for(int i = 0; i < 6; i++) {
    			if(id == myArrangePokeIcons[i].getId() && e.getAction() == MotionEvent.ACTION_DOWN) {
    				Object dragInfo = v;
    				mDragLayer.startDrag(v, myArrangePokeIcons[i], dragInfo, DragController.DRAG_ACTION_MOVE);
    				break;
    			}
    		}
    		return true;
    	}
    };
    
    public OnClickListener battleListener = new OnClickListener() {
    	public void onClick(View v) {
    		int id = v.getId();
    		// Check to see if click was on attack button
    		for(int i = 0; i < 4; i++)
    			if(id == attackLayouts[i].getId())
    				netServ.socket.sendMessage(battle.constructAttack((byte)i), Command.BattleMessage);
    		// Check to see if click was on pokelist button
    		for(int i = 0; i < 6; i++) {
    			if(id == pokeListButtons[i].getId()) {
    				netServ.socket.sendMessage(battle.constructSwitch((byte)i), Command.BattleMessage);
    				realViewSwitcher.snapToScreen(0);
    			}
    		}
    		battle.clicked = true;
    		updateButtons();
    	}
    };
    

    public OnLongClickListener moveListener = new OnLongClickListener() {
    	public boolean onLongClick(View v) {
    		int id = v.getId();
    		for(int i = 0; i < 4; i++)
    			if(id == attackLayouts[i].getId() && !attackNames[i].equals("No Move")) {
    				lastClickedMove = battle.displayedMoves[i];
    				showDialog(BattleDialog.MoveInfo.ordinal());
    				return true;
    			}
    		return false;
    	}
    };
    
    public OnLongClickListener spriteListener = new OnLongClickListener() {
		public boolean onLongClick(View v) {
			if(v.getId() == pokeSprites[me].getId())
				showDialog(BattleDialog.MyDynamicInfo.ordinal());
			else
				showDialog(BattleDialog.OppDynamicInfo.ordinal());
			return true;
		}	
    };
    
    void setPokeListButtonEnabled(int num, boolean enabled) {
    	setLayoutEnabled(pokeListButtons[num], enabled);
    	setTextViewEnabled(pokeListNames[num], enabled);
    	setTextViewEnabled(pokeListItems[num], enabled);
    	setTextViewEnabled(pokeListAbilities[num], enabled);
    	setTextViewEnabled(pokeListHPs[num], enabled);
    	//setTextViewEnabled(pokeListIcons[num], enabled);
    	for(int i = 0; i < 4; i++)
    		setTextViewEnabled(pokeListMovePreviews[num][i], enabled);
    }
    
    void setAttackButtonEnabled(int num, boolean enabled) {
    	attackLayouts[num].setEnabled(enabled);
		attackNames[num].setEnabled(enabled);
		attackNames[num].setShadowLayer((float)1.5, 1, 1, resources.getColor(enabled ? R.color.poke_text_shadow_enabled : R.color.poke_text_shadow_disabled));
		attackPPs[num].setEnabled(enabled);
		attackPPs[num].setShadowLayer((float)1.5, 1, 1, resources.getColor(enabled ? R.color.pp_text_shadow_enabled : R.color.pp_text_shadow_disabled));
    }
    
    void setLayoutEnabled(ViewGroup v, boolean enabled) {
    	v.setEnabled(enabled);
    	v.getBackground().setAlpha(enabled ? 255 : 128);
    }
    
    void setTextViewEnabled(TextView v, boolean enabled) {
    	v.setEnabled(enabled);
    	v.setTextColor(v.getTextColors().withAlpha(enabled ? 255 : 128).getDefaultColor());
    }
    @Override
    public void onBackPressed() {
    	if(netServ != null && netServ.hasBattle() && !battle.gotEnd)
    		netServ.socket.sendMessage(battle.constructCancel(), Command.BattleMessage);
    	else {
    		startActivity(new Intent(BattleActivity.this, ChatActivity.class));
    		finish();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.battleoptions, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.to_chat:
    		startActivity(new Intent(this, ChatActivity.class));
    		finish();
    		break;
    	case R.id.forfeit:
    		if (netServ != null && netServ.hasBattle() && !battle.gotEnd)
    			showDialog(BattleDialog.ConfirmForfeit.ordinal());
    		break;
    	case R.id.draw:
    		//TODO: Offer Draw
    		//showRearrangeTeamDialog();
    		break;
        }
        return true;
    }

	public void notifyRearrangeTeamDialog() {
		runOnUiThread(new Runnable() { public void run() { checkRearrangeTeamDialog(); } } );
	}
	
	private void checkRearrangeTeamDialog() {
		if (netServ != null && netServ.hasBattle() && battle.shouldShowPreview) {
			showDialog(BattleDialog.RearrangeTeam.ordinal());
		}
	}
    
	void endBattle() {
		if (netServ != null && netServ.socket != null && netServ.socket.isConnected() && netServ.hasBattle()) {
    		Baos bID = new Baos();
    		bID.putInt(battle.bID);
    		netServ.socket.sendMessage(bID, Command.BattleFinished);
		}
	}
	
    protected Dialog onCreateDialog(final int id) {
    	int player = me;
    	final AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        switch(BattleDialog.values()[id]) {
        case RearrangeTeam: {
        	View layout = inflater.inflate(R.layout.rearrange_team_dialog, (LinearLayout)findViewById(R.id.rearrange_team_dialog));
        	builder.setView(layout)
        	.setPositiveButton("Done", new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface dialog, int which) {
        			netServ.socket.sendMessage(battle.constructRearrange(), Command.BattleMessage);
        			battle.shouldShowPreview = false;
        			removeDialog(id);
        		}})
        		.setCancelable(false);
        	dialog = builder.create();

        	mDragLayer = (DragLayer)layout.findViewById(R.id.drag_my_poke);
        	for(int i = 0; i < 6; i++){
        		BattlePoke poke = battle.myTeam.pokes[i];
        		myArrangePokeIcons[i] = (PokeDragIcon)layout.findViewById(resources.getIdentifier("my_arrange_poke" + (i+1), "id", pkgName));
        		myArrangePokeIcons[i].setOnTouchListener(dialogListener);
        		myArrangePokeIcons[i].setImageDrawable(getIcon(poke.uID));
        		myArrangePokeIcons[i].num = i;
        		myArrangePokeIcons[i].battleActivity = this;

        		ShallowShownPoke oppPoke = battle.oppTeam.pokes[i];
        		oppArrangePokeIcons[i] = (ImageView)layout.findViewById(resources.getIdentifier("foe_arrange_poke" + (i+1), "id", pkgName));
        		oppArrangePokeIcons[i].setImageDrawable(getIcon(oppPoke.uID));
        	}
            return dialog;
        }
        case ConfirmForfeit:
			builder.setMessage("Really Forfeit?")
			.setCancelable(true)
			.setPositiveButton("Forfeit", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					endBattle();
				}
			})
			.setNegativeButton("Cancel", null);
			return builder.create();
        case OppDynamicInfo:
        	player = opp;
        case MyDynamicInfo:
        	if(netServ != null) {
        		final Dialog simpleDialog = new Dialog(this);
        		simpleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        		simpleDialog.setContentView(R.layout.dynamic_info_layout);

        		TextView t = (TextView)simpleDialog.findViewById(R.id.nameTypeView); 
        		t.setText((player == me ? battle.myTeam.pokes[0] : battle.currentPoke(player)).nameAndType());
				t = (TextView)simpleDialog.findViewById(R.id.statNamesView);
        		t.setText(battle.dynamicInfo[player].statsAndHazards());
        		t = (TextView)simpleDialog.findViewById(R.id.statNumsView);
        		if (player == me)
	        		t.setText(battle.myTeam.pokes[0].printStats());
        		else
        			t.setVisibility(View.GONE);
        		t = (TextView)simpleDialog.findViewById(R.id.statBoostView);
        		String s = battle.dynamicInfo[player].boosts(player == me);
        		if (!"\n\n\n\n".equals(s))
        			t.setText(s);
        		else
        			t.setVisibility(View.GONE);
        		
        		simpleDialog.setCanceledOnTouchOutside(true);
        		simpleDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						removeDialog(id);
					}
        		});
        		simpleDialog.findViewById(R.id.dynamic_info_layout).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						simpleDialog.cancel();
					}
        		});
        		return simpleDialog;
        	}
        case MoveInfo:
        	dialog = builder.setTitle(lastClickedMove.name)
        	.setMessage(lastClickedMove.descAndEffects())
        	.setOnCancelListener(new DialogInterface.OnCancelListener() {
        		public void onCancel(DialogInterface dialog) {
        			removeDialog(id);
        		}
        	})
        	.create();
        	dialog.setCanceledOnTouchOutside(true);
        	return dialog;
        default:
            return new Dialog(this);
        }
    }
}