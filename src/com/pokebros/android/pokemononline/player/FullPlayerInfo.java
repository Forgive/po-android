package com.pokebros.android.pokemononline.player;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.PokeParser;
import com.pokebros.android.pokemononline.QColor;
import com.pokebros.android.pokemononline.SerializeBytes;

// Contains all the information about the player.
// Used for logging into the server.
public class FullPlayerInfo extends SerializeBytes {
	protected PokeParser p;
	protected PlayerTeam playerTeam;
		
	protected boolean ladderEnabled = false;
	protected boolean showTeam = true;
	protected QColor nameColor = new QColor();
	
	public FullPlayerInfo(Bais msg) {
		playerTeam = new PlayerTeam(msg);
		ladderEnabled = msg.readBool();
		showTeam = msg.readBool();
		nameColor = new QColor(msg);
	}
	
	public FullPlayerInfo() {
		try {
			p = new PokeParser();
			playerTeam = new PlayerTeam(p);
		}
		catch (Exception e) {
			//TODO: Warn Player that the default team has been loaded with a toast.
			System.out.println("NO TEAM IMPORTED, SYSTEM LOADED DEFAULT TEAM");
			playerTeam = new PlayerTeam();
		}
	}
	
	public String toString() { return playerTeam.nick(); }
	public String nick() { return playerTeam.nick(); }
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.putBaos(playerTeam);
		bytes.putBool(ladderEnabled);
		bytes.putBool(showTeam);
		bytes.putBaos(nameColor);
		return bytes;
	}
}
