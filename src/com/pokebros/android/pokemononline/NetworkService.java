package com.pokebros.android.pokemononline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.pokebros.android.pokemononline.battle.Battle;
import com.pokebros.android.pokemononline.battle.BattleConf;
import com.pokebros.android.pokemononline.battle.BattleTeam;
import com.pokebros.android.pokemononline.player.FullPlayerInfo;
import com.pokebros.android.pokemononline.player.PlayerInfo;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

public class NetworkService extends Service {
	private final IBinder binder = new LocalBinder();
	protected int NOTIFICATION = 4356;
	protected NotificationManager noteMan;
	
	Thread sThread, rThread;
	PokeClientSocket socket = null;
	
	private Bais msg;
	
	private FullPlayerInfo meLoginPlayer = new FullPlayerInfo();
	private PlayerInfo mePlayer = new PlayerInfo();
	protected Battle battle;// = new Battle();
	
	protected Hashtable<Integer, Channel> channels = new Hashtable<Integer, Channel>();
	public Hashtable<Integer, PlayerInfo> players = new Hashtable<Integer, PlayerInfo>();
	
	int bID = -1;
	public class LocalBinder extends Binder {
		NetworkService getService() {
			return NetworkService.this;
		}
	}
	
	@Override
	// This is *NOT* called every time someone binds to us, I don't really know why
	// but onServiceConnected is correctly called in the activity sooo....
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	@Override
	// This is called once
	public void onCreate() {
		showNotification(ChatActivity.class, "Chat");
		noteMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		super.onCreate();
	}
	
	public void connect(String ip, int port) {
		socket = new PokeClientSocket(ip, port);
        /*sThread = new Thread(new NetworkSendThread(socket, trainer.serializeBytes(), Command.Login));
        sThread.start();*/
		// Sending messages no longer blocks, so there
		// is no need to spawn a new thread.
		socket.waitConnect();

		socket.sendMessage(meLoginPlayer.serializeBytes(), Command.Login);
		
		// Polling the socket also no longer blocks, but we'll just
		// throw it in its own thread until we think of something better
		// (assuming we do think of something better)
		rThread = new Thread(new Runnable() {
        	public void run() {
        		while(true) {
        			try {
        				socket.recvMessagePoll();
        			} catch (IOException e) {
        				// Disconnected
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
        });
        rThread.start();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Bundle bundle = intent.getExtras();
		if (bundle != null && bundle.containsKey("ip"))
			connect(bundle.getString("ip"), bundle.getShort("port"));
		return START_STICKY;
	}
	
    protected void showNotification(Class<?> toStart, String text) {
        Notification notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());
        
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent notificationIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, toStart), Intent.FLAG_ACTIVITY_NEW_TASK);

        notification.setLatestEventInfo(this, "POAndroid", text, notificationIntent);
        this.startForeground(NOTIFICATION, notification);
    }
	
	public void handleMsg() {
		/* Completely obvious way to "convert"
		 * a byte into a value in an enum.
		 */
		int i = msg.read();
		Command c = Command.values()[i];
		System.out.println("Received: " + c.toString());
		switch(c) {
		case BattleList:
		case JoinChannel:
		case LeaveChannel:
		case ChannelBattle:
		case ChannelMessage:
		case HtmlChannel: {
			Channel ch = channels.get(msg.readInt());
			if(ch != null)
				ch.handleChannelMsg(c, msg);
			else
				System.out.println("Received message for nonexistant channel");
			break;
		}
		case TierSelection:
			msg.readInt();
			ArrayList<String> list = new ArrayList<String>();
			while(msg.available() != 0) {
				msg.read();
				list.add(msg.readQString());
			}
			System.out.println(list.toString());
			break;
		case ChallengeStuff:
			byte desc, mode;
			int opponent, clauses;
			desc = msg.readByte();
			opponent = msg.readInt();
			clauses = msg.readInt();
			mode = msg.readByte();
			System.out.println("Desc: " + desc + " Opponent: " + opponent + " Clauses: " + clauses + " Mode: " + mode);
			Notification note = new Notification(R.drawable.icon, "You've been challenged!", System.currentTimeMillis());
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra("desc", desc);
			intent.putExtra("opponent", opponent);
			intent.putExtra("clauses", clauses);
			intent.putExtra("mode", mode);
	        note.setLatestEventInfo(this, "POAndroid", "You've been challenged!", PendingIntent.getActivity(this, 0,
	                intent, Intent.FLAG_ACTIVITY_NEW_TASK));
			noteMan.notify(NOTIFICATION+1, note);
			break;
		case ChannelsList:
			int numChannels = msg.readInt();
			for(int k = 0; k < numChannels; k++) {
				int chanID = msg.readInt();
				channels.put(chanID, new Channel(chanID, msg.readQString(), this));
			}
			System.out.println(channels.toString());
			break;
		case ChannelPlayers: {
			Channel ch = channels.get(msg.readInt());
			int numPlayers = msg.readInt();
			if(ch != null) {
				for(int k = 0; k < numPlayers; k++) {
					int id = msg.readInt();
					ch.players.put(id, players.get(id));
				}
			}
			else
				System.out.println("Received message for nonexistant channel");
			break;
		}
		case HtmlMessage:
			String htmlMessage = msg.readQString();
			System.out.println("Html Message: " + htmlMessage);
			break;
		/* Only sent when player is in a PM with you and logs out */
		case Logout:
			int playerID = msg.readInt();
			System.out.println("Player " + playerID + " logged out.");
			break;
		case BattleFinished:
			int battleID = msg.readInt();
			byte battleDesc = msg.readByte();
			int id1 = msg.readInt();
			int id2 = msg.readInt();
			String outcome = new String();
			switch(battleDesc) {
			case 0: // Forfeit
				outcome = " won by forfeit against ";
				break;
			case 1: // Win
				outcome = " won against ";
				break;
			case 2: // Tie
				outcome = " tied with ";
				break;
			case 3: // Close
				outcome = " was close against ";
				break;
			default:
				outcome = " had no idea against ";
			}
			System.out.println("Outcome of battle " + battleID + ": Player " + id1 + outcome + "Player " + id2);
		case SendPM:
			playerID = msg.readInt();
			// Ignore the message
			String pm = new String("This user is running the Pokemon Online Android client and cannot respond to private messages.");
			Baos bb = new Baos();
			bb.putInt(playerID);
			bb.putString(pm);
			socket.sendMessage(bb, Command.SendPM);
			break;
		case PlayersList:
			while(msg.available() > 0) {
				PlayerInfo p = new PlayerInfo(msg);
				if(!players.containsKey(p.id()))
					players.put(p.id(), p);
			}
			System.out.println("Trainer list: " + players.toString());
			break;
		case BattleMessage:
			msg.readInt(); // currently support only one battle, unneeded
			msg.readInt(); // discard the size, unneeded 
			battle.receiveCommand(msg);
			break;
		case EngageBattle:
			bID = msg.readInt();
			int pID1 = msg.readInt();
			int pID2 = msg.readInt();
			// This is us!
			if(pID1 == 0) {
				BattleConf conf = new BattleConf(msg);
				BattleTeam team = new BattleTeam(msg);
				// Start the battle
				if(pID1 == 0) { // This is us!
					battle = new Battle(conf, team, players.get(conf.id(0)),
						players.get(conf.id(1)), mePlayer.id(), bID, this);
					System.out.println("The battle between " + mePlayer.nick() + 
						" and " + players.get(pID2).nick() + " has begun!");
					Intent in = new Intent(this, BattleActivity.class);
					in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(in);
				}
			}
			break;
		case Login:
			mePlayer = new PlayerInfo(msg);
			players.put(mePlayer.id(), mePlayer);
			break;
		default:
			System.out.println("Unimplented message");
		}
	}
	protected void herp() {
		System.out.println("HERP");
	}
	
    public void disconnect() {
    	//TODO: Send logout message and disconnect socket
    	this.stopForeground(true);
    }
	
}
