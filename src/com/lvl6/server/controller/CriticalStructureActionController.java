package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.CriticalStructureActionRequestEvent;
import com.lvl6.events.response.CriticalStructureActionResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.CriticalStructureActionRequestProto;
import com.lvl6.proto.EventProto.CriticalStructureActionRequestProto.CritStructActionType;
import com.lvl6.proto.EventProto.CriticalStructureActionResponseProto;
import com.lvl6.proto.EventProto.CriticalStructureActionResponseProto.Builder;
import com.lvl6.proto.EventProto.CriticalStructureActionResponseProto.CritStructActionStatus;
import com.lvl6.proto.InfoProto.CritStructType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.StructOrientation;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class CriticalStructureActionController extends EventController {

  public CriticalStructureActionController() {
    numAllocatedThreads = 3;
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
  protected void processRequestEvent(RequestEvent event) {
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

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

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
      server.unlockPlayer(senderProto.getUserId());      
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
    if (cStructType == CritStructType.ARMORY) {
      if (user.getLevel() < ControllerConstants.MIN_LEVEL_FOR_ARMORY) {
        resBuilder.setStatus(CritStructActionStatus.NOT_ACCESSIBLE_TO_USERS_LEVEL);
      }
    }
    if (cStructType == CritStructType.VAULT) {
      if (user.getLevel() < ControllerConstants._MIN_LEVEL_FOR_VAULT) {
        resBuilder.setStatus(CritStructActionStatus.NOT_ACCESSIBLE_TO_USERS_LEVEL);
      }
    }
    if (cStructType == CritStructType.MARKETPLACE) {
      if (user.getLevel() < ControllerConstants.MIN_LEVEL_FOR_MARKETPLACE) {
        resBuilder.setStatus(CritStructActionStatus.NOT_ACCESSIBLE_TO_USERS_LEVEL);
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
    if (action == CritStructActionType.MOVE || action == CritStructActionType.PLACE) {
      if (!UpdateUtils.updateUserCritstructCoord(user.getId(), cp, cStructType)) {
        log.error("error in changing critstruct location for " + user + " " + cStructType.toString());
      }
    } else if (action == CritStructActionType.ROTATE){
      if (!UpdateUtils.updateUserCritstructOrientation(user.getId(), orientation, cStructType)) {
        log.error("error in changing critstruct orientation for " + user + " " + cStructType.toString());
      }
    }
  }
}
