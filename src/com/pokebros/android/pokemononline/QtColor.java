package com.pokebros.android.pokemononline;

public enum QtColor {
	Blue("0000ff");
	private final String color;
	private QtColor(String color) {
		this.color = color;
	}
	public String toString() {
		return color;
	}
}