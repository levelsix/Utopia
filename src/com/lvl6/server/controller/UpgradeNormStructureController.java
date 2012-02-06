package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.UpgradeNormStructureRequestEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.UpgradeNormStructureResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
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

    resBuilder.setSender(senderProto);

    Structure struct = null;
    UserStruct userStruct = UserStructRetrieveUtils.getSpecificUserStruct(userStructId);

    if (userStruct != null) {
      struct = StructureRetrieveUtils.getStructForStructId(userStruct.getStructId());
    }

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      boolean legitUpgrade = checkLegitUpgrade(resBuilder, user, userStruct, struct);
      UpgradeNormStructureResponseEvent resEvent = new UpgradeNormStructureResponseEvent(senderProto.getUserId());
      resEvent.setUpgradeNormStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      
      if (legitUpgrade) {
        writeChangesToDB(user, userStruct, struct);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in UpgradeNormStructure processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, UserStruct userStruct, Structure struct) {
    // TODO Auto-generated method stub
    if (user.updateRelativeDiamondsCoinsWoodNaive(calculateUpgradeDiamondCost(struct.getUpgradeDiamondCostBase(), userStruct.getLevel()), 
        calculateUpgradeCoinCost(struct.getUpgradeCoinCostBase(), userStruct.getLevel()), 
        calculateUpgradeWoodCost(struct.getUpgradeWoodCostBase(), userStruct.getLevel()))) {
      log.error("problem in updating user stats after upgrade");
    }
    if (!UpdateUtils.updateUserStructLevel(userStruct.getId(), 1)) {
      log.error("problem in upgrading user struct leve");
    }
  }

  private boolean checkLegitUpgrade(Builder resBuilder, User user, UserStruct userStruct,
      Structure struct) {
    if (user == null || userStruct == null || struct == null) {
      resBuilder.setStatus(UpgradeNormStructureStatus.OTHER_FAIL);
      return false;
    }

    int upgradeCoinCost = calculateUpgradeCoinCost(struct.getUpgradeCoinCostBase(), userStruct.getLevel());
    int upgradeWoodCost = calculateUpgradeWoodCost(struct.getUpgradeWoodCostBase(), userStruct.getLevel());
    int upgradeDiamondCost = calculateUpgradeDiamondCost(struct.getUpgradeDiamondCostBase(), userStruct.getLevel());

    if (user.getCoins() < upgradeCoinCost) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_ENOUGH_COINS);
      return false;
    }
    if (user.getWood() < upgradeWoodCost) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_ENOUGH_WOOD);
      return false;
    }
    if (user.getDiamonds() < upgradeDiamondCost) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_ENOUGH_DIAMONDS);
      return false;
    }
    resBuilder.setStatus(UpgradeNormStructureStatus.SUCCESS);
    return true;
  }

  private int calculateUpgradeCoinCost(int upgradeCoinCostBase, int oldLevel) {
    int result = upgradeCoinCostBase*(oldLevel/2);   //TODO: change later
    return Math.max(0, result);
  }

  private int calculateUpgradeDiamondCost(int upgradeDiamondCostBase, int oldLevel) {
    int result = upgradeDiamondCostBase*(oldLevel/2);   //TODO: change later
    return Math.max(0, result);
  }

  private int calculateUpgradeWoodCost(int upgradeWoodCostBase, int oldLevel) {
    int result = upgradeWoodCostBase*(oldLevel/2);   //TODO:change later
    return Math.max(0, result);
  }


}
