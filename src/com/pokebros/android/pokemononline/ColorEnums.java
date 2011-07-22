package com.pokebros.android.pokemononline;

import com.pokebros.android.pokemononline.poke.PokeEnums.Status;

public class ColorEnums {
	public enum QtColor {
		White("#ffffff>"),
		Black("#000000>"),
		Red("#ff0000>"),
		DarkRed("#800000>"),
		Green("#00ff00>"),
		DarkGreen("#008000>"),
		Blue("#0000ff>"),
		DarkBlue("#000080>"),
		Cyan("#00ffff>"),
		DarkCyan("#008080>"),
		Magenta("#ff00ff>"),
		DarkMagenta("#800080>"),
		Yellow("#ffff00>"),
		DarkYellow("#808000>"),
		Gray("#a0a0a4>"),
		DarkGray("#808080>"),
		LightGray("#c0c0c0>");
		private final String color;
		private QtColor(String color) {
			this.color = color;
		}
		public String toString() {
			return color;
		}
	}
	
	public enum TypeColor {
		Normal("#a8a878>"),
		Fighting("#c03028>"),
		Flying("#a890f0>"),
		Poison("#a040a0>"),
		Ground("#e0c068>"),
		Rock("#b8a038>"),
		Bug("#a8b820>"),
		Ghost("#705898>"),
		Steel("#b8b8d0>"),
		Fire("#f08030>"),
		Water("#6890f0>"),
		Grass("#78c850>"),
		Electric("#f8d030>"),
		Psychic("#f85888>"),
		Ice("#98d8d8>"),
		Dragon("#7038f8>"),
		Dark("#705848>"),
		Curse("#68a090>");
		private final String color;
		private TypeColor(String color) {
			this.color = color;
		}
		public String toString() {
			return color;
		}
	}
		
	public static class StatusColor {
		private final String color;
		public StatusColor(int status) {
			switch (Status.values()[status]) {
			case Koed: color = "#171ba>"; break;
			case Fine: color = TypeColor.Normal.toString(); break;
			case Paralysed: color = TypeColor.Electric.toString(); break;
			case Burnt: color = TypeColor.Fire.toString(); break;
			case Frozen: color = TypeColor.Ice.toString(); break;
			case Asleep: color = TypeColor.Psychic.toString(); break;
			case Poisoned: color = TypeColor.Poison.toString(); break;
			case Confused: color = TypeColor.Ghost.toString(); break;
			default: color = ">";
			}
		}
		public String toString() {
			return color;
		}
	}
}