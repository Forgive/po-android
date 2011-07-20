package com.pokebros.android.pokemononline.player;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.PokeParser;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.Team;

// Player as represented in the teambuilder.
public class PlayerTeam extends SerializeBytes {
	protected String nick = "BROLOLZHAH";
	protected String info = "SUP";
	protected String loseMsg = "SHUCKS!";
	protected String winMsg = "YEEEAAAAHH!!!";
	protected String defaultTier = "OU";
	protected String tier = "OU";
	protected Team team = new Team();
	short avatar = 72;
	
	public String nick() { return nick; }
	
	public PlayerTeam(Bais msg) {
		nick = msg.readQString();
		info = msg.readQString();
		loseMsg = msg.readQString();
		winMsg = msg.readQString();
		avatar = msg.readShort();
		defaultTier = msg.readQString();
		team = new Team(msg);
	}
	
	public PlayerTeam(PokeParser p) {
		nick = p.getNick();
		info = p.getInfo();
		loseMsg = p.getLoseMsg();
		winMsg = p.getWinMsg();
		avatar = p.getAvatar();
		defaultTier = p.getDefaultTier();
		team = new Team();
	}
	
	public PlayerTeam() {}
	
	public String toString() {
		return nick;
	}
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.putString(nick);
		bytes.putString(info);
		bytes.putString(loseMsg);
		bytes.putString(winMsg);	
		bytes.putShort(avatar);
		bytes.putString(defaultTier);
		bytes.putBaos(team);
		return bytes;
	}

}
