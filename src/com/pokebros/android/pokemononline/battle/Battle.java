package com.pokebros.android.pokemononline.battle;

import java.util.ArrayList;

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
	
	short[] time = new short[2];
	boolean[] ticking = new boolean[2];
	int[] startingTime = new int[2];
	
	int mode = 0, numberOfSlots = 0;
	byte me = 0, opp = 1;
	int gen = 0;
	int bID = 0;
	
	OpponentPoke[][] pokes = new OpponentPoke[2][6];
	ArrayList<Boolean> pokeAlive = new ArrayList<Boolean>();
	
	public Battle(BattleConf conf, BattleTeam team, PlayerInfo p1, PlayerInfo p2, int meID, int bID) {
		mode = conf.mode; // singles, doubles, triples
		this.bID = bID;
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
	
	// This is mainly for compatibility with doubles.
	private PlayerInfo playerBySpot(int spot) {
		return players[spot % 2];
	}
	
	private OpponentPoke currentPokeBySpot(int spot) {
		return pokes[spot % 2][spot / 2];
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
		default:
			System.out.println("Battle command unimplemented");
		}
	}
}
