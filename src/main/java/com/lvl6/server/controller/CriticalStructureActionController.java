package com.lvl6.server.controller;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.events.request.CriticalStructureActionRequestEvent;
import com.lvl6.events.response.CriticalStructureActionResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.CriticalStructureActionRequestProto;
import com.lvl6.proto.EventProto.CriticalStructureActionRequestProto.CritStructActionType;
import com.lvl6.proto.EventProto.CriticalStructureActionResponseProto;
import com.lvl6.proto.EventProto.CriticalStructureActionResponseProto.Builder;
import com.lvl6.proto.EventProto.CriticalStructureActionResponseProto.CritStructActionStatus;
import com.lvl6.proto.InfoProto.CritStructType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.StructOrientation;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;

/*
 * NOT READY/BEING USED YET
 */

  @Component @DependsOn("gameServer") public class CriticalStructureActionController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public CriticalStructureActionController() {
    numAllocatedThreads = 1;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new CriticalStructureActionRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_CRIT_STRUCTURE_ACTION_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    CriticalStructureActionRequestProto reqProto = ((CriticalStructureActionRequestEvent)event).getCriticalStructureActionRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    CritStructActionType action = reqProto.getActionType();
    CritStructType cStructType = reqProto.getCritStructType();
    CoordinatePair cp = null;
    StructOrientation orientation = null;

    if (action == CritStructActionType.MOVE || action == CritStructActionType.PLACE) {
      if (reqProto.hasCritStructCoordinates()) {
        cp = new CoordinatePair(reqProto.getCritStructCoordinates().getX(), reqProto.getCritStructCoordinates().getY());
      }
    } else if (action == CritStructActionType.ROTATE) {
      orientation = reqProto.getOrientation();
    }

    CriticalStructureActionResponseProto.Builder resBuilder = CriticalStructureActionResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legitAction = checkLegitAction(resBuilder, user, cStructType, action, cp, orientation);

      CriticalStructureActionResponseEvent resEvent = new CriticalStructureActionResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setCriticalStructureActionResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitAction) {
        writeChangesToDB(user, action, cStructType, cp, orientation);
      }
    } catch (Exception e) {
      log.error("exception in CriticalStructureAction processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private boolean checkLegitAction(Builder resBuilder, User user,
      CritStructType cStructType, CritStructActionType action, CoordinatePair cp, StructOrientation orientation) {
    if (user == null || cStructType == null || action == null) {
      resBuilder.setStatus(CritStructActionStatus.OTHER_FAIL);
      return false;
    }
    if (action == CritStructActionType.MOVE || action == CritStructActionType.PLACE) {
      if (cp == null) {
        resBuilder.setStatus(CritStructActionStatus.OTHER_FAIL);
        return false;
      }
    } else if (action == CritStructActionType.ROTATE) {
      if (orientation == null) {
        resBuilder.setStatus(CritStructActionStatus.OTHER_FAIL);
        return false;
      }
    } else {
      return false;
    }
    if (action == CritStructActionType.PLACE) {
      if (cStructType != CritStructType.ARMORY && cStructType != CritStructType.VAULT
          && cStructType != CritStructType.MARKETPLACE) {
        resBuilder.setStatus(CritStructActionStatus.CANNOT_PLACE_NON_PLACEABLE_CRIT_STRUCT);
        return false;
      }
    }
    if (action != CritStructActionType.ROTATE && cStructType == CritStructType.AVIARY) {
      resBuilder.setStatus(CritStructActionStatus.CANNOT_MOVE_AVIARY);
      return false;
    }
    resBuilder.setStatus(CritStructActionStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, CritStructActionType action, CritStructType cStructType, CoordinatePair cp,
      StructOrientation orientation) {
//    if (action == CritStructActionType.MOVE || action == CritStructActionType.PLACE) {
//      if (!UpdateUtils.get().updateUserCritstructCoord(user.getId(), cp, cStructType)) {
//        log.error("error in changing critstruct location for " + user + " " + cStructType.toString());
//      }
//    } else if (action == CritStructActionType.ROTATE){
//      if (!UpdateUtils.get().updateUserCritstructOrientation(user.getId(), orientation, cStructType)) {
//        log.error("error in changing critstruct orientation for " + user + " " + cStructType.toString());
//      }
//    }
  }
}
