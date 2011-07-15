package com.pokebros.android.pokemononline;

import java.io.ByteArrayInputStream;
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
			handler.sendMessage(handler.obtainMessage(0, "BROBRO"));
					//handler.obtainMessage((int) baos.toByteArray()[2], baos));
		}
	}
	
	public String readQtString() {
		int len = readInt();
		
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
	
	public short readShort() {
		short s = 0;
		s |= (msg.read() << 8);
		s |= (msg.read());
		
		return s;
	}
	
	public int readInt() {
		int i = 0;
		i |= (msg.read() << 24);
		i |= (msg.read() << 16);
		i |= (msg.read() << 8);
		i |= (msg.read());
		
		return i;
	}
	
	public void handleMessage() {
		/* Completely obvious way to "convert"
		 * a byte into a value in an enum.
		 */
		Command c = Command.values()[msg.read()];
		switch(c) {
		case TierSelection:
		}
	}
}