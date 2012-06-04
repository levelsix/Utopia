package com.lvl6.utils;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.catalina.websocket.MessageInbound;

public class OpenWebSockets extends ConcurrentHashMap<String, MessageInbound> {
	private static final long serialVersionUID = -678869737511029844L;
	
}
