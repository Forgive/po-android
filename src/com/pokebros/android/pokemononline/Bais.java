package com.pokebros.android.pokemononline;

import java.io.ByteArrayInputStream;

public class Bais extends ByteArrayInputStream {
	public Bais(byte[] b) {
		super(b);
	}
	
	public String readQtString() {
		int len = readInt();
		System.out.println("String length: " + len);
		
		/* Yeah, I know, everything in Java is signed.
		 * If you're sending strings too long to fit in
		 * an unsigned int, may God help you.
		 */
		byte[] bytes = new byte[len];
		read(bytes, 0, len);
		
		String str = null;
		try {
			str = new String(bytes, "UTF-16BE");
		} catch (Exception e) {
			System.exit(-1);
		}
		
		return str;
	}
	
	public short readShort() {
		short s = 0;
		s |= (read() << 8);
		s |= (read() & 0xff);
		
		return s;
	}
	
	public byte readByte() {
		return (byte)read();
	}
	
	public int readInt() {
		int i = 0;
		i |= (read() << 24);
		i |= ((read() & 0xff0000)  << 16);
		i |= ((read() & 0xff00) << 8);
		i |= ((read() & 0xff));
		
		return i;
	}
}
