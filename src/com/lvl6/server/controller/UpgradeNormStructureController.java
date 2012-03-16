package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.List;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.UpgradeNormStructureRequestEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.UpgradeNormStructureResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.UpgradeNormStructureRequestProto;
import com.lvl6.proto.EventProto.UpgradeNormStructureResponseProto;
import com.lvl6.proto.EventProto.UpgradeNormStructureResponseProto.Builder;
import com.lvl6.proto.EventProto.UpgradeNormStructureResponseProto.UpgradeNormStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class UpgradeNormStructureController extends EventController {

  public UpgradeNormStructureController() {
    numAllocatedThreads = 4;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new UpgradeNormStructureRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_UPGRADE_NORM_STRUCTURE_EVENT;
  }


  @Override
  protected void processRequestEvent(RequestEvent event) {
    UpgradeNormStructureRequestProto reqProto = ((UpgradeNormStructureRequestEvent)event).getUpgradeNormStructureRequestProto();

    UpgradeNormStructureResponseProto.Builder resBuilder = UpgradeNormStructureResponseProto.newBuilder();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();
    Timestamp timeOfUpgrade = new Timestamp(reqProto.getTimeOfUpgrade());
    
    resBuilder.setSender(senderProto);

    Structure struct = null;
    UserStruct userStruct = UserStructRetrieveUtils.getSpecificUserStruct(userStructId);

    if (userStruct != null) {
      struct = StructureRetrieveUtils.getStructForStructId(userStruct.getStructId());
    }

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      boolean legitUpgrade = checkLegitUpgrade(resBuilder, user, userStruct, struct, timeOfUpgrade);
      UpgradeNormStructureResponseEvent resEvent = new UpgradeNormStructureResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setUpgradeNormStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitUpgrade) {
        writeChangesToDB(user, userStruct, struct, timeOfUpgrade);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in UpgradeNormStructure processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, UserStruct userStruct, Structure struct, Timestamp timeOfUpgrade) {
    // TODO Auto-generated method stub
    if (!user.updateRelativeDiamondsCoinsExperienceNaive(-1*calculateUpgradeDiamondCost(userStruct.getLevel(), struct), -1*calculateUpgradeCoinCost(userStruct.getLevel(), struct), 0)) {
      log.error("problem in updating user stats after upgrade");
    }
    if (!UpdateUtils.updateUserStructLastretrievedLastupgradeIscomplete(userStruct.getId(), null, timeOfUpgrade, false)) {
      log.error("problem in upgrading user struct lastupgradetime and complete");
    }
  }

  private boolean checkLegitUpgrade(Builder resBuilder, User user, UserStruct userStruct,
      Structure struct, Timestamp timeOfUpgrade) {
    if (user == null || userStruct == null || struct == null || userStruct.getLastRetrieved() == null) {
      resBuilder.setStatus(UpgradeNormStructureStatus.OTHER_FAIL);
      return false;
    }
    if (!MiscMethods.checkClientTimeBeforeApproximateNow(timeOfUpgrade)) {
      resBuilder.setStatus(UpgradeNormStructureStatus.CLIENT_TOO_AHEAD_OF_SERVER_TIME);
      return false;
    }
    if (!userStruct.isComplete()) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_BUILT_YET);
      return false;
    }
    if (timeOfUpgrade.getTime() < userStruct.getLastRetrieved().getTime()) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_BUILT_YET);
      return false;
    }
    if (userStruct.getLevel() == ControllerConstants.UPGRADE_NORM_STRUCTURE__MAX_STRUCT_LEVEL) {
      resBuilder.setStatus(UpgradeNormStructureStatus.AT_MAX_LEVEL_ALREADY);
      return false;
    }

    int upgradeCoinCost = calculateUpgradeCoinCost(userStruct.getLevel(), struct);
    int upgradeDiamondCost = calculateUpgradeDiamondCost(userStruct.getLevel(), struct);

    if (user.getId() != userStruct.getUserId()) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_USERS_STRUCT);
      return false;
    }
    if (user.getCoins() < upgradeCoinCost) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_ENOUGH_MATERIALS);
      return false;
    }
    if (user.getDiamonds() < upgradeDiamondCost) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_ENOUGH_MATERIALS);
      return false;
    }
    List<UserStruct> userStructs = UserStructRetrieveUtils.getUserStructsForUser(user.getId());
    if (userStructs != null) {
      for (UserStruct us : userStructs) {
        if (!us.isComplete() && us.getLastRetrieved() != null && us.getLastUpgradeTime() != null) {
          resBuilder.setStatus(UpgradeNormStructureStatus.ANOTHER_STRUCT_STILL_UPGRADING);
          return false;
        }
      }
    }
    resBuilder.setStatus(UpgradeNormStructureStatus.SUCCESS);
    return true;
  }

  private int calculateUpgradeCoinCost(int oldLevel, Structure struct) {
    return Math.max(0, (int)(struct.getCoinPrice() * Math.pow(ControllerConstants.UPGRADE_NORM_STRUCTURE__UPGRADE_STRUCT_COIN_COST_BASE, oldLevel)));
  }
  
  private int calculateUpgradeDiamondCost(int oldLevel, Structure struct) {
    return Math.max(0, (int)(struct.getDiamondPrice() * Math.pow(ControllerConstants.UPGRADE_NORM_STRUCTURE__UPGRADE_STRUCT_DIAMOND_COST_BASE, oldLevel)));
  }

}
