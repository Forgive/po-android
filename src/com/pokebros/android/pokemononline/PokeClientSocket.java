package com.pokebros.android.pokemononline;

import static java.lang.System.out;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;
import java.io.ByteArrayOutputStream;

public class PokeClientSocket implements Runnable {
	private String ipAddr;
	private int portNum;
	private Socket socket;
	private DataOutputStream outData;
	private DataInputStream inData;

	public PokeClientSocket(String inIpAddr, int inPortNum)
	{
		ipAddr = inIpAddr;
		portNum = inPortNum;
		inData = null;
		outData = null;
		socket = null;
	}

	public void run() {
		connect();
		Trainer trainer = new Trainer();
		sendBytes(trainer.serializeBytes(), (byte)2);
	}
	
	public void connect() {
		try {
			System.out.println("THIS SHIT'S ABOUT TO GET REAL0");
			socket = new Socket(ipAddr, portNum);
			System.out.println("THIS SHIT'S ABOUT TO GET REAL1");
			outData = new DataOutputStream(socket.getOutputStream());
			System.out.println("THIS SHIT'S ABOUT TO GET REAL2");
			inData = new DataInputStream(socket.getInputStream());
			System.out.println("THIS SHIT'S ABOUT TO GET REAL3");
		} catch (IOException ioe) {
			out.println("ERROR: Unable to connect - " +
					"is the server running?");
			System.exit(10);
		}
	}

	public boolean sendBytes(ByteArrayOutputStream msgToSend, byte msgType) {
		boolean success=false;
		ByteArrayOutputStream bytesToSend = new ByteArrayOutputStream();
		byte firstLen = (byte) (msgToSend.size() / 256);
		byte secondLen = (byte) (msgToSend.size() % 256);
		System.out.println("THIS SHIT'S ABOUT TO GET REAL4");
		bytesToSend.write(firstLen);
		System.out.println("THIS SHIT'S ABOUT TO GET REAL5");
		bytesToSend.write(secondLen);
		bytesToSend.write(msgType);
		try {
			System.out.println("THIS SHIT'S ABOUT TO GET REAL6");
			bytesToSend.write(msgToSend.toByteArray());
		} catch (IOException e) {
			System.out.println("Caught IOException Writing message");
			System.exit(-1);
		}
		
		System.out.println("OMFG SEND");
		System.out.println(bytesToSend.toString());
		try {
			outData.write(bytesToSend.toByteArray());
			System.out.println("THIS SHIT'S ABOUT TO GET REAL7");
			success = true;
		} catch (IOException e) {
			System.out.println("Caught IOException Writing To Socket Stream!");
			System.exit(-1);
		}

		return (success);
	}

	public ByteArrayOutputStream recvBytes() {
		ByteArrayOutputStream recvd = new ByteArrayOutputStream();
		byte firstLen;
		byte secondLen;

		try {
			firstLen = inData.readByte();
			secondLen = inData.readByte();
			for (int i=0; i < (firstLen * 256 + secondLen); ++i) {
				recvd.write(inData.readByte());
			}
		} catch (IOException e) {
			System.out.println("Caught IOException Reading From Socket Stream!");
		}
		return recvd;
	}

}