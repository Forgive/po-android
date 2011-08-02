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
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
		Challenge,
		AskForPass,
		ConfirmDisconnect,
		FindBattle,
		TierSelection
	}
	
	public final static int SWIPE_TIME_THRESHOLD = 100;
	public final static int CONTEXTMENU_CHALLENGEPLAYER = 0;
	public final static int CONTEXTMENU_VIEWPLAYERINFO = 1;
	
	public PlayerListAdapter playerListAdapter;
	
	private PlayerListAdapter playerAdapter;
	private ChannelListAdapter channelAdapter;
	
	//public ProgressDialog progressDialog;
	
	private NetworkService netServ = null;
	private ScrollView chatScroll;
	private TextView chatBox;
	private EditText chatInput;
	private ChatRealViewSwitcher chatViewSwitcher;

	class TierAlertDialog extends AlertDialog {
		public Tier parentTier = null;
		public ListView dialogListView = null;
		
		protected TierAlertDialog(Context context, Tier t) {
			super(context);
			parentTier = t;
			dialogListView = makeTierListView();
			setTitle("Tier Selection");
			setView(dialogListView);
			setIcon(0); // Don't want an icon
		}
		
		@Override
		public void onBackPressed() {
			if(parentTier.parentTier == null) { // this is the top level
				dismiss();
			}
			else {
				dialogListView.setAdapter(new ArrayAdapter<Tier>(ChatActivity.this, R.layout.tier_list_item, parentTier.parentTier.subTiers));
				parentTier = parentTier.parentTier;
			}
		}
		
		ListView makeTierListView() {
			ListView lv = new ListView(ChatActivity.this);
			lv.setAdapter(new ArrayAdapter<Tier>(ChatActivity.this, R.layout.tier_list_item, parentTier.subTiers));
			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Tier self = parentTier.subTiers.get((int)id);
					if(self.subTiers.size() > 0) {
						parentTier = self;
						((ListView)parent).setAdapter(new ArrayAdapter<Tier>(ChatActivity.this, 
								R.layout.tier_list_item, parentTier.subTiers));
					}
					else {
						Baos b = new Baos();
						b.putString(self.name);
						netServ.socket.sendMessage(b, Command.TierSelection);
						Toast.makeText(ChatActivity.this, "Tier Selected: " + self.name, Toast.LENGTH_SHORT).show();
						dismiss();
					}
				}
			});
			return lv;
		}
	}

	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) { //TODO: Implement a Loading Screen
		System.out.println("CREATED CHAT ACTIVITY");
		
		//progressDialog = ProgressDialog.show(ChatActivity.this, "","Loading. Please wait...", true);
		//progressDialog.setCancelable(true);

		
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
        registerForContextMenu(players);
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
      /*  players.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO: Display player description
				System.out.println("Player -- Long click works");
				
				return true;
			}
		});*/
        
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
		// Hide the soft-keyboard when the activity is created
        chatInput.setInputType(InputType.TYPE_NULL);
        chatInput.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				chatInput.setInputType(InputType.TYPE_CLASS_TEXT);
				chatInput.onTouchEvent(event);
				return true;
			}
		});
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
		chatViewSwitcher.setCurrentScreen(1);
		if (netServ != null && (!netServ.hasBattle() || netServ.battle.isOver))
			netServ.showNotification(ChatActivity.class, "Chat");
		checkChallenges();
		checkAskForPass();
		checkFailedConnection();
	}

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ = ((NetworkService.LocalBinder)service).getService();
			if (!netServ.hasBattle() || netServ.battle.isOver)
				netServ.showNotification(ChatActivity.class, "Chat");
			
			netServ.chatActivity = ChatActivity.this;
			
			populateUI();
	        checkChallenges();
	        checkAskForPass();
	        checkFailedConnection();
        }
		
		public void onServiceDisconnected(ComponentName className) {
			netServ.chatActivity = null;
			netServ = null;
		}
	};
	
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
					chatViewSwitcher.invalidate();
				}
			}});
	}
	
	public void updateChat() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (netServ.currentChannel == null)
					return;
				synchronized(netServ.currentChannel.histDelta) {
					chatBox.append(netServ.currentChannel.histDelta);
					if (netServ.currentChannel.histDelta.length() != 0) {
						chatScroll.post(new Runnable() {
							public void run() {						
								if(!chatViewSwitcher.isPressed())
								chatScroll.smoothScrollTo(0, chatBox.getMeasuredHeight());
							}
						});
					}
					netServ.currentChannel.hist.append(netServ.currentChannel.histDelta);
					netServ.currentChannel.histDelta.clear();
				}
			}});
	}
	
	public void notifyChallenge() {
		runOnUiThread(new Runnable() { public void run() { checkChallenges(); } } );
	}
	
	private void checkChallenges() {
		if (netServ != null) {
			IncomingChallenge challenge = netServ.challenges.poll();
			if (challenge != null) {
				ChatActivity.this.showDialog(ChatDialog.Challenge.ordinal(), challenge.toBundle());
				netServ.noteMan.cancel(IncomingChallenge.note);
			}
		}
	}

	public void notifyAskForPass() {
		runOnUiThread(new Runnable() { public void run() { checkAskForPass(); } } );
	}
	
	private void checkAskForPass() {
		if (netServ != null && netServ.askedForPass)
			showDialog(ChatDialog.AskForPass.ordinal());
	}
	
	public void notifyFailedConnection() {
		disconnect();
	}
	
	private void checkFailedConnection() {
		if(netServ != null && netServ.failedConnect) {
			Toast.makeText(this, "OH GOD WHY", Toast.LENGTH_LONG).show();
			disconnect();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(final int id, final Bundle args) {
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
					removeDialog(id);
					checkChallenges();
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
					removeDialog(id);
					checkChallenges();
				}
			});
			return builder.create();
		case AskForPass:
        	//View layout = inflater.inflate(R.layout.ask_for_pass, null);
        	final EditText passField = new EditText(this);
        	passField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        	//passField.setTransformationMethod(PasswordTransformationMethod.getInstance());
			builder.setMessage("Please enter your password " + netServ.mePlayer.nick() + ".")
			.setCancelable(true)
			.setView(passField)
			.setPositiveButton("Done", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (netServ != null) {
						netServ.sendPass(passField.getText().toString());
					}
					removeDialog(id);
				}
			})
			.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					removeDialog(id);
					disconnect();
				}
			});
			final AlertDialog dialog = builder.create();
        	passField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
					}
				}
			});
			return dialog;
		case ConfirmDisconnect:
			builder.setMessage("Really disconnect?")
			.setCancelable(true)
			.setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					disconnect();
				}
			})
			.setNegativeButton("Cancel", null);
			return builder.create();
		case FindBattle:
			final EditText range = new EditText(this);
			range.setInputType(InputType.TYPE_CLASS_NUMBER);
			range.setHint("Range");
			final boolean[] options = new boolean[3];
			builder.setTitle(R.string.find_a_battle)
			.setMultiChoiceItems(new CharSequence[]{"Force Rated", "Force Same Tier", "Only within range"}, null, new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					options[which] = isChecked;
				}
			})
			.setView(range)
			.setPositiveButton("Find", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (netServ != null && netServ.socket.isConnected()) {
						netServ.findingBattle = true;
						Short inRange;
						try {
							inRange = new Short(range.getText().toString());
						} catch (NumberFormatException e) {
							inRange = 200;
						}
						System.out.println("Force Rated: " + options[0] + " Force Same Tier: " + options[1] + " Only within Range: " + options[2]);
						netServ.socket.sendMessage(
								constructFindBattle(options[0], options[1], options[2], inRange, (byte) 0),
								Command.FindBattle);
					}
				}
			});
			return builder.create();
		case TierSelection:
			return new TierAlertDialog(this, netServ.superTier);
		}
		return new Dialog(this); // Should never get here but needed to run
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
    	} 
    	else if(netServ != null && !netServ.hasBattle()) {
    		findbattle.setTitle(R.string.find_a_battle);
    	}
    	else if(netServ != null && netServ.hasBattle()) {
    		findbattle.setTitle(R.string.tobattlescreen);
    	}
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case R.id.chat_disconnect:
			showDialog(ChatDialog.ConfirmDisconnect.ordinal());
    		break;
		case R.id.findbattle:
			if (netServ.socket.isConnected()) {
				if (netServ.findingBattle && !netServ.hasBattle()) {
					netServ.findingBattle = false;
					netServ.socket.sendMessage(
							constructChallenge(ChallengeDesc.Cancelled.ordinal(), 0, Clauses.SleepClause.mask(), Mode.Singles.ordinal()),
							Command.ChallengeStuff);
				} else if(!netServ.findingBattle && netServ.hasBattle()) {
	    			Intent in = new Intent(this, BattleActivity.class);
	    			in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    			startActivity(in);
	    			break;
	    		} 
				else {
					showDialog(ChatDialog.FindBattle.ordinal());
				}
			}
			break;
		case R.id.preferences:
			// Launch Preference activity
			//Toast.makeText(ChatActivity.this, "Preferences not Implemented Yet",
            //        Toast.LENGTH_SHORT).show();
			showDialog(ChatDialog.TierSelection.ordinal());
			break;
    	}
    	return true;
    }
    
/*    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
      if (v.getId()==R.id.playerlisting) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
       String pname = playerListAdapter.getItem(info.position).nick();
        menu.setHeaderTitle(pname);
          menu.add(Menu.NONE, CONTEXTMENU_CHALLENGEPLAYER, 0, "Challenge " + pname);
          menu.add(Menu.NONE, CONTEXTMENU_VIEWPLAYERINFO, 0, "View Player Info");
        }
      }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	switch(item.getItemId()){
    	case CONTEXTMENU_CHALLENGEPLAYER: 
    		Toast.makeText(this, "Challenge button", Toast.LENGTH_LONG).show();
    		if (netServ.socket.isConnected())
				netServ.socket.sendMessage(constructChallenge(ChallengeDesc.Sent.ordinal(), 
						playerListAdapter.getItem(info.position).id, 
						Clauses.SleepClause.ordinal(), Mode.Singles.ordinal()), Command.ChallengeStuff);
    		
    		
    		break;
    	case CONTEXTMENU_VIEWPLAYERINFO:
    		Toast.makeText(this, "Info button" + info.toString(), Toast.LENGTH_LONG).show();
    		break;
    	}
    	return true;
    }
*/

    private void disconnect() {
		if (netServ != null)
			netServ.disconnect();
		Intent intent = new Intent(this, RegistryActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("sticky", true);
		
		if(netServ == null || netServ.socket == null)
			intent.putExtra("failedConnect", true);
		startActivity(intent);
		ChatActivity.this.finish();
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


