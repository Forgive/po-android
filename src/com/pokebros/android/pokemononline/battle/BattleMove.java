package com.pokebros.android.pokemononline.battle;

import android.graphics.Color;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.DataBaseHelper;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.ColorEnums.TypeColor;

public class BattleMove extends SerializeBytes {
	public byte currentPP = 0;
	public byte totalPP = 0;
	public short num = 0;
	DataBaseHelper dbh;
	
	public String toString() {
		return dbh.query("SELECT name FROM [Moves] WHERE _id = " + num);
	}
	
	public int getColor() {
		String s = TypeColor.values()[getType()].toString();
		s = s.replaceAll(">", "");
		return Color.parseColor(s);
	}
	
	public String getTypeString() {
		return Type.values()[getType()].toString();
	}
	
	public byte getType() {
		return new Byte(dbh.query("SELECT type FROM [Moves] WHERE _id = " + num));
	}
	
	public BattleMove(DataBaseHelper db) {
		dbh = db;
	}
	
	public BattleMove(Bais msg, DataBaseHelper db) {
		num = msg.readShort();
		currentPP = msg.readByte();
		totalPP = msg.readByte();
		dbh = db;
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.putShort(num);
		b.write(currentPP);
		b.write(totalPP);
		return b;
	}
}
