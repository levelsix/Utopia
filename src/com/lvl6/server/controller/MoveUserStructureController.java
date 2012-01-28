package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.MoveUserStructureRequestEvent;
import com.lvl6.events.response.MoveUserStructureResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.proto.EventProto.MoveUserStructureRequestProto;
import com.lvl6.proto.EventProto.MoveUserStructureResponseProto;
import com.lvl6.proto.EventProto.MoveUserStructureResponseProto.MoveUserStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class MoveUserStructureController extends EventController {

  @Override
  public RequestEvent createRequestEvent() {
    return new MoveUserStructureRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_MOVE_USER_STRUCTURE_EVENT;
  }

  
  @Override
  protected void processRequestEvent(RequestEvent event) {
    MoveUserStructureRequestProto reqProto = ((MoveUserStructureRequestEvent)event).getMoveUserStructureRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();
    CoordinatePair newCoords = new CoordinatePair(reqProto.getCurStructCoordinates().getX(), reqProto.getCurStructCoordinates().getY());

    MoveUserStructureResponseProto.Builder resBuilder = MoveUserStructureResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    //only locking so you cant move it hella times
    
    try {
      if (!UpdateUtils.updateUserStructCoord(userStructId, newCoords)) {
        resBuilder.setStatus(MoveUserStructureStatus.FAIL);
      } else {
        resBuilder.setStatus(MoveUserStructureStatus.SUCCESS);
      }
      MoveUserStructureResponseEvent resEvent = new MoveUserStructureResponseEvent(senderProto.getUserId());
      resEvent.setMoveUserStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

    } catch (Exception e) {
      log.error("exception in MoveUserStructure processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }
}
