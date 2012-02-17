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

  public static void checkAndSendQuestsCompleteBasic(GameServer server, int userId, MinimumUserProto senderProto) {
    List<UserQuest> inProgressUserQuests = UserQuestRetrieveUtils.getInProgressUserQuestsForUser(userId);
    if (inProgressUserQuests != null) {
      for (UserQuest userQuest : inProgressUserQuests) {
        Quest quest = QuestRetrieveUtils.getQuestForQuestId(userQuest.getQuestId());
        if (quest != null) {
          QuestUtils.checkAndSendQuestComplete(server, quest, userQuest, senderProto, true);
        }
      }
    }
  }

  public static boolean checkAndSendQuestComplete(GameServer server, Quest quest, UserQuest userQuest,
      MinimumUserProto senderProto, boolean sendCompleteMessage) {
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
          for (BuildStructJob bsj : bsjs.values()) {
            if (structIdsToUserStructs.get(bsj.getStructId()) == null || structIdsToUserStructs.get(bsj.getStructId()).size() < bsj.getQuantity()) {
              return false;
            }
          }
        }

        if (upgradeStructJobsRequired != null && upgradeStructJobsRequired.size()>0) {
          Map<Integer, UpgradeStructJob> usjs = UpgradeStructJobRetrieveUtils.getUpgradeStructJobsForUpgradeStructJobIds(upgradeStructJobsRequired);
          for (UpgradeStructJob usj : usjs.values()) {
            if (structIdsToUserStructs.get(usj.getStructId()) == null) {
              return false;
            }
            boolean usjComplete = false;
            for (UserStruct us : structIdsToUserStructs.get(usj.getStructId())) {
              if (us.getLevel() >= usj.getLevelReq()) {
                usjComplete = true;
                break;
              }
            }
            if (!usjComplete) {
              return false;
            }
          }
        }
      }
      if (possessEquipJobsRequired != null && possessEquipJobsRequired.size() > 0) {
        Map<Integer, UserEquip> equipIdsToUserEquips = UserEquipRetrieveUtils.getEquipIdsToUserEquipsForUser(userQuest.getUserId());
        if (equipIdsToUserEquips == null || equipIdsToUserEquips.size() <= 0) {
          return false;
        }
        Map<Integer, PossessEquipJob> pejs = PossessEquipJobRetrieveUtils.getPossessEquipJobsForPossessEquipJobIds(possessEquipJobsRequired);
        for (PossessEquipJob pej : pejs.values()) {
          if (equipIdsToUserEquips.get(pej.getEquipId()) == null || equipIdsToUserEquips.get(pej.getEquipId()).getQuantity() < pej.getQuantity()) {
            return false;
          }
        }
      }
      if (server != null && senderProto != null && sendCompleteMessage) {
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
