package com.pokebros.android.pokemononline;

import java.util.ArrayList;
import java.util.TimerTask;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BattleActivity extends Activity {
	Button[] attack = new Button[4];
	TextView[] timers = new TextView[2];
	
	private NetworkService netServ = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
                changeName(msg);
        }
    };
    
    /*class UpdateTimeTask extends TimerTask {
    	   public void run() {
    		   if(netServ.battle.isMyTimerTicking()) {
    			   long millis = System.currentTimeMillis() - 
    			   		netServ.battle.myStartingTime();
    			   int seconds = 300 - (int) (millis / 1000);
    			   int minutes = seconds / 60;
    			   seconds     = seconds % 60;

    			   timerB.setText(String.format("%d:%02d", minutes, seconds));
    		   }
    	   }
    	} */
    
	private Runnable updateTimeTask = new Runnable() {
		public void run() {
			for(int i = 0; i < 2; i++) {
				int seconds;
				if (netServ.battle.ticking[i]) {
					long millis = SystemClock.uptimeMillis()
					- netServ.battle.startingTime[i];
					seconds = netServ.battle.time[i] - (int) (millis / 1000);
				}
				else
					seconds = netServ.battle.time[i];

				if(seconds < 0) seconds = 0;
				else if(seconds > 300) seconds = 300;

				int minutes = (seconds / 60);
				seconds = seconds % 60;
				timers[i].setText(String.format("%02d:%02d", minutes, seconds));
			}
			handler.postDelayed(this, 200);
		}
	};
    
    private Messenger messenger = new Messenger(handler);
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ =	((NetworkService.LocalBinder)service).getService();
			netServ.herp();
			Toast.makeText(BattleActivity.this, "Service connected",
                    Toast.LENGTH_SHORT).show();
			
	        ArrayList<String> moves = netServ.battle.myMoves(0);
	        for(int i = 0; i < 4; i++)
	        	attack[i].setText(moves.get(i));
	        
	        timers[netServ.battle.me] = (TextView)findViewById(R.id.timerB);
	        timers[netServ.battle.opp] = (TextView)findViewById(R.id.timerA);
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
        setContentView(R.layout.battle);

        Intent intent = new Intent(BattleActivity.this, NetworkService.class);
        intent.putExtra("Messenger", messenger);
        intent.putExtra("Type", "battle");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        //startService(intent);//new Intent(this, NetworkService.class));
        //Capture out button from layout
        attack[0] = (Button)findViewById(R.id.attack1);
        attack[1] = (Button)findViewById(R.id.attack2);
        attack[2] = (Button)findViewById(R.id.attack3);
        attack[3] = (Button)findViewById(R.id.attack4);

        //Register the onCLick listener with the implementation above
        for(int i = 0; i < 4; i++)
        	attack[i].setOnClickListener(battleListener);
        
        handler.postDelayed(updateTimeTask, 100);
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