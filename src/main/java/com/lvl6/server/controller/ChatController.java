package com.lvl6.server.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ChatRequestEvent;
import com.lvl6.events.response.ChatResponseEvent;
import com.lvl6.proto.EventProto.ChatRequestProto;
import com.lvl6.proto.EventProto.ChatResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;

/*
 * NOT READY/BEING USED YET
 */

 @Component public class ChatController extends EventController {

  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public ChatController() {
    numAllocatedThreads = 1;
  }
  /** 
   * do ChatController specific initialization here 
   */

  @Override
  public RequestEvent createRequestEvent() {
    return new ChatRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_CHAT_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    ChatRequestProto reqProto = ((ChatRequestEvent)event).getChatRequestProto();
    
    MinimumUserProto senderProto = reqProto.getSender();
    String message = reqProto.getMessage();
    ChatResponseProto resProto = ChatResponseProto.newBuilder().setMessage(message).setSender(senderProto).build();
    
    ChatResponseEvent resEvent = new ChatResponseEvent();
    resEvent.setTag(event.getTag());
    List<MinimumUserProto> recipients = reqProto.getRecipientsList();
    if (recipients != null && recipients.size() > 0) {
      int[] recipientIds = new int[recipients.size()];
      int i = 0;
      for (MinimumUserProto recipient : recipients) {
        recipientIds[i] = recipient.getUserId();
        i++;
      }
      resEvent.setRecipients(recipientIds);
    }
    resEvent.setChatResponseProto(resProto);
    
    server.writeEvent(resEvent);
  }


}