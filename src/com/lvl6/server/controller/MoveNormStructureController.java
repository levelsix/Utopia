package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.MoveNormStructureRequestEvent;
import com.lvl6.events.response.MoveNormStructureResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.proto.EventProto.MoveNormStructureRequestProto;
import com.lvl6.proto.EventProto.MoveNormStructureResponseProto;
import com.lvl6.proto.EventProto.MoveNormStructureResponseProto.MoveNormStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class MoveNormStructureController extends EventController {

  @Override
  public RequestEvent createRequestEvent() {
    return new MoveNormStructureRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_MOVE_NORM_STRUCTURE_EVENT;
  }

  
  @Override
  protected void processRequestEvent(RequestEvent event) {
    MoveNormStructureRequestProto reqProto = ((MoveNormStructureRequestEvent)event).getMoveNormStructureRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();
    CoordinatePair newCoords = new CoordinatePair(reqProto.getCurStructCoordinates().getX(), reqProto.getCurStructCoordinates().getY());

    MoveNormStructureResponseProto.Builder resBuilder = MoveNormStructureResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    //only locking so you cant move it hella times
    
    try {
      if (!UpdateUtils.updateUserStructCoord(userStructId, newCoords)) {
        resBuilder.setStatus(MoveNormStructureStatus.FAIL);
      } else {
        resBuilder.setStatus(MoveNormStructureStatus.SUCCESS);
      }
      MoveNormStructureResponseEvent resEvent = new MoveNormStructureResponseEvent(senderProto.getUserId());
      resEvent.setMoveNormStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

    } catch (Exception e) {
      log.error("exception in MoveNormStructure processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }
}
