package com.pokebros.android.pokemononline.player;

import java.util.Comparator;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.QColor;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.poke.UniqueID;

// Represents a player in the player list
public class PlayerInfo extends SerializeBytes {
	public int id = 0;
	String nick = "BROLOLHAH";
	String info = "SUP";
	byte auth = 0, flags = 0;
	public short rating = 0;
	public UniqueID[] pokes = new UniqueID[6];
	short avatar = 72;
	public String tier = "OU";
	QColor color = new QColor();
	byte gen = 5;
	
	public String nick() { return nick; }
	public String info() { return info; }
	public String toString() { return nick; }
	
	public PlayerInfo(FullPlayerInfo player) { nick = player.nick(); }
	
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

	static public class ComparePlayerInfos implements Comparator<PlayerInfo> {
		public int compare(PlayerInfo p1, PlayerInfo p2) {
			return p1.nick.compareTo(p2.nick);
		}
	}

	
	public boolean equals(PlayerInfo p) {
		return nick.equals(p);
	}
}