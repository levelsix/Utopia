package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.FinishNormStructWaittimeWithDiamondsRequestEvent;
import com.lvl6.events.response.FinishNormStructWaittimeWithDiamondsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.FinishNormStructWaittimeWithDiamondsRequestProto;
import com.lvl6.proto.EventProto.FinishNormStructWaittimeWithDiamondsRequestProto.NormStructWaitTimeType;
import com.lvl6.proto.EventProto.FinishNormStructWaittimeWithDiamondsResponseProto;
import com.lvl6.proto.EventProto.FinishNormStructWaittimeWithDiamondsResponseProto.Builder;
import com.lvl6.proto.EventProto.FinishNormStructWaittimeWithDiamondsResponseProto.FinishNormStructWaittimeStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class FinishNormStructWaittimeWithDiamondsController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
  public FinishNormStructWaittimeWithDiamondsController() {
    numAllocatedThreads = 2;
  }
  
  public static String silver = "silver";
  public static String gold = "gold";
  
  @Override
  public RequestEvent createRequestEvent() {
    return new FinishNormStructWaittimeWithDiamondsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {

    FinishNormStructWaittimeWithDiamondsRequestProto reqProto = ((FinishNormStructWaittimeWithDiamondsRequestEvent)event).getFinishNormStructWaittimeWithDiamondsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();
    Timestamp timeOfSpeedup = new Timestamp(reqProto.getTimeOfSpeedup());
    NormStructWaitTimeType waitTimeType = reqProto.getWaitTimeType();

    FinishNormStructWaittimeWithDiamondsResponseProto.Builder resBuilder = FinishNormStructWaittimeWithDiamondsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());      
      UserStruct userStruct = RetrieveUtils.userStructRetrieveUtils().getSpecificUserStruct(userStructId);
      Structure struct = null;
      if (userStruct != null) {
        struct = StructureRetrieveUtils.getStructForStructId(userStruct.getStructId());
      }

      boolean legitSpeedup = checkLegitSpeedup(resBuilder, user, userStruct, timeOfSpeedup, waitTimeType, struct);

      FinishNormStructWaittimeWithDiamondsResponseEvent resEvent = new FinishNormStructWaittimeWithDiamondsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setFinishNormStructWaittimeWithDiamondsResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitSpeedup) {
        Map<String, Integer> money = new HashMap<String, Integer>();
        writeChangesToDB(user, userStruct, timeOfSpeedup, waitTimeType, struct, money);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        writeToUserCurrencyHistory(user, timeOfSpeedup, money);
        if (waitTimeType == NormStructWaitTimeType.FINISH_CONSTRUCTION) {
          QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, null, false);
        }
        if (waitTimeType == NormStructWaitTimeType.FINISH_UPGRADE) {
          QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, null, false);
        }
      }
    } catch (Exception e) {
      log.error("exception in FinishNormStructWaittimeWithDiamondsController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, UserStruct userStruct, Timestamp timeOfPurchase, NormStructWaitTimeType waitTimeType, Structure struct,
      Map<String, Integer> money) {
    if (waitTimeType == NormStructWaitTimeType.FINISH_CONSTRUCTION) {
      int goldCost = struct.getInstaBuildDiamondCost() * -1;
      if (!user.updateRelativeDiamondsNaive(goldCost)) {
        log.error("problem with using diamonds to finish norm struct build");
      } else {
        if (!UpdateUtils.get().updateUserStructLastretrievedLastupgradeIscomplete(userStruct.getId(), timeOfPurchase, null, true)) {
          log.error("problem with using diamonds to finish norm struct build");
        }
        money.put(gold, goldCost);
        money.put(silver, null);
      }
    }
    if (waitTimeType == NormStructWaitTimeType.FINISH_INCOME_WAITTIME) {
      int goldCost = calculateDiamondCostForInstaRetrieve(userStruct, struct)*-1;
      int silverCost = MiscMethods.calculateIncomeGainedFromUserStruct(struct.getIncome(), userStruct.getLevel());
      if (!user.updateRelativeDiamondsCoinsExperienceNaive(goldCost, silverCost, 0)) {
        log.error("problem with using diamonds to finish norm struct income waittime");
      } else {
        if (!UpdateUtils.get().updateUserStructLastretrievedLastupgradeIscomplete(userStruct.getId(), timeOfPurchase, null, true)) {
          log.error("problem with using diamonds to finish norm struct income waittime");
        }
        money.put(gold, goldCost);
        money.put(silver, silverCost);
      }
    }
    if (waitTimeType == NormStructWaitTimeType.FINISH_UPGRADE) {
      int goldCost = calculateDiamondCostForInstaUpgrade(userStruct, struct) * -1;
      if (!user.updateRelativeDiamondsNaive(goldCost)) {
        log.error("problem with using diamonds to finish norm struct upgrade waittime");
      } else {
        if (!UpdateUtils.get().updateUserStructLastretrievedIscompleteLevelchange(userStruct.getId(), timeOfPurchase, true, 1)) {
          log.error("problem with using diamodns to finish upgrade waittime");
        }
        money.put(gold, goldCost);
        money.put(silver, null);
      }
    }
  }

  private boolean checkLegitSpeedup(Builder resBuilder, User user, UserStruct userStruct, Timestamp timeOfSpeedup, NormStructWaitTimeType waitTimeType, Structure struct) {
    if (user == null || userStruct == null || waitTimeType == null || struct == null || userStruct.getUserId() != user.getId() || userStruct.isComplete()) {
      resBuilder.setStatus(FinishNormStructWaittimeStatus.OTHER_FAIL);
      log.error("something passed in is null. user=" + user + ", userStruct=" + userStruct + ", waitTimeType="
          + waitTimeType + ", struct=" + struct + ", struct owner's id=" + userStruct.getUserId());
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(timeOfSpeedup)) {
      resBuilder.setStatus(FinishNormStructWaittimeStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + timeOfSpeedup + ", servertime~="
          + new Date());
      return false;
    }

    //TODO:
    if (timeOfSpeedup.getTime() < userStruct.getPurchaseTime().getTime()) {
      resBuilder.setStatus(FinishNormStructWaittimeStatus.OTHER_FAIL);
      log.error("time passed in is before time user struct was purchased. timeOfSpeedup=" + timeOfSpeedup
          + ", struct was purchased=" + userStruct.getPurchaseTime());
      return false;
    }
    
    
    int diamondCost;
    if (waitTimeType == NormStructWaitTimeType.FINISH_CONSTRUCTION) {
      diamondCost = struct.getInstaBuildDiamondCost();
    } else if (waitTimeType == NormStructWaitTimeType.FINISH_INCOME_WAITTIME) {
      diamondCost = calculateDiamondCostForInstaRetrieve(userStruct, struct);
    } else if (waitTimeType == NormStructWaitTimeType.FINISH_UPGRADE) {
      diamondCost = calculateDiamondCostForInstaUpgrade(userStruct, struct);
    } else {
      resBuilder.setStatus(FinishNormStructWaittimeStatus.OTHER_FAIL);
      log.error("norm struct wait time type is unknown: " + waitTimeType);
      return false;
    }
    if (user.getDiamonds() < diamondCost) {
      resBuilder.setStatus(FinishNormStructWaittimeStatus.NOT_ENOUGH_DIAMONDS);
      log.error("user doesn't have enough diamonds. has " + user.getDiamonds() +", needs " + diamondCost);
      return false;
    }
    resBuilder.setStatus(FinishNormStructWaittimeStatus.SUCCESS);
    return true;  
  }
  
  private int calculateDiamondCostForInstaRetrieve(UserStruct userStruct, Structure struct) {
    int result = struct.getInstaRetrieveDiamondCostBase() * userStruct.getLevel();
    return Math.max(1, result);
  }
  
  private int calculateDiamondCostForInstaUpgrade(UserStruct userStruct, Structure struct) {
    int result = (int)(struct.getInstaUpgradeDiamondCostBase() * userStruct.getLevel() * 
        ControllerConstants.FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS__DIAMOND_COST_FOR_INSTANT_UPGRADE_MULTIPLIER);
    return Math.max(1, result);
  }

  public void writeToUserCurrencyHistory(User aUser, Timestamp timeOfPurchase, Map<String, Integer> money) {
    try {
      int amount = money.size();
      String reasonForChange = ControllerConstants.UCHRFC__FINISH_NORM_STRUCT;
      
      int numInserted = 0;
      if(2 == amount) {
        Map<String, Integer> previousGoldSilver = null;
        MiscMethods.writeToUserCurrencyOneUserGoldAndSilver(aUser, timeOfPurchase, 
            money, previousGoldSilver, reasonForChange);
      } else if (1 == amount) {
        int userId = aUser.getId();
        int isSilver = 0;
        int currencyChange = money.get(gold);
        int currencyBefore = aUser.getDiamonds() - currencyChange;
        
        numInserted = InsertUtils.get().insertIntoUserCurrencyHistory(userId, timeOfPurchase, isSilver, 
            currencyChange, currencyBefore, reasonForChange);
        log.info("Should be 1. Rows inserted into user_currency_history: " + numInserted);
      }
    } catch (Exception e) {
      log.error("Maybe table's not there or duplicate keys? ", e);
    }
  }
}
