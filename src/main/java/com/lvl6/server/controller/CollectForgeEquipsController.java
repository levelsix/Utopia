package com.lvl6.server.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.CollectForgeEquipsRequestEvent;
import com.lvl6.events.response.CollectForgeEquipsResponseEvent;
import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.CollectForgeEquipsRequestProto;
import com.lvl6.proto.EventProto.CollectForgeEquipsResponseProto;
import com.lvl6.proto.EventProto.CollectForgeEquipsResponseProto.Builder;
import com.lvl6.proto.EventProto.CollectForgeEquipsResponseProto.CollectForgeEquipsStatus;
import com.lvl6.proto.EventProto.MenteeFinishedQuestResponseProto.MenteeQuestType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UnhandledBlacksmithAttemptRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.QuestUtils;

@Component @DependsOn("gameServer") public class CollectForgeEquipsController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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
    resBuilder.setBlacksmithId(blacksmithId);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      Map<Integer, BlacksmithAttempt> blacksmithIdToBlacksmithAttempt = 
          UnhandledBlacksmithAttemptRetrieveUtils.getUnhandledBlacksmithAttemptsForUser(senderProto.getUserId());

      boolean legitCollection = checkLegitCollection(resBuilder, blacksmithId, blacksmithIdToBlacksmithAttempt, user);

      BlacksmithAttempt blacksmithAttempt = null;
      boolean successfulForge = false;

      if (legitCollection) {
        blacksmithAttempt = blacksmithIdToBlacksmithAttempt.get(blacksmithId);
        successfulForge = checkIfSuccessfulForge(blacksmithAttempt, EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(blacksmithAttempt.getEquipId()));
        if (successfulForge) {
          //forging enhanced weapons deletes the enhancement percentages
          int newUserEquipId = InsertUtils.get().insertUserEquip(user.getId(), blacksmithAttempt.getEquipId(), blacksmithAttempt.getGoalLevel(),
              ControllerConstants.DEFAULT_USER_EQUIP_ENHANCEMENT_PERCENT);
          if (newUserEquipId < 0) {
            resBuilder.setStatus(CollectForgeEquipsStatus.OTHER_FAIL);
            log.error("problem with giving 1 of equip " + blacksmithAttempt.getEquipId() + " to forger " + user.getId());
            legitCollection = false;
          } else {
            resBuilder.addUserEquips(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
                new UserEquip(newUserEquipId, user.getId(), blacksmithAttempt.getEquipId(), blacksmithAttempt.getGoalLevel(), 0)));
          }
          
          //forge quest
          QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, SpecialQuestAction.SUCCESSFULLY_FORGE_AN_ITEM, true);
        } else {
          //retain the equip enhancement percents
          int equipOneEnhancementPercent = blacksmithAttempt.getEquipOneEnhancementPercent();
          int equipTwoEnhancementPercent = blacksmithAttempt.getEquipTwoEnhancementPercent();
          int newUserEquipId1 = InsertUtils.get().insertUserEquip(user.getId(), blacksmithAttempt.getEquipId(), blacksmithAttempt.getGoalLevel() - 1,
              equipOneEnhancementPercent);
          int newUserEquipId2 = InsertUtils.get().insertUserEquip(user.getId(), blacksmithAttempt.getEquipId(), blacksmithAttempt.getGoalLevel() - 1,
              equipTwoEnhancementPercent);
          
          if (newUserEquipId1 < 0 || newUserEquipId2 < 0) {
            resBuilder.setStatus(CollectForgeEquipsStatus.OTHER_FAIL);
            log.error("problem with giving 2 of equip " + blacksmithAttempt.getEquipId() + " to forger " + user.getId() + " at level " + (blacksmithAttempt.getGoalLevel() - 1));
            legitCollection = false;
          } else {
              resBuilder.addUserEquips(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
                new UserEquip(newUserEquipId1, user.getId(), blacksmithAttempt.getEquipId(), blacksmithAttempt.getGoalLevel() - 1, equipOneEnhancementPercent)));
              resBuilder.addUserEquips(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
                new UserEquip(newUserEquipId2, user.getId(), blacksmithAttempt.getEquipId(), blacksmithAttempt.getGoalLevel() - 1, equipTwoEnhancementPercent)));
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

      updateMentorshipQuests(senderProto, successfulForge, blacksmithAttempt);
      
    } catch (Exception e) {
      log.error("exception in CollectForgeEquips processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private void writeChangesToDB(BlacksmithAttempt blacksmithAttempt,
      boolean successfulForge) {
    if (!InsertUtils.get().insertForgeAttemptIntoBlacksmithHistory(blacksmithAttempt, successfulForge)) {
      log.error("problem with inserting blacksmith attempt into history, blacksmith attempt=" + blacksmithAttempt + ", successfulForge=" + successfulForge);
    } 
    if (!DeleteUtils.get().deleteBlacksmithAttempt(blacksmithAttempt.getId())) {
      log.error("problem with deleting blacksmith attempt");
    }
  }

  private boolean checkIfSuccessfulForge(BlacksmithAttempt blacksmithAttempt, Equipment equipment) {
    if (blacksmithAttempt.isGuaranteed())
      return true;
    
    float chanceOfSuccess = MiscMethods.calculateChanceOfSuccessForForge(equipment, blacksmithAttempt.getGoalLevel());

    return Math.random() <= chanceOfSuccess;
  }

  private boolean checkLegitCollection(Builder resBuilder, int blacksmithId, 
      Map<Integer, BlacksmithAttempt> blacksmithIdToBlacksmithAttempt, User user) {
    
    if (user == null || null == blacksmithIdToBlacksmithAttempt || 
        blacksmithIdToBlacksmithAttempt.isEmpty()) {
      resBuilder.setStatus(CollectForgeEquipsStatus.OTHER_FAIL);
      log.error("a parameter passed in is null or invalid. blacksmithIdToBlacksmithAttempt= "
          + blacksmithIdToBlacksmithAttempt + ", user= " + user);
      return false;
    }
    
    //check if user has at the maximum limit of equips being forged
    int numEquipsBeingForged = blacksmithIdToBlacksmithAttempt.size();
    int numEquipsUserCanForge = ControllerConstants.FORGE_DEFAULT_NUMBER_OF_FORGE_SLOTS
        + user.getNumAdditionalForgeSlots();

    //make it so that the user emails us...
    if (numEquipsBeingForged > numEquipsUserCanForge) {
      resBuilder.setStatus(CollectForgeEquipsStatus.TOO_MANY_EQUIPS_BEING_FORGED);
      log.error("unexpected error: user has more forge attempts than allowed. numAllowed=" 
          + numEquipsUserCanForge + ", numBeingForged=" + numEquipsBeingForged + 
          " blacksmithIdToBlacksmithAttempt=" + blacksmithIdToBlacksmithAttempt);
      return false;
    }
    
    BlacksmithAttempt blacksmithAttempt = null;
    if (blacksmithIdToBlacksmithAttempt.containsKey(blacksmithId)) {
      blacksmithAttempt = blacksmithIdToBlacksmithAttempt.get(blacksmithId);
    }
    if (null == blacksmithAttempt) {
      resBuilder.setStatus(CollectForgeEquipsStatus.OTHER_FAIL);
      log.error("unexpected error: no weapon being forged has specified id. blacksmithId="
          + blacksmithId + ", blacksmithIdToBlacksmithAttempt=" + blacksmithIdToBlacksmithAttempt);
      return false;
    }
    Equipment equip = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(blacksmithAttempt.getEquipId());

    if (!blacksmithAttempt.isAttemptComplete()) {
      resBuilder.setStatus(CollectForgeEquipsStatus.NOT_DONE_YET);
      log.error("user trying to collect from an incomplete forge attempt: " + blacksmithAttempt);
      return false;
    }

    if (blacksmithAttempt.getUserId() != user.getId() || equip == null) {
      resBuilder.setStatus(CollectForgeEquipsStatus.OTHER_FAIL);
      log.error("wrong blacksmith attempt. blacksmith attempt is " + blacksmithAttempt + ", blacksmith id passed in is " + blacksmithId + ", equip = " + equip);
      return false;
    }

    resBuilder.setStatus(CollectForgeEquipsStatus.SUCCESS);
    return true;  
  }
  
  //for people that have mentors check if this successful forge completes his mentors quest
  private void updateMentorshipQuests(MinimumUserProto senderProto,
      boolean successfulForge, BlacksmithAttempt blacksmithAttempt) {
    if (!successfulForge) {
      return;
    }
    
    int forgeEquipLevel = blacksmithAttempt.getGoalLevel();
    int forgeLevelForQuest = ControllerConstants.MENTORSHIPS__MENTEE_EQUIP_FORGE_LEVEL_FOR_QUEST;
    
    if (forgeEquipLevel == forgeLevelForQuest) {
      MenteeQuestType type = MenteeQuestType.FORGED_EQUIP_TO_LEVEL_N;
      MiscMethods.sendMenteeFinishedQuests(senderProto, type, server);
    }
  }
  
}