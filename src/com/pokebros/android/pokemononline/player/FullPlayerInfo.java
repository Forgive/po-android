package com.pokebros.android.pokemononline.player;

import java.io.File;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.PokeParser;
import com.pokebros.android.pokemononline.QColor;
import com.pokebros.android.pokemononline.SerializeBytes;

// Contains all the information about the player.
// Used for logging into the server.
public class FullPlayerInfo extends SerializeBytes {
	protected PlayerTeam playerTeam;
		
	protected boolean ladderEnabled = true;
	protected boolean showTeam = false;
	protected QColor nameColor = new QColor();
	private File team;
	private String myPath = "/sdcard/team.xml";
	private PokeParser p;
	
	public FullPlayerInfo(Bais msg) {
		playerTeam = new PlayerTeam(msg);
		ladderEnabled = msg.readBool();
		showTeam = msg.readBool();
		nameColor = new QColor(msg);
	}
	
	public FullPlayerInfo(String path) {
		if (path == null)
			team = new File(myPath);
		else
			team = new File(path);
		if (team.exists()) {
			if (path == null)
				p = new PokeParser(myPath);
			else 
				p = new PokeParser(path);
			playerTeam = new PlayerTeam(p);
		}
		else {	
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
