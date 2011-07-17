package com.pokebros.android.pokemononline;

import java.util.ArrayList;

public class Channel {
	protected String name;
	protected int id;
	protected int events = 0;
	protected boolean isReadyToQuit = false;
	

	protected ArrayList<Integer> playerIDs = new ArrayList<Integer>();
	
	public String toString() {
		return name;
	}
	
	public Channel(int i, String n) {
		id = i;
		name = n;
	}
	
	public void addPlayer(int p) {
		playerIDs.add(p);
		System.out.println("Player " + p + " joined channel " + name);
	}
	
	public void removePlayer(int p) {
		// ArrayList.remove(int) and ArrayList.remove(Object) are different functions
		if(playerIDs.remove(new Integer(p)))
			System.out.println("Player " + p + " has left channel " + name);
	}
	
	public void printLine(String s) {
		System.out.println("Message on channel " + name + ": " + s);
	}
	
	public void printHtml(String s) {
	}
}
