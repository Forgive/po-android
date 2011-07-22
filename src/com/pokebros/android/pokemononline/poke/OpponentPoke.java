package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;

// This class represents the Opponent's poke during a battle.
public class OpponentPoke extends SerializeBytes {
	String rnick, nick = "";
	int fullStatus = 0;
	UniqueID uID = new UniqueID();
	boolean shiny = false;
	byte gender = 0;
	byte lifePercent = 0;
	byte level = 0;
	
	public String nick() { return nick; }
	public String rnick() { return rnick; }
	public OpponentPoke(Bais msg, int player) {
		uID = new UniqueID(msg);
		rnick = nick = msg.readQString();
		if (player == 0)
			nick = "The foe's " + nick;
		lifePercent = msg.readByte();
		fullStatus = msg.readInt();
		gender = msg.readByte();
		shiny = msg.readBool();
		level = msg.readByte();
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.putBaos(uID);
		b.putString(nick);
		b.write(lifePercent);
		b.putInt(fullStatus);
		b.write(gender);
		b.putBool(shiny);
		b.write(level);
		return b;
	}
}
