package com.pokebros.android.pokemononline;

import java.util.Enumeration;

import com.pokebros.android.pokemononline.player.PlayerInfo;
import com.pokebros.android.pokemononline.battle.ChallengeEnums.*;
import de.marcreichelt.android.ChatRealViewSwitcher;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableStringBuilder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.view.KeyEvent;

public class ChatActivity extends Activity {
	public enum ChatDialog {
		Challenge
	}
	
	public final static int SWIPE_TIME_THRESHOLD = 100;
	
	private PlayerListAdapter playerAdapter;
	private ChannelListAdapter channelAdapter;
	
	private NetworkService netServ = null;
	private ScrollView chatScroll;
	private TextView chatBox;
	private EditText chatInput;
	private ChatRealViewSwitcher chatViewSwitcher;
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) { //TODO: Implement a Loading Screen
		System.out.println("CREATED CHAT ACTIVITY");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        chatScroll = (ScrollView) findViewById(R.id.chatScroll);
    	chatBox = (TextView)findViewById(R.id.chatBox);
    	chatViewSwitcher = (ChatRealViewSwitcher)findViewById(R.id.chatPokeSwitcher);
    	chatViewSwitcher.setCurrentScreen(1);
 
    	//Player List Stuff**
        ListView players = (ListView)findViewById(R.id.playerlisting);
        playerAdapter = new PlayerListAdapter(this, R.id.playerlisting);
        players.setAdapter(playerAdapter);
        players.setOnItemClickListener(new OnItemClickListener() {
        	// Set the edit texts on list item click
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				if (netServ.socket.isConnected())
					netServ.socket.sendMessage(constructChallenge(ChallengeDesc.Sent.ordinal(), 
							((PlayerListAdapter)parent.getAdapter()).getItem(position).id, 
							Clauses.SleepClause.ordinal(), Mode.Singles.ordinal()), Command.ChallengeStuff);
			}        	
		});
        players.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO: Display player description
				System.out.println("Player -- Long click works");
				return true;
			}
		});
        
        //Channel List Stuff**
        ListView channels = (ListView)findViewById(R.id.channellisting);
        channelAdapter = new ChannelListAdapter(this, R.id.channellisting);
        channels.setAdapter(channelAdapter);
        channels.setOnItemClickListener(new OnItemClickListener() {
        	// Set the edit texts on list item click
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//TODO: Connect to channel
				System.out.println("Channel -- click works");
			}
		});
        channels.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO: Disconnect from channel
				System.out.println("Channel -- Long click works");
				return true;
			}
		});
        
        bindService(new Intent(ChatActivity.this, NetworkService.class), connection,
        		Context.BIND_AUTO_CREATE);
        chatInput = (EditText) findViewById(R.id.chatInput);
        chatInput.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
            	// and the socket is connected
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER) &&
                    netServ.socket.isConnected()) {
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
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (netServ != null && (!netServ.hasBattle() || netServ.battle.isOver))
			netServ.showNotification(ChatActivity.class, "Chat");
	}

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ = ((NetworkService.LocalBinder)service).getService();
			Toast.makeText(ChatActivity.this, "Service connected",
                    Toast.LENGTH_SHORT).show();
			if (!netServ.hasBattle() || netServ.battle.isOver)
				netServ.showNotification(ChatActivity.class, "Chat");
			
			netServ.chatActivity = ChatActivity.this;
			
			populateUI();

	        handleDialogs();
        }
		
		public void onServiceDisconnected(ComponentName className) {
			netServ.chatActivity = null;
			netServ = null;
		}
	};
	
	public void handleDialogs() {
		Intent intent = getIntent();
        if (intent.hasExtra("dialog")) {
        	Bundle extras = intent.getExtras();
        	showDialog(extras.getInt("dialog"), extras);
        	netServ.noteMan.cancel(extras.getInt("note"));
    		intent.removeExtra("dialog");
    		setIntent(intent);
        }
	}
	
	public void populateUI() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (netServ.currentChannel != null) {
					// Populate the player list
					Enumeration<PlayerInfo> e = netServ.currentChannel.players.elements();
					playerAdapter.setNotifyOnChange(false);
					while(e.hasMoreElements()) {
						playerAdapter.addPlayer(e.nextElement());
					}
					playerAdapter.setNotifyOnChange(true);
					playerAdapter.sortByNick();
					//Populate the Channel list
					Enumeration<Channel> c = netServ.channels.elements();
					channelAdapter.setNotifyOnChange(false);
					while(c.hasMoreElements())
						channelAdapter.addChannel(c.nextElement());
					channelAdapter.setNotifyOnChange(true);
					channelAdapter.sortByName();
					//Load scrollback	
					chatBox.setText(netServ.currentChannel.hist);
					chatScroll.post(new Runnable() {
						public void run() {
							chatScroll.smoothScrollTo(0, chatBox.getMeasuredHeight());
						}
					});
					updateChat();
				}
			}});
	}
	
	public void updateChat() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (netServ.currentChannel != null) {
					SpannableStringBuilder delta = netServ.currentChannel.histDelta;
					chatBox.append(delta);
					if (delta.length() != 0) {
						chatScroll.post(new Runnable() {
							public void run() {						
								if(!chatViewSwitcher.isPressed())
								chatScroll.smoothScrollTo(0, chatBox.getMeasuredHeight());
							}
						});
					}
					netServ.currentChannel.hist.append(delta);
					delta.clear();
				}
			}});
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		System.out.println("GOT INTENT AND IT HAS DIALOG");
		System.out.println(intent.hasExtra("dialog"));
		setIntent(intent);
		if (netServ != null) // We are already connected to the service
			handleDialogs(); // so handle dialogs here instead of onServiceConnected
	}
	
	public void showDialogFromService(ChatDialog id, final Bundle args) {
		runOnUiThread(new Runnable() {
			public void run() {
				showDialog(0, args);
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id, final Bundle args) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (ChatDialog.values()[id]) {
		case Challenge:
			builder.setMessage(this.getString(R.string.accept_challenge) + " " + args.getString("oppName") + "?") // TODO add challenge info
			.setCancelable(false)
			.setPositiveButton(this.getString(R.string.accept), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Accept challenge
					if (netServ.socket.isConnected())
						netServ.socket.sendMessage(
								constructChallenge(ChallengeDesc.Accepted.ordinal(),
										args.getInt("opponent"),
										args.getInt("clauses"),
										args.getByte("mode")),
										Command.ChallengeStuff);
					// Without removeDialog() the dialog is reused and can only
					// be modified in onPrepareDialog(). This dialog changes
					// so much that I doubt it's worth the code to deal with
					// onPrepareDialog() but we should use it if we have complex
					// dialogs that only need to change a little
					removeDialog(ChatDialog.Challenge.ordinal());
				}
			})
			.setNegativeButton(this.getString(R.string.decline), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Accept challenge
					if (netServ.socket.isConnected())
						netServ.socket.sendMessage(
								constructChallenge(ChallengeDesc.Refused.ordinal(),
										args.getInt("opponent"),
										args.getInt("clauses"),
										args.getByte("mode")),
										Command.ChallengeStuff);
					removeDialog(ChatDialog.Challenge.ordinal());
				}
			});
			break;
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
    public boolean onPrepareOptionsMenu(Menu menu) {
    	MenuItem findbattle = menu.findItem(R.id.findbattle);
    	if (netServ != null && netServ.findingBattle) {
    		findbattle.setTitle("Cancel Find Battle");
    	} else {
    		findbattle.setTitle("Find Battle");
    	}
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.backtobattle: 
    		if(netServ.hasBattle()) {
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
    		netServ.disconnect();
    		Intent intent = new Intent(this, RegistryActivity.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		intent.putExtra("sticky", true);
    		startActivity(intent);
    		ChatActivity.this.finish();
    		break;
		case R.id.findbattle:
			if (netServ.socket.isConnected()) {
				if (netServ.findingBattle) {
					netServ.findingBattle = false;
					netServ.socket.sendMessage(
							constructChallenge(ChallengeDesc.Cancelled.ordinal(), 0, Clauses.SleepClause.mask(), Mode.Singles.ordinal()),
							Command.ChallengeStuff);
				} else {
					netServ.findingBattle = true;
					// TODO present menu to choose these bools
					netServ.socket.sendMessage(
							constructFindBattle(false, false, false, (short) 200, (byte) 0),
							Command.FindBattle);
				}
			}
			break;
    	}
    	return true;
    }
    
    private Baos constructChallenge(int desc, int opp, int clauses, int mode) {
    	Baos challenge = new Baos();
    	challenge.write(desc);
    	challenge.putInt(opp);
    	challenge.putInt(clauses);
    	challenge.write(mode);
    	return challenge;
    }
    
    private Baos constructFindBattle(boolean forceRated, boolean forceSameTier,
    		boolean onlyInRange, short range, byte mode) {
		Baos find = new Baos();
		find.putBool(forceRated);
		find.putBool(forceSameTier);
		find.putBool(onlyInRange);
		find.putBool(false); // Padding
		find.putShort(range);
		find.write(mode); // singles/doubles/triples
		return find;
    }

	public void removePlayer(final PlayerInfo pi){
		runOnUiThread(new Runnable() {
			public void run() {
            	playerAdapter.removePlayer(pi);
			}
		});
	}
	
	public void addPlayer(final PlayerInfo pi) {
		runOnUiThread(new Runnable() {
			public void run() {
            	playerAdapter.addPlayer(pi);
			}
		});
	}
	
	public void removeChannel(final Channel ch){
		runOnUiThread(new Runnable() {
			public void run() {
            	channelAdapter.removeChannel(ch);
			}
		});
	}
	
	public void addChannel(final Channel ch) {
		runOnUiThread(new Runnable() {
			public void run() {
            	channelAdapter.addChannel(ch);
			}
		});
	}
	
    @Override
    public void onDestroy() {
    	unbindService(connection);
    	super.onDestroy();
    }
}


