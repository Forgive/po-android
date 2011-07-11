package com.pokebros.android.pokemononline;

import java.io.ByteArrayOutputStream;

public class NetworkSendThread implements Runnable {
	
	private PokeClientSocket socket;
	private ByteArrayOutputStream bytes;
	private Command comm;
	
	public NetworkSendThread(PokeClientSocket s, ByteArrayOutputStream b, Command c) {
		socket = s;
		if(!socket.isConnected())
			socket.connect();
		
		bytes = b;
		comm = c;
	}
	
	public void run() {
		socket.sendMessage(bytes, comm);
	}
}
