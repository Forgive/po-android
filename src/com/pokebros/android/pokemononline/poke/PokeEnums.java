package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.R;

public class PokeEnums {
	public enum Gender {
		Neutral,
		Male,
		Female
	}

	public enum Status {
		Fine, //= 0,
		Paralysed, //= 1,
		Asleep, //= 2,
		Frozen, //= 3,
		Burnt, //= 4,
		Poisoned, //= 5,
		Confused, //= 6,
		Attracted, //= 7,
		Wrapped, //= 8,
		Nightmared, //= 9,
		Tormented, //= 12,
		Disabled, //= 13,
		Drowsy, //= 14,
		HealBlocked, //= 15,
		Sleuthed, //= 17,
		Seeded, //= 18,
		Embargoed, //= 19,
		Requiemed, //= 20,
		Rooted, //= 21,
		Koed //= 31
	}

	public enum StatusFeeling {
		FeelConfusion,
		HurtConfusion,
		FreeConfusion,
		PrevParalysed,
		PrevFrozen,
		FreeFrozen,
		FeelAsleep,
		FreeAsleep,
		HurtBurn,
		HurtPoison
	}
	
	public enum StatusKind {
		NoKind,
		SimpleKind,
		TurnKind,
		AttractKind,
		WrapKind
	}

	/* For simplicity issues we keep the same order as in Gender. You can assume it'll stay
   that way for next versions.

   That allows you to do PokemonInfo::Picture(pokenum, (Gender)GenderAvail(pokenum)) */

	public enum GenderAvail {
		NeutralAvail,
		MaleAvail,
		FemaleAvail,
		MaleAndFemaleAvail
	}

	public enum Type {
		Normal,
		Fighting,
		Flying,
		Poison,
		Ground,
		Rock,
		Bug,
		Ghost,
		Steel,
		Fire,
		Water,
		Grass,
		Electric,
		Psychic,
		Ice,
		Dragon,
		Dark,
		Curse
	}

	public enum Nature {
		Hardy,
		Lonely,
		Brave,
		Adamant,
		Naughty,
		Bold,
		Docile,
		Relaxed,
		Impish,
		Lax,
		Timid,
		Hasty,
		Serious,
		Jolly,
		Naive,
		Modest,
		Mild,
		Quiet,
		Bashful,
		Rash,
		Calm,
		Gentle,
		Sassy,
		Careful,
		Quirky
	}
	
	public enum Stat {
		HP { public final int rstring() { return R.string.empty; } },
		Attack { public final int rstring() { return R.string.stat_attack; } },
		Defense { public final int rstring() { return R.string.stat_defense; } },
		SpAttack  { public final int rstring() { return R.string.stat_spAttack; } },
		SpDefense { public final int rstring() { return R.string.stat_spDefense; } },
		Speed { public final int rstring() { return R.string.stat_speed; } },
		Accuracy { public final int rstring() { return R.string.stat_accuracy; } },
		Evasion { public final int rstring() { return R.string.stat_evasion; } },
		AllStats { public final int rstring() { return R.string.empty; } };
		public abstract int rstring();
	}
}
