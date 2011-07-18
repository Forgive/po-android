package com.pokebros.android.pokemononline;

import java.util.Hashtable;

import com.pokebros.android.pokemononline.player.PlayerInfo;

public class Channel {
	protected String name;
	protected int id;
	protected int events = 0;
	protected boolean isReadyToQuit = false;
	
	protected Hashtable<Integer, PlayerInfo> players = new Hashtable<Integer, PlayerInfo>();
	
	public String toString() {
		return name;
	}
	
	public Channel(int i, String n) {
		id = i;
		name = n;
	}
	
	public void addTrainer(PlayerInfo p) {
		if(p != null) {
			players.put(p.id(), p);
			System.out.println("Player " + p.nick() + " joined channel " + name);
		}
		else
			System.out.println("Unknown player in channel " + name);
	}
	
	public void removeTrainer(int p) {
		if(players.remove(new Integer(p)) != null)
			System.out.println("Player " + p + " has left channel " + name);
	}
	
	public void printLine(String s) {
		System.out.println("Message on channel " + name + ": " + s);
	}
	
	public void printHtml(String s) {
	}
}
