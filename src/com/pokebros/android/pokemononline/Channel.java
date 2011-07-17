package com.pokebros.android.pokemononline;

import java.util.Hashtable;

public class Channel {
	protected String name;
	protected int id;
	protected int events = 0;
	protected boolean isReadyToQuit = false;
	
	protected Hashtable<Integer, Trainer> trainers = new Hashtable<Integer, Trainer>();
	
	public String toString() {
		return name;
	}
	
	public Channel(int i, String n) {
		id = i;
		name = n;
	}
	
	public void addTrainer(Trainer t) {
		if(t != null) {
			trainers.put(t.id, t);
			System.out.println("Player " + t.nick + " joined channel " + name);
		}
		else
			System.out.println("Unknown player in channel " + name);
	}
	
	public void removeTrainer(int p) {
		if(trainers.remove(new Integer(p)) != null)
			System.out.println("Player " + p + " has left channel " + name);
	}
	
	public void printLine(String s) {
		System.out.println("Message on channel " + name + ": " + s);
	}
	
	public void printHtml(String s) {
	}
}
