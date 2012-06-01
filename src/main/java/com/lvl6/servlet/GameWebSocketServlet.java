package com.lvl6.servlet;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;

public class GameWebSocketServlet extends WebSocketServlet{

	private static final long serialVersionUID = 1L;
	
		
	@Override
	protected StreamInbound createWebSocketInbound(String arg0) {
		return new GameMessageInbound();
	}

	
	
}
