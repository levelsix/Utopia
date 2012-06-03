package com.lvl6.servlet;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.log4j.Logger;

public class GameWebSocketServlet extends WebSocketServlet{

	protected static Logger log = Logger.getLogger(GameWebSocketServlet.class);
	private static final long serialVersionUID = 1L;
	
		
	@Override
	protected StreamInbound createWebSocketInbound(String arg0) {
		log.info("Creating new GameMessageInbound: "+arg0);
		return new GameMessageInbound();
	}

	
	
}
