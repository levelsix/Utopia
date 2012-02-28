package com.lvl6.server.controller;

import java.sql.Timestamp;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.FinishNormStructWaittimeWithDiamondsRequestEvent;
import com.lvl6.events.response.FinishNormStructWaittimeWithDiamondsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.proto.EventProto.FinishNormStructWaittimeWithDiamondsRequestProto;
import com.lvl6.proto.EventProto.FinishNormStructWaittimeWithDiamondsRequestProto.NormStructWaitTimeType;
import com.lvl6.proto.EventProto.FinishNormStructWaittimeWithDiamondsResponseProto;
import com.lvl6.proto.EventProto.FinishNormStructWaittimeWithDiamondsResponseProto.Builder;
import com.lvl6.proto.EventProto.FinishNormStructWaittimeWithDiamondsResponseProto.FinishNormStructWaittimeStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class FinishNormStructWaittimeWithDiamondsController extends EventController{

  public FinishNormStructWaittimeWithDiamondsController() {
    numAllocatedThreads = 2;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new FinishNormStructWaittimeWithDiamondsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {

    FinishNormStructWaittimeWithDiamondsRequestProto reqProto = ((FinishNormStructWaittimeWithDiamondsRequestEvent)event).getFinishNormStructWaittimeWithDiamondsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();
    Timestamp timeOfPurchase = new Timestamp(reqProto.getTimeOfPurchase());
    NormStructWaitTimeType waitTimeType = reqProto.getWaitTimeType();

    FinishNormStructWaittimeWithDiamondsResponseProto.Builder resBuilder = FinishNormStructWaittimeWithDiamondsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());      
      UserStruct userStruct = UserStructRetrieveUtils.getSpecificUserStruct(userStructId);
      Structure struct = null;
      if (userStruct != null) {
        struct = StructureRetrieveUtils.getStructForStructId(userStruct.getStructId());
      }

      boolean legitBuild = checkLegitBuild(resBuilder, user, userStruct, timeOfPurchase, waitTimeType, struct);

      FinishNormStructWaittimeWithDiamondsResponseEvent resEvent = new FinishNormStructWaittimeWithDiamondsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setFinishNormStructWaittimeWithDiamondsResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitBuild) {
        writeChangesToDB(user, userStruct, timeOfPurchase, waitTimeType, struct);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto);
      }
    } catch (Exception e) {
      log.error("exception in FinishNormStructWaittimeWithDiamondsController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, UserStruct userStruct, Timestamp timeOfPurchase, NormStructWaitTimeType waitTimeType, Structure struct) {
    if (waitTimeType == NormStructWaitTimeType.FINISH_CONSTRUCTION) {
      if (!user.updateRelativeDiamondsNaive(calculateDiamondCostForInstaBuild(userStruct, struct) * -1)) {
        log.error("problem with using diamonds to finish norm struct build");
      } else {
        if (!UpdateUtils.updateUserStructLastretrievedLastupgradeIscomplete(userStruct.getId(), timeOfPurchase, null, true)) {
          log.error("problem with using diamonds to finish norm struct build");
        }
      }
    }
    if (waitTimeType == NormStructWaitTimeType.FINISH_INCOME_WAITTIME) {
      if (!user.updateRelativeDiamondsCoinsExperienceNaive(calculateDiamondCostForInstaRetrieve(userStruct, struct)*-1, MiscMethods.calculateIncomeGainedFromUserStruct(struct.getIncome(), userStruct.getLevel()), 0)) {
        log.error("problem with using diamonds to finish norm struct income waittime");
      } else {
        if (!UpdateUtils.updateUserStructLastretrievedLastupgradeIscomplete(userStruct.getId(), timeOfPurchase, null, true)) {
          log.error("problem with using diamonds to finish norm struct income waittime");
        }
      }
    }
    if (waitTimeType == NormStructWaitTimeType.FINISH_UPGRADE) {
      if (!user.updateRelativeDiamondsNaive(calculateDiamondCostForInstaUpgrade(userStruct, struct) * -1)) {
        log.error("problem with using diamonds to finish norm struct upgrade waittime");
      } else {
        if (!UpdateUtils.updateUserStructLastretrievedIscompleteLevelchange(userStruct.getId(), timeOfPurchase, true, 1)) {
          log.error("problem with using diamodns to finish upgrade waittime");
        }
      }
    }
  }

  private boolean checkLegitBuild(Builder resBuilder, User user, UserStruct userStruct, Timestamp timeOfPurchase, NormStructWaitTimeType waitTimeType, Structure struct) {
    if (user == null || userStruct == null || waitTimeType == null || struct == null || userStruct.getUserId() != user.getId() || userStruct.isComplete()) {
      resBuilder.setStatus(FinishNormStructWaittimeStatus.OTHER_FAIL);
      return false;
    }
    if (!MiscMethods.checkClientTimeBeforeApproximateNow(timeOfPurchase)) {
      resBuilder.setStatus(FinishNormStructWaittimeStatus.CLIENT_TOO_AHEAD_OF_SERVER_TIME);
      return false;
    }
    if (timeOfPurchase.getTime() < userStruct.getPurchaseTime().getTime()) {
      resBuilder.setStatus(FinishNormStructWaittimeStatus.OTHER_FAIL);
      return false;
    }
    int diamondCost;
    if (waitTimeType == NormStructWaitTimeType.FINISH_CONSTRUCTION) {
      diamondCost = calculateDiamondCostForInstaBuild(userStruct, struct);
    } else if (waitTimeType == NormStructWaitTimeType.FINISH_INCOME_WAITTIME) {
      diamondCost = calculateDiamondCostForInstaRetrieve(userStruct, struct);
    } else if (waitTimeType == NormStructWaitTimeType.FINISH_UPGRADE) {
      diamondCost = calculateDiamondCostForInstaUpgrade(userStruct, struct);
    } else {
      resBuilder.setStatus(FinishNormStructWaittimeStatus.OTHER_FAIL);
      return false;
    }
    if (user.getDiamonds() < diamondCost) {
      resBuilder.setStatus(FinishNormStructWaittimeStatus.NOT_ENOUGH_DIAMONDS);
      return false;
    }
    resBuilder.setStatus(FinishNormStructWaittimeStatus.SUCCESS);
    return true;  
  }

  private int calculateDiamondCostForInstaBuild(UserStruct userStruct, Structure struct) {
    int result = struct.getInstaBuildDiamondCostBase() * userStruct.getLevel();
    return Math.max(1, result);
  }

  private int calculateDiamondCostForInstaRetrieve(UserStruct userStruct, Structure struct) {
    int result = struct.getInstaRetrieveDiamondCostBase() * userStruct.getLevel();
    return Math.max(1, result);
  }
  
  private int calculateDiamondCostForInstaUpgrade(UserStruct userStruct, Structure struct) {
    int result = struct.getInstaUpgradeDiamondCostBase() * userStruct.getLevel();
    return Math.max(1, result);
  }

}
