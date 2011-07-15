package com.pokebros.android.pokemononline;
import java.io.ByteArrayOutputStream;

class DummyQColor {
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
	
	public ByteArrayOutputStream serializeBytes() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bytes.write(spec);
		
		Utils.putShort(bytes, alpha);
		Utils.putShort(bytes, red);
		Utils.putShort(bytes, green);
		Utils.putShort(bytes, blue);
		Utils.putShort(bytes, pad);
		
		return bytes;
	}
}

public class Trainer {
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
	
	public ByteArrayOutputStream serializeBytes() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Utils.putString(bytes, nick);
		Utils.putString(bytes, info);
		Utils.putString(bytes, loseMsg);
		Utils.putString(bytes, winMsg);	
		Utils.putShort(bytes, avatar);
		Utils.putString(bytes, defaultTier);
		Utils.putBaos(bytes, team.serializeBytes());
		Utils.putBool(bytes, ladderEnabled);
		Utils.putBool(bytes, showTeam);
		Utils.putBaos(bytes, nameColor.serializeBytes());
		return bytes;
	}
}
