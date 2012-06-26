package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.QuestAcceptRequestEvent;
import com.lvl6.events.response.QuestAcceptResponseEvent;
import com.lvl6.info.Quest;
import com.lvl6.info.User;
import com.lvl6.info.UserQuest;
import com.lvl6.proto.EventProto.QuestAcceptRequestProto;
import com.lvl6.proto.EventProto.QuestAcceptResponseProto;
import com.lvl6.proto.EventProto.QuestAcceptResponseProto.Builder;
import com.lvl6.proto.EventProto.QuestAcceptResponseProto.QuestAcceptStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;

  @Component @DependsOn("gameServer") public class QuestAcceptController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  
  
  @Autowired
  protected InsertUtil insertUtils;

  public void setInsertUtils(InsertUtil insertUtils) {
	this.insertUtils = insertUtils;
  }

  
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
  protected void processRequestEvent(RequestEvent event) throws Exception {
    QuestAcceptRequestProto reqProto = ((QuestAcceptRequestEvent)event).getQuestAcceptRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int questId = reqProto.getQuestId();

    QuestAcceptResponseProto.Builder resBuilder = QuestAcceptResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      Quest quest = QuestRetrieveUtils.getQuestForQuestId(questId);

      boolean legitAccept = checkLegitAccept(resBuilder, user, quest);

      if (legitAccept) resBuilder.setCityIdOfAcceptedQuest(quest.getCityId());
      
      QuestAcceptResponseEvent resEvent = new QuestAcceptResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setQuestAcceptResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitAccept) {
        boolean tasksComplete = (quest.getTasksRequired() == null || quest.getTasksRequired().size() == 0);
        
        boolean defeatTypeJobsComplete = true;
        boolean goodSide = MiscMethods.checkIfGoodSide(user.getType());
        List<Integer> defeatTypeJobIds = (goodSide) ? quest.getDefeatBadGuysJobsRequired()
            : quest.getDefeatGoodGuysJobsRequired();
        if (defeatTypeJobIds != null && defeatTypeJobIds.size() > 0) defeatTypeJobsComplete = false;
        
        if (quest.getSpecialQuestActionRequired() != null) {
          tasksComplete = true;
          defeatTypeJobsComplete = true;
        }
        
        UserQuest uq = new UserQuest(user.getId(), quest.getId(), false, false, tasksComplete, defeatTypeJobsComplete, 0);
        writeChangesToDB(uq);
        QuestUtils.checkQuestCompleteAndMaybeSendIfJustCompleted(server, quest, uq, senderProto, true, null);
      }

    } catch (Exception e) {
      log.error("exception in QuestAccept processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(UserQuest uq) {
    if (!insertUtils.insertUnredeemedUserQuest(uq.getUserId(), uq.getQuestId(), uq.isTasksComplete(), uq.isDefeatTypeJobsComplete())) {
      log.error("problem with inserting unredeemd user quest: " + uq);
    }
  }

  private boolean checkLegitAccept(Builder resBuilder, User user, Quest quest) {
    if (user == null || quest == null) {
      resBuilder.setStatus(QuestAcceptStatus.OTHER_FAIL);
      log.error("parameter passed in is null. user=" + user + ", quest=" + quest);
      return false;
    }
    List<UserQuest> inProgressAndRedeemedUserQuests = RetrieveUtils.userQuestRetrieveUtils().getUnredeemedAndRedeemedUserQuestsForUser(user.getId());
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
        log.error("quest with id " + quest.getId() + " is not available to user");
        return false;
      }
    }
    resBuilder.setStatus(QuestAcceptStatus.OTHER_FAIL);
    return false;
  }
}
