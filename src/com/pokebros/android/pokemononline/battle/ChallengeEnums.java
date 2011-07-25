package com.pokebros.android.pokemononline.battle;

public class ChallengeEnums {
	public enum ChallengeDesc
	{
		Sent,
		Accepted,
		Cancelled,
		Busy,
		Refused,
		InvalidTeam,
		InvalidGen,

		ChallengeDescLast
	};

	public enum Clauses
	{
		SleepClause { public final int mask() { return 1; } },
		FreezeClause { public final int mask() { return 2; } },
		DisallowSpectator { public final int mask() { return 4; } },
		ItemClause { public final int mask() { return 8; } },
		ChallengeCup { public final int mask() { return 16; } },
		NoTimeOut { public final int mask() { return 32; } },
		SpeciesClause { public final int mask() { return 64; } },
		RearrangeTeams { public final int mask() { return 128; } },
		SelfKO { public final int mask() { return 256; } };
		
		public abstract int mask();
	};

	public enum Mode
	{
		Singles,
		Doubles,
		Triples,
		Rotation,
	};
}
