package com.pokebros.android.pokemononline;

class UniqueID {
	short pokeNum;
	byte subNum;
}

public class Poke {
	protected
		UniqueID uID;
		byte gen;
		String nick;
		short item;
		short ability;
		byte nature;
		byte gender;
		boolean shiny;
		byte happiness;
		byte level;
}
