package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.CriticalStructureActionRequestEvent;
import com.lvl6.events.response.CriticalStructureActionResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.CriticalStructureActionRequestProto;
import com.lvl6.proto.EventProto.CriticalStructureActionRequestProto.CritStructAction;
import com.lvl6.proto.EventProto.CriticalStructureActionResponseProto;
import com.lvl6.proto.EventProto.CriticalStructureActionResponseProto.Builder;
import com.lvl6.proto.EventProto.CriticalStructureActionResponseProto.CriticalStructureAction;
import com.lvl6.proto.InfoProto.CritStructType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class CriticalStructureActionController extends EventController {

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
    CoordinatePair cp = new CoordinatePair(reqProto.getCritStructCoordinates().getX(), reqProto.getCritStructCoordinates().getY());
    CritStructAction action = reqProto.getCritStructAction();
    CritStructType cStructType = reqProto.getCritStructType();

    CriticalStructureActionResponseProto.Builder resBuilder = CriticalStructureActionResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

      boolean legitAction = checkLegitAction(resBuilder, user, cStructType, action);

      CriticalStructureActionResponseEvent resEvent = new CriticalStructureActionResponseEvent(senderProto.getUserId());
      resEvent.setCriticalStructureActionResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitAction) {
        writeChangesToDB(user, cp, cStructType);
      }
    } catch (Exception e) {
      log.error("exception in CriticalStructureAction processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private boolean checkLegitAction(Builder resBuilder, User user,
      CritStructType cStructType, CritStructAction action) {
    if (user == null || cStructType == null || action == null) {
      resBuilder.setStatus(CriticalStructureAction.OTHER_FAIL);
      return false;
    }
    if (action == CritStructAction.PLACE) {
      if (cStructType != CritStructType.ARMORY || cStructType != CritStructType.VAULT
          || cStructType != CritStructType.MARKETPLACE) {
        resBuilder.setStatus(CriticalStructureAction.CANNOT_PLACE_NON_PLACEABLE_CRIT_STRUCT);
        return false;
      }
    }
    if (cStructType == CritStructType.ARMORY) {
      if (user.getLevel() < ControllerConstants.PLACE_CRITSTRUCT__MIN_LEVEL_ARMORY) {
        resBuilder.setStatus(CriticalStructureAction.NOT_ACCESSIBLE_TO_USERS_LEVEL);
      }
    }
    if (cStructType == CritStructType.VAULT) {
      if (user.getLevel() < ControllerConstants.PLACE_CRITSTRUCT__MIN_LEVEL_VAULT) {
        resBuilder.setStatus(CriticalStructureAction.NOT_ACCESSIBLE_TO_USERS_LEVEL);
      }
    }
    if (cStructType == CritStructType.MARKETPLACE) {
      if (user.getLevel() < ControllerConstants.PLACE_CRITSTRUCT__MIN_LEVEL_MARKETPLACE) {
        resBuilder.setStatus(CriticalStructureAction.NOT_ACCESSIBLE_TO_USERS_LEVEL);
      }
    }
    if (cStructType == CritStructType.AVIARY) {
      resBuilder.setStatus(CriticalStructureAction.CANNOT_MOVE_AVIARY);
      return false;
    }
    resBuilder.setStatus(CriticalStructureAction.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, CoordinatePair cp, CritStructType cStructType) {
    if (!UpdateUtils.updateUserCritstructCoord(user.getId(), cp, cStructType)) {
      log.error("error in changing critstruct location for " + user + " " + cStructType.toString());
    }
  }

}
