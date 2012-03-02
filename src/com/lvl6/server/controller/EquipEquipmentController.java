package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.EquipEquipmentRequestEvent;
import com.lvl6.events.response.EquipEquipmentResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.EquipEquipmentRequestProto;
import com.lvl6.proto.EventProto.EquipEquipmentResponseProto;
import com.lvl6.proto.EventProto.EquipEquipmentResponseProto.Builder;
import com.lvl6.proto.EventProto.EquipEquipmentResponseProto.EquipEquipmentStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class EquipEquipmentController extends EventController {

  public EquipEquipmentController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new EquipEquipmentRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_EQUIP_EQUIPMENT_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    EquipEquipmentRequestProto reqProto = ((EquipEquipmentRequestEvent)event).getEquipEquipmentRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int structId = reqProto.getStructId();
    CoordinatePair cp = new CoordinatePair(reqProto.getStructCoordinates().getX(), reqProto.getStructCoordinates().getY());
    Timestamp timeOfPurchase = new Timestamp(reqProto.getTimeOfPurchase());
    
    EquipEquipmentResponseProto.Builder resBuilder = EquipEquipmentResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      Structure struct = StructureRetrieveUtils.getStructForStructId(structId);

      boolean legitPurchaseNorm = checkLegitPurchaseNorm(resBuilder, struct, user, timeOfPurchase);

      if (legitPurchaseNorm) {
        int userStructId = InsertUtils.insertUserStruct(user.getId(), struct.getId(), cp, timeOfPurchase);
        if (userStructId <= 0) {
          legitPurchaseNorm = false;
          resBuilder.setStatus(EquipEquipmentStatus.OTHER_FAIL);
        } else {
          resBuilder.setUserStructId(userStructId);
        }
      }

      EquipEquipmentResponseEvent resEvent = new EquipEquipmentResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setEquipEquipmentResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitPurchaseNorm) {
        writeChangesToDB(user, struct);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in EquipEquipment processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, Structure struct) {
    int diamondChange = Math.max(0, struct.getDiamondPrice());
    int coinChange = Math.max(0, struct.getCoinPrice());

    if (!user.updateRelativeDiamondsCoinsExperienceNaive(diamondChange*-1, coinChange*-1, 0)) {
      log.error("problem with changing user stats after purchasing a structure");
    }
  }

  private boolean checkLegitPurchaseNorm(Builder resBuilder, Structure struct,
      User user, Timestamp timeOfPurchase) {
    if (user == null || struct == null || timeOfPurchase == null) {
      resBuilder.setStatus(EquipEquipmentStatus.OTHER_FAIL);
      return false;
    }
    if (!MiscMethods.checkClientTimeBeforeApproximateNow(timeOfPurchase)) {
      resBuilder.setStatus(EquipEquipmentStatus.CLIENT_TOO_AHEAD_OF_SERVER_TIME);
      return false;
    }
    if (user.getLevel() < struct.getMinLevel()) {
      resBuilder.setStatus(EquipEquipmentStatus.LEVEL_TOO_LOW);
      return false;
    }
    if (user.getCoins() < struct.getCoinPrice()) {
      resBuilder.setStatus(EquipEquipmentStatus.NOT_ENOUGH_MATERIALS);
      return false;
    }
    if (user.getDiamonds() < struct.getDiamondPrice()) {
      resBuilder.setStatus(EquipEquipmentStatus.NOT_ENOUGH_MATERIALS);
      return false;
    }
    
    Map<Integer, List<UserStruct>> structIdsToUserStructs = UserStructRetrieveUtils.getStructIdsToUserStructsForUser(user.getId());
    if (structIdsToUserStructs != null) {
      for (Integer structId : structIdsToUserStructs.keySet()) {
        List<UserStruct> userStructsOfSameStructId = structIdsToUserStructs.get(structId);
        if (userStructsOfSameStructId != null) {
          if (structId == struct.getId() && userStructsOfSameStructId.size() >= ControllerConstants.PURCHASE_NORM_STRUCTURE__MAX_NUM_OF_CERTAIN_STRUCTURE) {
            resBuilder.setStatus(EquipEquipmentStatus.ALREADY_HAVE_MAX_OF_THIS_STRUCT);
            return false;
          }
          for (UserStruct us : userStructsOfSameStructId) {
            if (!us.isComplete() && us.getLastRetrieved() == null) {
              resBuilder.setStatus(EquipEquipmentStatus.ANOTHER_STRUCT_STILL_BUILDING);
              return false;
            }
          }
        }
      }
    }
    resBuilder.setStatus(EquipEquipmentStatus.SUCCESS);
    return true;
  }
}
