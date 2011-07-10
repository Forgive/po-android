package com.pokebros.android.pokemononline;

import static java.lang.System.out;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

public class PokeClientSocket {
	private String ipAddr;
	private int portNum;
	private Socket socket;
	private ObjectOutputStream outData;
	private ObjectInputStream inData;

	public PokeClientSocket(String inIpAddr, int inPortNum)
	{
		ipAddr = inIpAddr;
		portNum = inPortNum;
		inData = null;
		outData = null;
		socket = null;
	}

	public boolean connect() {
		try {
			socket = new Socket(ipAddr, portNum);
			outData = new ObjectOutputStream(socket.getOutputStream());
			inData = new ObjectInputStream(socket.getInputStream());
		} catch (IOException ioe) {
			out.println("ERROR: Unable to connect - " +
					"is the server running?");
			System.exit(10);
		}
		return true;
	}

	public boolean sendBytes(byte[] bytesToSend) {
		boolean success=false;

		try {
			outData.write(bytesToSend);
			success = true;
		} catch (IOException e) {
			System.out.println("Caught IOException Writing To Socket Stream!");
			System.exit(-1);
		}

		return (success);
	}

	public String recvString() {
		Vector< Byte > byteVec = new Vector< Byte >();
		byte [] byteAry;
		byte recByte;
		String receivedString = "";

		try {
			recByte = inData.readByte();
			while (recByte != 0) {
				byteVec.add(recByte);
				recByte = inData.readByte();
			}

			byteAry = new byte[byteVec.size()];

			for (int ind = 0; ind < byteVec.size(); ind++) {
				byteAry[ind] = byteVec.elementAt(ind).byteValue();
			}

			receivedString = new String(byteAry);
		} catch (IOException ioe) {
			out.println("ERROR: receiving string from socket");
			System.exit(8);
		}
		out.println("Recieved "+receivedString);
		return (receivedString);
	}

}