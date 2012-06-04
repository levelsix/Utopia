package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.QuestRedeemRequestEvent;
import com.lvl6.events.response.QuestRedeemResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Quest;
import com.lvl6.info.User;
import com.lvl6.info.UserQuest;
import com.lvl6.proto.EventProto.QuestRedeemRequestProto;
import com.lvl6.proto.EventProto.QuestRedeemResponseProto;
import com.lvl6.proto.EventProto.QuestRedeemResponseProto.Builder;
import com.lvl6.proto.EventProto.QuestRedeemResponseProto.QuestRedeemStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class QuestRedeemController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public QuestRedeemController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new QuestRedeemRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_QUEST_REDEEM_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    QuestRedeemRequestProto reqProto = ((QuestRedeemRequestEvent)event).getQuestRedeemRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int questId = reqProto.getQuestId();

    QuestRedeemResponseProto.Builder resBuilder = QuestRedeemResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    boolean legitRedeem = false;
    UserQuest userQuest = null;
    Quest quest = null;

    server.lockPlayer(senderProto.getUserId());

    try {
      userQuest = UserQuestRetrieveUtils.getSpecificUnredeemedUserQuest(senderProto.getUserId(), questId);
      quest = QuestRetrieveUtils.getQuestForQuestId(questId);
      legitRedeem = checkLegitRedeem(resBuilder, userQuest, quest);

      List<UserQuest> inProgressAndRedeemedUserQuests = null;
      if (legitRedeem) {
        inProgressAndRedeemedUserQuests = UserQuestRetrieveUtils.getUnredeemedAndRedeemedUserQuestsForUser(senderProto.getUserId());
        List<Integer> inProgressQuestIds = new ArrayList<Integer>();
        List<Integer> redeemedQuestIds = new ArrayList<Integer>();

        if (inProgressAndRedeemedUserQuests != null) {
          for (UserQuest uq : inProgressAndRedeemedUserQuests) {
            if (uq.isRedeemed() || uq.getQuestId() == questId) {
              redeemedQuestIds.add(uq.getQuestId());
            } else {
              inProgressQuestIds.add(uq.getQuestId());  
            }
          }
          Map<Integer, Quest> questIdsToQuests = QuestRetrieveUtils.getQuestIdsToQuests();
          List<Integer> availableQuestIds = QuestUtils.getAvailableQuestsForUser(redeemedQuestIds, inProgressQuestIds);
          for (Integer availableQuestId : availableQuestIds) {
            Quest q = questIdsToQuests.get(availableQuestId);
            if (q.getQuestsRequiredForThis().contains(questId)) {
              resBuilder.addNewlyAvailableQuests(CreateInfoProtoUtils.createFullQuestProtoFromQuest(senderProto.getUserType(), q));
            }
          }
        }
      }
      QuestRedeemResponseEvent resEvent = new QuestRedeemResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setQuestRedeemResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitRedeem) {
        User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
        writeChangesToDB(userQuest, quest, user, senderProto);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in QuestRedeem processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
    if (legitRedeem && quest != null && userQuest != null && senderProto.getUserType() != null) {
      clearUserQuestData(quest, userQuest, senderProto.getUserType());
    }

  }

  private void clearUserQuestData(Quest quest, UserQuest userQuest, UserType userType) {
    if (quest.getTasksRequired() != null && quest.getTasksRequired().size() > 0) {
      if (!DeleteUtils.deleteUserQuestInfoInTaskProgressAndCompletedTasks(userQuest.getUserId(), userQuest.getQuestId(), quest.getTasksRequired().size())) {
        log.error("problem with deleting user quest info in user quest task tables. questid=" + userQuest.getQuestId() 
            + ", num tasks it has is " + quest.getTasksRequired().size());
      }
    }
    boolean goodSide = MiscMethods.checkIfGoodSide(userType);
    List<Integer> defeatTypeJobs = null;
    if (goodSide) {
      defeatTypeJobs = quest.getDefeatBadGuysJobsRequired();
    } else {
      defeatTypeJobs = quest.getDefeatGoodGuysJobsRequired();
    }
    if (defeatTypeJobs != null && defeatTypeJobs.size() > 0) {
      if (!DeleteUtils.deleteUserQuestInfoInDefeatTypeJobProgressAndCompletedDefeatTypeJobs(userQuest.getUserId(), userQuest.getQuestId(), defeatTypeJobs.size())) {
        log.error("problem with deleting user quest info for defeat type job tables. questid=" + userQuest.getQuestId() 
            + ", num defeat type jobs it has is " + defeatTypeJobs.size());
      }
    }    
  }

  private void writeChangesToDB(UserQuest userQuest, Quest quest, User user, MinimumUserProto senderProto) {
    if (!UpdateUtils.updateRedeemUserQuest(userQuest.getUserId(), userQuest.getQuestId())) {
      log.error("problem with marking user quest as redeemed. questId=" + userQuest.getQuestId());
    }

    if (quest.getEquipIdGained() > 0) {
      if (!UpdateUtils.incrementUserEquip(userQuest.getUserId(), quest.getEquipIdGained(), 1)) {
        log.error("problem with giving user 1 reward equip after completing the quest, equipId=" 
            + quest.getEquipIdGained());
      } else {
        QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, null, false);
      }
    }

    int coinsGained = Math.max(0, quest.getCoinsGained());
    int diamondsGained = Math.max(0, quest.getDiamondsGained());
    int expGained = Math.max(0,  quest.getExpGained());
    if (!user.updateRelativeDiamondsCoinsExperienceNaive(diamondsGained, coinsGained, expGained)) {
      log.error("problem with giving user " + diamondsGained + " diamonds, " + coinsGained
          + " coins, " + expGained + " exp");
    }
  }

  private boolean checkLegitRedeem(Builder resBuilder, UserQuest userQuest, Quest quest) {
    if (userQuest == null || userQuest.isRedeemed()) {
      resBuilder.setStatus(QuestRedeemStatus.OTHER_FAIL);
      log.error("user quest is null or redeemed already. userQuest=" + userQuest);
      return false;
    }
    if (!userQuest.isComplete()) {
      resBuilder.setStatus(QuestRedeemStatus.NOT_COMPLETE);
      log.error("user quest is not complete");
      return false;
    }
    resBuilder.setStatus(QuestRedeemStatus.SUCCESS);
    return true;  
  }

}