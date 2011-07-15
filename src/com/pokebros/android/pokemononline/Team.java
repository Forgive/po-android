package com.pokebros.android.pokemononline;
import java.io.ByteArrayOutputStream;

public class Team extends SerializeBytes {
	protected byte gen;
	protected Poke[] pokes;
		
	public Team() {
			gen = 5;
			pokes = new Poke[6];
			for(int i = 0; i < 6; i++) pokes[i] = new Poke();
	}
	
	public ByteArrayOutputStream serializeBytes() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bytes.write(gen);
		for(int i = 0; i < 6; i++)
			Utils.putBaos(bytes, pokes[i]);
		return bytes;
	}
}
