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
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity {
	private NetworkService netServ = null;
	private ScrollView chatScroll;
	private TextView chatBox;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	if (msg.getData().containsKey("ChannelMessage")) {
        		chatBox.append(msg.getData().getString("ChannelMessage") + "\n");
            	chatScroll.post(new Runnable() {
            		public void run() {
            			chatScroll.fullScroll(View.FOCUS_DOWN);
            		}
            	});
        	}
        }
    };
    private Messenger messenger = new Messenger(handler);
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ =	((NetworkService.LocalBinder)service).getService();
			Toast.makeText(ChatActivity.this, "Service connected",
                    Toast.LENGTH_SHORT).show();
			netServ.herp();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			netServ = null;
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		System.out.println("CREATED");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        chatScroll = (ScrollView) findViewById(R.id.chatScroll);
    	chatBox = (TextView) findViewById(R.id.chatBox);
        
        Intent intent = new Intent(ChatActivity.this, NetworkService.class);
        intent.putExtra("Messenger", messenger);
        intent.putExtra("Type", "chat");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
//        startService(intent);//new Intent(this, NetworkService.class));
    }
	
    @Override
    public void onDestroy() {
    	unbindService(connection);
    	super.onDestroy();
    }
}
