package com.lvl6.server.controller;

import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveThreeCardMonteRequestEvent;
import com.lvl6.events.response.RetrieveThreeCardMonteResponseEvent;
import com.lvl6.info.MonteCard;
import com.lvl6.info.MonteCard.MonteCardType;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.RetrieveThreeCardMonteRequestProto;
import com.lvl6.proto.EventProto.RetrieveThreeCardMonteResponseProto;
import com.lvl6.proto.EventProto.RetrieveThreeCardMonteResponseProto.Builder;
import com.lvl6.proto.EventProto.RetrieveThreeCardMonteResponseProto.RetrieveThreeCardMonteStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.ThreeCardMonteRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;

@Component @DependsOn("gameServer") public class RetrieveThreeCardMonteController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveThreeCardMonteController() {
    numAllocatedThreads = 15;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveThreeCardMonteRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_THREE_CARD_MONTE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveThreeCardMonteRequestProto reqProto = ((RetrieveThreeCardMonteRequestEvent)event).getRetrieveThreeCardMonteRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

    RetrieveThreeCardMonteResponseProto.Builder resBuilder = RetrieveThreeCardMonteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(RetrieveThreeCardMonteStatus.SUCCESS);

    populateResBuilder(resBuilder, user);
    RetrieveThreeCardMonteResponseProto resProto = resBuilder.build();

    RetrieveThreeCardMonteResponseEvent resEvent = new RetrieveThreeCardMonteResponseEvent(senderProto.getUserId());
    resEvent.setTag(event.getTag());
    resEvent.setRetrieveThreeCardMonteResponseProto(resProto);

    server.writeEvent(resEvent);
  }

  private void populateResBuilder(Builder resBuilder, User user) {
    UserType userType = user.getType();
    int level = user.getLevel();
    MonteCard card = getMostRecentCardForTypeAndLevel(MonteCardType.BAD, level);
    if (card != null) resBuilder.setBadMonteCard(CreateInfoProtoUtils.createMonteCardProtoFromMonteCard(card, userType));
    else log.error("Unable to find bad monte card.");

    card = getMostRecentCardForTypeAndLevel(MonteCardType.MEDIUM, level);
    if (card != null) resBuilder.setMediumMonteCard(CreateInfoProtoUtils.createMonteCardProtoFromMonteCard(card, userType));
    else log.error("Unable to find medium monte card.");

    card = getMostRecentCardForTypeAndLevel(MonteCardType.GOOD, level);
    if (card != null) resBuilder.setGoodMonteCard(CreateInfoProtoUtils.createMonteCardProtoFromMonteCard(card, userType));
    else log.error("Unable to find good  monte card.");
  }
  
  private MonteCard getMostRecentCardForTypeAndLevel(MonteCardType type, int level) {
    Map<Integer,MonteCard> map = ThreeCardMonteRetrieveUtils.getMonteCardIdsToMonteCards();
    MonteCard mostRecentCard = null;
    Date curDate = new Date();
    for (Integer i : map.keySet()) {
      MonteCard card = map.get(i);
      if (card.getCardType() == type && card.getStartDate().before(curDate)) {
        if (mostRecentCard == null || card.getStartDate().after(mostRecentCard.getStartDate())) {
          mostRecentCard = card;
        } else if (card.getStartDate().equals(curDate) && mostRecentCard.getMinLevel() < card.getMinLevel() && card.getMinLevel() < level) {
          mostRecentCard = card;
        }
      }
    }
    return mostRecentCard;
  }
}
