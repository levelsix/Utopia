package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.UpgradeNormStructureRequestEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.UpgradeNormStructureResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.UpgradeNormStructureRequestProto;
import com.lvl6.proto.EventProto.UpgradeNormStructureResponseProto;
import com.lvl6.proto.EventProto.UpgradeNormStructureResponseProto.Builder;
import com.lvl6.proto.EventProto.UpgradeNormStructureResponseProto.UpgradeNormStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class UpgradeNormStructureController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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
  protected void processRequestEvent(RequestEvent event) throws Exception {
    UpgradeNormStructureRequestProto reqProto = ((UpgradeNormStructureRequestEvent)event).getUpgradeNormStructureRequestProto();

    UpgradeNormStructureResponseProto.Builder resBuilder = UpgradeNormStructureResponseProto.newBuilder();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();
    Timestamp timeOfUpgrade = new Timestamp(reqProto.getTimeOfUpgrade());
    
    resBuilder.setSender(senderProto);

    Structure struct = null;
    UserStruct userStruct = RetrieveUtils.userStructRetrieveUtils().getSpecificUserStruct(userStructId);

    if (userStruct != null) {
      struct = StructureRetrieveUtils.getStructForStructId(userStruct.getStructId());
    }

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      int previousSilver = 0;
      int previousGold = 0;
      
      boolean legitUpgrade = checkLegitUpgrade(resBuilder, user, userStruct, struct, timeOfUpgrade);
      UpgradeNormStructureResponseEvent resEvent = new UpgradeNormStructureResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setUpgradeNormStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitUpgrade) {
        previousSilver = user.getCoins() + user.getVaultBalance();
        previousGold = user.getDiamonds();
        
        Map<String, Integer> money = new HashMap<String, Integer>();
        writeChangesToDB(user, userStruct, struct, timeOfUpgrade, money);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        writeToUserCurrencyHistory(user, userStruct, timeOfUpgrade, money, previousSilver, previousGold);
      }
    } catch (Exception e) {
      log.error("exception in UpgradeNormStructure processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private void writeChangesToDB(User user, UserStruct userStruct, Structure struct, Timestamp timeOfUpgrade,
      Map<String, Integer> money) {
    int goldChange = -1*calculateUpgradeDiamondCost(userStruct.getLevel(), struct);
    int silverChange = -1*calculateUpgradeCoinCost(userStruct.getLevel(), struct);
    
    // TODO Auto-generated method stub
    if (!user.updateRelativeDiamondsCoinsExperienceNaive(goldChange, silverChange, 0)) {
      log.error("problem with updating user stats: diamondChange=" + goldChange
          + ", coinChange=" + silverChange + ", user is " + user);
    } else {
      //everything went well
      if (0 != goldChange) {
        money.put(MiscMethods.gold, goldChange);
      }
      if (0 != silverChange) {
        money.put(MiscMethods.silver, silverChange);
      }
    }
    if (!UpdateUtils.get().updateUserStructLastretrievedLastupgradeIscomplete(userStruct.getId(), null, timeOfUpgrade, false)) {
      log.error("problem with changing time of upgrade to " + timeOfUpgrade + " and marking as incomplete, the user struct " + userStruct);
    }
  }

  private boolean checkLegitUpgrade(Builder resBuilder, User user, UserStruct userStruct,
      Structure struct, Timestamp timeOfUpgrade) {
    if (user == null || userStruct == null || struct == null || userStruct.getLastRetrieved() == null) {
      resBuilder.setStatus(UpgradeNormStructureStatus.OTHER_FAIL);
      log.error("parameter passed in is null. user=" + user + ", user struct=" + userStruct + ", struct="
          + struct + ", userStruct's last retrieve time=" + userStruct.getLastRetrieved());
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(timeOfUpgrade)) {
      resBuilder.setStatus(UpgradeNormStructureStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + timeOfUpgrade + ", servertime~="
          + new Date());
      return false;
    }
    if (!userStruct.isComplete()) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_BUILT_YET);
      log.error("user struct is not complete yet");
      return false;
    }
    if (timeOfUpgrade.getTime() < userStruct.getLastRetrieved().getTime()) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_BUILT_YET);
      log.error("the upgrade time " + timeOfUpgrade + " is before the last time the building was retrieved:"
          + userStruct.getLastRetrieved());
      return false;
    }
    if (userStruct.getLevel() == ControllerConstants.UPGRADE_NORM_STRUCTURE__MAX_STRUCT_LEVEL) {
      resBuilder.setStatus(UpgradeNormStructureStatus.AT_MAX_LEVEL_ALREADY);
      log.error("user struct at max level already, which is " + ControllerConstants.UPGRADE_NORM_STRUCTURE__MAX_STRUCT_LEVEL);
      return false;
    }
    int upgradeCoinCost = calculateUpgradeCoinCost(userStruct.getLevel(), struct);
    int upgradeDiamondCost = calculateUpgradeDiamondCost(userStruct.getLevel(), struct);

    if (user.getId() != userStruct.getUserId()) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_USERS_STRUCT);
      log.error("user struct belongs to someone else with id " + userStruct.getUserId());
      return false;
    }
    if (user.getCoins() < upgradeCoinCost) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_ENOUGH_MATERIALS);
      log.error("user doesn't have enough coins, has " + user.getCoins() + ", needs " + upgradeCoinCost);
      return false;
    }
    if (user.getDiamonds() < upgradeDiamondCost) {
      resBuilder.setStatus(UpgradeNormStructureStatus.NOT_ENOUGH_MATERIALS);
      log.error("user doesn't have enough diamonds, has " + user.getDiamonds() + ", needs " + upgradeDiamondCost);
      return false;
    }
    List<UserStruct> userStructs = RetrieveUtils.userStructRetrieveUtils().getUserStructsForUser(user.getId());
    if (userStructs != null) {
      for (UserStruct us : userStructs) {
        if (!us.isComplete() && us.getLastRetrieved() != null && us.getLastUpgradeTime() != null) {
          resBuilder.setStatus(UpgradeNormStructureStatus.ANOTHER_STRUCT_STILL_UPGRADING);
          log.error("another struct is still upgrading: user struct=" + us);
          return false;
        }
      }
    }
    resBuilder.setStatus(UpgradeNormStructureStatus.SUCCESS);
    return true;
  }

  private int calculateUpgradeCoinCost(int oldLevel, Structure struct) {
    return Math.max(0, (int)(struct.getCoinPrice() * Math.pow(ControllerConstants.UPGRADE_NORM_STRUCTURE__UPGRADE_STRUCT_COIN_COST_EXPONENT_BASE, oldLevel)));
  }
  
  private int calculateUpgradeDiamondCost(int oldLevel, Structure struct) {
    return Math.max(0, (int)(struct.getDiamondPrice() * Math.pow(ControllerConstants.UPGRADE_NORM_STRUCTURE__UPGRADE_STRUCT_DIAMOND_COST_EXPONENT_BASE, oldLevel)));
  }
  
  private void writeToUserCurrencyHistory(User aUser, UserStruct userStruct, Timestamp timeOfUpgrade, 
      Map<String, Integer> money, int previousSilver, int previousGold) {
    
    int userStructId = userStruct.getId();
    int structId = userStruct.getStructId();
    int prevLevel = userStruct.getLevel();
    String structDetails = "uStructId:" + userStructId + " structId:" + structId
        + " prevLevel:" + prevLevel;
    
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    String reasonForChange = ControllerConstants.UCHRFC__UPGRADE_NORM_STRUCT + " "
        + structDetails;
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String gold = MiscMethods.gold;
    String silver = MiscMethods.silver;
    
    previousGoldSilver.put(silver, previousSilver);
    previousGoldSilver.put(gold, previousGold);
    
    reasonsForChanges.put(gold, reasonForChange);
    reasonsForChanges.put(silver,  reasonForChange);
    
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, timeOfUpgrade, 
        money, previousGoldSilver, reasonsForChanges);
  }

}
