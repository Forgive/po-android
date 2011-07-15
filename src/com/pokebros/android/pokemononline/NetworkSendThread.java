package com.pokebros.android.pokemononline;

public class NetworkSendThread implements Runnable {
	
	private PokeClientSocket socket;
	private Baos bytes;
	private Command comm;
	
	public NetworkSendThread(PokeClientSocket s, Baos b, Command c) {
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
