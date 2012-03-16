package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.QuestRedeemRequestEvent;
import com.lvl6.events.response.QuestRedeemResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Quest;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserQuest;
import com.lvl6.info.jobs.PossessEquipJob;
import com.lvl6.proto.EventProto.QuestRedeemRequestProto;
import com.lvl6.proto.EventProto.QuestRedeemResponseProto;
import com.lvl6.proto.EventProto.QuestRedeemResponseProto.Builder;
import com.lvl6.proto.EventProto.QuestRedeemResponseProto.QuestRedeemStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.PossessEquipJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class QuestRedeemController extends EventController {

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
  protected void processRequestEvent(RequestEvent event) {
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
        inProgressAndRedeemedUserQuests = UserQuestRetrieveUtils.getInProgressAndRedeemedUserQuestsForUser(senderProto.getUserId());
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
        log.error("problem with deleting user quest info in user quest task tables");
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
        log.error("problem with deleting user quest info for defeat type job tables");
      }
    }    
  }

  private void writeChangesToDB(UserQuest userQuest, Quest quest, User user, MinimumUserProto senderProto) {
    if (!UpdateUtils.updateRedeemUserQuest(userQuest.getUserId(), userQuest.getQuestId())) {
      log.error("problem with logging user quest as redeemed");
    }

    //take away equips
    List<Integer> possessEquipJobsIds = quest.getPossessEquipJobsRequired();
    if (possessEquipJobsIds != null && possessEquipJobsIds.size() > 0) {
      Map<Integer, PossessEquipJob> possessEquipJobsForPossessEquipJobIds = PossessEquipJobRetrieveUtils.getPossessEquipJobsForPossessEquipJobIds(possessEquipJobsIds);
      Map<Integer, Integer> equipIdToQuantityLost = new HashMap<Integer, Integer>();
      for (Integer possessEquipJobId : possessEquipJobsIds) {
        PossessEquipJob pej = possessEquipJobsForPossessEquipJobIds.get(possessEquipJobId);
        if (equipIdToQuantityLost.containsKey(pej.getEquipId())) {
          equipIdToQuantityLost.put(pej.getEquipId(), equipIdToQuantityLost.get(pej.getEquipId()) + pej.getQuantity());
        } else {
          equipIdToQuantityLost.put(pej.getEquipId(), pej.getQuantity());
        }
      }
      if (equipIdToQuantityLost.size() > 0) {
        Map<Integer, UserEquip> equipIdToUserEquip = UserEquipRetrieveUtils.getEquipIdsToUserEquipsForUser(userQuest.getUserId());
        for (Integer equipIdLost : equipIdToQuantityLost.keySet()) {
          if (UpdateUtils.decrementUserEquip(userQuest.getUserId(), equipIdLost, equipIdToUserEquip.get(equipIdLost).getQuantity(), equipIdToQuantityLost.get(equipIdLost))) {
            log.error("problem with taking away user equip for quest req");
          }
        }
      }
    }

    if (quest.getEquipIdGained() > 0) {
      if (!UpdateUtils.incrementUserEquip(userQuest.getUserId(), quest.getEquipIdGained(), 1)) {
        log.error("problem with giving user reward equip after completing the quest");
      } else {
        QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, null, null, null, quest.getEquipIdGained(), 1);
      }
    }

    int coinsGained = Math.max(0, quest.getCoinsGained());
    int diamondsGained = Math.max(0, quest.getDiamondsGained());
    int expGained = Math.max(0,  quest.getExpGained());
    if (!user.updateRelativeDiamondsCoinsExperienceNaive(diamondsGained, coinsGained, expGained)) {
      log.error("problem with giving user currency rewards after completing the quest");
    }
  }

  private boolean checkLegitRedeem(Builder resBuilder, UserQuest userQuest, Quest quest) {
    if (userQuest == null || userQuest.isRedeemed()) {
      resBuilder.setStatus(QuestRedeemStatus.OTHER_FAIL);
      return false;
    }
    if (!QuestUtils.checkQuestCompleteAndMaybeSend(null, quest, userQuest, null, false, null, null, null, null, null)) {
      resBuilder.setStatus(QuestRedeemStatus.NOT_COMPLETE);
      return false;
    }
    resBuilder.setStatus(QuestRedeemStatus.SUCCESS);
    return true;  
  }

}
