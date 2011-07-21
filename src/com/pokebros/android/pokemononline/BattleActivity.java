package com.pokebros.android.pokemononline;

import java.util.ArrayList;

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
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class BattleActivity extends Activity {
	public Button[] attack = new Button[4];
	public TextView[] timers = new TextView[2];
	public TextView infoView;
	public ScrollView infoScroll;
	TextView[] names = new TextView[2];
	private NetworkService netServ = null;
	private RealViewSwitcher realViewSwitcher;
	
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
	    	netServ.battle.hist.append(delta);
			delta.clear();
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
			
	        ArrayList<String> moves = netServ.battle.myMoves(0);
	        for(int i = 0; i < 4; i++)
	        	attack[i].setText(moves.get(i));
	        timers[netServ.battle.me] = (TextView)findViewById(R.id.timerB);
	        timers[netServ.battle.opp] = (TextView)findViewById(R.id.timerA);
	        names[netServ.battle.me] = (TextView)findViewById(R.id.nameB);
	        names[netServ.battle.opp] = (TextView)findViewById(R.id.nameA);
	        
	        names[netServ.battle.me].setText(netServ.battle.myNick());
	        names[netServ.battle.opp].setText(netServ.battle.oppNick());
	        
	        infoView.setText(netServ.battle.hist);
	    	infoScroll.post(new Runnable() {
	    		public void run() {
	    			infoScroll.smoothScrollTo(0, infoView.getMeasuredHeight());
	    		}
	    	});
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
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	System.out.println("BattleActivity Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battle_pokeviewer);

        Intent intent = new Intent(BattleActivity.this, NetworkService.class);
        intent.putExtra("Type", "battle");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        //Capture out button from layout
        attack[0] = (Button)findViewById(R.id.attack1);
        attack[1] = (Button)findViewById(R.id.attack2);
        attack[2] = (Button)findViewById(R.id.attack3);
        attack[3] = (Button)findViewById(R.id.attack4);

        infoView = (TextView)findViewById(R.id.infoWindow);
        infoScroll = (ScrollView)findViewById(R.id.infoScroll);
        realViewSwitcher = (RealViewSwitcher)findViewById(R.id.battlePokeSwitcher);
        //Register the onCLick listener with the implementation above
        for(int i = 0; i < 4; i++) {
        	attack[i].setOnClickListener(battleListener);
        }
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
    
    @Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		if(realViewSwitcher.onTouchEvent(e))
			return true;
		return super.dispatchTouchEvent(e);
	}
    
    public OnClickListener battleListener = new OnClickListener() {
    	public void onClick(View v) {
    		//attack!!
    		if(v == findViewById(R.id.attack1)){
    			System.out.println("Attack 1 pressed");
    			netServ.socket.sendMessage(netServ.battle.constructAttack((byte)0), Command.BattleMessage);
    		}
    		else if(v == findViewById(R.id.attack2)){
    			System.out.println("Attack 2 pressed");
    			netServ.socket.sendMessage(netServ.battle.constructAttack((byte)1), Command.BattleMessage);
    		}
    		else if(v == findViewById(R.id.attack3)){
    			System.out.println("Attack 3 pressed");
    			netServ.socket.sendMessage(netServ.battle.constructAttack((byte)2), Command.BattleMessage);
    		}
    		else if(v == findViewById(R.id.attack4)){
    			System.out.println("Attack 4 pressed");
    			netServ.socket.sendMessage(netServ.battle.constructAttack((byte)3), Command.BattleMessage);
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
        	Toast.makeText(this, "You pressed the icon in the battle!", Toast.LENGTH_LONG).show();
        return true;
    }
}