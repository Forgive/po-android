package com.pokebros.android.pokemononline;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import com.pokebros.android.pokemononline.player.PlayerTeam;
import com.pokebros.android.pokemononline.poke.TeamPoke;

public class PokeParser
{
	String nick, info, loseMsg, winMsg, defaultTier;
	short avatar;
	boolean ladderEnabled, showTeam;
	byte gen;
	short pokeNum;
	byte subNum;
	String pokenick;
	short item, ability;
	byte nature, gender;
	boolean shiny;
	byte happiness, level;
	short[] moves;
	byte[] DVs;
	byte[] EVs;
	FileInputStream in;
	StringBuffer inLine;
	InputStreamReader isr;
	BufferedReader inRd;
	
	public PokeParser() throws Exception {
		in = new FileInputStream("/sdcard/team.xml");
		inLine = new StringBuffer();
		isr = new InputStreamReader(in);
		 
		inRd = new BufferedReader(isr);
		System.out.println(getOriginalMyXML());
	}
	
	private String getOriginalMyXML() throws Exception{
		String text;
		while ((text = inRd.readLine()) != null) {
			inLine.append(text);
			inLine.append("\n");
		}
		in.close();
		return inLine.toString();
	}
	
	public TeamPoke getPoke() {
		TeamPoke tp = new TeamPoke();
		return tp;				
	}
	
	public PlayerTeam getPlayer() {
		PlayerTeam pt = new PlayerTeam();
		return pt;
	}
	
	public String getNick() {return nick;}
	public String getInfo() {return info;}
	public String getLoseMsg() {return loseMsg;}
	public String getWinMsg() {return winMsg;}
	public String getDefaultTier() {return defaultTier;}
	public short getAvatar() {return avatar;}
	public boolean getLadderEnabled() {return ladderEnabled;}
	public boolean getShowTeam() {return showTeam;}
	public byte getGen() {return gen;}
	public short pokeNum() {return pokeNum;}
	public byte subNum() {return subNum;}
	public String getPokeNick() {return pokenick;}
	public short getItem() {return item;}
	public short getAbility() {return ability;}
	public byte getNature() {return nature;}
	public byte getGender() {return gender;}
	public boolean getShiny() {return shiny;}
	public byte getHappiness() {return happiness;}
	public byte getLevel() {return level;}
	public short[] getmoves() {return moves;}
	public byte[] getDVs() {return DVs;}
	public byte[] getEVs() {return EVs;}
	
	
	public void setNick(String s) {nick = s;}
	public void setInfo(String s) {info = s;}
	public void setLoseMsg(String s) {loseMsg = s;}
	public void setWinMsg(String s) {winMsg = s;}
	public void setDefaultTier(String s) {defaultTier = s;}
	public void setAvatar(short s) {avatar = s;}
	public void setLadderEnabled(boolean b) {ladderEnabled = b;}
	public void setShowTeam(boolean b) {showTeam = b;}
	public void setGen(byte b) {gen = b;}
	public void setPokeNum(short s) {pokeNum = s;}
	public void setSubNum(byte b) {subNum = b;}
	public void setPokeNick(String s) {pokenick = s;}
	public void setItem(short s) {item = s;}
	public void setAbility(short s) {ability = s;}
	public void setNature(byte b) {nature = b;}
	public void setGender(byte b) {gender = b;}
	public void setShiny(boolean b) {shiny = b;}
	public void setHappiness(byte b) {happiness = b;}
	public void setLevel(byte b) {level = b;}
	public void setMoves(short[] s) {for (int i = 0; i < 4; ++i) {moves[i] = s[i];}}
	public void setDVs(byte[] b) {for (int i = 0; i < 4; ++i) {DVs[i] = b[i];}}
	public void setEVs(byte[] b) {for (int i = 0; i < 4; ++i) {EVs[i] = b[i];}}
}