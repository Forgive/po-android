package com.pokebros.android.pokemononline;

import de.marcreichelt.android.RealViewSwitcher;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class BattleActivity extends Activity {
	public final static int SWIPE_TIME_THRESHOLD = 100;
	
	RealViewSwitcher realViewSwitcher;
	ProgressBar[] hpBars = new ProgressBar[2];
	TextView[] currentPokeNames = new TextView[2];
	public Button[] attack = new Button[4];
	public TextView[] timers = new TextView[2];
	TextView[] pokeListNames = new TextView[6];
	TextView[] pokeListHPs = new TextView[6];
	LinearLayout[] pokeListButtons = new LinearLayout[6];
	public TextView infoView;
	public ScrollView infoScroll;
	TextView[] names = new TextView[2];
	private NetworkService netServ = null;
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	System.out.println("BattleActivity Created");
        super.onCreate(savedInstanceState);
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
        
        pokeListButtons[0] = (LinearLayout)findViewById(R.id.pokeViewLayout1);
        pokeListButtons[1] = (LinearLayout)findViewById(R.id.pokeViewLayout2);
        pokeListButtons[2] = (LinearLayout)findViewById(R.id.pokeViewLayout3);
        pokeListButtons[3] = (LinearLayout)findViewById(R.id.pokeViewLayout4);
        pokeListButtons[4] = (LinearLayout)findViewById(R.id.pokeViewLayout5);
        pokeListButtons[5] = (LinearLayout)findViewById(R.id.pokeViewLayout6);
        
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
    
	public Runnable animateHPBars = new Runnable() {
		public void run() {
			for(int i = 0; i < 2; i++) {
				if(hpBars[i].getProgress() > netServ.battle.currentPoke(i).lifePercent)
					hpBars[i].incrementProgressBy(-1);
				//if(hpBars[i].getSecondaryProgress() > netServ.battle.currentPoke(i).lifePercent)
				//	hpBars[i].incrementSecondaryProgressBy(-1);
				if(hpBars[i].getProgress() < netServ.battle.currentPoke(i).lifePercent)
					hpBars[i].incrementProgressBy(1);
				//if(hpBars[i].getSecondaryProgress() < netServ.battle.currentPoke(i).lifePercent)
				//	hpBars[i].incrementSecondaryProgressBy(3);
			}
			for(int i = 0; i < 2; i++) {
				if(hpBars[i].getProgress() != netServ.battle.currentPoke(i).lifePercent)// ||
						//hpBars[i].getSecondaryProgress() != netServ.battle.currentPoke(i).lifePercent)
					handler.postDelayed(this, 20);
			}
		}
	};
	
	public Runnable updateUITask = new Runnable() {
		public void run() {
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
			
			if(netServ.battle.pokeChanged) {
				// Load correct moveset and name
				currentPokeNames[netServ.battle.me].setText(netServ.battle.currentPoke(netServ.battle.me).rnick());
				hpBars[netServ.battle.me].setProgress(netServ.battle.currentPoke(netServ.battle.me).lifePercent);
		        for(int i = 0; i < 4; i++) {
		        	attack[i].setText(netServ.battle.myTeam.pokes[0].moves[i].toString());
		        }
		        netServ.battle.pokeChanged = false;
			}
			if(netServ.battle.oppPokeChanged) {
				currentPokeNames[netServ.battle.opp].setText(netServ.battle.currentPoke(netServ.battle.opp).rnick());
				hpBars[netServ.battle.opp].setProgress(netServ.battle.currentPoke(netServ.battle.opp).lifePercent);
				netServ.battle.oppPokeChanged = false;
			}
			
			for(int i = 0; i < 6; i++) {
				byte teamNum = netServ.battle.myTeam.pokes[i].teamNum;
	    		pokeListHPs[teamNum].setText(netServ.battle.myTeam.pokes[i].currentHP +
	    				"/" + netServ.battle.myTeam.pokes[i].totalHP);
			}
	    	netServ.battle.hist.append(delta);
			delta.clear();
			handler.postDelayed(animateHPBars, 50);
			handler.postDelayed(this, 1000);
		}
	};

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ =	((NetworkService.LocalBinder)service).getService();
			netServ.herp();
			netServ.showNotification(BattleActivity.class, "Battle");
			Toast.makeText(BattleActivity.this, "Service connected",
                    Toast.LENGTH_SHORT).show();
			
			// Set the UI to display the correct info
	        
	        // We don't know which timer is which until the battle starts,
	        // so set them here.
	        timers[netServ.battle.me] = (TextView)findViewById(R.id.timerB);
	        timers[netServ.battle.opp] = (TextView)findViewById(R.id.timerA);
	        names[netServ.battle.me] = (TextView)findViewById(R.id.nameB);
	        names[netServ.battle.opp] = (TextView)findViewById(R.id.nameA);
	        
	        names[netServ.battle.me].setText(netServ.battle.players[netServ.battle.me].nick);
	        names[netServ.battle.opp].setText(netServ.battle.players[netServ.battle.opp].nick);
	        
	        hpBars[netServ.battle.me] = (ProgressBar)findViewById(R.id.hpBarB);
	        hpBars[netServ.battle.opp] = (ProgressBar)findViewById(R.id.hpBarA);
	        
	        currentPokeNames[netServ.battle.me] = (TextView)findViewById(R.id.currentPokeNameB);
	        currentPokeNames[netServ.battle.opp] = (TextView)findViewById(R.id.currentPokeNameA);
	        
	        // Load scrollback
	        infoView.setText(netServ.battle.hist);
	    	infoScroll.post(new Runnable() {
	    		public void run() {
	    			infoScroll.smoothScrollTo(0, infoView.getMeasuredHeight());
	    		}
	    	});
	    	
	    	// Load your pokes into the poke list
	    	for(int i = 0; i < 6; i++) {
	    		// XXX just do nick for now, should actually look up
	    		// poke once we get the database stuff going
	    		byte teamNum = netServ.battle.myTeam.pokes[i].teamNum;
	    		pokeListNames[teamNum].setText(netServ.battle.myTeam.pokes[i].nick);
	    	}
	    	
	    	// Prompt a UI update of the pokemon
	        netServ.battle.pokeChanged = netServ.battle.oppPokeChanged = true;
	    	// Set up the UI polling and timer updating
	        handler.postDelayed(updateUITask, 50);
	        handler.postDelayed(updateTimeTask, 100);
		}
		
		public void onServiceDisconnected(ComponentName className) {
			netServ = null;
			Toast.makeText(BattleActivity.this, "Service disconnected",
					Toast.LENGTH_SHORT).show();
		}
	};
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Toast.makeText(BattleActivity.this, "New Intent", Toast.LENGTH_SHORT);
	}
    
    @Override
    public void onDestroy() {
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
    	}
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.battleoptions, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId())
        {
    case R.id.forfeit_yes:
    	//TODO: implement forfeit
    	break;
    case R.id.forfeit_no:
    	break;
    case R.id.draw:
    	break;
        }
        return true;
    }
}