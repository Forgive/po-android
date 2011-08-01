package com.pokebros.android.pokemononline;

import java.util.ArrayList;

import android.widget.ArrayAdapter;

public class Tier {
	public byte level = 0;
	public String name = "null";
	public ArrayList<Tier> subTiers = new ArrayList<Tier>();
	public Tier parentTier = null;
	public ArrayAdapter<Tier> subTiersAdapter;
	
	public Tier(byte level, String name) {
		this.level = level;
		this.name = name;
		subTiers = new ArrayList<Tier>();
	}
	
	public Tier() { subTiers = new ArrayList<Tier>(); }
	
	public String toString() { return name; }
	
	public void addSubTier(Tier t) {
		subTiers.add(t);
		subTiersAdapter.add(t);
	}
}
