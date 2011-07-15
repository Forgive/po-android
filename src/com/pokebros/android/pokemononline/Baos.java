package com.pokebros.android.pokemononline;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Baos extends ByteArrayOutputStream {
	public Baos() {
		super();
	}
	
	public void putInt(int i) {
		byte[] bytes = new byte[4];
		
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putInt(i);
		bb.rewind();
		bb.get(bytes);
		
		try {
			write(bytes);
		} catch (Exception e) {
			System.exit(-1);
		}
	}
	
	public void putShort(short s) {
		byte[] bytes = new byte[2];
		
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putShort(s);
		bb.rewind();
		bb.get(bytes);
		
		try {
			write(bytes);
		} catch (Exception e) {
			System.exit(-1);
		}
	}
	
	public void putString(String s) {
		/* For some reason, QtStrings' length
		 * are double those of the corresponding
		 * Java string.
		 */
		putInt(s.length()*2);
		try {
			write(s.getBytes("UTF-16BE"));
		} catch (Exception e) {
			System.exit(-1);
		}
	}
	
	public void putBool(boolean bool) {
		write((byte)(bool ? 1 : 0));
	}
	
	public void putBaos(SerializeBytes src) {
		try {
			write(src.serializeBytes().toByteArray());
		} catch (Exception e) {
			System.exit(-1);
		}
	}
}
