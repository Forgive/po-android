package com.pokebros.android.pokemononline;

class DummyQColor extends Player {
	protected byte spec;
	protected short alpha;
	protected short red;
	protected short green;
	protected short blue;
	protected short pad;
	
	public DummyQColor(Bais msg) {
		spec = msg.readByte();
		alpha = msg.readShort();
		red = msg.readShort();
		green = msg.readShort();
		blue = msg.readShort();
		pad = msg.readShort();
	}
	
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

public class Trainer extends Player {
	protected int id = 0;
	protected byte auth = 0;
	protected byte flags = 0;
	protected int rating = 0;
	
	protected String loseMsg = "SHUCKS!";
	protected String winMsg = "YEEEAAAAHH!!!";
		
	protected short avatar = 72;
	protected String defaultTier = "OU";
	protected String tier = "OU";
	protected Team team = new Team();
	protected UniqueID[] pokes = new UniqueID[6];
		
	protected boolean ladderEnabled = false;
	protected boolean showTeam = true;
	protected DummyQColor nameColor = new DummyQColor();
	
	protected byte gen = 0;
	
	public Trainer(Bais msg) {
		id = msg.readInt();
		nick = msg.readQString();
		info = msg.readQString();
		auth = msg.readByte();
		flags = msg.readByte();
		rating = msg.readShort();
		for(int i = 0; i < 6; i++)
			pokes[i] = new UniqueID(msg);
		avatar = msg.readShort();
		tier = msg.readQString();
		nameColor = new DummyQColor(msg);
		gen = msg.readByte();
	}
	
	public Trainer() {}
	
	public String toString() {
		return nick;
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
