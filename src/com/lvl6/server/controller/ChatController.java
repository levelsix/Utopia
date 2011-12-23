package com.lvl6.server.controller;

import com.lvl6.events.ChatRequestEvent;
import com.lvl6.events.RequestEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.properties.EventProtocol;

public class ChatController extends EventController {

  /** 
   * do ChatController specific initialization here 
   */
  @Override  
  public void initController() {
    log.info("initController for " + this.getClass().toString());
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new ChatRequestEvent();
  }

  @Override
  public byte getEventType() {
    return EventProtocol.C_CHAT_EVENT;
  }

  @Override
  protected void processEvent(ResponseEvent event) {
    server.writeEvent(event);
  }

}
