package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.QuestAcceptRequestEvent;
import com.lvl6.events.response.QuestAcceptResponseEvent;
import com.lvl6.info.Quest;
import com.lvl6.info.User;
import com.lvl6.info.UserQuest;
import com.lvl6.info.jobs.DefeatTypeJob;
import com.lvl6.proto.EventProto.QuestAcceptRequestProto;
import com.lvl6.proto.EventProto.QuestAcceptResponseProto;
import com.lvl6.proto.EventProto.QuestAcceptResponseProto.Builder;
import com.lvl6.proto.EventProto.QuestAcceptResponseProto.QuestAcceptStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.DefeatTypeJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;

public class QuestAcceptController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public QuestAcceptController() {
    numAllocatedThreads = 5;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new QuestAcceptRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_QUEST_ACCEPT_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    QuestAcceptRequestProto reqProto = ((QuestAcceptRequestEvent)event).getQuestAcceptRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int questId = reqProto.getQuestId();

    QuestAcceptResponseProto.Builder resBuilder = QuestAcceptResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      Quest quest = QuestRetrieveUtils.getQuestForQuestId(questId);

      boolean legitAccept = checkLegitAccept(resBuilder, user, quest);

      boolean defeatTypeJobsComplete = true;
      if (legitAccept) {
        resBuilder.setCityIdOfAcceptedQuest(quest.getCityId());
        boolean goodSide = MiscMethods.checkIfGoodSide(user.getType());
        List<Integer> defeatTypeJobIds = (goodSide) ? quest.getDefeatBadGuysJobsRequired()
            : quest.getDefeatGoodGuysJobsRequired();
        if (defeatTypeJobIds != null && defeatTypeJobIds.size() > 0) {
          defeatTypeJobsComplete = false;
          Map<Integer, DefeatTypeJob> defeatTypeJobIdsToDefeatTypeJobs = DefeatTypeJobRetrieveUtils.getDefeatTypeJobsForDefeatTypeJobIds(defeatTypeJobIds);
          if (defeatTypeJobIdsToDefeatTypeJobs != null) {
            Map<UserType, Integer> numToGenerate = new HashMap<UserType, Integer>();
            for (DefeatTypeJob dtj : defeatTypeJobIdsToDefeatTypeJobs.values()) {
              numToGenerate.put(dtj.getEnemyType(), dtj.getNumEnemiesToDefeat());
            }
            for (UserType type : numToGenerate.keySet()) {
              List<UserType> temp = new ArrayList<UserType>();
              temp.add(type);
              List<User> users = UserRetrieveUtils.getUsers(temp,
                  numToGenerate.get(type), user.getLevel(), user.getId(), true, null, null, null, null, true);
              if (users != null) {
                for (User u : users) {
                  resBuilder.addEnemiesIfQuestsHaveDefeatTypeJob(CreateInfoProtoUtils.createFullUserProtoFromUser(u));
                }
              }
            }
          }
        }
      }
      
      QuestAcceptResponseEvent resEvent = new QuestAcceptResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setQuestAcceptResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitAccept) {
        boolean tasksComplete = (quest.getTasksRequired() == null || quest.getTasksRequired().size() == 0);
        UserQuest uq = new UserQuest(user.getId(), quest.getId(), false, tasksComplete, defeatTypeJobsComplete, false);
        writeChangesToDB(uq);
        QuestUtils.checkQuestCompleteAndMaybeSend(server, quest, uq, senderProto, true, null, null, null, null, null);
      }

    } catch (Exception e) {
      log.error("exception in QuestAccept processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(UserQuest uq) {
    if (!InsertUtils.insertUnredeemedUserQuest(uq.getUserId(), uq.getQuestId(), uq.isTasksComplete(), uq.isDefeatTypeJobsComplete())) {
      log.error("problem with inserting user quest");
    }
  }

  private boolean checkLegitAccept(Builder resBuilder, User user, Quest quest) {
    if (user == null || quest == null) {
      resBuilder.setStatus(QuestAcceptStatus.OTHER_FAIL);
      return false;
    }
    List<UserQuest> inProgressAndRedeemedUserQuests = UserQuestRetrieveUtils.getUnredeemedAndRedeemedUserQuestsForUser(user.getId());
    List<Integer> inProgressQuestIds = new ArrayList<Integer>();
    List<Integer> redeemedQuestIds = new ArrayList<Integer>();

    if (inProgressAndRedeemedUserQuests != null) {
      for (UserQuest uq : inProgressAndRedeemedUserQuests) {
        if (uq.isRedeemed()) {
          redeemedQuestIds.add(uq.getQuestId());
        } else {
          inProgressQuestIds.add(uq.getQuestId());  
        }
      }
      List<Integer> availableQuestIds = QuestUtils.getAvailableQuestsForUser(redeemedQuestIds, inProgressQuestIds);
      if (availableQuestIds != null && availableQuestIds.contains(quest.getId())) {
        resBuilder.setStatus(QuestAcceptStatus.SUCCESS);
        return true;
      } else {
        resBuilder.setStatus(QuestAcceptStatus.NOT_AVAIL_TO_USER);
        return false;
      }
    }
    resBuilder.setStatus(QuestAcceptStatus.OTHER_FAIL);
    return false;
  }
}
