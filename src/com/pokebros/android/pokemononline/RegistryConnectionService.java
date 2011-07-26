package com.pokebros.android.pokemononline;

import java.io.IOException;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class RegistryConnectionService extends Service {
	
	public interface RegistryCommandListener {
	
		public abstract void ServerListEnd();

		/*
		 * Called when Registry sends us a new Server
		 */
		public abstract void NewServer(String name, String desc,
				short players, String ip, short maxplayers, short port);
	}
	
	private final IBinder binder = new LocalBinder();
	private RegistryCommandListener listener = null;
	
	Thread sThread, rThread;
	PokeClientSocket socket;
	private Bais msg;

	public class LocalBinder extends Binder {
		RegistryConnectionService getService() {
			return RegistryConnectionService.this;
		}
	}
	
	@Override
	// This is called every time someone binds to us
	public IBinder onBind(Intent intent) {
		connect();
		return binder;
	}
	
	public void setListener(RegistryCommandListener listener) {
		this.listener = listener;
	}
	
	@Override
	// This is called once
	public void onCreate() {
		super.onCreate();
	}
	
	private void connect() {
		// XXX This should probably have a timeout
		new Thread(new Runnable() {
        	public void run() {
        		socket = new PokeClientSocket("pokemon-online.dynalias.net", 5081);
        		socket.waitConnect();		
        		while(true) {
        			try {
        				socket.recvMessagePoll();
        			} catch (IOException e) {
        				// disconnected
        				break;
        			}
        			Baos tmp = socket.getMsg();
        			if(tmp != null) {
        				msg = new Bais(tmp.toByteArray());
        				handleMsg();
        			} else {
        				// don't use all CPU when no message
        				try {
        					Thread.sleep(10);
        				} catch (InterruptedException e) {
        					// no action
        				}
        			}
        		}
        	}
        }).start();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
    
	public void handleMsg() {

		/* Completely obvious way to "convert"
		 * a byte into a value in an enum.
		 */
		int i = msg.read();
		Command c = Command.values()[i];	
		
		switch(c) {
		case PlayersList: {
			String name = msg.readQString();
			String desc =  msg.readQString();
			short players =  msg.readShort();
			String ip =  msg.readQString();
			short maxplayers = msg.readShort();
			short port = msg.readShort();

			if (listener != null)
				listener.NewServer(name, desc, players, ip, maxplayers, port); 
			break;
		}
		case ServerListEnd:
			if (listener != null)
				listener.ServerListEnd();
			socket.close(); // server stops sending any meaningless data 
			break;
		default:
			System.err.println("Unknown message");
		}
	}
}
