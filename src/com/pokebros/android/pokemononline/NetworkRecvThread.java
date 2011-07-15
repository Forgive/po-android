package com.pokebros.android.pokemononline;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

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
	private ByteArrayInputStream msg;
	
	public NetworkRecvThread(PokeClientSocket s, Handler h) {
		socket = s;
		handler = h;
		if(!socket.isConnected())
			socket.connect();
	}
	
	public void run() {
		while(true) {
			msg = new ByteArrayInputStream(socket.recvMessage().toByteArray());
			handleMsg();
			handler.sendMessage(handler.obtainMessage(0, "BROBRO"));
					//handler.obtainMessage((int) baos.toByteArray()[2], baos));
		}
	}
	
	public String readQtString() {
		int len = readInt();
		System.out.println("String length: " + len);
		
		/* Yeah, I know, everything in Java is signed.
		 * If you're sending strings too long to fit in
		 * an unsigned int, may God help you.
		 */
		byte[] bytes = new byte[len];
		msg.read(bytes, 0, len);
		
		String str = null;
		try {
			str = new String(bytes, "UTF-16BE");
		} catch (Exception e) {
			System.exit(-1);
		}
		
		return str;
	}
	
	public ArrayList<String> readQStringList() {
		readInt();
		ArrayList<String> list = new ArrayList<String>();
		while(msg.available() != 0) {
			msg.read();
			list.add(readQtString());
		}
		
		return list;
	}
	
	public short readShort() {
		short s = 0;
		s |= (msg.read() << 8);
		s |= (msg.read() & 0xff);
		
		return s;
	}
	
	public int readInt() {
		int i = 0;
		i |= (msg.read() << 24);
		i |= ((msg.read() & 0xff0000)  << 16);
		i |= ((msg.read() & 0xff00) << 8);
		i |= ((msg.read() & 0xff));
		
		return i;
	}
	
	public void handleTierSelection() {
		System.out.println(readQStringList().toString());
	}
	
	public void handleMsg() {
		/* Completely obvious way to "convert"
		 * a byte into a value in an enum.
		 */
		int i = msg.read();
		System.out.println("Command ID: " + i);
		Command c = Command.values()[i];
		switch(c) {
			case TierSelection:
				handleTierSelection();
				break;
			default:
				break;
		}
	}
}
