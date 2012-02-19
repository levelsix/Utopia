package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.MoveOrRotateNormStructureRequestEvent;
import com.lvl6.events.response.MoveOrRotateNormStructureResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.proto.EventProto.MoveOrRotateNormStructureRequestProto;
import com.lvl6.proto.EventProto.MoveOrRotateNormStructureRequestProto.MoveOrRotateNormStructType;
import com.lvl6.proto.EventProto.MoveOrRotateNormStructureResponseProto;
import com.lvl6.proto.EventProto.MoveOrRotateNormStructureResponseProto.MoveOrRotateNormStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.StructOrientation;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class MoveOrRotateNormStructureController extends EventController {

  @Override
  public RequestEvent createRequestEvent() {
    return new MoveOrRotateNormStructureRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_MOVE_OR_ROTATE_NORM_STRUCTURE_EVENT;
  }


  @Override
  protected void processRequestEvent(RequestEvent event) {
    MoveOrRotateNormStructureRequestProto reqProto = ((MoveOrRotateNormStructureRequestEvent)event).getMoveOrRotateNormStructureRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();
    MoveOrRotateNormStructType type = reqProto.getType();

    CoordinatePair newCoords = null;
    StructOrientation orientation = null;
    if (type == MoveOrRotateNormStructType.MOVE) {
      newCoords = new CoordinatePair(reqProto.getCurStructCoordinates().getX(), reqProto.getCurStructCoordinates().getY());
    } else if (type == MoveOrRotateNormStructType.ROTATE) {
      orientation = reqProto.getNewOrientation();
    }

    MoveOrRotateNormStructureResponseProto.Builder resBuilder = MoveOrRotateNormStructureResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    //only locking so you cant moveOrRotate it hella times

    try {
      boolean legit = true;
      if (type == MoveOrRotateNormStructType.MOVE && newCoords == null) {
        legit = false;
        resBuilder.setStatus(MoveOrRotateNormStructureStatus.OTHER_FAIL);
      } else if (type == MoveOrRotateNormStructType.ROTATE && orientation == null) {
        legit = false;
        resBuilder.setStatus(MoveOrRotateNormStructureStatus.OTHER_FAIL);
      }

      if (legit) {
        if (type == MoveOrRotateNormStructType.MOVE) {
          if (!UpdateUtils.updateUserStructCoord(userStructId, newCoords)) {
            resBuilder.setStatus(MoveOrRotateNormStructureStatus.OTHER_FAIL);
          } else {
            resBuilder.setStatus(MoveOrRotateNormStructureStatus.SUCCESS);
          }
        } else {
          if (!UpdateUtils.updateUserStructOrientation(userStructId, orientation)) {
            resBuilder.setStatus(MoveOrRotateNormStructureStatus.OTHER_FAIL);
          } else {
            resBuilder.setStatus(MoveOrRotateNormStructureStatus.SUCCESS);
          }
        }
      }
      MoveOrRotateNormStructureResponseEvent resEvent = new MoveOrRotateNormStructureResponseEvent(senderProto.getUserId());
      resEvent.setMoveOrRotateNormStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

    } catch (Exception e) {
      log.error("exception in MoveOrRotateNormStructure processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }
}
