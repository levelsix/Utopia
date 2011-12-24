package com.lvl6.server.controller;

import com.lvl6.events.ChatRequestEvent;
import com.lvl6.events.GameEvent;
import com.lvl6.events.RequestEvent;
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
  protected void processRequestEvent(RequestEvent event) {
    server.writeEvent(event);
  }


}



/*
removed playerId
hardcoded into chatresponseevent the event type
made eventcontroller's processEvent call processRequestEvent abstract class thats overridden in subcontrollers
server's writeevent only accepts responseevents now
created empty user class
eventWriter's methods are now processResponseEvent not processEvent
removed eventType from protos- these are in the header for client->server, and hardcoded in responseevents
  chatcontroller needs to return event type for hashmap of eventtype to controller
  requestevent does not need eventtype- the correct controller for that requestevent is determined by the attachment's eventtype
  responseevent needs eventtype so that the messageout has the eventType

add requirement that responseevents can have -1 for playerId

if recipients will live in the chatResponseProto, why do they also live in BroadcastResponseEvent.
they shouldnt live in the proto- proto represents the data we send to each user. that doesnt need the recipients.
*/