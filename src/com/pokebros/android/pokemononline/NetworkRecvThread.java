package com.pokebros.android.pokemononline;

import java.util.ArrayList;
import java.util.Hashtable;

import android.os.Handler;

enum Command {
    WhatAreYou, //= 0,
    WhoAreYou,
    Login,
    Logout,
    SendMessage,
    PlayersList,
    SendTeam,
    ChallengeStuff,
    EngageBattle,
    BattleFinished,
    BattleMessage, // = 10,
    BattleChat,
    KeepAlive, /* obsolete since we use a native Qt option now */
    AskForPass,
    Register,
    PlayerKick,
    PlayerBan,
    ServNumChange,
    ServDescChange,
    ServNameChange,
    SendPM, // = 20,
    Away,
    GetUserInfo,
    GetUserAlias,
    GetBanList,
    CPBan,
    CPUnban,
    SpectateBattle,
    SpectatingBattleMessage,
    SpectatingBattleChat,
    SpectatingBattleFinished, // = 30,
    LadderChange,
    ShowTeamChange,
    VersionControl,
    TierSelection,
    ServMaxChange,
    FindBattle,
    ShowRankings,
    Announcement,
    CPTBan,
    CPTUnban, // = 40,
    PlayerTBan,
    GetTBanList,
    BattleList,
    ChannelsList,
    ChannelPlayers,
    JoinChannel,
    LeaveChannel,
    ChannelBattle,
    RemoveChannel,
    AddChannel, // = 50,
    ChannelMessage,
    ChanNameChange,
    HtmlMessage,
    HtmlChannel,
    ServerName,
    SpecialPass,
    ServerListEnd,              // Indicates end of transmission for registry.
    SetIP                       // Indicates that a proxy server sends the real ip of client
};

public class NetworkRecvThread implements Runnable {
	private PokeClientSocket socket;
	private Handler handler;
	private Bais msg;
	
	public NetworkRecvThread(PokeClientSocket s, Handler h) {
		socket = s;
		handler = h;
		if(!socket.isConnected())
			socket.connect();
	}
	
	public void run() {
		while(true) {
			msg = new Bais(socket.recvMessage().toByteArray());
			handleMsg();
			handler.sendMessage(handler.obtainMessage(0, "BROBRO"));
					//handler.obtainMessage((int) baos.toByteArray()[2], baos));
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
			String htmlMessage = msg.readQtString();
			System.out.println("Html Message: " + htmlMessage);
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
		default:
			break;
		}
	}
}
