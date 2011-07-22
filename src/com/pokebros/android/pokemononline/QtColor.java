package com.pokebros.android.pokemononline;

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