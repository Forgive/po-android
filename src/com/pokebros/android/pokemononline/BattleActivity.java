package com.pokebros.android.pokemononline;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BattleActivity extends Activity {
	private NetworkService netServ = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
                changeName(msg);
        }
    };
    private Messenger messenger = new Messenger(handler);
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ =	((NetworkService.LocalBinder)service).getService();
			netServ.herp();
			Toast.makeText(BattleActivity.this, "Service connected",
                    Toast.LENGTH_SHORT).show();
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battle);

        Intent intent = new Intent(BattleActivity.this, NetworkService.class);
        intent.putExtra("Messenger", messenger);
        intent.putExtra("Type", "battle");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        //startService(intent);//new Intent(this, NetworkService.class));
        //Capture out button from layout
        Button attack1 = (Button)findViewById(R.id.attack1);
        Button attack2 = (Button)findViewById(R.id.attack2);
        Button attack3 = (Button)findViewById(R.id.attack3);
        Button attack4 = (Button)findViewById(R.id.attack4);
        //Register the onCLick listener with the implementation above
        attack1.setOnClickListener(battleListener);
        attack2.setOnClickListener(battleListener);
        attack3.setOnClickListener(battleListener);
        attack4.setOnClickListener(battleListener);
    }
    
    @Override
    public void onDestroy() {
    	unbindService(connection);
    	super.onDestroy();
    }

    public void changeName(Message msg) {
    	if (msg != null && msg.obj != null) {
    		TextView myView = (TextView) findViewById(R.id.nameA);
    		myView.setText(msg.obj.toString());
    	}
    }
    
    public OnClickListener battleListener = new OnClickListener() {
    	public void onClick(View v) {
    		//attack!!
    		if(v == findViewById(R.id.attack1)){
    		netServ.socket.sendMessage(netServ.battle.constructAttack((byte)0), Command.BattleMessage);
    		}
    		else if(v == findViewById(R.id.attack2)){
    			netServ.socket.sendMessage(netServ.battle.constructAttack((byte)1), Command.BattleMessage);
    		}
    		else if(v == findViewById(R.id.attack3)){
    			netServ.socket.sendMessage(netServ.battle.constructAttack((byte)2), Command.BattleMessage);
    		}
    		else if(v == findViewById(R.id.attack4)){
    			netServ.socket.sendMessage(netServ.battle.constructAttack((byte)3), Command.BattleMessage);
    		}
    	}
    };
}