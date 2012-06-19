package com.lvl6.utils.utilmethods;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.events.response.QuestCompleteResponseEvent;
import com.lvl6.info.NeutralCityElement;
import com.lvl6.info.Quest;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserQuest;
import com.lvl6.info.UserStruct;
import com.lvl6.info.jobs.BuildStructJob;
import com.lvl6.info.jobs.PossessEquipJob;
import com.lvl6.info.jobs.UpgradeStructJob;
import com.lvl6.proto.EventProto.QuestCompleteResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BuildStructJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.NeutralCityElementsRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.PossessEquipJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.UpgradeStructJobRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.QuestGraph;
import com.lvl6.utils.RetrieveUtils;

public class QuestUtils {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public static void checkAndSendQuestsCompleteBasic(GameServer server, int userId, MinimumUserProto senderProto, 
      SpecialQuestAction justCompletedSpecialQuestAction, boolean checkOnlySpecialQuests) {
    List<UserQuest> inProgressUserQuests = UserQuestRetrieveUtils.getIncompleteUserQuestsForUser(userId);
    if (inProgressUserQuests != null) {
      for (UserQuest userQuest : inProgressUserQuests) {
        if (!userQuest.isComplete()) {
          Quest quest = QuestRetrieveUtils.getQuestForQuestId(userQuest.getQuestId());
          if (quest != null) {
            //if it's a special quest but didnt just do one, skip
            if (quest.getSpecialQuestActionRequired() != null && justCompletedSpecialQuestAction == null) {
              continue;
            }
            //if you just did a special quest only and this quest doesnt have a special quest
            if (checkOnlySpecialQuests && quest.getSpecialQuestActionRequired() == null) {
              continue;
            }
            QuestUtils.checkQuestCompleteAndMaybeSendIfJustCompleted(server, quest, userQuest, senderProto, true, justCompletedSpecialQuestAction);
          } else {
            log.error("quest for userQuest does not exist. user quest's quest is " + userQuest.getQuestId());
          }
        }
      }
    }
  }

  public static boolean checkQuestCompleteAndMaybeSendIfJustCompleted(GameServer server, Quest quest, UserQuest userQuest,
      MinimumUserProto senderProto, boolean sendCompleteMessageIfJustCompleted, SpecialQuestAction justCompletedSpecialQuestAction) {
    if (userQuest.isComplete()) return true;                        //already completed

    if (userQuest != null && userQuest.isTasksComplete() && userQuest.isDefeatTypeJobsComplete()) {
      if (userQuest.getCoinsRetrievedForReq() < quest.getCoinRetrievalAmountRequired()) return false;

      if (quest.getSpecialQuestActionRequired() != null) {
        if (justCompletedSpecialQuestAction != null && justCompletedSpecialQuestAction == quest.getSpecialQuestActionRequired()) {
          sendQuestCompleteResponseIfRequestedAndUpdateUserQuest(server, quest, userQuest, senderProto,
              sendCompleteMessageIfJustCompleted);
          return true;
        }
        return false;
      }


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
            int quantityBuilt = 0;
            if (structIdsToUserStructs.get(bsj.getStructId()) != null) {
              for (UserStruct us : structIdsToUserStructs.get(bsj.getStructId())) {
                if (us.getLastRetrieved() != null) {
                  quantityBuilt++;
                }
              }
            }
            if (quantityBuilt <  bsj.getQuantity()) {
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
              }
            }
            if (!usjComplete) {
              return false;
            }
          }
        }
      }
      if (possessEquipJobsRequired != null && possessEquipJobsRequired.size() > 0) {
        Map<Integer, UserEquip> equipIdsToUserEquips = RetrieveUtils.userEquipRetrieveUtils().getEquipIdsToUserEquipsForUser(userQuest.getUserId());
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
      sendQuestCompleteResponseIfRequestedAndUpdateUserQuest(server, quest, userQuest, senderProto,
          sendCompleteMessageIfJustCompleted);
      return true;
    }
    return false;
  }


  private static void sendQuestCompleteResponseIfRequestedAndUpdateUserQuest(GameServer server, Quest quest,
      UserQuest userQuest, MinimumUserProto senderProto,
      boolean sendCompleteMessageIfJustCompleted) {
    if (server != null && senderProto != null && sendCompleteMessageIfJustCompleted) {
      sendQuestCompleteResponse(server, senderProto, quest);
    }
    if (!userQuest.isComplete() && !UpdateUtils.get().updateUserQuestIscomplete(userQuest.getUserId(), userQuest.getQuestId())) {
      log.error("problem with marking user quest as complete. userquest=" + userQuest);
    }
  }

  private static void sendQuestCompleteResponse (GameServer server, MinimumUserProto senderProto, Quest quest){
    QuestCompleteResponseProto.Builder builder = QuestCompleteResponseProto.newBuilder().setSender(senderProto)
        .setQuestId(quest.getId());
    NeutralCityElement neutralCityElement = NeutralCityElementsRetrieveUtils.getNeutralCityElement(quest.getCityId(), quest.getAssetNumWithinCity());
    if (neutralCityElement != null) {
      builder.setNeutralCityElement(CreateInfoProtoUtils.createNeutralCityElementProtoFromNeutralCityElement(neutralCityElement, senderProto.getUserType()));
    }
    QuestCompleteResponseEvent event = new QuestCompleteResponseEvent(senderProto.getUserId());
    event.setQuestCompleteResponseProto(builder.build());
    server.writeEvent(event);
  }

  public static List<Integer> getAvailableQuestsForUser(List<Integer> redeemed, List<Integer> inProgress) {
    QuestGraph graph = QuestRetrieveUtils.getQuestGraph();
    return graph.getQuestsAvailable(redeemed, inProgress);
  }


}
