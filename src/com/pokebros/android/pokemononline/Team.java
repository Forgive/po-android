package com.pokebros.android.pokemononline;

public class Team extends SerializeBytes {
	protected byte gen = 5;
	protected Poke[] pokes = new Poke[6];
	
	public Team(Bais msg) {
		gen = msg.readByte();
		for(int i = 0; i < 6; i++)
			pokes[i] = new Poke(msg);
	}
	public Team() {
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
