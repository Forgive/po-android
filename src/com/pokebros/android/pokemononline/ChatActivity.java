package com.pokebros.android.pokemononline;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.view.KeyEvent;

public class ChatActivity extends Activity {
	private NetworkService netServ = null;
	private ScrollView chatScroll;
	private TextView chatBox;
	private EditText chatInput;
	private boolean inBattle;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	if (msg.getData().containsKey("ChannelMessage")) {
        		chatBox.append(msg.getData().getString("ChannelMessage") + "\n");
            	chatScroll.post(new Runnable() {
            		public void run() {
		    			chatScroll.smoothScrollTo(0, chatBox.getMeasuredHeight());
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
			if (netServ.battle == null)
				netServ.showNotification(ChatActivity.class, "Chat");
		}
		
		public void onServiceDisconnected(ComponentName className) {
			netServ = null;
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		System.out.println("CREATED CHAT ACTIVITY");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        chatScroll = (ScrollView) findViewById(R.id.chatScroll);
    	chatBox = (TextView)findViewById(R.id.chatBox);
        
        Intent intent = new Intent(ChatActivity.this, NetworkService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
//        startService(intent);//new Intent(this, NetworkService.class));
        chatInput = (EditText) findViewById(R.id.chatInput);
        chatInput.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                  // Perform action on key press
                	Baos b = new Baos();
                	b.putInt(0);
                	b.putString(chatInput.getText().toString());
                	netServ.socket.sendMessage(b, Command.ChannelMessage);
                	chatInput.getText().clear();
                  return true;
                }
                return false;
            }
        });
        // Handle challenges
        System.out.println("INTENT: "+getIntent().toString());
        if (getIntent().hasExtra("opponent")) {
                showDialog(0);
        }
	}
	
	@Override
	public void onNewIntent(Intent intent) {
        System.out.println("INTENT: "+intent.toString());
        if (intent.hasExtra("opponent")) {
        	showDialog(0, intent.getExtras());
        	netServ.noteMan.cancel(netServ.NOTIFICATION+1);
        }
	}
	
	@Override
	protected Dialog onCreateDialog(int id, final Bundle args) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
		case 0:
			builder.setMessage("Accept challenge?") // TODO add challenge info
			.setCancelable(false)
			.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Accept challenge
					Baos b = new Baos();
					b.write(1);
					b.putInt(args.getInt("opponent"));
					b.putInt(args.getInt("clauses"));
					b.write(args.getByte("mode"));
			        netServ.socket.sendMessage(b, Command.ChallengeStuff);
				}
			})
			.setNegativeButton("Decline", null);
		}
		return builder.create();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatoptions, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.backtobattle: 
    		if(netServ.battle != null) {
    			Intent in = new Intent(this, BattleActivity.class);
    			in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			startActivity(in);
    			break;
    		}
    		else {
    			Toast.makeText(ChatActivity.this, R.string.notinbattle,
    					Toast.LENGTH_SHORT).show();
    			break;	
    		}
		case R.id.chat_disconnect:
    		finish();
    		break;
    	}
    	return true;
    }
	
    @Override
    public void onDestroy() {
    	unbindService(connection);
    	super.onDestroy();
    }
}
