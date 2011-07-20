package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;

public class UniqueID extends SerializeBytes {
	protected short pokeNum;
	protected byte subNum;
	
	public UniqueID(Bais msg) {
		pokeNum = msg.readShort();
		subNum = msg.readByte();
	}
	
	public UniqueID(short s, byte b) {
		pokeNum = s;
		subNum = b;
	}
	
	public UniqueID() {
			pokeNum = 1;
			subNum = 0;
	}
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.putShort(pokeNum);
		bytes.write(subNum);
		return bytes;
	}
}
