package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveThreeCardMonteRequestEvent;
import com.lvl6.events.response.RetrieveThreeCardMonteResponseEvent;
import com.lvl6.info.MonteCard;
import com.lvl6.proto.EventProto.RetrieveThreeCardMonteRequestProto;
import com.lvl6.proto.EventProto.RetrieveThreeCardMonteResponseProto;
import com.lvl6.proto.EventProto.RetrieveThreeCardMonteResponseProto.Builder;
import com.lvl6.proto.EventProto.RetrieveThreeCardMonteResponseProto.RetrieveThreeCardMonteStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;

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

    RetrieveThreeCardMonteResponseProto.Builder resBuilder = RetrieveThreeCardMonteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(RetrieveThreeCardMonteStatus.SUCCESS);

    populateResBuilder(resBuilder);
    RetrieveThreeCardMonteResponseProto resProto = resBuilder.build();

    RetrieveThreeCardMonteResponseEvent resEvent = new RetrieveThreeCardMonteResponseEvent(senderProto.getUserId());
    resEvent.setTag(event.getTag());
    resEvent.setRetrieveThreeCardMonteResponseProto(resProto);

    server.writeEvent(resEvent);
  }

  private void populateResBuilder(Builder resBuilder) {
    populateBadPrizes(resBuilder);
    populateMediumPrizes(resBuilder);
    populateGoodPrizes(resBuilder);
  }
}
