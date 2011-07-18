package com.pokebros.android.pokemononline.battle;

import java.util.ArrayList;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.player.FullPlayerInfo;
import com.pokebros.android.pokemononline.player.PlayerInfo;
import com.pokebros.android.pokemononline.poke.OpponentPoke;
import com.pokebros.android.pokemononline.poke.UniqueID;

enum BattleCommand
{
    SendOut,
    SendBack,
    UseAttack,
    OfferChoice,
    BeginTurn,
    ChangePP,
    ChangeHp,
    Ko,
    Effective, /* to tell how a move is effective */
    Miss,
    CriticalHit, // = 10,
    Hit, /* for moves like fury double kick etc. */
    StatChange,
    StatusChange,
    StatusMessage,
    Failed,
    BattleChat,
    MoveMessage,
    ItemMessage,
    NoOpponent,
    Flinch, // = 20,
    Recoil,
    WeatherMessage,
    StraightDamage,
    AbilityMessage,
    AbsStatusChange, 
    Substitute, 
    BattleEnd,
    BlankMessage,
    CancelMove,
    Clause, // = 30,
    DynamicInfo, // = 31,
    DynamicStats, // = 32,
    Spectating,
    SpectatorChat,
    AlreadyStatusMessage,
    TempPokeChange,
    ClockStart, // = 37,
    ClockStop, // = 38,
    Rated,
    TierSection, // = 40,
    EndMessage,
    PointEstimate,
    MakeYourChoice,
    Avoid,
    RearrangeTeam,
    SpotShifts
};

public class Battle {
	PlayerInfo[] players = new PlayerInfo[2];
	ArrayList<Boolean> sub = new ArrayList<Boolean>();
	ArrayList<UniqueID> specialSprite = new ArrayList<UniqueID>();
	ArrayList<UniqueID> lastSeenSpecialSprite = new ArrayList<UniqueID>();
	
	short[] time = new short[2];
	boolean[] ticking = new boolean[2];
	int[] startingTime = new int[2];
	
	int mode = 0, numberOfSlots = 0;
	int myself = 0, opponent = 1;
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
			System.out.println(players[toSpot].nick() + " sent out " + p.nick() + "!");
			break;
		default:
			System.out.println("Battle command unimplemented");
		}
	}
}
