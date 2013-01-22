package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.events.request.UserQuestDetailsRequestEvent;
import com.lvl6.events.response.UserQuestDetailsResponseEvent;
import com.lvl6.info.Quest;
import com.lvl6.info.UserQuest;
import com.lvl6.proto.EventProto.UserQuestDetailsRequestProto;
import com.lvl6.proto.EventProto.UserQuestDetailsResponseProto;
import com.lvl6.proto.EventProto.UserQuestDetailsResponseProto.Builder;
import com.lvl6.proto.EventProto.UserQuestDetailsResponseProto.UserQuestDetailsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;

  @Component @DependsOn("gameServer") public class UserQuestDetailsController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  public UserQuestDetailsController() {
    numAllocatedThreads = 8;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new UserQuestDetailsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_USER_QUEST_DETAILS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    UserQuestDetailsRequestProto reqProto = ((UserQuestDetailsRequestEvent)event).getUserQuestDetailsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int questId = reqProto.getQuestId();
    log.debug("Processing UserQuestDetailsRequest");
    UserQuestDetailsResponseProto.Builder resBuilder = UserQuestDetailsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(UserQuestDetailsStatus.SUCCESS);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      List<UserQuest> questsToCheck = null;
      if (questId > 0) {
        UserQuest questToCheck = RetrieveUtils.userQuestRetrieveUtils().getSpecificUnredeemedUserQuest(senderProto.getUserId(), questId);
        if (questToCheck != null) {
          questsToCheck = new ArrayList<UserQuest>();
          questsToCheck.add(questToCheck);
        } else {
          resBuilder.setStatus(UserQuestDetailsStatus.SUPPLIED_QUESTID_CURRENTLY_NOT_IN_PROGRESS);
          log.error("questId " + questId + "wasn't in progress for user before redeem attempt");
        }
      } else {
        questsToCheck = RetrieveUtils.userQuestRetrieveUtils().getUnredeemedUserQuestsForUser(senderProto.getUserId());
      }
      if (questsToCheck != null && questsToCheck.size() > 0) {
        addFullUserQuestDataLarges(resBuilder, questsToCheck, senderProto.getUserType());
      }
      UserQuestDetailsResponseProto resProto = resBuilder.build();
      UserQuestDetailsResponseEvent resEvent = new UserQuestDetailsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setUserQuestDetailsResponseProto(resProto);
      log.debug("Sending response for UserQuestDetailsResponse");
      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in UserQuestDetailsController processEvent", e);
    } finally {
    	log.debug("Unlocking player "+ senderProto.getUserId());
      server.unlockPlayer(senderProto.getUserId()); 
    }
  }

  private void addFullUserQuestDataLarges(Builder resBuilder, List<UserQuest> inProgressUserQuests, UserType userType) {
    Map<Integer, Quest> questIdsToQuests = QuestRetrieveUtils.getQuestIdsToQuests();
    resBuilder.addAllInProgressUserQuestData(CreateInfoProtoUtils.createFullUserQuestDataLarges(inProgressUserQuests, questIdsToQuests, userType));
  }
}
