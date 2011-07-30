package com.pokebros.android.pokemononline;

import java.util.ArrayList;

public class Tier {
	public byte level = 0;
	public String name = "null";
	public ArrayList<Tier> subTiers = new ArrayList<Tier>();
	public Tier parentTier = null;
	
	public Tier(byte level, String name) {
		this.level = level;
		this.name = name;
		subTiers = new ArrayList<Tier>();
	}
	
	public Tier() { subTiers = new ArrayList<Tier>(); }
}
