package com.lvl6.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;

public class GameMessageInbound extends MessageInbound {

	protected static Logger log = Logger.getLogger(GameMessageInbound.class);
	
	@Override
	protected void onBinaryMessage(ByteBuffer message) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onTextMessage(CharBuffer message) throws IOException {
		log.info("Recieved incoming message from WebSocket: "+message);
	}

	@Override
	protected void onOpen(WsOutbound outbound) {
		// TODO Auto-generated method stub
		super.onOpen(outbound);
	}

	@Override
	protected void onClose(int status) {
		// TODO Auto-generated method stub
		super.onClose(status);
	}

}
