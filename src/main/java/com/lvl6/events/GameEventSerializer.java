package com.lvl6.events;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;

import com.google.common.primitives.Bytes;
import com.lvl6.properties.Globals;
import com.lvl6.utils.Attachment;

public class GameEventSerializer extends AbstractByteArraySerializer {
	
	
	private static final Logger log = LoggerFactory.getLogger(GameEventSerializer.class);
	/**
	 * This class serializes/deserializes GameEvents for Spring IP 
	 * socket listeners
	 */
	
	protected ByteOrder getByteOrder() {
		return ByteOrder.BIG_ENDIAN;
	}
	
	@Override
	public void serialize(byte[] bytes, OutputStream outputStream) throws IOException {
		log.debug("Serializing outbound message");
		outputStream.write(bytes);
		outputStream.flush();
	}

	
	
	
	
	@Override
	public byte[] deserialize(InputStream inputStream) throws IOException {
		int maxMessageSize = Globals.MAX_EVENT_SIZE + Attachment.HEADER_SIZE;
		List<Byte> bytes = new ArrayList<Byte>();
		int n = 0;
		int bite;
		byte[] paysize = new byte[4];
		int payloadSize = maxMessageSize;//message+header
		log.debug("Deserializing message...available to read:" + inputStream.available());
		while (true) {
			bite = inputStream.read();
			if (bite < 0 && n == 0) {
				throw new SoftEndOfStreamException("Stream closed between payloads");
			}
			bytes.add((byte) bite);
			//check header for payload size
			//payload size is 3rd int in header (index 8 thru 11 in byte[])
			if ( n < 12 && n > 7){
				paysize[n-8] = (byte) bite;
				if(n == 11) {
					payloadSize = ByteBuffer.wrap(paysize).order(getByteOrder()).getInt()+Attachment.HEADER_SIZE;
					log.debug("Message size: "+payloadSize);
				}
			}
			if(n == payloadSize - 1) {
				break;
			}
			if (n >= maxMessageSize) {
				throw new IOException("event not ready before max message length: "	+ maxMessageSize);
			}
			if(payloadSize > maxMessageSize) {
				throw new IOException("Message size: "+payloadSize+" is greater then max message size: "+maxMessageSize);
			}
			n++;
		};
		return Bytes.toArray(bytes);
	}
	
	
	
	
	
	
	
//	@Override
//	public byte[] deserialize(InputStream inputStream) throws IOException {
//		Attachment att = new Attachment();
//		List<Byte> bytes = new ArrayList<Byte>();
//		int n = 0;
//		int bite;
//		log.info("Available to read:" + inputStream.available());
//		while (true) {
//			bite = inputStream.read();
//			if (bite < 0 && n == 0) {
//				throw new SoftEndOfStreamException("Stream closed between payloads");
//			}
//			bytes.add((byte) bite);
//			att.readBuff.put((byte) bite);
//			if(att.eventReady()) {
//				break;
//			}
//			if (n >= Globals.MAX_EVENT_SIZE + Attachment.HEADER_SIZE) {
//				throw new IOException("event not ready before max message length: "
//						+ (Globals.MAX_EVENT_SIZE + Attachment.HEADER_SIZE));
//			}
//		};
//		return Bytes.toArray(bytes);
//	}

}
