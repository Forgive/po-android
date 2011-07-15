package com.pokebros.android.pokemononline;

public class Team extends SerializeBytes {
	protected byte gen;
	protected Poke[] pokes;
		
	public Team() {
			gen = 5;
			pokes = new Poke[6];
			for(int i = 0; i < 6; i++) pokes[i] = new Poke();
	}
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.write(gen);
		for(int i = 0; i < 6; i++)
			bytes.putBaos(pokes[i]);
		return bytes;
	}
}
