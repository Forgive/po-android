package com.pokebros.android.pokemononline;

import java.util.Hashtable;

import android.text.Html;
import android.text.SpannableStringBuilder;

import com.pokebros.android.pokemononline.player.PlayerInfo;

public class Channel {
	protected String name;
	protected int id;
	protected int events = 0;
	protected boolean isReadyToQuit = false;
	
	public Hashtable<Integer, PlayerInfo> players = new Hashtable<Integer, PlayerInfo>();
	
	public SpannableStringBuilder hist = new SpannableStringBuilder();
	public SpannableStringBuilder histDelta = new SpannableStringBuilder();
	
	private NetworkService netServ;
	
	public String name(){ return name; }
	
	public String toString() {
		return name;
	}
	
	public Channel(int i, String n, NetworkService net) {
		id = i;
		name = n;
		netServ = net;
		hist.append(Html.fromHtml("<i>Joined channel: <b>" + name + "</b></i>"));
	}

	public void addPlayer(PlayerInfo p) {
		if(p != null) {
			players.put(p.id(), p);
			
			if(netServ != null && netServ.chatActivity != null)
				netServ.chatActivity.addPlayer(p);
		}
		else
			System.out.println("Tried to add nonexistant player id" +
					"to channel " + name + ", ignoring");
	}
	
	public void removePlayer(PlayerInfo p){
		if(p != null){
			players.remove(p.id());
			
			if(netServ != null && netServ.chatActivity != null)
				netServ.chatActivity.removePlayer(p);
		}
		else
			System.out.println("Tried to remove nonexistant player id" +
					"from channel " + name + ", ignoring");
	}
	
	public void handleChannelMsg(Command c, Bais msg) {
			switch(c) {
			case JoinChannel: {
				PlayerInfo p = netServ.players.get(msg.readInt());
				addPlayer(p);		
				break;
			}
			case ChannelMessage:
				//makes name bold since first occurence of : marks end of name in msg.
				 String message = "<br><b>" + msg.readQString();
				 message = message.replaceFirst(":", ":</b>");
				 histDelta.append(Html.fromHtml(message));
				break;
			case HtmlChannel:
				histDelta.append(Html.fromHtml(msg.readQString()));
				break;
			case LeaveChannel:
				PlayerInfo p = netServ.players.get(msg.readInt());
				removePlayer(p);
				break;
			default:
				break;
			}
		}
}
