package com.pokebros.android.pokemononline;

import android.text.SpannableStringBuilder;

public class EscapeHtml extends SpannableStringBuilder {
	public EscapeHtml(CharSequence toEscape) {
		super(toEscape);
		clearSpans();
	}
}
