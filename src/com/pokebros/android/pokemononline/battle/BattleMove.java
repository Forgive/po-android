package com.pokebros.android.pokemononline.battle;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;

public class BattleMove extends SerializeBytes {
	byte currentPP = 0;
	byte totalPP = 0;
	short num = 0;
	
	public BattleMove(Bais msg) {
		num = msg.readShort();
		currentPP = msg.readByte();
		totalPP = msg.readByte();
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.putShort(num);
		b.write(currentPP);
		b.write(totalPP);
		return b;
	}
}
