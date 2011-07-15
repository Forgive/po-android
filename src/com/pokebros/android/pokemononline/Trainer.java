package com.pokebros.android.pokemononline;

class DummyQColor extends SerializeBytes {
	protected byte spec;
	protected short alpha;
	protected short red;
	protected short green;
	protected short blue;
	protected short pad;
		
	public DummyQColor() {
			spec = 0;
			alpha |= 0xffff;
			red = green = blue = 0;
			pad = 0;
	}
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.write(spec);
		
		bytes.putShort(alpha);
		bytes.putShort(red);
		bytes.putShort(green);
		bytes.putShort(blue);
		bytes.putShort(pad);
		
		return bytes;
	}
}

public class Trainer extends SerializeBytes {
	protected String nick;
	protected String info;
	protected String loseMsg;
	protected String winMsg;
		
	protected short avatar;
	protected String defaultTier;
	protected Team team;
		
	protected boolean ladderEnabled;
	protected boolean showTeam;
	protected DummyQColor nameColor;
		
	public Trainer() {
			nick = "BROBRO";
			info = "Sup Bro";
			loseMsg = "SHUCKS!";
			winMsg = "YEAAAH!!!";
			avatar = 72;
			defaultTier = "OU";
			team = new Team();
			ladderEnabled = showTeam = true;
			nameColor = new DummyQColor();
	}
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.putString(nick);
		bytes.putString(info);
		bytes.putString(loseMsg);
		bytes.putString(winMsg);	
		bytes.putShort(avatar);
		bytes.putString(defaultTier);
		bytes.putBaos(team);
		bytes.putBool(ladderEnabled);
		bytes.putBool(showTeam);
		bytes.putBaos(nameColor);
		return bytes;
	}
}
