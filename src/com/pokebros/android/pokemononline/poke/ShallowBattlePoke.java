package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.poke.PokeEnums.Status;

// This class represents the Opponent's poke during a battle.
public class ShallowBattlePoke extends SerializeBytes {
	public String rnick, nick = "";
	int fullStatus = 0;
	public UniqueID uID = new UniqueID();
	public boolean shiny = false;
	public byte gender = 0;
	public byte lifePercent = 0;
	byte level = 0;
	public byte lastKnownPercent = 0;
	public boolean sub = false;
	
	public ShallowBattlePoke() {}; // For pokes who have not been sent out;
	
	public ShallowBattlePoke(Bais msg, boolean isMe) {
		uID = new UniqueID(msg);
		rnick = nick = msg.readQString();
		if (!isMe)
			nick = "The foe's " + nick;
		lifePercent = msg.readByte();
		fullStatus = msg.readInt();
		gender = msg.readByte();
		shiny = msg.readBool();
		level = msg.readByte();
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.putBaos(uID);
		b.putString(nick);
		b.write(lifePercent);
		b.putInt(fullStatus);
		b.write(gender);
		b.putBool(shiny);
		b.write(level);
		return b;
	}
	
	public void changeStatus(byte status) {
		/* Clears past status */
		fullStatus = fullStatus & ~( (1 << Status.Koed.poValue()) | 0x3F);
		/* Adds new status */
		fullStatus = fullStatus | ( 1 << status);
	}
	
	public final int status() {
		if ((fullStatus & (1 << Status.Koed.poValue())) != 0)
			return Status.Koed.poValue();
		// intlog2(fullStatus & 0x3F)
		int x = fullStatus & 0x3F;
		int i;
		for (i = 0; x > 1; i++) {
			x/=2;
		}
		return i;
	}
}
