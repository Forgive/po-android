package com.pokebros.android.pokemononline;

import com.pokebros.android.pokemononline.poke.BattlePoke;
import com.pokebros.android.pokemononline.poke.ShallowBattlePoke;

import de.marcreichelt.android.RealViewSwitcher;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class BattleActivity extends Activity {
	public final static int SWIPE_TIME_THRESHOLD = 100;
	
	RealViewSwitcher realViewSwitcher;
	TextProgressBar[] hpBars = new TextProgressBar[2];
	int[] lastHPs = new int[2];
	TextView[] currentPokeNames = new TextView[2];
	public Button[] attack = new Button[4];
	public TextView[] timers = new TextView[2];
	TextView[] pokeListNames = new TextView[6];
	TextView[] pokeListItems = new TextView[6];
	TextView[] pokeListAbilities = new TextView[6];
	TextView[] pokeListHPs = new TextView[6];
	ImageView[] pokeListIcons = new ImageView[6];
	LinearLayout[] pokeListButtons = new LinearLayout[6];
	public TextView infoView;
	public ScrollView infoScroll;
	TextView[] names = new TextView[2];
	ImageView[] pokeSprites = new ImageView[2];
	private NetworkService netServ = null;
	int me, opp;
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	System.out.println("BattleActivity Created");
        super.onCreate(savedInstanceState);
		if (getIntent().hasExtra("endBattle")) {
			finish();
			return;
		}
        setContentView(R.layout.battle_pokeviewer);

        Intent intent = new Intent(BattleActivity.this, NetworkService.class);
        intent.putExtra("Type", "battle");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        
        realViewSwitcher = (RealViewSwitcher)findViewById(R.id.battlePokeSwitcher);
        //Capture out button from layout
        attack[0] = (Button)findViewById(R.id.attack1);
        attack[1] = (Button)findViewById(R.id.attack2);
        attack[2] = (Button)findViewById(R.id.attack3);
        attack[3] = (Button)findViewById(R.id.attack4);
        
        pokeListNames[0] = (TextView)findViewById(R.id.pokename1);
        pokeListNames[1] = (TextView)findViewById(R.id.pokename2);
        pokeListNames[2] = (TextView)findViewById(R.id.pokename3);
        pokeListNames[3] = (TextView)findViewById(R.id.pokename4);
        pokeListNames[4] = (TextView)findViewById(R.id.pokename5);
        pokeListNames[5] = (TextView)findViewById(R.id.pokename6);

        pokeListHPs[0] = (TextView)findViewById(R.id.hp1);
        pokeListHPs[1] = (TextView)findViewById(R.id.hp2);
        pokeListHPs[2] = (TextView)findViewById(R.id.hp3);
        pokeListHPs[3] = (TextView)findViewById(R.id.hp4);
        pokeListHPs[4] = (TextView)findViewById(R.id.hp5);
        pokeListHPs[5] = (TextView)findViewById(R.id.hp6);

        pokeListItems[0] = (TextView)findViewById(R.id.item1);
        pokeListItems[1] = (TextView)findViewById(R.id.item2);
        pokeListItems[2] = (TextView)findViewById(R.id.item3);
        pokeListItems[3] = (TextView)findViewById(R.id.item4);
        pokeListItems[4] = (TextView)findViewById(R.id.item5);
        pokeListItems[5] = (TextView)findViewById(R.id.item6);

        pokeListAbilities[0] = (TextView)findViewById(R.id.ability1);
        pokeListAbilities[1] = (TextView)findViewById(R.id.ability2);
        pokeListAbilities[2] = (TextView)findViewById(R.id.ability3);
        pokeListAbilities[3] = (TextView)findViewById(R.id.ability4);
        pokeListAbilities[4] = (TextView)findViewById(R.id.ability5);
        pokeListAbilities[5] = (TextView)findViewById(R.id.ability6);
        
        pokeListButtons[0] = (LinearLayout)findViewById(R.id.pokeViewLayout1);
        pokeListButtons[1] = (LinearLayout)findViewById(R.id.pokeViewLayout2);
        pokeListButtons[2] = (LinearLayout)findViewById(R.id.pokeViewLayout3);
        pokeListButtons[3] = (LinearLayout)findViewById(R.id.pokeViewLayout4);
        pokeListButtons[4] = (LinearLayout)findViewById(R.id.pokeViewLayout5);
        pokeListButtons[5] = (LinearLayout)findViewById(R.id.pokeViewLayout6);
        
        pokeListIcons[0] = (ImageView)findViewById(R.id.pokeViewIcon1);
        pokeListIcons[1] = (ImageView)findViewById(R.id.pokeViewIcon2);
        pokeListIcons[2] = (ImageView)findViewById(R.id.pokeViewIcon3);
        pokeListIcons[3] = (ImageView)findViewById(R.id.pokeViewIcon4);
        pokeListIcons[4] = (ImageView)findViewById(R.id.pokeViewIcon5);
        pokeListIcons[5] = (ImageView)findViewById(R.id.pokeViewIcon6);
        
        infoView = (TextView)findViewById(R.id.infoWindow);
        infoScroll = (ScrollView)findViewById(R.id.infoScroll);
        //Register the onCLick listener with the implementation above
        for(int i = 0; i < 4; i++) {
        	attack[i].setOnClickListener(battleListener);
        }
        for(int i = 0; i < 6; i++)
        	pokeListButtons[i].setOnClickListener(battleListener);
    }
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
                changeName(msg);
        }
    };
    
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
    
	void hpUpdateHack(int i) {
		hpBars[i].incrementProgressBy(1);
		hpBars[i].incrementProgressBy(-1);
	}
	
	public Runnable animateHPBars = new Runnable() {
		public void run() {
			for(int i = 0; i < 2; i++) {
				ShallowBattlePoke poke = netServ.battle.currentPoke(i);
				if(poke != null) {
					// Update the bars to reflect the current life percentage
					if(hpBars[i].getProgress() > poke.lifePercent)
						hpBars[i].incrementProgressBy(-1);
					else if(hpBars[i].getProgress() < poke.lifePercent) {
						hpBars[i].incrementProgressBy(1);
					}
					int progress = hpBars[i].getProgress();
					
					// Check to see if the bars need to change color, and do it
					Rect bounds = hpBars[i].getProgressDrawable().getBounds();
					if(progress > 50 && (lastHPs[i] <= 50)) {
						hpBars[i].setProgressDrawable(getResources().getDrawable(R.drawable.green_progress));
						hpUpdateHack(i);
					}
					else if((progress <= 50 && progress > 20) && (lastHPs[i] <= 20 || lastHPs[i] > 50)) {
						hpBars[i].setProgressDrawable(getResources().getDrawable(R.drawable.yellow_progress));
						hpUpdateHack(i);
					}
					else if((progress <= 20) && (lastHPs[i] > 20)) {
						hpBars[i].setProgressDrawable(getResources().getDrawable(R.drawable.red_progress));
						hpUpdateHack(i);
					}
					hpBars[i].getProgressDrawable().setBounds(bounds);
					
					// Update the percentage display on the hp bar
					hpBars[i].setText(progress + "%");
					lastHPs[i] = progress;
				}
			}
			// See if the animation has finished yet
			for(int i = 0; i < 2; i++) {
				ShallowBattlePoke poke = netServ.battle.currentPoke(i);
				if(poke != null) {
					if(hpBars[i].getProgress() != poke.lifePercent)
						handler.postDelayed(this, 100);
				}
			}
		}
	};
	
	public void updateBattleInfo() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (!netServ.hasBattle())
					return;
				SpannableStringBuilder delta = netServ.battle.histDelta;
				infoView.append(delta);
				if (delta.length() != 0) {
			    	infoScroll.post(new Runnable() {
			    		public void run() {
			    			infoScroll.smoothScrollTo(0, infoView.getMeasuredHeight());
			    		}
			    	});
				}
				infoScroll.invalidate();
		    	netServ.battle.hist.append(delta);
				delta.clear();
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
					hpBars[me].setProgress(netServ.battle.currentPoke(me).lifePercent);
					BattlePoke battlePoke = netServ.battle.myTeam.pokes[0];
			        for(int i = 0; i < 4; i++) {
			        	attack[i].setText(battlePoke.moves[i].toString());
			        	String type = battlePoke.moves[i].getTypeString();
			        	type = type.toLowerCase();
			        	int resID = getResources().getIdentifier(type + "_type_button",
					        		"drawable", "com.pokebros.android.pokemononline");
			        	attack[i].setBackgroundResource(resID);
			        }
			        int resID;
			        if (poke.sub)
			        	resID = getResources().getIdentifier("sub_back", "drawable", "com.pokebros.android.pokemononline");
			        else
				        resID = getResources().getIdentifier("p" + poke.uID.pokeNum + "_back",
				        		"drawable", "com.pokebros.android.pokemononline");
					pokeSprites[me].setImageDrawable(getResources().getDrawable(resID));
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
					hpBars[opp].setProgress(netServ.battle.currentPoke(opp).lifePercent);
					int resID;
					if (poke.sub)
						resID = getResources().getIdentifier("sub_front", "drawable", "com.pokebros.android.pokemononline");
					else
						resID = getResources().getIdentifier("p" + poke.uID.pokeNum + "_front",
			        		"drawable", "com.pokebros.android.pokemononline");
			        pokeSprites[opp].setImageDrawable(getResources().getDrawable(resID));
				}
			}
		});		
	}
	
	public void updateButtons(final boolean enabled) {
		runOnUiThread(new Runnable() {
			public void run() {
				for(int i = 0; i < 4; i++) {
					attack[i].setEnabled(enabled);
				}
				for(int i = 0; i < 6; i++) {
					pokeListButtons[i].setEnabled(enabled);
				}
			}
		});
	}
	
	public void updateTeam() {
		runOnUiThread(new Runnable() {
			public void run() {
				for (int i = 0; i < 6; i++) {
					BattlePoke poke = netServ.battle.myTeam.pokes[i];
					int resID = getResources().getIdentifier("pi" + poke.uID.pokeNum +
							"_icon", "drawable", "com.pokebros.android.pokemononline");
					//int resID = getResources().getIdentifier("p" + poke.uID.pokeNum +
					//		"_front", "drawable", "com.pokebros.android.pokemononline");
					pokeListIcons[i].setImageResource(resID);
					pokeListNames[i].setText(poke.nick);
					pokeListHPs[i].setText(poke.currentHP +
							"/" + poke.totalHP);
				}
			}
		});
	}
	
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ =	((NetworkService.LocalBinder)service).getService();
			netServ.herp();

			netServ.showNotification(BattleActivity.class, "Battle");
			Toast.makeText(BattleActivity.this, "Service connected",
                    Toast.LENGTH_SHORT).show();
			
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
	        	lastHPs[i] = netServ.battle.currentPoke(i).lifePercent;
	        
	        // Load scrollback
	        infoView.setText(netServ.battle.hist);
	        updateBattleInfo();
	    	
	    	// Prompt a UI update of the pokemon
	        updateMyPoke();
	        updateOppPoke();
	        
	        // Enable or disable buttons
	        updateButtons(netServ.battle.clickable);
	        
	    	// Start timer updating
	        handler.postDelayed(updateTimeTask, 100);
	        
	        // Don't set netServ.battleActivity until after we've finished
	        // getting UI elements. Otherwise there's a race condition if Battle
	        // wants to update one of our UI elements we haven't gotten yet.
			netServ.battleActivity = BattleActivity.this;
		}
		
		public void onServiceDisconnected(ComponentName className) {
			netServ.battleActivity = null;
			netServ = null;
			Toast.makeText(BattleActivity.this, "Service disconnected",
					Toast.LENGTH_SHORT).show();
		}
	};
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.hasExtra("endBattle"))
			finish();
	}
    
    @Override
    public void onDestroy() {
    	if (netServ == null || !netServ.hasBattle())
    		System.out.println("BATTLE ACTIVITY GONE");
    	if (netServ != null)
    		unbindService(connection);
    	super.onDestroy();
    }

    public void changeName(Message msg) {
    	if (msg != null && msg.obj != null) {
    		TextView myView = (TextView)findViewById(R.id.nameA);
    		myView.setText(msg.obj.toString());
    	}
    }
    
    public OnClickListener battleListener = new OnClickListener() {
    	public void onClick(View v) {
    		int id = v.getId();
    		// Check for attacks
    		for(int i = 0; i < 4; i++)
    			if(id == attack[i].getId())
    				netServ.socket.sendMessage(netServ.battle.constructAttack((byte)i), Command.BattleMessage);
    		for(int i = 0; i < 6; i++) {
    			if(id == pokeListButtons[i].getId()) {
    				netServ.socket.sendMessage(netServ.battle.constructSwitch((byte)i), Command.BattleMessage);
    				realViewSwitcher.snapToScreen(0);
    			}
    		}
    		for(int i = 0; i < 4; i++) {
				attack[i].setEnabled(false);
			}
			for(int i = 0; i < 6; i++) {
				pokeListButtons[i].setEnabled(false);
			}
    	}
    };
    
    @Override
    public void onBackPressed() {
    	if(netServ != null)
    		netServ.socket.sendMessage(netServ.battle.constructCancel(), Command.BattleMessage);
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
    	case R.id.forfeit_yes:
    		//TODO: implement forfeit
    		Baos forfeit = new Baos();
    		forfeit.putInt(netServ.battle.bID);
    		netServ.socket.sendMessage(forfeit, Command.BattleFinished);
    		break;
    	case R.id.forfeit_no:
    		break;
    	case R.id.draw:
    		break;
        }
        return true;
    }
}