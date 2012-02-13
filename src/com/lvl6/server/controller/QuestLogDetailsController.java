package com.lvl6.server.controller;

import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.QuestLogDetailsRequestEvent;
import com.lvl6.events.response.QuestLogDetailsResponseEvent;
import com.lvl6.info.Quest;
import com.lvl6.info.UserQuest;
import com.lvl6.proto.EventProto.QuestLogDetailsRequestProto;
import com.lvl6.proto.EventProto.QuestLogDetailsResponseProto;
import com.lvl6.proto.EventProto.QuestLogDetailsResponseProto.Builder;
import com.lvl6.proto.EventProto.QuestLogDetailsResponseProto.QuestLogDetailsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;

public class QuestLogDetailsController extends EventController {

  @Override
  public RequestEvent createRequestEvent() {
    return new QuestLogDetailsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_QUEST_LOG_DETAILS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    QuestLogDetailsRequestProto reqProto = ((QuestLogDetailsRequestEvent)event).getQuestLogDetailsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    QuestLogDetailsResponseProto.Builder resBuilder = QuestLogDetailsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(QuestLogDetailsStatus.SUCCESS);

    server.lockPlayer(senderProto.getUserId());
    try {

      List<UserQuest> inProgressUserQuests = UserQuestRetrieveUtils.getInProgressUserQuestsForUser(senderProto.getUserId());
      if (inProgressUserQuests != null && inProgressUserQuests.size() > 0) {
        addFullUserQuestDataLarges(resBuilder, inProgressUserQuests, senderProto.getUserType());
      }
      QuestLogDetailsResponseProto resProto = resBuilder.build();
      QuestLogDetailsResponseEvent resEvent = new QuestLogDetailsResponseEvent(senderProto.getUserId());
      resEvent.setQuestLogDetailsResponseProto(resProto);
      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in QuestLogDetailsController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId()); 
    }
  }

  private void addFullUserQuestDataLarges(Builder resBuilder, List<UserQuest> inProgressUserQuests, UserType userType) {
    Map<Integer, Quest> questIdsToQuests = QuestRetrieveUtils.getQuestIdsToQuests();
    resBuilder.addAllInProgressUserQuestData(CreateInfoProtoUtils.createFullUserQuestDataLarges(inProgressUserQuests, questIdsToQuests, userType));
  }
}
