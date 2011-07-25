package com.pokebros.android.pokemononline.battle;

import android.graphics.Color;
import android.graphics.PorterDuff;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.ColorEnums.TypeColor;

public class BattleMove extends SerializeBytes {
	byte currentPP = 0;
	byte totalPP = 0;
	short num = 0;
	
	public String toString() {
		return Battle.queryDB("SELECT name FROM [Moves] WHERE _id = " + num);	
	}
	
	public int getColor() {
		int type = new Integer(Battle.queryDB("SELECT type FROM [Moves] WHERE _id = " + num));
		String s = TypeColor.values()[type].toString();
		s = s.replaceAll(">", "");
		return Color.parseColor(s);
	}
	
	public String getTypeString() {
		int type = new Integer(Battle.queryDB("SELECT type FROM [Moves] WHERE _id = " + num));
		String s = Type.values()[type].toString();
		return s.replaceAll(">", "");
	}
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
