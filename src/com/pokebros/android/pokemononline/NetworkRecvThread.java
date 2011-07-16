package com.pokebros.android.pokemononline;

import java.util.ArrayList;
import java.util.Hashtable;

public class NetworkRecvThread implements Runnable {
	private PokeClientSocket socket;
	private Bais msg;
	
	public NetworkRecvThread(PokeClientSocket s) {
		socket = s;
		if(!socket.isConnected())
			socket.connect();
	}
	
	public void run() {
		while(true) {
			msg = new Bais(socket.recvMessage().toByteArray());
			handleMsg();
		}
	}
	
	public void handleChannelMsg(Command c) {
		switch(c) {
		case JoinChannel:
			int playerID = msg.readInt();
			System.out.println("Player " + playerID + " joined the channel");
			break;
		case ChannelMessage:
			String message = msg.readQtString();
			System.out.println("Message: " + message);
			break;
		case HtmlChannel:
			String htmlChannel = msg.readQtString();
			System.out.println("Html Channel: " + htmlChannel);
			break;
		case LeaveChannel:
			playerID = msg.readInt();
			System.out.println("Player " + playerID + " has left the channel.");
			break;
		default:
			break;
		}
	}
	
	public void handleMsg() {
		/* Completely obvious way to "convert"
		 * a byte into a value in an enum.
		 */
		int i = msg.read();
		System.out.println("Command ID: " + i);
		Command c = Command.values()[i];
		switch(c) {
		case BattleList:
		case JoinChannel:
		case LeaveChannel:
		case ChannelBattle:
		case ChannelMessage:
		case HtmlChannel:
			int chanID = msg.readInt();
			handleChannelMsg(c);
			break;
		case TierSelection:
			msg.readInt();
			ArrayList<String> list = new ArrayList<String>();
			while(msg.available() != 0) {
				msg.read();
				list.add(msg.readQtString());
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
			Baos b = new Baos();
			b.write(1);
			b.putInt(opponent);
			b.putInt(clauses);
			b.write(mode);
			Thread cThread = new Thread(new NetworkSendThread(socket, b, Command.ChallengeStuff));
	        cThread.start();
			break;
		case ChannelsList:
			int numChannels = msg.readInt();
			Hashtable<Integer, String> channels = new Hashtable<Integer, String>();
			for(int k = 0; k < numChannels; k++) {
				channels.put(msg.readInt(), msg.readQtString());
			}
			System.out.println(channels.toString());
			break;
		case ChannelPlayers:
			chanID = msg.readInt();
			int numPlayers = msg.readInt();
			ArrayList<Integer> playerIDs = new ArrayList<Integer>();
			for(int k = 0; k < numPlayers; k++)
				playerIDs.add(msg.readInt());
			System.out.println("Channel ID: " + chanID);
			System.out.println("Players: " + playerIDs.toString());
			break;
		case HtmlMessage:
			String htmlMessage = msg.readQtString();
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
		default:
			System.out.println("Unimplented message");
		}
	}
}
