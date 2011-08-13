package com.pokebros.android.pokemononline;

import com.pokebros.android.pokemononline.battle.BattleMove;
import com.pokebros.android.pokemononline.battle.Type;
import java.util.Random;

import com.android.launcher.DragController;
import com.android.launcher.DragLayer;
import com.android.launcher.DragSource;
import com.android.launcher.PokeDragIcon;
import com.pokebros.android.pokemononline.poke.BattlePoke;
import com.pokebros.android.pokemononline.poke.ShallowShownPoke;
import com.pokebros.android.pokemononline.poke.UniqueID;
import com.pokebros.android.pokemononline.poke.PokeEnums.Gender;
import com.pokebros.android.pokemononline.poke.ShallowBattlePoke;
import com.pokebros.android.pokemononline.poke.PokeEnums.Status;

import de.marcreichelt.android.RealViewSwitcher;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class BattleActivity extends Activity {
	public enum BattleDialog {
		RearrangeTeam,
		ConfirmForfeit, 
		OppDynamicInfo, 
		MyDynamicInfo
	}

	public final static int SWIPE_TIME_THRESHOLD = 100;
    final String packName = "com.pokebros.android.pokemononline";
    
	DragLayer mDragLayer;
	
	RealViewSwitcher realViewSwitcher;
	RelativeLayout battleView;
	TextProgressBar[] hpBars = new TextProgressBar[2];
	TextView[] currentPokeNames = new TextView[2];
	
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
	ImageView[] pokeSprites = new ImageView[2];
	
	LinearLayout topAttackRowLayout;
	LinearLayout bottomAttackRowLayout;
	LinearLayout struggleRowLayout;
	RelativeLayout struggleLayout;
	
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
			
			synchronized (netServ.battle) {
				netServ.battle.notify();
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
        setContentView(R.layout.battle_pokeviewer);

        bindService(new Intent(BattleActivity.this, NetworkService.class), connection,
        		Context.BIND_AUTO_CREATE);
        
        resources = getResources();
        realViewSwitcher = (RealViewSwitcher)findViewById(R.id.battlePokeSwitcher);
        
        for(int i = 0; i < 4; i++) {
        	attackNames[i] = (TextView)findViewById(resources.getIdentifier("attack" + (i+1) + "Name", "id", packName));
        	attackPPs[i] = (TextView)findViewById(resources.getIdentifier("attack" + (i+1) + "PP", "id", packName));
        	attackLayouts[i] = (RelativeLayout)findViewById(resources.getIdentifier("attack" + (i+1) + "Layout", "id", packName));
        	attackLayouts[i].setOnClickListener(battleListener);
        }
        for(int i = 0; i < 6; i++) {
        	pokeListNames[i] = (TextView)findViewById(resources.getIdentifier("pokename" + (i+1), "id", packName));
        	pokeListHPs[i] = (TextView)findViewById(resources.getIdentifier("hp" + (i+1), "id", packName));
        	pokeListItems[i] = (TextView)findViewById(resources.getIdentifier("item" + (i+1), "id", packName));
        	pokeListAbilities[i] = (TextView)findViewById(resources.getIdentifier("ability" + (i+1), "id", packName));
        	pokeListButtons[i] = (RelativeLayout)findViewById(resources.getIdentifier("pokeViewLayout" + (i+1), "id", packName));
        	pokeListButtons[i].setOnClickListener(battleListener);
        	pokeListIcons[i] = (ImageView)findViewById(resources.getIdentifier("pokeViewIcon" + (i+1), "id", packName));
        	
        	for(int j = 0; j < 4; j++)
        		pokeListMovePreviews[i][j] = (TextView)findViewById(resources.getIdentifier("p" + (i+1) + "_attack" + (j+1), "id", packName));
        }
        
        infoView = (TextView)findViewById(R.id.infoWindow);
        infoScroll = (ScrollView)findViewById(R.id.infoScroll);
        battleView = (RelativeLayout)findViewById(R.id.battleScreen);
        
        topAttackRowLayout = (LinearLayout)findViewById(R.id.topAttackRowLayout);
    	bottomAttackRowLayout = (LinearLayout)findViewById(R.id.bottomAttackRowLayout);
    	struggleRowLayout = (LinearLayout)findViewById(R.id.struggleRowLayout);
    	struggleLayout = (RelativeLayout)findViewById(R.id.struggleLayout);
    	
    	struggleLayout.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			netServ.socket.sendMessage(netServ.battle.constructAttack((byte)-1), Command.BattleMessage); // This is how you struggle
    		}
    	});
    }
	
	private Handler handler = new Handler();
    
	private Runnable updateTimeTask = new Runnable() {
		public void run() {
			if (netServ.battle == null)
				return;
			for(int i = 0; i < 2; i++) {
				int seconds;
				if (netServ.battle.ticking[i]) {
					long millis = SystemClock.uptimeMillis()
					- netServ.battle.startingTime[i];
					seconds = netServ.battle.remainingTime[i] - (int) (millis / 1000);
				}
				else
					seconds = netServ.battle.remainingTime[i];

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
	
	public void updateBattleInfo() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (!netServ.hasBattle())
					return;
				synchronized (netServ.battle.histDelta) {
					infoView.append(netServ.battle.histDelta);
					if (netServ.battle.histDelta.length() != 0) {
						infoScroll.post(new Runnable() {
							public void run() {
								infoScroll.smoothScrollTo(0, infoView.getMeasuredHeight());
							}
						});
					}
					infoScroll.invalidate();
					netServ.battle.hist.append(netServ.battle.histDelta);
					netServ.battle.histDelta.clear();
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

	private Drawable getSprite(ShallowBattlePoke poke, boolean front) {
        String res;
        
        if (netServ.battle.shouldShowPreview || poke.status() == Status.Koed.poValue())
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
	        		if (resources.getIdentifier(res + "f", "drawable", "com.pokebros.android.pokemononline") == 0)
	        			// No special female sprite
	        			res = res + (poke.shiny ? "s" : "");
	        		else
	        			res = res + "f" + (poke.shiny ? "s" : "");
	        	}
        	}
        }
        System.out.println("SPRITE: " + res);
        return resources.getDrawable(resources.getIdentifier(res, "drawable", "com.pokebros.android.pokemononline"));
	}

	public void updateCurrentPokeListEntry() {
		if (netServ == null)
			return;
		runOnUiThread(new Runnable() {
			public void run() {
				BattlePoke battlePoke = netServ.battle.myTeam.pokes[0];
				pokeListHPs[0].setText(battlePoke.currentHP +
						"/" + battlePoke.totalHP);
				// TODO: Status ailments and stuff
			}
		});
	}

	public void updateMovePP(final int moveNum) {
		runOnUiThread(new Runnable() {
			public void run() {
				BattleMove move = netServ.battle.displayedMoves[moveNum];
				attackPPs[moveNum].setText("PP " + move.currentPP + "/" + move.totalPP);
			}
		});
	}
	public void updateMyPoke() {
		runOnUiThread(new Runnable() {
			public void run() {
				ShallowBattlePoke poke = netServ.battle.currentPoke(me);
				// Load correct moveset and name
				if(poke != null) {
					currentPokeNames[me].setText(poke.rnick);
					setHpBarTo(me, poke.lifePercent);
					BattlePoke battlePoke = netServ.battle.myTeam.pokes[0];
			        for(int i = 0; i < 4; i++) {
			        	BattleMove move = netServ.battle.displayedMoves[i];
			        	updateMovePP(i);
			        	attackNames[i].setText(move.toString());
			        	String type;
			        	if (move.num == 237)
			        		type = Type.values()[battlePoke.hiddenPowerType()].toString();
			        	else
			        		type = move.getTypeString();
			        	type = type.toLowerCase();
			        	attackLayouts[i].setBackgroundDrawable(resources.getDrawable(resources.getIdentifier(type + "_type_button",
					      		"drawable", "com.pokebros.android.pokemononline")));
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
					ShallowBattlePoke poke = netServ.battle.currentPoke(opp);
					// Load correct moveset and name
					if(poke != null) {
						currentPokeNames[opp].setText(poke.rnick);
						setHpBarTo(opp, poke.lifePercent);
						pokeSprites[opp].setImageDrawable(getSprite(poke, true));
					}
				}
		});		
	}
	
	public void updateButtons(final boolean allowSwitch, final boolean allowAttack, final boolean[] allowAttacks) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (!checkStruggle()) {
					for (int i = 0; i < 4; i++) {
						if (allowAttack) {
							setAttackButtonEnabled(i, allowAttacks[i]);
							//attackLayouts[i].setEnabled(allowAttacks[i]);
						}
						else {
							setAttackButtonEnabled(i, false);
							//attackLayouts[i].setEnabled(false);
						}
					}
				}
				for(int i = 0; i < 6; i++) {
					if (netServ.battle.myTeam.pokes[i].status() != Status.Koed.poValue())
						setPokeListButtonEnabled(i, allowSwitch);
					else
						setPokeListButtonEnabled(i, false);
				}
			}
		});
	}
	
	public boolean checkStruggle() {
		// XXX TODO This method should hide moves, show the button if necessary and return whether it showed the button
		boolean struggle = netServ.battle.shouldStruggle;
		if(struggle) {
			bottomAttackRowLayout.setVisibility(View.GONE);
			topAttackRowLayout.setVisibility(View.GONE);
			struggleRowLayout.setVisibility(View.VISIBLE);
		}
		else {
			bottomAttackRowLayout.setVisibility(View.VISIBLE);
			topAttackRowLayout.setVisibility(View.VISIBLE);
			struggleRowLayout.setVisibility(View.GONE);
		}
		return struggle;
	}
	
	private Drawable getIcon(UniqueID uid) {
		int resID = resources.getIdentifier("pi" + uid.pokeNum +
				(uid.subNum == 0 ? "" : "_" + uid.subNum) +
				"_icon", "drawable", "com.pokebros.android.pokemononline");
		if (resID == 0)
			resID = resources.getIdentifier("pi" + uid.pokeNum + "_icon",
					"drawable", "com.pokebros.android.pokemononline");
		return resources.getDrawable(resID);
	}
	
	public void updateTeam() {
		runOnUiThread(new Runnable() {
			public void run() {
				for (int i = 0; i < 6; i++) {
					BattlePoke poke = netServ.battle.myTeam.pokes[i];
					pokeListIcons[i].setImageDrawable(getIcon(poke.uID));
					pokeListNames[i].setText(poke.nick);
					pokeListHPs[i].setText(poke.currentHP +
							"/" + poke.totalHP);
					pokeListItems[i].setText(poke.itemString);
					pokeListAbilities[i].setText(poke.abilityString);
					for (int j = 0; j < 4; j++) {
						pokeListMovePreviews[i][j].setText(poke.moves[j].toString());
						int color;
						if (poke.moves[j].num == 237) // Hidden Power
							color = poke.hiddenPowerType();
						else
							color = poke.moves[j].getColor();
						pokeListMovePreviews[i][j].getBackground().setColorFilter(color, PorterDuff.Mode.DARKEN);
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
		checkRearrangeTeamDialog();
	}
	
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ = ((NetworkService.LocalBinder)service).getService();
			netServ.herp();
			if (!netServ.hasBattle()) {
	    		startActivity(new Intent(BattleActivity.this, ChatActivity.class));
				finish();
				return;
			}

			netServ.showNotification(BattleActivity.class, "Battle");

			battleView.setBackgroundResource(resources.getIdentifier("bg" + netServ.battle.background, "drawable", "com.pokebros.android.pokemononline"));
			
			// Set the UI to display the correct info
	        me = netServ.battle.me;
	        opp = netServ.battle.opp;
	        // We don't know which timer is which until the battle starts,
	        // so set them here.
	        timers[me] = (TextView)findViewById(R.id.timerB);
	        timers[opp] = (TextView)findViewById(R.id.timerA);
	        names[me] = (TextView)findViewById(R.id.nameB);
	        names[opp] = (TextView)findViewById(R.id.nameA);
	        
	        names[me].setText(netServ.battle.players[me].nick());
	        names[opp].setText(netServ.battle.players[opp].nick());
	        
	        hpBars[me] = (TextProgressBar)findViewById(R.id.hpBarB);
	        hpBars[opp] = (TextProgressBar)findViewById(R.id.hpBarA);
	        
	        currentPokeNames[me] = (TextView)findViewById(R.id.currentPokeNameB);
	        currentPokeNames[opp] = (TextView)findViewById(R.id.currentPokeNameA);
	        
	        pokeSprites[me] = (ImageView)findViewById(R.id.pokeSpriteB);
	        pokeSprites[opp] = (ImageView)findViewById(R.id.pokeSpriteA);
	        for(int i = 0; i < 2; i++)
	        	pokeSprites[i].setOnLongClickListener(spriteListener);
	        
	        // Load scrollback
	        infoView.setText(netServ.battle.hist);
	        updateBattleInfo();
	    	
	    	// Prompt a UI update of the pokemon
	        updateMyPoke();
	        updateOppPoke();
	        
	        // Enable or disable buttons
	        updateButtons(netServ.battle.allowSwitch, netServ.battle.allowAttack, netServ.battle.allowAttacks);
	        
	    	// Start timer updating
	        handler.postDelayed(updateTimeTask, 100);
	        
	        // Don't set netServ.battleActivity until after we've finished
	        // getting UI elements. Otherwise there's a race condition if Battle
	        // wants to update one of our UI elements we haven't gotten yet.
			netServ.battleActivity = BattleActivity.this;

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
    				netServ.socket.sendMessage(netServ.battle.constructAttack((byte)i), Command.BattleMessage);
    		// Check to see if click was on pokelist button
    		for(int i = 0; i < 6; i++) {
    			if(id == pokeListButtons[i].getId()) {
    				netServ.socket.sendMessage(netServ.battle.constructSwitch((byte)i), Command.BattleMessage);
    				realViewSwitcher.snapToScreen(0);
    			}
    		}
    		for(int i = 0; i < 4; i++) {
    			setAttackButtonEnabled(i, false);
			}
			for(int i = 0; i < 6; i++) {
				setPokeListButtonEnabled(i, false);
			}
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
		attackPPs[num].setEnabled(enabled);
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
    	if(netServ != null && !netServ.battle.gotEnd)
    		netServ.socket.sendMessage(netServ.battle.constructCancel(), Command.BattleMessage);
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
    		if (netServ != null && netServ.hasBattle() && !netServ.battle.gotEnd)
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
		if (netServ != null && netServ.hasBattle() && netServ.battle.shouldShowPreview) {
			showDialog(BattleDialog.RearrangeTeam.ordinal());
		}
	}
    
	void endBattle() {
		if (netServ != null && netServ.socket != null && netServ.socket.isConnected() && netServ.hasBattle()) {
    		Baos bID = new Baos();
    		bID.putInt(netServ.battle.bID);
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
        			netServ.socket.sendMessage(netServ.battle.constructRearrange(), Command.BattleMessage);
        			netServ.battle.shouldShowPreview = false;
        			removeDialog(id);
        		}})
        		.setCancelable(false);
        	dialog = builder.create();

        	mDragLayer = (DragLayer)layout.findViewById(R.id.drag_my_poke);
        	for(int i = 0; i < 6; i++){
        		BattlePoke poke = netServ.battle.myTeam.pokes[i];
        		myArrangePokeIcons[i] = (PokeDragIcon)layout.findViewById(resources.getIdentifier("my_arrange_poke" + (i+1), "id", packName));
        		myArrangePokeIcons[i].setOnTouchListener(dialogListener);
        		myArrangePokeIcons[i].setImageDrawable(getIcon(poke.uID));
        		myArrangePokeIcons[i].num = i;
        		myArrangePokeIcons[i].battleActivity = this;

        		ShallowShownPoke oppPoke = netServ.battle.oppTeam.pokes[i];
        		oppArrangePokeIcons[i] = (ImageView)layout.findViewById(resources.getIdentifier("foe_arrange_poke" + (i+1), "id", packName));
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
        		View layout = inflater.inflate(R.layout.dynamic_info_layout, (LinearLayout)findViewById(R.id.dynamic_info_layout));
        		builder.setView(layout);
        		TextView t = (TextView)layout.findViewById(R.id.statNamesView);
        		t.setText(netServ.battle.dynamicInfo[player].statsAndHazards());
        		t = (TextView)layout.findViewById(R.id.statNumsView);
        		t.setText(netServ.battle.dynamicInfo[player].numbers());
        		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						removeDialog(id);
					}
				});
        		return builder.create();
        	}
        default:
            return new Dialog(this);
        }
    }
    
}