package com.lvl6.server.controller;

import java.util.HashMap;
import java.util.Hashtable;

import com.lvl6.events.ChatEvent;
import com.lvl6.events.GameEvent;
import com.lvl6.utils.EventProtocol;

public class ChatController extends EventController {

  /** 
   * do ChatController specific initialization here 
   */
  @Override  
  public void initController() {
    log.info("initController for " + this.getClass().toString());
  }

  @Override
  public GameEvent createEvent() {
    return new ChatEvent();
  }

  @Override
  public byte getEventType() {
    return EventProtocol.C_CHAT_EVENT;
  }

  @Override
  protected void processEvent(GameEvent event) {
    // TODO impl
    /*
     * check if event is broadcast or normal. separate event types? no, can't. 
     * create an event, initialize it, and send it accordingly
     */
  }

  
}
