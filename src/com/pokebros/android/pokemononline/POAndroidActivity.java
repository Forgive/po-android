package com.pokebros.android.pokemononline;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

public class POAndroidActivity extends Activity {
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			((NetworkService.LocalBinder)service).getService();
			Toast.makeText(POAndroidActivity.this, "Service connected",
                    Toast.LENGTH_SHORT).show();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			Toast.makeText(POAndroidActivity.this, "Service disconnected",
					Toast.LENGTH_SHORT).show();
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        bindService(new Intent(POAndroidActivity.this, 
        		NetworkService.class), connection, Context.BIND_AUTO_CREATE);
        startService(new Intent(this, NetworkService.class));
    }

    public void changeName(Message msg) {
    	TextView myView = (TextView) findViewById(R.id.nameA);
        myView.setText(msg.obj.toString());
    }
}
