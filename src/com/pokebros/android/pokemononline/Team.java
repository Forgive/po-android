package com.pokebros.android.pokemononline;

import com.pokebros.android.pokemononline.poke.TeamPoke;
import com.pokebros.android.pokemononline.poke.UniqueID;

public class Team extends SerializeBytes {
	protected byte gen = 5;
	protected TeamPoke[] pokes = new TeamPoke[6];
	
	public Team(Bais msg) {
		gen = msg.readByte();
		for(int i = 0; i < 6; i++)
			pokes[i] = new TeamPoke(msg);
	}
	
	public Team() {
		for (int i = 0; i < 6; i++)
			pokes[i] = new TeamPoke();
	}
	
	public Team(TeamPoke[] tp) {
		for (int i = 0; i < 6; i++) {
			pokes[i] = tp[i];
		}
	}
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.write(gen);
		for(int i = 0; i < 6; i++)
			bytes.putBaos(pokes[i]);
		return bytes;
	}
}
