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
	
	public String toString() {
		return name;
	}
	
	public Channel(int i, String n, NetworkService net) {
		id = i;
		name = n;
		netServ = net;
	}

	public void handleChannelMsg(Command c, Bais msg) {
			switch(c) {
			case JoinChannel: {
				PlayerInfo p = netServ.players.get(msg.readInt());
				if(p != null)
					players.put(p.id(), p);
				else
					System.out.println("Tried to add nonexistant player id" +
							"to channel " + id + ", ignoring");
				break;
			}
			case ChannelMessage:
				histDelta.append(msg.readQString());
				break;
			case HtmlChannel:
				histDelta.append(Html.fromHtml(msg.readQString()));
				break;
			case LeaveChannel:
				players.remove(msg.readInt());
				break;
			default:
				break;
			}
		}
}
