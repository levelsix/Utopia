package com.lvl6.server;

import com.lvl6.events.ResponseEvent;
import com.lvl6.utils.Wrap;

public abstract class EventWriter extends Wrap {

	public EventWriter() {
		super();
	}

	public abstract void processGlobalChatResponseEvent(ResponseEvent event);

	public abstract void processPreDBResponseEvent(ResponseEvent event, String udid);
}