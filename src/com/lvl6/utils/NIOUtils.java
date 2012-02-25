package com.lvl6.utils;

import java.nio.*;
import java.nio.channels.*;

import com.lvl6.events.ResponseEvent;
import com.lvl6.properties.Globals;

/**
 * NIOUtils.java
 *
 * Misc utility functions to simplify dealing w/ NIO channels and buffers.
 */
public class NIOUtils {

	/** 
	 * first, writes the header, then the 
	 * event into the given ByteBuffer
	 * in preparation for the channel write
	 */
	public static void prepBuffer(ResponseEvent event, ByteBuffer writeBuffer) {
		// write header
		writeBuffer.clear();

		int type = event.getEventType().getNumber();
		writeBuffer.put((byte) (type & 0xFF));
		writeBuffer.put((byte) ((type & 0xFF00) >> 8));
		writeBuffer.put((byte) ((type & 0xFF0000) >> 16));
		writeBuffer.put((byte) ((type & 0xFF000000) >> 24));
		
		int tag = event.getTag();
		writeBuffer.put((byte) (tag & 0xFF));
		writeBuffer.put((byte) ((tag & 0xFF00) >> 8));
		writeBuffer.put((byte) ((tag & 0xFF0000) >> 16));
		writeBuffer.put((byte) ((tag & 0xFF000000) >> 24));

		int sizePos = writeBuffer.position();
		writeBuffer.putInt(0); // placeholder for payload size
		int size = event.write(writeBuffer);
		// insert the payload size in the placeholder spot
		writeBuffer.put(sizePos, (byte) (size & 0xFF));
		writeBuffer.put(sizePos+1, (byte) ((size & 0xFF00) >> 8));
		writeBuffer.put(sizePos+2, (byte) ((size & 0xFF0000) >> 16));
		writeBuffer.put(sizePos+3, (byte) ((size & 0xFF000000) >> 24));

		// prepare for a channel.write
		writeBuffer.flip();
	}


	/** 
	 * write the contents of a ByteBuffer to the given SocketChannel
	 */
	public static void channelWrite(SocketChannel channel, ByteBuffer writeBuffer) {
		long nbytes = 0;
		long toWrite = writeBuffer.remaining();

		// loop on the channel.write() call since it will not necessarily
		// write all bytes in one shot
		try {
			while (nbytes != toWrite) {
				nbytes += channel.write(writeBuffer);

				try {
					Thread.sleep(Globals.CHANNEL_WRITE_SLEEP);
				}
				catch (InterruptedException e) {}
			}
		}
		catch (ClosedChannelException cce) {
		}
		catch (Exception e) {
		} 

		// get ready for another write if needed
		writeBuffer.rewind();
	}

	/**
	 * write a String to a ByteBuffer, 
	 * prepended with a short integer representing the length of the String
	 */
	public static void putStr(ByteBuffer buff, String str) {
		if (str == null) {
			buff.put((byte)0);
		}
		else {
			buff.put((byte)str.length());
			buff.put(str.getBytes());
		}
	}

	/**
	 * read a String from a ByteBuffer 
	 * that was written w/the putStr method
	 */
	public static String getStr(ByteBuffer buff) {
		byte len = buff.get();
		if (len == 0) {
			return null;
		}
		else {
			byte[] b = new byte[len];
			buff.get(b);
			return new String(b);
		}
	}


}
