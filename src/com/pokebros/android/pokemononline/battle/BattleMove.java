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
	public String name = "null";
	public byte type;
	
	public String toString() {
		return name;
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
		return type;
	}
	
	public BattleMove(int n, DataBaseHelper db) {
		num = (short) n;
		name = db.query("SELECT name FROM [Moves] WHERE _id = " + num);
		type = new Byte(db.query("SELECT type FROM [Moves] WHERE _id = " + num));
	}
	
	public BattleMove(BattleMove bm, DataBaseHelper db) {
		currentPP = bm.currentPP;
		num = bm.num;
		totalPP = (byte)(new Byte(db.query("SELECT pp FROM [Moves] WHERE _id = " + num)) * 1.6);
		name = db.query("SELECT name FROM [Moves] WHERE _id = " + num);
		type = bm.type;
	}
	
	public BattleMove(Bais msg, DataBaseHelper db) {
		num = msg.readShort();
		currentPP = msg.readByte();
		totalPP = msg.readByte();
		name = db.query("SELECT name FROM [Moves] WHERE _id = " + num);
		type = new Byte(db.query("SELECT type FROM [Moves] WHERE _id = " + num));
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.putShort(num);
		b.write(currentPP);
		b.write(totalPP);
		return b;
	}
}
