package com.lvl6.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.apache.catalina.websocket.MessageInbound;

public class GameMessageInbound extends MessageInbound {

	@Override
	protected void onBinaryMessage(ByteBuffer message) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onTextMessage(CharBuffer message) throws IOException {
		// TODO Auto-generated method stub

	}

}
