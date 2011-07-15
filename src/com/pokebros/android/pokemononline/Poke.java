package com.pokebros.android.pokemononline;
import java.io.ByteArrayOutputStream;

class UniqueID {
	protected short pokeNum;
	protected byte subNum;
	
	public UniqueID() {
			pokeNum = 1;
			subNum = 0;
	}
	
	public ByteArrayOutputStream serializeBytes() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Utils.putShort(bytes, pokeNum);
		bytes.write(subNum);
		return bytes;
	}
}

public class Poke {
	protected UniqueID uID;
	protected String nick;
	protected short item;
	protected short ability;
	protected byte nature;
	protected byte gender;
	protected boolean shiny;
	protected byte happiness;
	protected byte level;
	protected int[] moves;
	protected byte[] DVs;
	protected byte[] EVs;
		
	public Poke() {
		uID = new UniqueID();
		nick = "LOLZ";
		item = 0;
		ability = 65;
		nature = 0;
		gender = 1;
		shiny = true;
		happiness = 127;
		level = 100;
		moves = new int[4];
		moves[0] = 331;
		moves[1] = 213;
		moves[2] = 412;
		moves[3] = 210;
		DVs = new byte[6];
		DVs[0] = DVs[1] = DVs[2] = DVs[3] = DVs[4] = DVs[5] = 31;
		EVs = new byte[6];
		EVs[0] = EVs[1] = EVs[2] = EVs[3] = EVs[4] = EVs[5] = 10;
	}

	public ByteArrayOutputStream serializeBytes() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		Utils.putBaos(bytes, uID.serializeBytes());
		Utils.putString(bytes, nick);
		Utils.putShort(bytes, item);
		Utils.putShort(bytes, ability);
		bytes.write(nature);
		bytes.write(gender);
		Utils.putBool(bytes, shiny);
		bytes.write(happiness);
		bytes.write(level);
		for (int i = 0; i < 4; i++) {
			Utils.putInt(bytes, moves[i]);
		}
		for (int i = 0; i < 6; i++) bytes.write(DVs[i]);
		for (int i = 0; i < 6; i++) bytes.write(EVs[i]);
		return bytes;
	}
}
