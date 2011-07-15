package com.pokebros.android.pokemononline;

import static java.lang.System.out;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
			socket = new Socket(ipAddr, portNum);
			outData = new DataOutputStream(socket.getOutputStream());
			inData = new DataInputStream(socket.getInputStream());
		} catch (IOException ioe) {
			out.println("ERROR: Unable to connect to " + ipAddr + " - " +
					"is the server running?");
			System.exit(10);
		}
	}

	public boolean sendMessage(Baos msgToSend, Command msgType) {
		boolean success=false;
		Baos bytesToSend = new Baos();
		bytesToSend.putShort((short)(msgToSend.size() + 1));
		bytesToSend.write((byte)msgType.ordinal());
		try {
			bytesToSend.write(msgToSend.toByteArray());
		} catch (IOException e) {
			System.out.println("Caught IOException Writing message");
			System.exit(-1);
		}
		System.out.println(bytesToSend.toString());
		try {
			outData.write(bytesToSend.toByteArray());
			success = true;
		} catch (IOException e) {
			System.out.println("Caught IOException Writing To Socket Stream!");
			System.exit(-1);
		}

		return (success);
	}

	public Baos recvMessage() {
		Baos recvd = new Baos();
		byte firstLen;
		byte secondLen;

		try {
			firstLen = inData.readByte();
			secondLen = inData.readByte();
			/* Java has only signed data types. We get around
			 * this by shifting the most significant bits 8 places left
			 * and then OR'ing it with the 8 least significant bits.
			 * We need to AND the LSBs with 0xff to get rid of the
			 * 1's of two's complement (sign is preserved when casting).
			 */
			int len = (firstLen << 8) | (secondLen & 0xff);
			System.out.println("Length: " + len);
			for (int i=0; i < len; ++i) {
				recvd.write(inData.readByte());
			}
		} catch (IOException e) {
			System.out.println("Caught IOException Reading From Socket Stream!");
		}
		return recvd;
	}
}