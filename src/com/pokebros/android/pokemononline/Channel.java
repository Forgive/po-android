package com.pokebros.android.pokemononline;

import java.util.Hashtable;
import java.util.LinkedList;

import android.text.Html;
import android.text.SpannableStringBuilder;

import com.pokebros.android.pokemononline.player.PlayerInfo;

public class Channel {
	protected String name;
	protected int id;
	protected int events = 0;
	public int lastSeen = 0;
	protected boolean isReadyToQuit = false;
	public final static int HIST_LIMIT = 1000;
	
	public Hashtable<Integer, PlayerInfo> players = new Hashtable<Integer, PlayerInfo>();
	
	LinkedList<SpannableStringBuilder> messageList = new LinkedList<SpannableStringBuilder>();
	
	public void writeToHist(CharSequence text) {
		SpannableStringBuilder spannable;
		if (text.getClass() != SpannableStringBuilder.class)
			spannable = new SpannableStringBuilder(text);
		else
			spannable = (SpannableStringBuilder)text;
		synchronized(messageList) {
			messageList.add(spannable);
			lastSeen++;
			if (messageList.size() > HIST_LIMIT)
				messageList.remove();
		}
	}
	
	private NetworkService netServ;
	
	public String name(){ return name; }
	
	public String toString() {
		return name;
	}
	
	public Channel(int i, String n, NetworkService net) {
		id = i;
		name = n;
		netServ = net;
		writeToHist(Html.fromHtml("<i>Joined channel: <b>" + name + "</b></i>"));
	}

	public void addPlayer(PlayerInfo p) {
		if(p != null) {
			players.put(p.id, p);
			
			if(netServ != null && netServ.chatActivity != null && this.equals(netServ.joinedChannels.peek()))
				netServ.chatActivity.addPlayer(p);
		}
		else
			System.out.println("Tried to add nonexistant player id" +
					"to channel " + name + ", ignoring");
	}
	
	public void removePlayer(PlayerInfo p){
		if(p != null){
			players.remove(p.id);
			
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
				if (p.id == netServ.mePlayer.id) { // We joined the channel
					netServ.joinedChannels.addFirst(this);
					if (netServ.chatActivity != null) {
						netServ.chatActivity.populateUI(true);
						netServ.chatActivity.progressDialog.dismiss();
					}
				}
				addPlayer(p);
				break;
			}
			case ChannelMessage:
				//int id = msg.readInt();
				PlayerInfo player = null;
				//if (id != 0) {
					//player = netServ.players.get(id);
					//if (player == null)
						player = new PlayerInfo();
					String[] splitMessage = msg.readQString().split(":", 2);
					if (splitMessage.length < 2) // XXX only necessary while playerId is not included in ChannelMessage
						writeToHist(Html.fromHtml(splitMessage[0]));
					//if (player.auth < 3)
						splitMessage[1] = NetworkService.escapeHtml(splitMessage[1]);
					/*else
						message = splitMessage[1].toString();*/
					splitMessage[0] = "<font " + player.color + (player.auth > 0 ? "+<i><b>" : "<b>") +
							splitMessage[0] + (player.auth > 0 ? "</i>:</b></font>" : ":</b></font>");
					writeToHist(Html.fromHtml(splitMessage[0] + splitMessage[1]));
				/*} else {
					writeToHist(Html.fromHtml(splitMessage[0]));
				}*/
				break;
			case HtmlChannel:
				writeToHist(Html.fromHtml(msg.readQString()));
				break;
			case LeaveChannel:
				PlayerInfo p = netServ.players.get(msg.readInt());
				if (p.id == netServ.mePlayer.id) { // We left the channel
					// XXX this runtime complexity sucks
					netServ.joinedChannels.remove(this);
					if (netServ.chatActivity != null) {
						netServ.chatActivity.populateUI(true);
					}
				}
				removePlayer(p);
				break;
			default:
				break;
			}
		}
}
