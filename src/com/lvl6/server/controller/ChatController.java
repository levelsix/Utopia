package com.lvl6.server.controller;

import com.lvl6.events.ChatRequestEvent;
import com.lvl6.events.ChatResponseEvent;
import com.lvl6.events.GameEvent;
import com.lvl6.events.RequestEvent;
import com.lvl6.properties.EventProtocol;
import com.lvl6.proto.EventProto.ChatRequestProto;
import com.lvl6.proto.EventProto.ChatResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;

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
  protected void processRequestEvent(RequestEvent event) {
    ChatRequestProto reqProto = ((ChatRequestEvent)event).getChatRequestProto();
    
    MinimumUserProto senderProto = reqProto.getSender();
    String message = reqProto.getMessage();
    ChatResponseProto resProto = ChatResponseProto.newBuilder().setMessage(message).setSender(senderProto).build();
    
    ChatResponseEvent resEvent = new ChatResponseEvent();
    resEvent.setChatResponseProto(resProto);
    
    server.writeEvent(resEvent);
  }


}