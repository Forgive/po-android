package com.pokebros.android.pokemononline;

import static java.lang.System.out;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.io.ByteArrayOutputStream;

public class PokeClientSocket {
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
	
	public boolean isConnected() { return socket != null && socket.isConnected(); }
	
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
			out.println("ERROR: Unable to connect to " + ipAddr + " - " +
					"is the server running?");
			System.exit(10);
		}
	}

	public boolean sendMessage(ByteArrayOutputStream msgToSend, Command msgType) {
		boolean success=false;
		ByteArrayOutputStream bytesToSend = new ByteArrayOutputStream();
		byte firstLen = (byte) (msgToSend.size() / 256);
		byte secondLen = (byte) (msgToSend.size() % 256);
		bytesToSend.write(firstLen);
		bytesToSend.write(secondLen);
		bytesToSend.write((byte)msgType.ordinal());
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

	public ByteArrayOutputStream recvMessage() {
		ByteArrayOutputStream recvd = new ByteArrayOutputStream();
		byte firstLen;
		byte secondLen;

		try {
			firstLen = inData.readByte();
			secondLen = inData.readByte();
			//if (firstLen == 0 && secondLen == 0) return recvd;
			System.out.println("FirstLen: " + firstLen + " SecondLen: " + secondLen);
			for (int i=0; i < (firstLen * 256 + secondLen); ++i) {
				recvd.write(inData.readByte());
			}
		} catch (IOException e) {
			System.out.println("Caught IOException Reading From Socket Stream!");
		}
		return recvd;
	}

}