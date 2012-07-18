package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.FinishForgeAttemptWaittimeWithDiamondsRequestEvent;
import com.lvl6.events.response.FinishForgeAttemptWaittimeWithDiamondsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.FinishForgeAttemptWaittimeWithDiamondsRequestProto;
import com.lvl6.proto.EventProto.FinishForgeAttemptWaittimeWithDiamondsRequestProto.ForgeAttemptWaitTimeType;
import com.lvl6.proto.EventProto.FinishForgeAttemptWaittimeWithDiamondsResponseProto;
import com.lvl6.proto.EventProto.FinishForgeAttemptWaittimeWithDiamondsResponseProto.Builder;
import com.lvl6.proto.EventProto.FinishForgeAttemptWaittimeWithDiamondsResponseProto.FinishForgeAttemptWaittimeStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class FinishForgeAttemptWaittimeWithDiamondsController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public FinishForgeAttemptWaittimeWithDiamondsController() {
    numAllocatedThreads = 2;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new FinishForgeAttemptWaittimeWithDiamondsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_FINISH_FORGE_ATTEMPT_WAITTIME_WITH_DIAMONDS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {

    FinishForgeAttemptWaittimeWithDiamondsRequestProto reqProto = ((FinishForgeAttemptWaittimeWithDiamondsRequestEvent)event).getFinishForgeAttemptWaittimeWithDiamondsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();
    Timestamp timeOfSpeedup = new Timestamp(reqProto.getTimeOfSpeedup());
    ForgeAttemptWaitTimeType waitTimeType = reqProto.getWaitTimeType();

    FinishForgeAttemptWaittimeWithDiamondsResponseProto.Builder resBuilder = FinishForgeAttemptWaittimeWithDiamondsResponseProto.newBuilder();
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

      FinishForgeAttemptWaittimeWithDiamondsResponseEvent resEvent = new FinishForgeAttemptWaittimeWithDiamondsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setFinishForgeAttemptWaittimeWithDiamondsResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitSpeedup) {
        writeChangesToDB(user, userStruct, timeOfSpeedup, waitTimeType, struct);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        if (waitTimeType == ForgeAttemptWaitTimeType.FINISH_CONSTRUCTION) {
          QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, null, false);
        }
        if (waitTimeType == ForgeAttemptWaitTimeType.FINISH_UPGRADE) {
          QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, null, false);
        }
      }
    } catch (Exception e) {
      log.error("exception in FinishForgeAttemptWaittimeWithDiamondsController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, UserStruct userStruct, Timestamp timeOfPurchase, ForgeAttemptWaitTimeType waitTimeType, Structure struct) {
    if (waitTimeType == ForgeAttemptWaitTimeType.FINISH_CONSTRUCTION) {
      if (!user.updateRelativeDiamondsNaive(struct.getInstaBuildDiamondCost()
          * -1)) {
        log.error("problem with using diamonds to finish norm struct build");
      } else {
        if (!UpdateUtils.get().updateUserStructLastretrievedLastupgradeIscomplete(userStruct.getId(), timeOfPurchase, null, true)) {
          log.error("problem with using diamonds to finish norm struct build");
        }
      }
    }
    if (waitTimeType == ForgeAttemptWaitTimeType.FINISH_INCOME_WAITTIME) {
      if (!user.updateRelativeDiamondsCoinsExperienceNaive(calculateDiamondCostForInstaRetrieve(userStruct, struct)*-1, MiscMethods.calculateIncomeGainedFromUserStruct(struct.getIncome(), userStruct.getLevel()), 0)) {
        log.error("problem with using diamonds to finish norm struct income waittime");
      } else {
        if (!UpdateUtils.get().updateUserStructLastretrievedLastupgradeIscomplete(userStruct.getId(), timeOfPurchase, null, true)) {
          log.error("problem with using diamonds to finish norm struct income waittime");
        }
      }
    }
    if (waitTimeType == ForgeAttemptWaitTimeType.FINISH_UPGRADE) {
      if (!user.updateRelativeDiamondsNaive(calculateDiamondCostForInstaUpgrade(userStruct, struct) * -1)) {
        log.error("problem with using diamonds to finish norm struct upgrade waittime");
      } else {
        if (!UpdateUtils.get().updateUserStructLastretrievedIscompleteLevelchange(userStruct.getId(), timeOfPurchase, true, 1)) {
          log.error("problem with using diamodns to finish upgrade waittime");
        }
      }
    }
  }

  private boolean checkLegitSpeedup(Builder resBuilder, User user, UserStruct userStruct, Timestamp timeOfSpeedup, ForgeAttemptWaitTimeType waitTimeType, Structure struct) {
    if (user == null || userStruct == null || waitTimeType == null || struct == null || userStruct.getUserId() != user.getId() || userStruct.isComplete()) {
      resBuilder.setStatus(FinishForgeAttemptWaittimeStatus.OTHER_FAIL);
      log.error("something passed in is null. user=" + user + ", userStruct=" + userStruct + ", waitTimeType="
          + waitTimeType + ", struct=" + struct + ", struct owner's id=" + userStruct.getUserId());
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(timeOfSpeedup)) {
      resBuilder.setStatus(FinishForgeAttemptWaittimeStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + timeOfSpeedup + ", servertime~="
          + new Date());
      return false;
    }

    //TODO:
    if (timeOfSpeedup.getTime() < userStruct.getPurchaseTime().getTime()) {
      resBuilder.setStatus(FinishForgeAttemptWaittimeStatus.OTHER_FAIL);
      log.error("time passed in is before time user struct was purchased. timeOfSpeedup=" + timeOfSpeedup
          + ", struct was purchased=" + userStruct.getPurchaseTime());
      return false;
    }
    
    
    int diamondCost;
    if (waitTimeType == ForgeAttemptWaitTimeType.FINISH_CONSTRUCTION) {
      diamondCost = struct.getInstaBuildDiamondCost();
    } else if (waitTimeType == ForgeAttemptWaitTimeType.FINISH_INCOME_WAITTIME) {
      diamondCost = calculateDiamondCostForInstaRetrieve(userStruct, struct);
    } else if (waitTimeType == ForgeAttemptWaitTimeType.FINISH_UPGRADE) {
      diamondCost = calculateDiamondCostForInstaUpgrade(userStruct, struct);
    } else {
      resBuilder.setStatus(FinishForgeAttemptWaittimeStatus.OTHER_FAIL);
      log.error("norm struct wait time type is unknown: " + waitTimeType);
      return false;
    }
    if (user.getDiamonds() < diamondCost) {
      resBuilder.setStatus(FinishForgeAttemptWaittimeStatus.NOT_ENOUGH_DIAMONDS);
      log.error("user doesn't have enough diamonds. has " + user.getDiamonds() +", needs " + diamondCost);
      return false;
    }
    resBuilder.setStatus(FinishForgeAttemptWaittimeStatus.SUCCESS);
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

}
