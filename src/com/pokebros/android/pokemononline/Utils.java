package com.pokebros.android.pokemononline;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* Serializing Utilities */
public class Utils {
	static public void putInt(ByteArrayOutputStream b, int i) {
		byte[] bytes = new byte[4];
		
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putInt(i);
		bb.rewind();
		bb.get(bytes);
		
		try {
			b.write(bytes);
		} catch (Exception e) {
			System.exit(-1);
		}
	}
	
	static public void putShort(ByteArrayOutputStream b, short s) {
		byte[] bytes = new byte[2];
		
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putShort(s);
		bb.rewind();
		bb.get(bytes);
		
		try {
			b.write(bytes);
		} catch (Exception e) {
			System.exit(-1);
		}
	}
	
	static public void putString(ByteArrayOutputStream b, String s) {
		/* For some reason, QtStrings' length
		 * are double those of the corresponding
		 * Java string.
		 */
		putInt(b, s.length()*2);
		try {
			b.write(s.getBytes("UTF-16BE"));
		} catch (Exception e) {
			System.exit(-1);
		}
	}
	
	static public void putBool(ByteArrayOutputStream b, boolean bool) {
		b.write((byte)(bool ? 1 : 0));
	}
	
	static public void putBaos(ByteArrayOutputStream dest, SerializeBytes src) {
		try {
			dest.write(src.serializeBytes().toByteArray());
		} catch (Exception e) {
			System.exit(-1);
		}
	}
}
