package com.pokebros.android.pokemononline;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class NetworkService extends Service {
	private final IBinder binder = new LocalBinder();
	private int NOTIFICATION = 4356;
	private boolean bound = false;
	
	public class LocalBinder extends Binder {
		NetworkService getService() {
			return NetworkService.this;
		}
	}
	
	@Override
	// This is called every time someone binds to us
	public IBinder onBind(Intent intent) {
		bound = true;
		return binder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		bound = false;
		return super.onUnbind(intent);
	}
	
	@Override
	// This is called once
	public void onCreate() {
		super.onCreate();
		
        PokeClientSocket socket = new PokeClientSocket("192.168.1.116", 5080);
        //PokeClientSocket socket = new PokeClientSocket("188.165.249.120", 5089);
        Trainer trainer = new Trainer();
        Thread sThread = new Thread(new NetworkSendThread(socket, trainer.serializeBytes(), Command.Login));
        sThread.start();
        Thread rThread = new Thread(new NetworkRecvThread(socket));
        rThread.start();
		showNotification();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
	
    private void showNotification() {
        CharSequence text = "Service Started!"; // XXX should probably be in R.String

        Notification notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());
        
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent notificationIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, POAndroidActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
        
        notification.setLatestEventInfo(this, "POAndroid", "Text", notificationIntent);
        
        this.startForeground(NOTIFICATION, notification);
    }
}
