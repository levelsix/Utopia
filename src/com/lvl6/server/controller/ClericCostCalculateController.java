package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ClericCostCalculateRequestEvent;
import com.lvl6.events.response.ClericCostCalculateResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.ClericCostCalculateRequestProto;
import com.lvl6.proto.EventProto.ClericCostCalculateResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;

public class ClericCostCalculateController extends EventController {

  //Formula: (int) Math.ceil(Math.pow(user.getLevel(), A)*healthToHeal*B);
  private static final double A = 3;
  private static final double B = .05;
  
  /** 
   * do ChatController specific initialization here 
   */
  @Override  
  public void initController() {
    log.info("initController for " + this.getClass().toString());
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new ClericCostCalculateRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_CLERIC_COST_CALC_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    ClericCostCalculateRequestProto reqProto = ((ClericCostCalculateRequestEvent)event).getClericCostCalcRequestProto();
    
    MinimumUserProto senderProto = reqProto.getSender();
    
    User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
    ClericCostCalculateResponseProto.Builder resBuilder = ClericCostCalculateResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    
    int cost = calculateClericCost(user);
    resBuilder.setCost(cost);
    
    ClericCostCalculateResponseProto resProto = resBuilder.build();
    ClericCostCalculateResponseEvent resEvent = new ClericCostCalculateResponseEvent(senderProto.getUserId());
    resEvent.setClericCostCalcResponseProto(resProto);
    server.writeEvent(resEvent);
  }

  private int calculateClericCost(User user) {
    int healthToHeal = user.getHealthMax() - user.getHealth();
    return (int) Math.ceil(Math.pow(user.getLevel(), A)*healthToHeal*B);
  }


}