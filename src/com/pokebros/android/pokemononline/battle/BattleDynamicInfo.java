package com.pokebros.android.pokemononline.battle;

import android.text.Html;
import android.text.SpannableStringBuilder;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.poke.ShallowBattlePoke;

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
	
	public SpannableStringBuilder statsAndHazards(ShallowBattlePoke p) {
		SpannableStringBuilder s;
		s = new SpannableStringBuilder(Html.fromHtml("<b>" + p.pokeName + "</b>"));
		s.append("\n" + p.types[0]);
		if(p.types[1] != Type.Curse) s.append("/" + p.types[1]);
		s.append("\n\nAttack:");
		s.append("\nDefense:");
		s.append("\nSp. Att:");
		s.append("\nSp. Def:");
		s.append("\nSpeed:");
		s.append("\nAccuracy:");
		s.append("\nEvasion:");
		if(flags != 0) s.append("\n");
		if((flags & SPIKES) != 0) s.append("\nSpikes");
		if((flags & SPIKESL2) != 0) s.append("\nSpikes Lvl. 2");
		if((flags & SPIKESL3) != 0) s.append("\nSpikes Lvl. 3");
		if((flags & STEALTHROCK) != 0) s.append("\nStealth Rock");
		if((flags & TOXICSPIKES) != 0) s.append("\nToxic Spikes");
		if((flags & TOXICSPIKESL2) != 0) s.append("\nToxic Spikes Lvl. 2");
		return s;
	}
	
	public String numbers() {
		String s = "\n\n\n";
		for(int i = 0; i < 7; i++)
			s += ((i == 0 ? "" : "\n") + (boosts[i] < 0 ? "" : "+") + boosts[i]);
		return s;
	}
}
