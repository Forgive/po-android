package com.pokebros.android.pokemononline;

class DummyQColor {
	protected
		byte spec;
		short alpha;
		short red;
		short green;
		short blue;
		short pad;
		
	public
		DummyQColor() {
			spec = 1;
			alpha |= 0xffff;
			red = green = blue = 0;
			pad = 0;
	}
}
public class Trainer {
	protected
		String nick;
		String info;
		String loseMsg;
		String winMsg;
		
		short avatar;
		String defaultTier;
		Team team;
		
		boolean ladderEnabled;
		boolean showTeam;
		DummyQColor nameColor;
		
	public
		Trainer() {
			nick = "BROBRO";
			info = "Sup";
			loseMsg = "FUCK!";
			winMsg = "YEAAAH!!!";
			avatar = 72;
			defaultTier = "OU";
			team = new Team();
			ladderEnabled = showTeam = true;
			nameColor = new DummyQColor();
	}
}
