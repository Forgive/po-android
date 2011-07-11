package com.pokebros.android.pokemononline;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class POAndroidActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        PokeClientSocket s = new PokeClientSocket("141.212.112.139", 5080);
        Trainer t = new Trainer();
        Thread cThread = new Thread(new NetworkSendThread(s, t.serializeBytes(), Command.Login));
        cThread.start();
        Thread lol = new Thread(new NetworkRecvThread(s, handler));
        lol.run();
    }
    
    
    private Handler handler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		changeTimer();
    	}
    };
    
    public void changeTimer() {
    	TextView myView = (TextView) findViewById(R.id.timerA);
        myView.setText("LOLOLLO");
    }
}
