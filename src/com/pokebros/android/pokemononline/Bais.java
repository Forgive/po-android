package com.pokebros.android.pokemononline;

import java.io.ByteArrayInputStream;

public class Bais extends ByteArrayInputStream {
	public Bais(byte[] b) {
		super(b);
	}
	
	public String readQString() {
		int len = readInt();
		/*
		 * QString has a special value of 0xFFFFFFFF
		 * if it's NULL, here empty string will do
		 */
		if (len < 0) {
			return "";
		}
		
		/* Yeah, I know, everything in Java is signed.
		 * Big enough number would turn into a negative size.
		 * Luckily most bytes in a packet is 65635 in PO protocol.
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
		/*// I changed this because it wasn't working,
		// please fix it if you want the faster version again
		// -Lamperi
		return read()*256*256*256 + read()*256*256 + read()*256 + read();*/
		int i = 0;
		i |= (read() << 24);
		i |= (read() << 16);
		i |= (read() << 8);
		i |=  read();
		
		return i;
	}
	
	public boolean readBool() {
		boolean ret = false;
		if(read() == 1) ret = true;
		return ret;
	}
}
