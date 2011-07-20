package com.pokebros.android.pokemononline.battle;

import java.util.ArrayList;

import android.os.SystemClock;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.player.PlayerInfo;
import com.pokebros.android.pokemononline.poke.OpponentPoke;
import com.pokebros.android.pokemononline.poke.UniqueID;

public class Battle {
	ArrayList<Boolean> sub = new ArrayList<Boolean>();
	ArrayList<UniqueID> specialSprite = new ArrayList<UniqueID>();
	ArrayList<UniqueID> lastSeenSpecialSprite = new ArrayList<UniqueID>();
	
	// 0 = you, 1 = opponent
	PlayerInfo[] players = new PlayerInfo[2];
	
	public short[] time = new short[2];
	public boolean[] ticking = new boolean[2];
	public long[] startingTime = new long[2];
	
	int mode = 0, numberOfSlots = 0;
	public byte me = 0, opp = 1;
	int gen = 0;
	int bID = 0;
	
	BattleTeam myTeam;
	
	OpponentPoke[][] pokes = new OpponentPoke[2][6];
	ArrayList<Boolean> pokeAlive = new ArrayList<Boolean>();
	
	public Battle(BattleConf conf, BattleTeam team, PlayerInfo p1, PlayerInfo p2, int meID, int bID) {
		mode = conf.mode; // singles, doubles, triples
		this.bID = bID;
		myTeam = team;
		// Only supporting singles for now
		numberOfSlots = 2;
		players[0] = p1;
		players[1] = p2;
		// Figure out who's who
		if(players[0].id() == meID) {
			me = 0;
			opp = 1;
		}
		else {
			me = 1;
			opp = 0;
		}
		
		time[0] = time[1] = 5*60;
		ticking[0] = ticking[1] = false;
	}
	
	public Boolean isMyTimerTicking() {
		return ticking[me];
	}
	
	public Boolean isOppTimerTicking() {
		return ticking[opp];
	}
	public long myStartingTime() {
		return startingTime[me];
	}
	
	public short myTime() {
		return time[me];
	}
	
	public short oppTime() {
		return time[opp];
	}
	
	// This is mainly for compatibility with doubles.
	private PlayerInfo playerBySpot(int spot) {
		return players[spot % 2];
	}
	
	private OpponentPoke currentPokeBySpot(int spot) {
		return pokes[spot % 2][spot / 2];
	}
	
	public ArrayList<String> myMoves(int n) {
		ArrayList<String> moves = new ArrayList<String>();
		for(int i = 0; i < 4; i++) {
			short moveNum = myTeam.pokes[n].moves[i].num;
			String moveName = MoveName.values()[moveNum].toString();
			moves.add(moveName.replaceAll("([A-Z])", " $1"));
		}
		return moves;
	}
	
	public String myNick() {
		return players[me].nick();
	}
	
	public String oppNick() {
		return players[opp].nick();
	}
	
	public Baos constructAttack(byte attack) {
		Baos b = new Baos();
		b.putInt(bID);
		AttackChoice ac = new AttackChoice(attack, opp);
		b.putBaos(new BattleChoice(me, ac, ChoiceType.AttackType));
		return b;
	}
	
	public void receiveCommand(Bais msg) {
		BattleCommand bc = BattleCommand.values()[msg.readByte()];
		byte toSpot = msg.readByte(); // Which poke are we talking about?
		System.out.println("Battle Command Received: " + bc.toString());
		switch(bc) {
		case SendOut:
			boolean isSilent = msg.readBool();
			byte fromSpot = msg.readByte();
			if(msg.available() > 0) // this is the first time you've seen it
				pokes[toSpot % 2][toSpot / 2] = new OpponentPoke(msg);
			System.out.println(playerBySpot(toSpot).nick() + " sent out " + 
					currentPokeBySpot(toSpot).nick() + "!");
			break;
		case UseAttack:
			short attack = msg.readShort();
			System.out.println(playerBySpot(toSpot) + "'s " + 
					currentPokeBySpot(toSpot).nick() +
					" used " + MoveName.values()[attack].toString() + "!");
		case ClockStart:
			time[toSpot % 2] = msg.readShort();
			startingTime[toSpot % 2] = SystemClock.uptimeMillis();
			ticking[toSpot % 2] = true;
			break;
		case ClockStop:
			time[toSpot % 2] = msg.readShort();
			ticking[toSpot % 2] = false;
			break;
		default:
			System.out.println("Battle command unimplemented");
		}
	}
}
