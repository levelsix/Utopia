package com.lvl6.utils.utilmethods;

import java.util.List;
import java.util.Map;

import com.lvl6.events.response.QuestCompleteResponseEvent;
import com.lvl6.info.Quest;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserQuest;
import com.lvl6.info.UserStruct;
import com.lvl6.info.jobs.BuildStructJob;
import com.lvl6.info.jobs.PossessEquipJob;
import com.lvl6.info.jobs.UpgradeStructJob;
import com.lvl6.proto.EventProto.QuestCompleteResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BuildStructJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.PossessEquipJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.UpgradeStructJobRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.QuestGraph;

public class QuestUtils {

  public static void checkAndSendQuestsCompleteBasic(GameServer server, int userId, MinimumUserProto senderProto, 
      Integer justBuiltStructId, Integer justUpgradedStructId, Integer justUpgradedStructLevel, 
      Integer justObtainedEquipId, Integer justObtainedEquipQuantity) {
    List<UserQuest> inProgressUserQuests = UserQuestRetrieveUtils.getInProgressUserQuestsForUser(userId);
    if (inProgressUserQuests != null) {
      for (UserQuest userQuest : inProgressUserQuests) {
        Quest quest = QuestRetrieveUtils.getQuestForQuestId(userQuest.getQuestId());
        if (quest != null) {
          QuestUtils.checkQuestCompleteAndMaybeSend(server, quest, userQuest, senderProto, true, 
              justBuiltStructId, justUpgradedStructId, justUpgradedStructLevel, justObtainedEquipId, 
              justObtainedEquipQuantity);
        }
      }
    }
  }

  public static boolean checkQuestCompleteAndMaybeSend(GameServer server, Quest quest, UserQuest userQuest,
      MinimumUserProto senderProto, boolean sendCompleteMessageIfJustCompleted, 
      Integer justBuiltStructId, Integer justUpgradedStructId, Integer justUpgradedStructLevel, 
      Integer justObtainedEquipId, Integer justObtainedEquipQuantity) {

    boolean sendMessage = false;

    if (sendCompleteMessageIfJustCompleted && 
        justBuiltStructId == null && justUpgradedStructId == null && justUpgradedStructLevel == null && 
        justObtainedEquipId == null) {    //came from task or defeat job type
      sendMessage = true;
    }

    /* for tasks and defeattypejobs, doing those triggers this. 
     * if justBuiltStructId isnt null and it didnt cause the quest completion, sendMessage = false
     * if justObtainedEquip isnt null and it didnt cause the quest completion, sendMessage = false
     * if justUpgradedStructId isnt null and it didnt cause the quest completion, sendMessage = false
     *    and level
     */

    if (userQuest != null && userQuest.isTasksComplete() && userQuest.isDefeatTypeJobsComplete()) {
      List<Integer> buildStructJobsRequired = quest.getBuildStructJobsRequired();
      List<Integer> upgradeStructJobsRequired = quest.getUpgradeStructJobsRequired();
      List<Integer> possessEquipJobsRequired = quest.getPossessEquipJobsRequired();

      if ((buildStructJobsRequired != null && buildStructJobsRequired.size()>0) || 
          (upgradeStructJobsRequired != null && upgradeStructJobsRequired.size()>0)) {
        Map<Integer, List<UserStruct>> structIdsToUserStructs = UserStructRetrieveUtils.getStructIdsToUserStructsForUser(userQuest.getUserId());
        if (structIdsToUserStructs == null || structIdsToUserStructs.size() <= 0) {
          return false;
        }

        if (buildStructJobsRequired != null && buildStructJobsRequired.size()>0) {
          Map<Integer, BuildStructJob> bsjs = BuildStructJobRetrieveUtils.getBuildStructJobsForBuildStructJobIds(buildStructJobsRequired);

          boolean justCompletedBuildStructJob = false;
          for (BuildStructJob bsj : bsjs.values()) {
            if (structIdsToUserStructs.get(bsj.getStructId()) == null || structIdsToUserStructs.get(bsj.getStructId()).size() < bsj.getQuantity()) {
              return false;
            } else {
              if (justBuiltStructId == bsj.getStructId() && structIdsToUserStructs.get(bsj.getStructId()).size() == bsj.getQuantity()) {
                justCompletedBuildStructJob = true;
              }
            }
          }
          if (justCompletedBuildStructJob && sendCompleteMessageIfJustCompleted) {
            sendMessage = true;
          }
        }

        if (upgradeStructJobsRequired != null && upgradeStructJobsRequired.size()>0) {
          Map<Integer, UpgradeStructJob> usjs = UpgradeStructJobRetrieveUtils.getUpgradeStructJobsForUpgradeStructJobIds(upgradeStructJobsRequired);
          
          boolean justCompletedUpgradeStructJob = false;
          for (UpgradeStructJob usj : usjs.values()) {
            if (structIdsToUserStructs.get(usj.getStructId()) == null) {
              return false;
            }
            boolean usjComplete = false;
            int numStructsThatJustCompletedThisUpgradeStructJob = 0;
            for (UserStruct us : structIdsToUserStructs.get(usj.getStructId())) {
              if (us.getLevel() >= usj.getLevelReq()) {
                usjComplete = true;
              }
              if (usj.getLevelReq() == justUpgradedStructLevel && usj.getStructId() == justUpgradedStructId) numStructsThatJustCompletedThisUpgradeStructJob++;
            }
            if (numStructsThatJustCompletedThisUpgradeStructJob == 1) justCompletedUpgradeStructJob = true;
            if (!usjComplete) {
              return false;
            }
          }
          if (justCompletedUpgradeStructJob && sendCompleteMessageIfJustCompleted) {
            sendMessage = true;
          }
        }
      }
      if (possessEquipJobsRequired != null && possessEquipJobsRequired.size() > 0) {
        Map<Integer, UserEquip> equipIdsToUserEquips = UserEquipRetrieveUtils.getEquipIdsToUserEquipsForUser(userQuest.getUserId());
        if (equipIdsToUserEquips == null || equipIdsToUserEquips.size() <= 0) {
          return false;
        }
        Map<Integer, PossessEquipJob> pejs = PossessEquipJobRetrieveUtils.getPossessEquipJobsForPossessEquipJobIds(possessEquipJobsRequired);
        boolean justCompletedPossessEquipJob = false;
        for (PossessEquipJob pej : pejs.values()) {
          if (equipIdsToUserEquips.get(pej.getEquipId()) == null || equipIdsToUserEquips.get(pej.getEquipId()).getQuantity() < pej.getQuantity()) {
            return false;
          } else {
            if (justObtainedEquipId == pej.getEquipId() && justObtainedEquipQuantity != null && 
                equipIdsToUserEquips.get(pej.getEquipId()).getQuantity() - justObtainedEquipQuantity <  pej.getQuantity()) {
              justCompletedPossessEquipJob = true;
            }
          }
        }
        if (justCompletedPossessEquipJob && sendCompleteMessageIfJustCompleted) {
          sendMessage = true;
        }
      }
      if (server != null && senderProto != null && sendMessage) {
        sendQuestCompleteResponse(server, senderProto, quest);      
      }
      return true;
    }
    return false;
  }

  public static void sendQuestCompleteResponse (GameServer server, MinimumUserProto senderProto, Quest quest){
    QuestCompleteResponseEvent event = new QuestCompleteResponseEvent(senderProto.getUserId());
    event.setQuestCompleteResponseProto(QuestCompleteResponseProto.newBuilder().setSender(senderProto)
        .setQuest(CreateInfoProtoUtils.createFullQuestProtoFromQuest(senderProto.getUserType(), quest)).build());
    server.writeEvent(event);
  }

  public static List<Integer> getAvailableQuestsForUser(List<Integer> redeemed, List<Integer> inProgress) {
    QuestGraph graph = QuestRetrieveUtils.getQuestGraph();
    return graph.getQuestsAvailable(redeemed, inProgress);
  }


}
