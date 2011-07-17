package com.pokebros.android.pokemononline;

import static java.lang.System.out;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class PokeClientSocket {
	private SocketChannel schan = null;

	private Baos thisMsg = new Baos();
	private ByteBuffer currentData = ByteBuffer.allocate(4096);
	private LinkedList<Baos> msgs = new LinkedList<Baos>();
	int remaining = 0, dataLen = 0;

	public PokeClientSocket(String inIpAddr, int inPortNum)
	{	
		try {
			schan = SocketChannel.open();
			schan.configureBlocking(false);
			schan.connect(new InetSocketAddress(inIpAddr, inPortNum));
		} catch (IOException e) {	
			System.exit(-1);
		}
	}

	public boolean isConnected() { 
		boolean ret = false;
		try {
			ret = schan.finishConnect(); 
		} catch (IOException e) {
			System.exit(-1);
		}
		return ret;
	}

	// This function blocks until the connection is ready to use.
	public void waitConnect() {
		try {
			while(!schan.finishConnect()) {}
		} catch (IOException ioe) {
			out.println("ERROR: Unable to connect - " +
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
		try {
			ByteBuffer b = ByteBuffer.allocate(bytesToSend.size());
			b.order(ByteOrder.BIG_ENDIAN);
			b.put(bytesToSend.toByteArray());
			b.rewind();
			schan.write(b);
			success = true;
		} catch (IOException e) {
			System.out.println("Caught IOException Writing To Socket Stream!");
			System.exit(-1);
		}

		return (success);
	}

	public Baos getMsg() {
		// This retrieves and removes the first
		// item in the queue, returning null 
		// if there is none.
		// I know, great name, right?
		return msgs.poll();
	}

	public void recvMessagePoll() {
		try {
			currentData.clear();
			dataLen = schan.read(currentData);
			currentData.flip();
			// Loop while there's still data in the buffer.
			while(dataLen > 0) {
				System.out.println("dataLen: " + dataLen);
				// If we're at the start of a new message,
				// and the buffer has at least the size of the
				// next message in it, start reading it in.
				if(remaining == 0 && dataLen >= 2) {
					remaining = currentData.getShort();
					dataLen -= 2;

					/* Java has only signed data types. We get around
					 * this by shifting the most significant bits 8 places left
					 * and then OR'ing it with the 8 least significant bits.
					 * We need to AND the LSBs with 0xff to get rid of the
					 * 1's of two's complement (sign is preserved when casting).
					 */
					System.out.println("Length: " + remaining);
				}
				// There's enough data in the buffer to finish the current message.
				if(remaining <= dataLen) {
					byte[] bytes = new byte[remaining];
					currentData.get(bytes, 0, remaining);
					thisMsg.write(bytes);
					
					// Add the read in message to the queue of
					// unprocessed messages.
					msgs.add(thisMsg);
					thisMsg = new Baos();
					dataLen -= remaining;
					remaining = 0;
				}
				// Otherwise, read what we can and put it into
				// the incomplete message.
				else {
					byte[] bytes = new byte[dataLen];
					currentData.get(bytes, 0, dataLen);
					thisMsg.write(bytes);
					remaining -= dataLen;
					dataLen = 0;
				}
			}
		}  catch (Exception e) {
			System.out.println("Caught IOException Reading From Socket Stream!");
		}
	}
}