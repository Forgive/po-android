package com.pokebros.android.pokemononline;

public class QColor extends SerializeBytes {
	protected byte spec;
	protected short alpha;
	protected short red;
	protected short green;
	protected short blue;
	protected short pad;
	
	public QColor(Bais msg) {
		spec = msg.readByte();
		alpha = msg.readShort();
		red = msg.readShort();
		green = msg.readShort();
		blue = msg.readShort();
		pad = msg.readShort();
	}
	
	public QColor() {
			spec = 0;
			alpha |= 0xffff;
			red = green = blue = 0;
			pad = 0;
	}
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.write(spec);
		
		bytes.putShort(alpha);
		bytes.putShort(red);
		bytes.putShort(green);
		bytes.putShort(blue);
		bytes.putShort(pad);
		
		return bytes;
	}
}

