package com.pokebros.android.pokemononline;

import static java.lang.System.out;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Vector;

public class PokeServerSocket {
	private int portNum;
	private Socket socket;
	private DataOutputStream outData;
	private DataInputStream inData;

	public PokeServerSocket( int inPortNum)
	{
		portNum = inPortNum;
		socket = null;
	}

	public void connect() {
		ServerSocket serverSock;
		try {
			serverSock = new ServerSocket(portNum);
			socket = serverSock.accept();
			outData = new DataOutputStream(socket.getOutputStream());
			inData = new DataInputStream(socket.getInputStream());
		} catch (IOException ioe) {
			out.println("ERROR: Caught exception starting server");
			System.exit(7);
		}
	}

	public boolean sendString(String strToSend) {
		boolean success=false;

		try {
			outData.writeBytes(strToSend);
			success = true;
		} catch (IOException e) {
			System.out.println("Caught IOException Writing To Socket Stream!");
			System.exit(-1);
		}
		return (success);
	}

	public String recvString() {
		Vector< Byte > recvBytes = new Vector< Byte >();
		byte [] byteAry;
		byte recByte;
		String receivedString = "";

		try {
			recByte = inData.readByte();
			while (recByte != 0) {
				recvBytes.add(recByte);
				recByte = inData.readByte();
			}

			byteAry = new byte[recvBytes.size()];

			for (int ind = 0; ind < recvBytes.size(); ind++) {
				byteAry[ind] = recvBytes.elementAt(ind).byteValue();
			}

			receivedString = new String(byteAry);
		} catch (IOException ioe) {
			out.println("ERROR: receiving string from socket");
			System.exit(8);
		}
		out.println("Received string "+receivedString);
		return (receivedString);
	}
}