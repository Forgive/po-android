package com.pokebros.android.pokemononline;

class UniqueID {
	short pokeNum;
	byte subNum;
	
	public
		UniqueID() {
			pokeNum = 1;
			subNum = 0;
	}
}

public class Poke {
	protected
		UniqueID uID;
		//byte gen;
		String nick;
		short item;
		short ability;
		byte nature;
		byte gender;
		boolean shiny;
		byte happiness;
		byte level;
		short[] moves;
		byte[] DVs;
		byte[] EVs;
		
	public 
		Poke() {
			uID = new UniqueID();
			//gen = 5;
			nick = "LOLZ";
			item = 37;
			ability = 65;
			nature = 2;
			gender = 1;
			shiny = true;
			happiness = 127;
			level = 100;
			moves = new short[4];
			moves[0] = 331;
			moves[1] = 213;
			moves[2] = 412;
			moves[3] = 210;
			DVs = new byte[6];
			DVs[0] = DVs[1] = DVs[2] = DVs[3] = DVs[4] = DVs[5] = 31;
			EVs = new byte[6];
			EVs[0] = EVs[1] = EVs[2] = EVs[3] = EVs[4] = EVs[5] = 10;
		}
}