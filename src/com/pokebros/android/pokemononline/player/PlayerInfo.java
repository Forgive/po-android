package com.pokebros.android.pokemononline.player;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.QColor;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.poke.UniqueID;

// Represents a player in the player list
public class PlayerInfo extends SerializeBytes {
	int id = 0;
	public String nick = "BROLOLHAH";
	String info = "SUP";
	byte auth = 0, flags = 0;
	short rating = 0;
	UniqueID[] pokes = new UniqueID[6];
	short avatar = 72;
	String tier = "OU";
	QColor color = new QColor();
	byte gen = 5;
	
	public String nick() { return nick; }
	public int id() { return id; }
	public String toString() { return nick; }
	
	public PlayerInfo() {}
	public PlayerInfo(Bais msg) {
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
		color = new QColor(msg);
		gen = msg.readByte();
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.putInt(id);
		b.putString(nick);
		b.putString(info);
		b.write(auth);
		b.write(flags);
		b.putShort(rating);
		
		for(int i = 0; i < 6; i++)
			b.putBaos(pokes[i]);
		
		b.putBaos(color);
		b.write(gen);
		return b;
	}
}
