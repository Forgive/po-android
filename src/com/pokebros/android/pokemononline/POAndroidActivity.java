package com.pokebros.android.pokemononline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class POAndroidActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /*Intent myIntent = new Intent(POAndroidActivity.this, ConnectActivity.class);
        POAndroidActivity.this.startActivity(myIntent);*/
        Thread cThread = new Thread(new PokeClientSocket("141.212.112.139", 5080));
        cThread.start();
    }
}
