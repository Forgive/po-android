package com.pokebros.android.pokemononline;

import java.io.ByteArrayOutputStream;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

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
	
	public NetworkRecvThread(PokeClientSocket s, Handler h) {
		socket = s;
		handler = h;
		if(!socket.isConnected())
			socket.connect();
	}
	
	public void run() {
		while(true) {
			ByteArrayOutputStream baos = socket.recvMessage();
			handler.sendMessage(handler.obtainMessage(0, "BROBRO"));
					//handler.obtainMessage((int) baos.toByteArray()[2], baos));
		}
	}
}
