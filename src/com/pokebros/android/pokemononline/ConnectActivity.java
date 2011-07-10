package com.pokebros.android.pokemononline;

import android.os.Bundle;
import android.app.Activity;
import java.io.ByteArrayOutputStream;

public class ConnectActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Trainer trainer = new Trainer();
        PokeClientSocket s = new PokeClientSocket("10.0.0.13", 5080);
        System.out.println("SOCKET FUCKING CREATED");
        s.connect();
        ByteArrayOutputStream bytes = trainer.serializeBytes();
        System.out.println("THIS SHIT'S ABOUT TO GET REAL");
        System.out.println(bytes.toString());
        System.out.println("SHIT GOT REAL");
        s.sendBytes(bytes);
    }
}
