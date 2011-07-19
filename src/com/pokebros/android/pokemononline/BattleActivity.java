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
//        startService(intent);//new Intent(this, NetworkService.class));

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
}