package com.pokebros.android.pokemononline;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;

import com.pokebros.android.pokemononline.ChatActivity.ChatDialog;
import com.pokebros.android.pokemononline.battle.Battle;
import com.pokebros.android.pokemononline.battle.BattleConf;
import com.pokebros.android.pokemononline.battle.BattleTeam;
import com.pokebros.android.pokemononline.battle.ChallengeEnums.ChallengeDesc;
import com.pokebros.android.pokemononline.player.BasicPlayerInfo;
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
	public static String escapeHtml(String toEscape) {
		toEscape.replaceAll("&", "&amp;");
		toEscape.replaceAll("<", "&lt;");
		toEscape.replaceAll(">", "&gt;");
		return toEscape;
	}

	private final IBinder binder = new LocalBinder();
	protected int NOTIFICATION = 4356;
	protected NotificationManager noteMan;
	public Channel currentChannel = null;
	Thread sThread, rThread;
	PokeClientSocket socket = null;
	boolean findingBattle = false;
	public ChatActivity chatActivity = null;
	public BattleActivity battleActivity = null;
	public LinkedList<IncomingChallenge> challenges = new LinkedList<IncomingChallenge>();
	public boolean askedForPass = false;
	private String salt = null;
	
	public boolean hasBattle() {
		return battle != null;
	}
	
	private FullPlayerInfo meLoginPlayer = new FullPlayerInfo();
	public PlayerInfo mePlayer = new PlayerInfo();
	protected Battle battle = null;// = new Battle();
	
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
	
	public void connect(final String ip, final int port) {
		// XXX This should probably have a timeout
		new Thread(new Runnable() {
			public void run() {
        		socket = new PokeClientSocket(ip, port);
				socket.waitConnect();
				socket.sendMessage(meLoginPlayer.serializeBytes(), Command.Login);
				if (chatActivity != null)
					chatActivity.populateUI();
				new Thread(new Runnable() {
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
		        				Bais msg = new Bais(tmp.toByteArray());
		        				handleMsg(msg);
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
		}).start();
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
	
	public void handleMsg(Bais msg) {
		byte i = msg.readByte();
		Command c = Command.values()[i];
		System.out.println("Received: " + c);
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
			break;
		case ChallengeStuff:
			IncomingChallenge challenge = new IncomingChallenge(msg);
			if (challenge.validate(players)) {
				challenges.addFirst(challenge);
				if (chatActivity != null && chatActivity.hasWindowFocus()) {
					chatActivity.notifyChallenge();
				} else {
					Notification note = new Notification(R.drawable.icon, "You've been challenged by " + challenge.oppName + "!", System.currentTimeMillis());
			        note.setLatestEventInfo(this, "POAndroid", "You've been challenged!", PendingIntent.getActivity(this, 0,
			                new Intent(NetworkService.this, ChatActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK));
			        noteMan.cancel(IncomingChallenge.note);
					noteMan.notify(IncomingChallenge.note, note);
				}
			}
			break;
		case ChannelsList:
			int numChannels = msg.readInt();
			for(int k = 0; k < numChannels; k++) {
				int chanId = msg.readInt();
				addChannel(msg.readQString(),chanId);
			}
			System.out.println(channels.toString());
			//currentChannel = channels.get(0);
			break;
		case ChannelPlayers:
			Channel ch = channels.get(msg.readInt());
			int numPlayers = msg.readInt();
			if(ch != null) {
				for(int k = 0; k < numPlayers; k++) {
					int id = msg.readInt();
					ch.addPlayer(players.get(id));
				}
			}
			else
				System.out.println("Received message for nonexistant channel");
			break;
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
			if (id1 == mePlayer.id || id2 == mePlayer.id) {
				if (players.get(id1) != null && players.get(id2) != null && battleDesc < 3)
					currentChannel.histDelta.append("\n" + players.get(id1).nick() + outcome + players.get(id2).nick() + ".");
				if (!battle.gotEnd) {
					battle.isOver = true;
					if (battleActivity != null)
						battleActivity.end();
				}
			}
			break;
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
			PlayerInfo p = new PlayerInfo(msg);
			if(!players.containsKey(p.id))
				players.put(p.id, p);
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
			if(pID1 == 0) { // This is us!
				BattleConf conf = new BattleConf(msg);
				BattleTeam team = new BattleTeam(msg);
				// Start the battle
				battle = new Battle(conf, team, players.get(conf.id(0)),
					players.get(conf.id(1)), mePlayer.id, bID, this);
				currentChannel.histDelta.append("\nBattle between " + mePlayer.nick() + 
					" and " + players.get(pID2).nick() + " started!");
				Intent in;
				in = new Intent(this, BattleActivity.class);
				in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(in);
				findingBattle = false;
			}
			break;
		case Login:
			mePlayer = new PlayerInfo(msg);
			players.put(mePlayer.id, mePlayer);
			break;
		case AskForPass:
			salt = msg.readQString();
			// XXX not sure what the second half is supposed to check
			// from analyze.cpp : 265 of PO's code
			if (salt.length() < 6) { //  || strlen((" " + salt).toUtf8().data()) < 7)
				System.out.println("Protocol Error: The server requires insecure authentication");
				break;
			}
			askedForPass = true;
			if (chatActivity != null && chatActivity.hasWindowFocus()) {
				chatActivity.notifyAskForPass();
			}
			break;
		case AddChannel:
			addChannel(msg.readQString(),msg.readInt());
			break;
		case RemoveChannel:
			int chanId = msg.readInt();
			chatActivity.removeChannel(channels.get(chanId));
			channels.remove(chanId);
			break;
		case ChanNameChange:
			chanId = msg.readInt();
			chatActivity.removeChannel(channels.get(chanId));
			channels.remove(chanId);
			channels.put(chanId, new Channel(chanId, msg.readQString(), this));
		default:
			System.out.println("Unimplented message");
		}
		if (hasBattle() && battleActivity != null && battle.histDelta.length() != 0)
			battleActivity.updateBattleInfo();
		if (chatActivity != null && currentChannel != null && currentChannel.histDelta.length() != 0)
			chatActivity.updateChat();
	}

	public void sendPass(String s) {
		askedForPass = false;
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
			Baos hashPass = new Baos();
			hashPass.putString(toHex(md5.digest((toHex(md5.digest(s.getBytes("ISO-8859-1"))) + salt).getBytes("ISO-8859-1"))));
			socket.sendMessage(hashPass, Command.AskForPass);
		} catch (NoSuchAlgorithmException nsae) {
			System.out.println("Attempting authentication threw an exception: " + nsae);
		} catch (UnsupportedEncodingException uee) {
			System.out.println("Attempting authentication threw an exception: " + uee);
		}
	}
	
	private String toHex(byte[] b) {
		return String.format("%x", new BigInteger(b));
	}
	
	protected void herp() {
		System.out.println("HERP");
	}
	
	protected void addChannel(String chanName, int chanId) {
		Channel c = new Channel(chanId, chanName, this);
		channels.put(chanId, c);
		if(chatActivity != null)
			chatActivity.addChannel(c);
	}
	
	protected void openRearrangeTeamDialog(){
		
	}
	
    public void disconnect() {
    	if (socket.isConnected())
    		socket.close();
    	this.stopForeground(true);
    	this.stopSelf();
    }
	
}
