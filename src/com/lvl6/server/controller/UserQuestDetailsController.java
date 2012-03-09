package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
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
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;

public class UserQuestDetailsController extends EventController {

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
  protected void processRequestEvent(RequestEvent event) {
    UserQuestDetailsRequestProto reqProto = ((UserQuestDetailsRequestEvent)event).getUserQuestDetailsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int questId = reqProto.getQuestId();

    UserQuestDetailsResponseProto.Builder resBuilder = UserQuestDetailsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(UserQuestDetailsStatus.SUCCESS);

    server.lockPlayer(senderProto.getUserId());
    try {
      List<UserQuest> questsToCheck = null;
      if (questId > 0) {
        UserQuest questToCheck = UserQuestRetrieveUtils.getSpecificUnredeemedUserQuest(senderProto.getUserId(), questId);
        if (questToCheck != null) {
          questsToCheck = new ArrayList<UserQuest>();
          questsToCheck.add(questToCheck);
        } else {
          resBuilder.setStatus(UserQuestDetailsStatus.SUPPLIED_QUESTID_CURRENTLY_NOT_IN_PROGRESS);
        }
      } else {
        questsToCheck = UserQuestRetrieveUtils.getInProgressUserQuestsForUser(senderProto.getUserId());
      }
      if (questsToCheck != null && questsToCheck.size() > 0) {
        addFullUserQuestDataLarges(resBuilder, questsToCheck, senderProto.getUserType());
      }
      UserQuestDetailsResponseProto resProto = resBuilder.build();
      UserQuestDetailsResponseEvent resEvent = new UserQuestDetailsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setUserQuestDetailsResponseProto(resProto);
      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in UserQuestDetailsController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId()); 
    }
  }

  private void addFullUserQuestDataLarges(Builder resBuilder, List<UserQuest> inProgressUserQuests, UserType userType) {
    Map<Integer, Quest> questIdsToQuests = QuestRetrieveUtils.getQuestIdsToQuests();
    resBuilder.addAllInProgressUserQuestData(CreateInfoProtoUtils.createFullUserQuestDataLarges(inProgressUserQuests, questIdsToQuests, userType));
  }
}
