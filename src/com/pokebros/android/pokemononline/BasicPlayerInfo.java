package com.pokebros.android.pokemononline;

// This class represents players in the player list.
public class BasicPlayerInfo extends SerializeBytes {
	protected String nick;
	protected String info;
	
	public BasicPlayerInfo() {
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
