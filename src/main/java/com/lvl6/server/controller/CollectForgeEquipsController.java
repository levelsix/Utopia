package com.lvl6.server.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.CollectForgeEquipsRequestEvent;
import com.lvl6.events.response.CollectForgeEquipsResponseEvent;
import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.CollectForgeEquipsRequestProto;
import com.lvl6.proto.EventProto.CollectForgeEquipsResponseProto;
import com.lvl6.proto.EventProto.CollectForgeEquipsResponseProto.Builder;
import com.lvl6.proto.EventProto.CollectForgeEquipsResponseProto.CollectForgeEquipsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UnhandledBlacksmithAttemptRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.QuestUtils;

@Component @DependsOn("gameServer") public class CollectForgeEquipsController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public CollectForgeEquipsController() {
    numAllocatedThreads = 3;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new CollectForgeEquipsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_COLLECT_FORGE_EQUIPS;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    CollectForgeEquipsRequestProto reqProto = ((CollectForgeEquipsRequestEvent)event).getCollectForgeEquipsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int blacksmithId = reqProto.getBlacksmithId();

    CollectForgeEquipsResponseProto.Builder resBuilder = CollectForgeEquipsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      List<BlacksmithAttempt> unhandledBlacksmithAttemptsForUser = UnhandledBlacksmithAttemptRetrieveUtils.getUnhandledBlacksmithAttemptsForUser(senderProto.getUserId());

      boolean legitCollection = checkLegitCollection(resBuilder, blacksmithId, unhandledBlacksmithAttemptsForUser, user);
      
      BlacksmithAttempt blacksmithAttempt = null;
      boolean successfulForge = false;
      
      if (legitCollection) {
        blacksmithAttempt = unhandledBlacksmithAttemptsForUser.get(0);
        successfulForge = checkIfSuccessfulForge(blacksmithAttempt, EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(blacksmithAttempt.getEquipId()));
        if (successfulForge) {
          int newUserEquipId = InsertUtils.get().insertUserEquip(user.getId(), blacksmithAttempt.getEquipId(), blacksmithAttempt.getGoalLevel());
          if (newUserEquipId < 0) {
            resBuilder.setStatus(CollectForgeEquipsStatus.OTHER_FAIL);
            log.error("problem with giving 1 of equip " + blacksmithAttempt.getEquipId() + " to forger " + user.getId());
            legitCollection = false;
          } else {
            resBuilder.addNewUserEquips(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
                new UserEquip(newUserEquipId, user.getId(), blacksmithAttempt.getEquipId(), blacksmithAttempt.getGoalLevel())));
          }
        } else {
          int newUserEquipId1 = InsertUtils.get().insertUserEquip(user.getId(), blacksmithAttempt.getEquipId(), blacksmithAttempt.getGoalLevel() - 1);
          int newUserEquipId2 = InsertUtils.get().insertUserEquip(user.getId(), blacksmithAttempt.getEquipId(), blacksmithAttempt.getGoalLevel() - 1);
          if (newUserEquipId1 < 0 || newUserEquipId2 < 0) {
            resBuilder.setStatus(CollectForgeEquipsStatus.OTHER_FAIL);
            log.error("problem with giving 2 of equip " + blacksmithAttempt.getEquipId() + " to forger " + user.getId() + " at level " + (blacksmithAttempt.getGoalLevel() - 1));
            legitCollection = false;
          } else {
            resBuilder.addNewUserEquips(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
                new UserEquip(newUserEquipId1, user.getId(), blacksmithAttempt.getEquipId(), blacksmithAttempt.getGoalLevel() - 1)));
            resBuilder.addNewUserEquips(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
                new UserEquip(newUserEquipId2, user.getId(), blacksmithAttempt.getEquipId(), blacksmithAttempt.getGoalLevel() - 1)));
          }
        }
      }
      
      CollectForgeEquipsResponseEvent resEvent = new CollectForgeEquipsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setCollectForgeEquipsResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      
      if (legitCollection) {
        writeChangesToDB(blacksmithAttempt, successfulForge);
      }
      
      QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, null, false);
      
    } catch (Exception e) {
      log.error("exception in CollectForgeEquips processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(BlacksmithAttempt blacksmithAttempt,
      boolean successfulForge) {
    if (!InsertUtils.get().insertForgeAttemptIntoBlacksmithHistory(blacksmithAttempt, successfulForge)) {
      log.error("problem with inserting blacksmith attempt into history, blacksmith attempt=" + blacksmithAttempt + ", successfulForge=" + successfulForge);
    } else {
      if (!DeleteUtils.get().deleteBlacksmithAttempt(blacksmithAttempt.getId())) {
        log.error("problem with deleting blacksmith attempt");
      }
    }
  }

  private boolean checkIfSuccessfulForge(BlacksmithAttempt blacksmithAttempt, Equipment equipment) {
    if (blacksmithAttempt.isGuaranteed())
      return true;
    
    float chanceOfSuccess = (1-equipment.getChanceOfForgeFailureBase()) - 
        ((1-equipment.getChanceOfForgeFailureBase()) / (ControllerConstants.FORGE_MAX_EQUIP_LEVEL - 1)) * 
        (blacksmithAttempt.getGoalLevel()-2);
    
    return Math.random() <= chanceOfSuccess;
  }

  private boolean checkLegitCollection(Builder resBuilder, int blacksmithId, List<BlacksmithAttempt> unhandledBlacksmithAttemptsForUser, User user) {
    if (unhandledBlacksmithAttemptsForUser == null || user == null || unhandledBlacksmithAttemptsForUser.size() != 1) {
      resBuilder.setStatus(CollectForgeEquipsStatus.OTHER_FAIL);
      log.error("a parameter passed in is null or invalid. unhandledBlacksmithAttemptsForUser= " + unhandledBlacksmithAttemptsForUser + ", user= " + user);
      return false;
    }

    BlacksmithAttempt blacksmithAttempt = unhandledBlacksmithAttemptsForUser.get(0);
    Equipment equip = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(blacksmithAttempt.getEquipId());

    if (!blacksmithAttempt.isAttemptComplete()) {
      resBuilder.setStatus(CollectForgeEquipsStatus.NOT_DONE_YET);
      log.error("user trying to collect from an incomplete forge attempt: " + blacksmithAttempt);
      return false;
    }

    if (blacksmithAttempt.getUserId() != user.getId() || blacksmithAttempt.getId() != blacksmithId || equip == null) {
      resBuilder.setStatus(CollectForgeEquipsStatus.OTHER_FAIL);
      log.error("wrong blacksmith attempt. blacksmith attempt is " + blacksmithAttempt + ", blacksmith id passed in is " + blacksmithId + ", equip = " + equip);
      return false;
    }

    resBuilder.setStatus(CollectForgeEquipsStatus.SUCCESS);
    return true;  
  }
}