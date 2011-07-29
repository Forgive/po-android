package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.battle.BattleMove;
import com.pokebros.android.pokemononline.poke.PokeEnums.Status;

// This class represents your poke during a battle.
public class BattlePoke extends ShallowBattlePoke {
	public short currentHP = 0;
	public short totalHP = 0;
	short item = 0;
	String itemString;
	short ability = 0;
	String abilityString;
	byte statusCount = 0;
	byte originalStatusCount = 0;
	byte nature = 0;
	byte happiness = 0;
	public byte teamNum;
	
	short[] stats = new short[5];
	public BattleMove[] moves = new BattleMove[4];
	
	int[] DVs = new int[6];
	int[] EVs = new int[6];
	
	public BattlePoke(Bais msg) {
		uID = new UniqueID(msg);
		nick = msg.readQString();
		totalHP = msg.readShort();
		currentHP = msg.readShort();
		gender = msg.readByte();
		shiny = msg.readBool();
		level = msg.readByte();
		item = msg.readShort();
		ability = msg.readShort();
		happiness = msg.readByte();
		
		for(int i = 0; i < 5; i++)
			stats[i] = msg.readShort();
		for(int i = 0; i < 4; i++)
			moves[i] = new BattleMove(msg);
		/* EVs and DVs are QLists on the server end,
		 * so we need to discard the int representing
		 * the number of items in the list. */
		//msg.readInt();
		for(int i = 0; i < 6; i++)
			EVs[i] = msg.readInt();
		//msg.readInt();
		for(int i = 0; i < 6; i++)
			DVs[i] = msg.readInt();
	}
	
	@Override
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.putBaos(uID);
		b.putString(nick);
		b.putShort(totalHP);
		b.putShort(currentHP);
		b.write(gender);
		b.putBool(shiny);
		b.write(level);
		b.putShort(item);
		b.putShort(ability);
		b.write(happiness);
		for(int i = 0; i < 5; i++)
			b.putShort(stats[i]);
		for(int i = 0; i < 4; i++)
			b.putBaos(moves[i]);
		for(int i = 0; i < 6; i++)
			b.write(EVs[i]);
		for(int i = 0; i < 6; i++)
			b.write(DVs[i]);
		return b;
	}
}
