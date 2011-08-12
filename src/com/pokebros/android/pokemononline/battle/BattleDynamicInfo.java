package com.pokebros.android.pokemononline.battle;

import android.text.Html;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.poke.PokeEnums;

public class BattleDynamicInfo extends SerializeBytes {
	public static final byte SPIKES = 1;
	public static final byte SPIKESL2 = 2;
	public static final byte SPIKESL3 = 4;
	public static final byte STEALTHROCK = 8;
	public static final byte TOXICSPIKES = 16;
	public static final byte TOXICSPIKESL2 = 32;
	
	byte[] boosts = new byte[7];
	byte flags;
	
	public BattleDynamicInfo(Bais b) {
		for(int i = 0; i < 7; i++) boosts[i] = b.readByte();
		flags = b.readByte();
	}
	
	@Override
	public Baos serializeBytes() {
		Baos b = new Baos();
		for(int i = 0; i < 7; i++) b.write(boosts[i]);
		b.write(flags);
		return b;
	}
	
	@Override
	public String toString() {
		String s = new String();
		s += ("Attack: +" + boosts[0]);
		s += ("\nDefense: +" + boosts[0]);
		s += ("\nSp. Att: +" + boosts[0]);
		s += ("\nSp. Def: +" + boosts[0]);
		s += ("\nSpeed: +" + boosts[0]);
		s += ("\nAccuracy: +" + boosts[0]);
		s += ("\nEvasion: +" + boosts[0]);
		
		if((flags & SPIKES) != 0) s += "\n\nSpikes";
		if((flags & SPIKESL2) != 0) s += "\nSpikes Lvl. 2";
		if((flags & SPIKESL3) != 0) s += "\nSpikes Lvl. 3";
		if((flags & STEALTHROCK) != 0) s += "\nStealth Rock";
		if((flags & TOXICSPIKES) != 0) s += "\nToxic Spikes";
		if((flags & TOXICSPIKESL2) != 0) s += "\nToxic Spikes Lvl. 2";
		return s;
	}
	
	public String statsAndHazards() {
		String s;
		s = "Attack:";
		s += "\nDefense:";
		s += "\nSp. Att:";
		s += "\nSp. Def:";
		s += "\nSpeed:";
		s += "\nAccuracy:";
		s += "\nEvasion:";
		if(flags != 0) s += "\n";
		if((flags & SPIKES) != 0) s += "\nSpikes";
		if((flags & SPIKESL2) != 0) s += "\nSpikes Lvl. 2";
		if((flags & SPIKESL3) != 0) s += "\nSpikes Lvl. 3";
		if((flags & STEALTHROCK) != 0) s += "\nStealth Rock";
		if((flags & TOXICSPIKES) != 0) s += "\nToxic Spikes";
		if((flags & TOXICSPIKESL2) != 0) s += "\nToxic Spikes Lvl. 2";
		return s;
	}
	
	public String numbers() {
		String s = new String();
		for(int i = 0; i < 7; i++)
			s += ((i == 0 ? "" : "\n") + (boosts[i] < 0 ? "" : "+") + boosts[i]);
		return s;
	}
}
