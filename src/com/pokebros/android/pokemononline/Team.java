package com.pokebros.android.pokemononline;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Team {
	protected byte gen;
	protected Poke[] pokes;
		
	public Team() {
			gen = 5;
			pokes = new Poke[6];
	}
	
	public ByteArrayOutputStream serializeBytes() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bytes.write(gen);
		for(int i = 0; i < 6; i++) {
			try {
				bytes.write(pokes[i].serializeBytes().toByteArray());
			} catch (IOException e) {
				System.exit(-1);
			}
		}
		return bytes;
	}
}
