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
	//int myself = 0, opponent = 1;
	int gen = 0;
	
	OpponentPoke[][] pokes = new OpponentPoke[2][6];
	ArrayList<Boolean> pokeAlive = new ArrayList<Boolean>();
	
	public Battle(PlayerInfo me, PlayerInfo opp, int mode) {
		this.mode = mode; // XXX don't really know what this means yet
		// Only supporting singles for now
		numberOfSlots = 2;
		
		players[0] = me;
		players[1] = opp;
		
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
		AttackChoice ac = new AttackChoice(attack, (byte)1);
		b.putBaos(new BattleChoice((byte)0, ac, ChoiceType.AttackType));
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
			OpponentPoke p = new OpponentPoke(msg);
			// XXX have no idea wtf toSpot means
			System.out.println(playerBySpot(toSpot).nick() + " sent out " + p.nick() + "!");
			break;
		case UseAttack:
			short attack = msg.readShort();
			System.out.println(playerBySpot(toSpot) + "'s " + currentPokeBySpot(toSpot).nick() +
					" used " + MoveName.values()[attack].toString() + "!");
		default:
			System.out.println("Battle command unimplemented");
		}
	}
}
