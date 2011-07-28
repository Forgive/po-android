package com.pokebros.android.pokemononline;

import java.util.Hashtable;
import java.util.Random;

import android.os.Bundle;

import com.pokebros.android.pokemononline.battle.ChallengeEnums.ChallengeDesc;
import com.pokebros.android.pokemononline.player.PlayerInfo;

public class IncomingChallenge {
	byte desc, mode;
	int opponent, clauses;
	String oppName = null;
	static int note = new Random(System.currentTimeMillis()).nextInt();
	
	public IncomingChallenge(Bais msg) {
		desc = msg.readByte();
		opponent = msg.readInt();
		clauses = msg.readInt();
		mode = msg.readByte();
	}
	
	public boolean validate(Hashtable<Integer, PlayerInfo> players) {
		if (players.get(opponent) != null)
			oppName = players.get(opponent).nick();
		return desc == ChallengeDesc.Sent.ordinal() && oppName != null;
	}
	
	public Bundle toBundle() {
		Bundle b = new Bundle();
		b.putByte("desc", desc);
		b.putByte("mode", mode);
		b.putInt("opponent", opponent);
		b.putInt("clauses", clauses);
		b.putString("oppName", oppName);
		return b;
	}
}
