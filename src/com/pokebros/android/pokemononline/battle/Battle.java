package com.pokebros.android.pokemononline.battle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.text.Html;
import android.text.SpannableStringBuilder;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.DataBaseHelper;
import com.pokebros.android.pokemononline.NetworkService;
import com.pokebros.android.pokemononline.ColorEnums.*;
import com.pokebros.android.pokemononline.player.PlayerInfo;
import com.pokebros.android.pokemononline.poke.BattlePoke;
import com.pokebros.android.pokemononline.poke.ShallowBattlePoke;
import com.pokebros.android.pokemononline.poke.UniqueID;
import com.pokebros.android.pokemononline.poke.PokeEnums.*;

public class Battle {
	public enum BattleResult {
		Forfeit,
		Win,
		Tie,
		Close
	}
	
	ArrayList<Boolean> sub = new ArrayList<Boolean>();
	ArrayList<UniqueID> specialSprite = new ArrayList<UniqueID>();
	ArrayList<UniqueID> lastSeenSpecialSprite = new ArrayList<UniqueID>();
	
	// 0 = you, 1 = opponent
	public PlayerInfo[] players = new PlayerInfo[2];
	
	public short[] remainingTime = new short[2];
	public boolean[] ticking = new boolean[2];
	public long[] startingTime = new long[2];
	
	int mode = 0, numberOfSlots = 0;
	public byte me = 0, opp = 1;
	int gen = 0;
	public int bID = 0;
	private static NetworkService netServ;
	public BattleTeam myTeam;
	public ShallowShownTeam oppTeam;
	public boolean isOver = false, gotEnd = false;
	public boolean allowSwitch, allowAttack;
	public boolean[] allowAttacks = new boolean[4];
	public int background;
	public boolean shouldShowPreview = false;
	
	ShallowBattlePoke[][] pokes = new ShallowBattlePoke[2][6];
	ArrayList<Boolean> pokeAlive = new ArrayList<Boolean>();
	
	public SpannableStringBuilder hist; //= new SpannableStringBuilder();
	public SpannableStringBuilder histDelta; //= new SpannableStringBuilder();
	
	public Battle(BattleConf conf, BattleTeam team, PlayerInfo p1, PlayerInfo p2, int meID, int bID, NetworkService ns) {
		hist = new SpannableStringBuilder();
		histDelta = new SpannableStringBuilder();
		netServ = ns;
		mode = conf.mode; // singles, doubles, triples
		this.bID = bID;
		myTeam = team;
		
		// Only supporting singles for now
		numberOfSlots = 2;
		players[0] = p1;
		players[1] = p2;
		// Figure out who's who
		if(players[0].id == meID) {
			me = 0;
			opp = 1;
		}
		else {
			me = 1;
			opp = 0;
		}
		
		remainingTime[0] = remainingTime[1] = 5*60;
		ticking[0] = ticking[1] = false;
		
		background = new Random().nextInt(11) + 1;
		
		histDelta.append("Battle between " + players[me].nick() + 
						" and " + players[opp].nick() + " started!");
		
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 6; j++) {
				pokes[i][j] = new ShallowBattlePoke();
			}
		}
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
		return remainingTime[me];
		
	}
	
	public short oppTime() {
		return remainingTime[opp];
	}
	
	public ShallowBattlePoke currentPoke(int player) {
		return pokes[player][0];
	}
	
	public boolean isOut(Byte poke) {
		return poke < numberOfSlots / 2;
	}
	
	public Baos constructCancel() {
		Baos b = new Baos();
		b.putInt(bID);
		b.putBaos(new BattleChoice(me, ChoiceType.CancelType));
		return b;
	}
	
	public Baos constructAttack(byte attack) {
		Baos b = new Baos();
		b.putInt(bID);
		AttackChoice ac = new AttackChoice(attack, opp);
		b.putBaos(new BattleChoice(me, ac, ChoiceType.AttackType));
		return b;
	}
	
	public Baos constructSwitch(byte toSpot) {
		Baos b = new Baos();
		b.putInt(bID);
		SwitchChoice sc = new SwitchChoice(toSpot);
		b.putBaos(new BattleChoice(me, sc, ChoiceType.SwitchType));
		return b;
	}
	
	public Baos constructRearrange() {
		Baos b = new Baos();
		b.putInt(bID);
		RearrangeChoice rc = new RearrangeChoice(myTeam);
		b.putBaos(new BattleChoice(me, rc, ChoiceType.RearrangeType));
		return b;
	}
	
	public static String queryDB(String query) {
		DataBaseHelper datHelp = new DataBaseHelper(netServ);
		try {
			datHelp.createDatabase();
		} catch (IOException e) {
			e.printStackTrace();
		}
		datHelp.open();
		SQLiteDatabase mess = datHelp.getReadableDatabase();
		Cursor messCurs = mess.rawQuery(query, null);
		messCurs.moveToFirst();
		String s = messCurs.getString(0);
		messCurs.close();
		mess.close();
		datHelp.close();
		
		return s;
	}
	
	public void receiveCommand(Bais msg)  {
		BattleCommand bc = BattleCommand.values()[msg.readByte()];
		byte player = msg.readByte();
		System.out.println("Battle Command Received: " + bc.toString());
		switch(bc) {
		case SendOut:
			boolean isSilent = msg.readBool();
			byte fromSpot = msg.readByte();
			
			if(player == me) {
				BattlePoke temp = myTeam.pokes[0];
				
				myTeam.pokes[0] = myTeam.pokes[fromSpot];
				myTeam.pokes[fromSpot] = temp;
			}
			
			ShallowBattlePoke tempPoke = pokes[player][0];
			pokes[player][0] = pokes[player][fromSpot];
			pokes[player][fromSpot] = tempPoke;
			
			if(msg.available() > 0) // this is the first time you've seen it
				pokes[player][0] = new ShallowBattlePoke(msg, (player == me) ? true : false);
			
			if(netServ.battleActivity != null) {
				if(player == me) netServ.battleActivity.updateMyPoke();
				else netServ.battleActivity.updateOppPoke();
			}
			if(!isSilent)
				histDelta.append("\n" + (players[player].nick() + " sent out " + 
						currentPoke(player).rnick + "!"));
			break;
		case SendBack:
			histDelta.append("\n" + (players[player].nick() + " called " + 
					currentPoke(player).rnick + " back!"));
			break;
		case UseAttack:
			short attack = msg.readShort();
			histDelta.append(Html.fromHtml("<br>" + currentPoke(player).nick +
					" used <font color =" + TypeColor.values()[new Integer(queryDB("SELECT type FROM [Moves] WHERE _id = " + attack))] +
					queryDB("SELECT name FROM [Moves] WHERE _id = " + attack) + "</font>!"));
			break;
		case BeginTurn:
			int turn = msg.readInt();
			histDelta.append(Html.fromHtml("<br><b><font color=" + QtColor.Blue + 
					"Start of turn " + turn + "</font></b>"));
			break;
		case Ko:
			histDelta.append(Html.fromHtml("<br><b>" + NetworkService.escapeHtml((currentPoke(player).nick) +
					" fainted!</b>")));
			if(netServ.battleActivity != null && player == me)
				netServ.battleActivity.switchToPokeViewer();
			break;
		case Hit:
			byte number = msg.readByte();
			histDelta.append("\nHit " + number + " time" + ((number > 1) ? "s!" : "!"));
			break;
		case Effective:
			byte eff = msg.readByte();
			switch (eff) {
			case 0:
				histDelta.append("\nIt had no effect!");
				break;
			case 1:
			case 2:
				histDelta.append(Html.fromHtml("<br><font color=" + QtColor.Gray +
						"It's not very effective...</font>"));
				break;
			case 8:
			case 16:
				histDelta.append(Html.fromHtml("<br><font color=" + QtColor.Blue +
						"It's super effective!</fontColor>"));
				break;
			}
			break;
		case CriticalHit:
			histDelta.append(Html.fromHtml("<br><font color=#6b0000>A critical hit!</font>"));
			break;
		case Miss:
			histDelta.append("\nThe attack of " + currentPoke(player).nick + " missed!");
			break;
		case Avoid:
			histDelta.append("\n" + currentPoke(player).nick + " avoided the attack!");
			break;
		case StatChange:
			byte stat = msg.readByte(), boost=msg.readByte();
			histDelta.append("\n" + currentPoke(player).nick + "'s " +
					netServ.getString(Stat.values()[stat].rstring()) +
					(Math.abs(boost) > 1 ? " sharply" : "") + (boost > 0 ? " rose!" : " fell!"));
			break;
		case StatusChange:
			final String[] statusChangeMessages = {
					" is paralyzed! It may be unable to move!",
					" fell asleep!",
					" was frozen solid!",
					" was burned!",
					" was poisoned!",
					" was badly poisoned!",
			};
			byte status = msg.readByte();
			boolean multipleTurns = msg.readBool();
			if (status > Status.Fine.poValue() && status <= Status.Poisoned.poValue()) {
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(status) + 
						currentPoke(player).nick + statusChangeMessages[status-1 +
                        (status == Status.Poisoned.poValue() && multipleTurns ? 1 : 0)] + "</font>"));
			}
			else if(status == Status.Confused.poValue()){
				/* The reason we need to handle confusion separately is because 
				 * poisoned and badly poisoned are not separate values in the Status
				 * enum, so confusion does not correspond to the same value in the above
				 * string array as its enum value. */
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(status) + 
						currentPoke(player).nick + " became confused!</font>"));
			}
			break;
		case AbsStatusChange:
			byte poke = msg.readByte();
			status = msg.readByte();
			
			if (poke < 0 || poke >= 6)
				break;
			
			if (status != Status.Confused.poValue()) {
				pokes[player][poke].changeStatus(status);
				if (player == me)
					myTeam.pokes[poke].changeStatus(status);
				if (isOut(poke) && netServ.battleActivity != null)
					netServ.battleActivity.updatePokes(player);
			}
			// XXX PO updates pokeballs here
			break;
		case AlreadyStatusMessage:
			status = msg.readByte();
			histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(status) +
					currentPoke(player).nick + " is already " + Status.poValues()[status] +
					".</font>"));
			break;
		case StatusMessage:
			status = msg.readByte();
			switch (StatusFeeling.values()[status]) {
			case FeelConfusion:
				histDelta.append(Html.fromHtml("<br><font color=" + TypeColor.Ghost +
						currentPoke(player).nick + " is confused!</font>"));
				break;
			case HurtConfusion:
				histDelta.append(Html.fromHtml("<br><font color=" + TypeColor.Ghost +
						"It hurt itself in its confusion!</font>"));
				break;
			case FreeConfusion:
				histDelta.append(Html.fromHtml("<br><font color=" + TypeColor.Dark +
						currentPoke(player).nick + " snapped out of its confusion!</font>"));
				break;
			case PrevParalysed:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Paralysed.poValue())+
						currentPoke(player).nick + " is paralyzed! It can't move!</font>"));
				break;
			case FeelAsleep:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Asleep.poValue()) +
						currentPoke(player).nick + " is fast asleep!</font>"));
				break;
			case FreeAsleep:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Asleep.poValue()) +
						currentPoke(player).nick + " woke up!</font>"));
				break;
			case HurtBurn:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Burnt.poValue()) +
						currentPoke(player).nick + " is hurt by its burn!</font>"));
				break;
			case HurtPoison:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Poisoned.poValue()) +
						currentPoke(player).nick + " is hurt by poison!</font>"));
				break;
			case PrevFrozen:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Frozen.poValue())+
						currentPoke(player).nick + " is frozen solid!</font>"));
				break;
			case FreeFrozen:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Frozen.poValue()) +
						currentPoke(player).nick + " thawed out!</font>"));
				break;
			}
			break;
		case Failed:
			histDelta.append("\nBut it failed!");
			break;
		case BattleChat:
		case EndMessage:
			String message = msg.readQString();
			if (message.equals(""))
				break;
			histDelta.append(Html.fromHtml("<br><font color=" + (player !=0 ? "#5811b1>" : QtColor.Green) +
					"<b>" + NetworkService.escapeHtml(players[player].nick()) + ": </b></font>" +
					NetworkService.escapeHtml(message)));
			break;
		case Spectating:
			boolean come = msg.readBool();
			int id = msg.readInt();
			// TODO addSpectator(come, id);
			break;
		case SpectatorChat:
			// TODO if (ignoreSpecs) break;
			id = msg.readInt();
			message = msg.readQString();
			histDelta.append(Html.fromHtml("<br><font color=" + QtColor.Blue + netServ.players.get(id) + 
					": " + NetworkService.escapeHtml(message)));
			break;
		case MoveMessage:
			// TODO
			short move = msg.readShort();
			byte part = msg.readByte();
			byte type = msg.readByte();
			byte foe = msg.readByte();
			short other = msg.readShort();
			System.out.println("OTHER IS: " + other);
			String q = msg.readQString();
			
			String s = queryDB("SELECT EFFECT" + part + " FROM [Move_message] WHERE _id = " + move);
			s = s.replaceAll("%s", currentPoke(player).nick);
			s = s.replaceAll("%ts", players[me].nick());
			s = s.replaceAll("%tf", players[opp].nick());
			if(type  != -1) s = s.replaceAll("%t", Type.values()[type].toString());
			if(foe   != -1) s = s.replaceAll("%f", currentPoke(foe).nick);
			if(other  != -1) s = s.replaceAll("%m", queryDB("SELECT name FROM [Moves] WHERE _id = " + other));
			s = s.replaceAll("%d", new Short(other).toString());
			s = s.replaceAll("%q", q);
			//s = s.replaceAll("%i", ItemName(other));
			//s = s.replaceAll("%a", AbilityName(other));
			if(other !=-1) s = s.replaceAll("%p", queryDB("SELECT name FROM [Pokemons] WHERE Num = " + other));
			
			histDelta.append("\n" + s);
			break;
		case NoOpponent:
			histDelta.append("\nBut there was no target...");
			break;
		case ItemMessage:
			// TODO
			break;
		case Flinch:
			histDelta.append("\n" + currentPoke(player).nick + " flinched!");
			break;
		case Recoil:
			boolean damaging = msg.readBool();
			if (damaging)
				histDelta.append("\n" + currentPoke(player).nick + " is hit with recoil!");
			else
				histDelta.append("\n" + currentPoke(player).nick + " had its energy drained!");
			break;
		case WeatherMessage:
			byte wstatus = msg.readByte(), weather = msg.readByte();
			if (weather == Weather.NormalWeather.ordinal())
				break;
			
			String color = new TypeForWeatherColor(weather).toString();
			switch (WeatherState.values()[wstatus]) {
			case EndWeather:
				switch (Weather.values()[weather]) {
				case Hail: message = "The hail subsided!"; break;
				case SandStorm: message = "The sandstorm subsided!"; break;
				case Sunny: message = "The sunlight faded!"; break;
				case Rain: message = "The rain stopped!"; break;
				default: message = "";
				}
				histDelta.append(Html.fromHtml("<br><font color=" + color + message + "</font color"));
				break;
			case HurtWeather:
				switch (Weather.values()[weather]) {
				case Hail: message = " is buffeted by the hail!"; break;
				case SandStorm: message = " is buffeted by the sandstorm!"; break;
				default: message = "";
				}
				histDelta.append(Html.fromHtml("<br><font color=" + color +
						currentPoke(player).nick + message + "</font>"));
				break;
			case ContinueWeather:
				switch (Weather.values()[weather]) {
				case Hail: message = "Hail continues to fall!"; break;
				case SandStorm: message = "The sandstorm rages!"; break;
				case Sunny: message = "The sunlight is strong!"; break;
				case Rain: message = "Rain continues to fall!"; break;
				default: message = "";
				}
				histDelta.append(Html.fromHtml("<br><font color=" + color + message + "</font color"));
				break;
			}
			break;
		case StraightDamage:
			short damage = msg.readShort();
			if(player == me) {
				histDelta.append("\n" + currentPoke(player).nick + " lost " + damage + 
						" HP! (" + (damage * 100 / myTeam.pokes[0].totalHP) + "% of its health)");
			}
			else {
				histDelta.append("\n" + currentPoke(player).nick + " lost " + damage + "% of its health!");
			}
			break;
		case AbilityMessage:
			// TODO
			break;
		case Substitute:
			currentPoke(player).sub = msg.readBool();
			if (player == me) {
				if (netServ.battleActivity != null)
					netServ.battleActivity.updateMyPoke();
			} else {
				if (netServ.battleActivity != null)
					netServ.battleActivity.updateOppPoke();
			}
			break;
		case BattleEnd:
			byte res = msg.readByte();
			if (res == BattleResult.Tie.ordinal())
				histDelta.append(Html.fromHtml("<br><b><font color =" + QtColor.Blue +
						"Tie between " + players[0].nick() + " and " + players[1].nick() +
						"!</b></font>")); // XXX Personally I don't think this deserves !
			else
				histDelta.append(Html.fromHtml("<br><b><font color =" + QtColor.Blue +
						players[player].nick() +" won the battle!</b></font>"));
			gotEnd = true;
			isOver = true;
			break;
		case BlankMessage:
			// XXX This prints out a lot of extra space
			// histDelta.append("\n");
			break;
		case Clause:
			// TODO
			break;
		case Rated:
			boolean rated = msg.readBool();
			histDelta.append(Html.fromHtml("<br><b><font color =" + QtColor.Blue + 
					(rated ? "Rated" : "Unrated") + "</b></font>"));
			// TODO Print clauses
			break;
		case TierSection:
			String tier = msg.readQString();
			histDelta.append(Html.fromHtml("<br><b><font color =" + QtColor.Blue +
					"Tier: " + tier + "</b></font>"));
			break;
		case TempPokeChange:
			// TODO
			break;
		case MakeYourChoice:
			// XXX is this correct behavior?
			allowSwitch = true;
			allowAttack = true;
			if (netServ.battleActivity != null)
				netServ.battleActivity.updateButtons(allowSwitch, allowAttack, allowAttacks);
			break;
		case OfferChoice:
			byte numSlot = msg.readByte(); // XXX what is this?
			allowSwitch = msg.readBool();
			allowAttack = msg.readBool();
			for (int i = 0; i < 4; i++) {
					allowAttacks[i] = msg.readBool();
					System.out.print("Allow attack " + i + ": ");
					System.out.println(allowAttacks[i]);
			}
			if (netServ.battleActivity != null)
				netServ.battleActivity.updateButtons(allowSwitch, allowAttack, allowAttacks);
			break;
		case CancelMove:
			if (netServ.battleActivity != null)
				netServ.battleActivity.updateButtons(allowSwitch, allowAttack, allowAttacks);
			break;
		case ClockStart:
			remainingTime[player % 2] = msg.readShort();
			startingTime[player % 2] = SystemClock.uptimeMillis();
			ticking[player % 2] = true;
			break;
		case ClockStop:
			remainingTime[player % 2] = msg.readShort();
			ticking[player % 2] = false;
			break;
		case ChangeHp:
			short newHP = msg.readShort();
			if(player == me) {
				myTeam.pokes[0].currentHP = newHP;
				currentPoke(player).lastKnownPercent = (byte)newHP;
				currentPoke(player).lifePercent = (byte)(newHP * 100 / myTeam.pokes[0].totalHP);
			}
			else {
				currentPoke(player).lastKnownPercent = (byte)newHP;
				currentPoke(player).lifePercent = (byte)newHP;
			}
			if(netServ.battleActivity != null) {
				netServ.battleActivity.animateHpBarTo(player, currentPoke(player).lifePercent);
				// Block until the hp animation has finished
				// Timeout after 10s
				try {
					synchronized (this) {
						wait(10000);
					}
				} catch (InterruptedException e) {}
			}
			break;
		case SpotShifts:
			// TODO
			break;
		case RearrangeTeam:
			// TODO
			oppTeam = new ShallowShownTeam(msg);
			shouldShowPreview = true;
			if(netServ.battleActivity != null)
				netServ.battleActivity.showRearrangeTeamDialog();
			break;
		default:
			System.out.println("Battle command unimplemented -- " + bc);
			break;
			
		}
	}
}
