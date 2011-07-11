package com.pokebros.android.pokemononline;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
		
		bytes.write(alpha >> 8);
		bytes.write(alpha);
		
		bytes.write(red >> 8);
		bytes.write(red);
		
		bytes.write(green >> 8);
		bytes.write(green);
		
		bytes.write(blue >> 8);
		bytes.write(blue);
		
		bytes.write(pad >> 8);
		bytes.write(pad);
		
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
	
	protected void putQString(ByteArrayOutputStream b, String s) {
		byte[] byteHold = new byte[4];
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putInt(s.length() << 1);
		bb.rewind();
		bb.get(byteHold);
		try {
			b.write(byteHold);
			b.write(s.getBytes("UTF-16BE"));
		} catch (Exception e) {
			System.exit(-1);
		}
	}	
	
	public ByteArrayOutputStream serializeBytes() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		putQString(bytes, nick);
		putQString(bytes, info);
		putQString(bytes, loseMsg);
		putQString(bytes, winMsg);
		
		bytes.write(avatar >> 8);
		bytes.write(avatar);
		
		putQString(bytes, defaultTier);
		
		try {
			bytes.write(team.serializeBytes().toByteArray());
		} catch (Exception e) {
			System.exit(-1);
		}
		
		bytes.write((int)(ladderEnabled ? 1 : 0));
		bytes.write((int)(showTeam ? 1 : 0));
		
		try {
			bytes.write(nameColor.serializeBytes().toByteArray());
		} catch (Exception e) {
			System.exit(-1);
		}
		return bytes;
	}
}
