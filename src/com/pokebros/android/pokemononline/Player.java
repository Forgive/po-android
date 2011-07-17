package com.pokebros.android.pokemononline;

public class Player extends SerializeBytes {
	protected String nick;
	protected String info;
	
	public Player() {
		nick = "BROBRO";
		info = "Sup Bro";
	}
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.putString(nick);
		bytes.putString(info);
		return bytes;
	}
}
