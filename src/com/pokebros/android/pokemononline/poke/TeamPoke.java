package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.PokeParser;
import com.pokebros.android.pokemononline.SerializeBytes;

// This class is how a poke is represented in the teambuilder.
public class TeamPoke extends SerializeBytes {
	protected UniqueID uID;
	protected String nick;
	protected short item;
	protected short ability;
	protected byte nature;
	protected byte gender;
	protected boolean shiny;
	protected byte happiness;
	protected byte level;
	protected int[] moves = new int[4];
	protected byte[] DVs = new byte[6];
	protected byte[] EVs = new byte[6];
	
	public TeamPoke(Bais msg) {
		uID = new UniqueID(msg);
		nick = msg.readQString();
		item = msg.readShort();
		ability = msg.readShort();
		nature = msg.readByte();
		gender = msg.readByte();
		shiny = msg.readBool();
		happiness = msg.readByte();
		level = msg.readByte();
		
		for(int i = 0; i < 4; i++)
			moves[i] = msg.readInt();
		for(int i = 0; i < 6; i++)
			DVs[i] = msg.readByte();
		for(int i = 0; i < 6; i++)
			EVs[i] = msg.readByte();
	}
	
	public TeamPoke() {
		uID = new UniqueID();
		nick = "LOLZ";
		item = 0;
		ability = 65;
		nature = 0;
		gender = 1;
		shiny = true;
		happiness = 127;
		level = 100;
		moves[0] = 331;
		moves[1] = 213;
		moves[2] = 412;
		moves[3] = 210;
		DVs[0] = DVs[1] = DVs[2] = DVs[3] = DVs[4] = DVs[5] = 31;
		EVs[0] = EVs[1] = EVs[2] = EVs[3] = EVs[4] = EVs[5] = 10;
	}

	public TeamPoke (PokeParser p) {
		
	}
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.putBaos(uID);
		bytes.putString(nick);
		bytes.putShort(item);
		bytes.putShort(ability);
		bytes.write(nature);
		bytes.write(gender);
		bytes.putBool(shiny);
		bytes.write(happiness);
		bytes.write(level);
		for (int i = 0; i < 4; i++) {
			bytes.putInt(moves[i]);
		}
		for (int i = 0; i < 6; i++) bytes.write(DVs[i]);
		for (int i = 0; i < 6; i++) bytes.write(EVs[i]);
		return bytes;
	}
}
