package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ReconnectRequestEvent;
import com.lvl6.events.response.ReconnectResponseEvent;
import com.lvl6.proto.EventProto.ReconnectRequestProto;
import com.lvl6.proto.EventProto.ReconnectResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;

@Component @DependsOn("gameServer") public class ReconnectController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public ReconnectController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new ReconnectRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RECONNECT_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    ReconnectRequestProto reqProto = ((ReconnectRequestEvent)event).getReconnectRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    ReconnectResponseProto.Builder resBuilder = ReconnectResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setIncomingResponseMessages(false);

    //TODO: 
    //grab events from hazel
    //if there are any
      //log it
      resBuilder.setIncomingResponseMessages(true);
      //send them

    ReconnectResponseProto resProto = resBuilder.build();
    ReconnectResponseEvent resEvent = new ReconnectResponseEvent(senderProto.getUserId());
    resEvent.setTag(event.getTag());
    resEvent.setReconnectResponseProto(resProto);

    server.writeEvent(resEvent);
  }

}
