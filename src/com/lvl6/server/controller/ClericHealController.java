package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ClericHealRequestEvent;
import com.lvl6.events.response.ClericHealResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.ClericHealRequestProto;
import com.lvl6.proto.EventProto.ClericHealResponseProto;
import com.lvl6.proto.EventProto.ClericHealResponseProto.HealStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;

public class ClericHealController extends EventController {
  
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
    int cost = reqProto.getCost();
    
    User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
    ClericHealResponseProto.Builder resBuilder = ClericHealResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    
    if (user.getVaultBalance() >= cost) {
      if (!user.updateRelativeVaultAbsoluteHealth(cost*-1)) {
        resBuilder.setStatus(HealStatus.OTHER_FAIL);
        log.error("problem with ClericHeal transaction");
      } else {
        resBuilder.setStatus(HealStatus.SUCCESS);
      }
    } else {
      resBuilder.setStatus(HealStatus.USER_NOT_ENOUGH_VAULT);
    }
    
    ClericHealResponseProto resProto = resBuilder.build();
    
    ClericHealResponseEvent resEvent = new ClericHealResponseEvent(senderProto.getUserId());
    resEvent.setClericHealResponseProto(resProto);
    
    server.writeEvent(resEvent);

  }


}
