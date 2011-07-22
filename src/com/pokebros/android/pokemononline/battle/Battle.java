package com.pokebros.android.pokemononline.battle;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.lang.Math;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.text.Html;
import android.text.SpannableStringBuilder;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.BattleActivity;
import com.pokebros.android.pokemononline.DataBaseHelper;
import com.pokebros.android.pokemononline.EscapeHtml;
import com.pokebros.android.pokemononline.NetworkService;
import com.pokebros.android.pokemononline.ColorEnums.QtColor;
import com.pokebros.android.pokemononline.ColorEnums.StatusColor;
import com.pokebros.android.pokemononline.ColorEnums.TypeColor;
import com.pokebros.android.pokemononline.player.PlayerInfo;
import com.pokebros.android.pokemononline.poke.OpponentPoke;
import com.pokebros.android.pokemononline.poke.UniqueID;
import com.pokebros.android.pokemononline.poke.PokeEnums.Status;
import com.pokebros.android.pokemononline.poke.PokeEnums.StatusFeeling;
import com.pokebros.android.pokemononline.poke.PokeEnums.Stat;

public class Battle {
	ArrayList<Boolean> sub = new ArrayList<Boolean>();
	ArrayList<UniqueID> specialSprite = new ArrayList<UniqueID>();
	ArrayList<UniqueID> lastSeenSpecialSprite = new ArrayList<UniqueID>();
	
	// 0 = you, 1 = opponent
	PlayerInfo[] players = new PlayerInfo[2];
	
	public short[] remainingTime = new short[2];
	public boolean[] ticking = new boolean[2];
	public long[] startingTime = new long[2];
	
	int mode = 0, numberOfSlots = 0;
	public byte me = 0, opp = 1;
	int gen = 0;
	int bID = 0;
	private NetworkService netServ;
	
	public BattleTeam myTeam;
	
	OpponentPoke[][] pokes = new OpponentPoke[2][6];
	ArrayList<Boolean> pokeAlive = new ArrayList<Boolean>();
	
	public SpannableStringBuilder hist = new SpannableStringBuilder();
	public SpannableStringBuilder histDelta = new SpannableStringBuilder();
	
	public Battle(BattleConf conf, BattleTeam team, PlayerInfo p1, PlayerInfo p2, int meID, int bID, NetworkService ns) {
		netServ = ns;
		mode = conf.mode; // singles, doubles, triples
		this.bID = bID;
		myTeam = team;
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
		
		remainingTime[0] = remainingTime[1] = 5*60;
		ticking[0] = ticking[1] = false;
		
		histDelta.append("The battle between " + players[me].nick() + 
						" and " + players[opp].nick() + " has begun!");
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
	
	// This is mainly for compatibility with doubles.
	private PlayerInfo playerBySpot(int spot) {
		return players[spot % 2];
	}
	
	private OpponentPoke currentPokeBySpot(int spot) {
		return pokes[spot % 2][spot / 2];
	}
	
	public ArrayList<String> myMoves(int n) {
		ArrayList<String> moves = new ArrayList<String>();
		for(int i = 0; i < 4; i++) {
			short moveNum = myTeam.pokes[n].moves[i].num;
			String moveName = MoveName.values()[moveNum].toString();
			moves.add(moveName.replaceAll("([A-Z])", " $1"));
		}
		return moves;
	}
	
	public String myNick() {
		return players[me].nick();
	}
	
	public String oppNick() {
		return players[opp].nick();
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
	
	public void receiveCommand(Bais msg)  {
		BattleCommand bc = BattleCommand.values()[msg.readByte()];
		byte toSpot = msg.readByte(); // Which poke are we talking about?
		System.out.println("Battle Command Received: " + bc.toString());
		switch(bc) {
		case SendOut:
			boolean isSilent = msg.readBool();
			byte fromSpot = msg.readByte();
			if(msg.available() > 0) // this is the first time you've seen it
				pokes[toSpot % 2][toSpot / 2] = new OpponentPoke(msg, toSpot%2);
			histDelta.append("\n" + (playerBySpot(toSpot).nick() + " sent out " + 
					currentPokeBySpot(toSpot).rnick() + "!"));
			break;
		case SendBack:
			histDelta.append("\n" + (playerBySpot(toSpot).nick() + " called " + 
					currentPokeBySpot(toSpot).rnick() + " back!"));
			break;
		case UseAttack:
			short attack = msg.readShort();
			histDelta.append("\n" + 	currentPokeBySpot(toSpot).nick() +
					" used " + MoveName.values()[attack].toString() + "!");
			break;
		case BeginTurn:
			int turn = msg.readInt();
			histDelta.append(Html.fromHtml("<br><b><font color=" + QtColor.Blue + 
					"Start of turn " + turn + "</font color></b>"));
			break;
		case Ko:
			histDelta.append(Html.fromHtml("<br><b>" + new EscapeHtml(currentPokeBySpot(toSpot).nick()) +
					" fainted!</b>"));
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
						"It's not very effective...</font color>"));
				break;
			case 8:
			case 16:
				histDelta.append(Html.fromHtml("<br><font color=" + QtColor.Blue +
						"It's super effective!</fontColor>"));
				break;
			}
			break;
		case CriticalHit:
			histDelta.append(Html.fromHtml("<br><font color=#6b0000>A critical hit!</font color>"));
			break;
		case Miss:
			histDelta.append("\nThe attack of " + currentPokeBySpot(toSpot).nick() + " missed!");
			break;
		case Avoid:
			histDelta.append("\n" + currentPokeBySpot(toSpot).nick() + " avoided the attack!");
			break;
		case StatChange:
			byte stat = msg.readByte(), boost=msg.readByte();
			histDelta.append("\n" + currentPokeBySpot(toSpot).nick() + "'s " +
					netServ.getString(Stat.values()[stat].string) + (Math.abs(boost) > 1 ? " sharply" : "")
					+ (boost > 0 ? " rose!" : " fell!"));
			break;
		case StatusChange:
			final String[] statusChangeMessages = {
					" is paralyzed! It may be unable to move!",
					" fell asleep!",
					" was frozen solid!",
					" was burned!",
					" was poisoned!",
					" was badly poisoned!",
					" became confused!"
			};
			byte status = msg.readByte();
			boolean multipleTurns = msg.readBool();
			if (status > Status.Fine.ordinal() && status <= Status.Confused.ordinal()) {
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(status) + 
						currentPokeBySpot(toSpot).nick() + statusChangeMessages[status-1 +
                        (status == Status.Poisoned.ordinal() && multipleTurns ? 1 : 0)] + "</font color>"));
			}
			break;
		case AbsStatusChange:
			// TODO
			break;
		case AlreadyStatusMessage:
			status = msg.readByte();
			histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(status) +
					currentPokeBySpot(toSpot).nick() + " is already " + Status.values()[status] +
					".</font color>"));
			break;
		case StatusMessage:
			status = msg.readByte();
			switch (StatusFeeling.values()[status]) {
			case FeelConfusion:
				histDelta.append(Html.fromHtml("<br><font color=" + TypeColor.Ghost +
						currentPokeBySpot(toSpot).nick() + " is confused!</font color>"));
				break;
			case HurtConfusion:
				histDelta.append(Html.fromHtml("<br><font color=" + TypeColor.Ghost +
						"It hurt itself in its confusion!</font color>"));
				break;
			case FreeConfusion:
				histDelta.append(Html.fromHtml("<br><font color=" + TypeColor.Dark +
						currentPokeBySpot(toSpot).nick() + " snapped out of its confusion!</font color>"));
				break;
			case PrevParalysed:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Paralysed.ordinal())+
						currentPokeBySpot(toSpot).nick() + " is paralyzed! It can't move!</font color>"));
				break;
			case FeelAsleep:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Asleep.ordinal()) +
						currentPokeBySpot(toSpot).nick() + " is fast asleep!</font color>"));
				break;
			case FreeAsleep:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Asleep.ordinal()) +
						currentPokeBySpot(toSpot).nick() + " woke up!</font color>"));
				break;
			case HurtBurn:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Burnt.ordinal()) +
						currentPokeBySpot(toSpot).nick() + " is hurt by its burn!</font color>"));
				break;
			case HurtPoison:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Poisoned.ordinal()) +
						currentPokeBySpot(toSpot).nick() + " is hurt by poison!</font color>"));
				break;
			case PrevFrozen:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Frozen.ordinal())+
						currentPokeBySpot(toSpot).nick() + " is frozen solid!</font color>"));
				break;
			case FreeFrozen:
				histDelta.append(Html.fromHtml("<br><font color=" + new StatusColor(Status.Frozen.ordinal()) +
						currentPokeBySpot(toSpot).nick() + " thawed out!</font color>"));
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
			histDelta.append(Html.fromHtml("<br><font color=" + (toSpot !=0 ? "#5811b1>" : QtColor.Green) +
					"<b>" + new EscapeHtml(playerBySpot(toSpot).nick()) + ": </b></font color>" +
					new EscapeHtml(message)));
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
					": " + new EscapeHtml(message)));
			break;
		case MoveMessage:
			short move = msg.readShort();
			byte part = msg.readByte();
			DataBaseHelper datHelp = new DataBaseHelper(netServ);
			try {
				datHelp.createDataBase();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			datHelp.openDataBase();
			//SQLiteDatabase mess = datHelp.getReadableDatabase();
			//Cursor messCurs = mess.rawQuery("SELECT EFFECT" + part + " FROM Move_message WHERE _id = " + move, new String[]{""});
			//String[] herp = {"Effect" + part};
			//Cursor messCurs = mess.query("Move_message", herp, "", new String[]{""}, "", "", "");
			//System.out.println("HERE GOES NOTHING " + datHelp.getString(move, part));
			break;
		case ClockStart:
			remainingTime[toSpot % 2] = msg.readShort();
			startingTime[toSpot % 2] = SystemClock.uptimeMillis();
			ticking[toSpot % 2] = true;
			break;
		case ClockStop:
			remainingTime[toSpot % 2] = msg.readShort();
			ticking[toSpot % 2] = false;
			break;
		default:
			System.out.println("Battle command unimplemented");
		}
	}
}
