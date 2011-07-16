package com.pokebros.android.pokemononline;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class NetworkService extends Service {
	private NotificationManager noteMan;
	private final IBinder binder = new LocalBinder();
	private int NOTIFICATION = 4356;
	public class LocalBinder extends Binder {
		NetworkService getService() {
			return NetworkService.this;
		}
	}
	
	@Override
	// This is called everytime someone binds to us
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		super.onUnbind(intent);
		return false; // Default, see android documentation
	}
	
	@Override
	// This is called once
	public void onCreate() {
		super.onCreate();
		
        PokeClientSocket socket = new PokeClientSocket("192.168.1.4", 5080);
        //PokeClientSocket socket = new PokeClientSocket("188.165.249.120", 5089);
        Trainer trainer = new Trainer();
        Thread sThread = new Thread(new NetworkSendThread(socket, trainer.serializeBytes(), Command.Login));
        sThread.start();
        Thread rThread = new Thread(new NetworkRecvThread(socket));
        rThread.start();
		noteMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		showNotification();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		noteMan.cancel(NOTIFICATION);
		super.onDestroy();
	}
	
    private void showNotification() {
        CharSequence text = "Service Started!"; // XXX should probably be in R.String

        Notification notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());
        
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent notificationIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, POAndroidActivity.class), PendingIntent.FLAG_UPDATE_CURRENT+Intent.FLAG_ACTIVITY_NEW_TASK);
        
        notification.setLatestEventInfo(this, "POAndroid", "Text", notificationIntent);
        //notification.contentIntent = notificationIntent;
        
        // Set the info for the views that show in the notification panel.
/*        notification.setLatestEventInfo(this, "POAndroid_Label", // XXX should probably be in R.String
                       text, contentIntent);*/
        
        this.startForeground(NOTIFICATION, notification);

        // Send the notification.
        //noteMan.notify(NOTIFICATION, notification);
    }
}
