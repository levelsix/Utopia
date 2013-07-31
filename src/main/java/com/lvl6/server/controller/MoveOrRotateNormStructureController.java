package com.lvl6.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.MoveOrRotateNormStructureRequestEvent;
import com.lvl6.events.response.MoveOrRotateNormStructureResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.UserStruct;
import com.lvl6.proto.EventProto.MoveOrRotateNormStructureRequestProto;
import com.lvl6.proto.EventProto.MoveOrRotateNormStructureRequestProto.MoveOrRotateNormStructType;
import com.lvl6.proto.EventProto.MoveOrRotateNormStructureResponseProto;
import com.lvl6.proto.EventProto.MoveOrRotateNormStructureResponseProto.MoveOrRotateNormStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.StructOrientation;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class MoveOrRotateNormStructureController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public MoveOrRotateNormStructureController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new MoveOrRotateNormStructureRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_MOVE_OR_ROTATE_NORM_STRUCTURE_EVENT;
  }


  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
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

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    //only locking so you cant moveOrRotate it hella times

    try {
      boolean legit = true;
      resBuilder.setStatus(MoveOrRotateNormStructureStatus.SUCCESS);
      
      UserStruct userStruct = RetrieveUtils.userStructRetrieveUtils().getSpecificUserStruct(userStructId);
      if (userStruct == null) {
        legit = false;
        resBuilder.setStatus(MoveOrRotateNormStructureStatus.SUCCESS);
      }
      
      if (type == MoveOrRotateNormStructType.MOVE && newCoords == null) {
        legit = false;
        resBuilder.setStatus(MoveOrRotateNormStructureStatus.OTHER_FAIL);
        log.error("asked to move, but the coordinates supplied in are null. reqProto's newStructCoordinates=" + reqProto.getCurStructCoordinates());
      } else if (type == MoveOrRotateNormStructType.ROTATE && orientation == null) {
        legit = false;
        resBuilder.setStatus(MoveOrRotateNormStructureStatus.OTHER_FAIL);
        log.error("asked to move, but the orientation supplied in is null. reqProto's orientation=" + reqProto.getNewOrientation());
      }

      if (legit) {
        if (type == MoveOrRotateNormStructType.MOVE) {
          if (!UpdateUtils.get().updateUserStructCoord(userStructId, newCoords)) {
            resBuilder.setStatus(MoveOrRotateNormStructureStatus.OTHER_FAIL);
            log.error("problem with updating coordinates to " + newCoords + " for user struct " + userStructId);
          } else {
            resBuilder.setStatus(MoveOrRotateNormStructureStatus.SUCCESS);
          }
        } else {
          if (!UpdateUtils.get().updateUserStructOrientation(userStructId, orientation)) {
            resBuilder.setStatus(MoveOrRotateNormStructureStatus.OTHER_FAIL);
            log.error("problem with updating orientation to " + orientation + " for user struct " + userStructId);
          } else {
            resBuilder.setStatus(MoveOrRotateNormStructureStatus.SUCCESS);
          }
        }
      }
      MoveOrRotateNormStructureResponseEvent resEvent = new MoveOrRotateNormStructureResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setMoveOrRotateNormStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

    } catch (Exception e) {
      log.error("exception in MoveOrRotateNormStructure processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }
}
