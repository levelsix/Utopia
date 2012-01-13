package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ClericHealRequestEvent;
import com.lvl6.events.response.ClericHealResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.ClericHealRequestProto;
import com.lvl6.proto.EventProto.ClericHealResponseProto;
import com.lvl6.proto.EventProto.ClericHealResponseProto.HealStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class ClericHealController extends EventController {
  
  //Formula: (int) Math.ceil(Math.pow(user.getLevel(), A)*healthToHeal*B);
  private static final double A = 3;
  private static final double B = .05;

  
  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());    
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new ClericHealRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_CLERIC_HEAL_EVENT;
  }
  
  /*
   * db stuff done before sending event to eventwriter/client because the client's not waiting 
   * on it immediately anyways
   */
  @Override
  protected void processRequestEvent(RequestEvent event) {
    ClericHealRequestProto reqProto = ((ClericHealRequestEvent)event).getClericHealRequestProto();
    
    MinimumUserProto senderProto = reqProto.getSender();
    
    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      int cost = calculateClericCost(user);
  
      ClericHealResponseProto.Builder resBuilder = ClericHealResponseProto.newBuilder();
      resBuilder.setSender(senderProto);
      
      if (user.getVaultBalance() >= cost) {
        if (!user.updateRelativeVaultAbsoluteHealth(cost*-1)) {
          resBuilder.setStatus(HealStatus.OTHER_FAIL);
          log.error("problem with ClericHeal transaction");
        } else {
          resBuilder.setCost(cost);
          resBuilder.setStatus(HealStatus.SUCCESS);
        }
      } else {
        resBuilder.setStatus(HealStatus.USER_NOT_ENOUGH_VAULT);
      }
      
      ClericHealResponseProto resProto = resBuilder.build();
      
      ClericHealResponseEvent resEvent = new ClericHealResponseEvent(senderProto.getUserId());
      resEvent.setClericHealResponseProto(resProto);
          
      server.writeEvent(resEvent);
      
      UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
      server.writeEvent(resEventUpdate);
    } catch (Exception e) {
      log.error("exception in ClericHealController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private int calculateClericCost(User user) {
    int healthToHeal = user.getHealthMax() - user.getHealth();
    return (int) Math.ceil(Math.pow(user.getLevel(), A)*healthToHeal*B);
  }

}
