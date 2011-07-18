package com.pokebros.android.pokemononline.battle;

import java.util.Random;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;

enum ChoiceType {
    CancelType,
    AttackType,
    SwitchType,
    RearrangeType,
    CenterMoveType,
    DrawType
};

// Please only call serializeBytes() on the Choices that make sense.
abstract class Choice extends SerializeBytes {
}

class CancelChoice extends Choice {
	public Baos serializeBytes() { 
		System.out.println("Error: serializeBytes called on CancelChoice");
		System.exit(-1);
		return null; 
	}
}

class AttackChoice extends Choice {
	byte attackSlot;
	byte attackTarget;
	
	public AttackChoice() {
		Random r = new Random();
		attackSlot = (byte)r.nextInt(4);
		attackTarget = 0;
	}
	
	public AttackChoice(Bais msg) {
		attackSlot = msg.readByte();
		attackTarget = msg.readByte();
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.write(attackSlot);
		b.write(attackTarget);
		return b;
	}
}

class SwitchChoice extends Choice {
	byte pokeSlot = 0;
	
	public SwitchChoice(Bais msg) {
		pokeSlot = msg.readByte();
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.write(pokeSlot);
		return b;
	}
}

class RearrangeChoice extends Choice {
	byte[] pokeIndexes = new byte[6];
	
	public RearrangeChoice(Bais msg) {
		try {
			msg.read(pokeIndexes);
		} catch (Exception e) {
			System.exit(-1);
		}
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		try {
			b.write(pokeIndexes);
		} catch (Exception e) {
			System.exit(-1);
		}
		return b;
	}
}

class MoveToCenterChoice extends Choice {
	public Baos serializeBytes() { 
		System.out.println("Error: serializeBytes called on MoveToCenterChoice");
		System.exit(-1);
		return null; 
	}
}

class DrawChoice extends Choice {
	public Baos serializeBytes() { 
		System.out.println("Error: serializeBytes called on DrawChoice");
		System.exit(-1);
		return null; 
	}
}

public class BattleChoice extends SerializeBytes {
	protected byte type;
	protected byte playerSlot;
	protected Choice choice;
	ChoiceType choiceType;
	
	public BattleChoice() {
		playerSlot = 1;
		type = (byte)ChoiceType.AttackType.ordinal();
		choice = new AttackChoice();
		choiceType = ChoiceType.AttackType;
	}
	
	public BattleChoice(Bais msg) {
		playerSlot = msg.readByte();
		type = msg.readByte();
		
		choiceType = ChoiceType.values()[type];
		switch(choiceType) {
		case SwitchType:
			choice = new SwitchChoice(msg);
			break;
		case AttackType:
			choice = new AttackChoice(msg);
			break;
		case RearrangeType:
			choice = new RearrangeChoice(msg);
			break;
		default:
			break;
		}
	}
	
	public Baos serializeBytes() {
		Baos b = new Baos();
		b.write(playerSlot);
		b.write(type);
		
		switch(choiceType) {
		case SwitchType:
		case AttackType:
		case RearrangeType:
			b.putBaos(choice); // Polymorphism!
			break;
		default:
			break;
		}
		return b;
	}
}
