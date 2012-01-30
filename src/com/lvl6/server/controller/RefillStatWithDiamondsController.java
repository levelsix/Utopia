package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RefillStatWithDiamondsRequestEvent;
import com.lvl6.events.response.RefillStatWithDiamondsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsRequestProto;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsRequestProto.StatType;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsResponseProto;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsResponseProto.Builder;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsResponseProto.RefillStatStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class RefillStatWithDiamondsController extends EventController{

  private static final int DIAMOND_COST_FOR_ENERGY_REFILL = ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_ENERGY_REFILL;
  private static final int DIAMOND_COST_FOR_STAMINA_REFILL = ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_STAMINA_REFILL;

  @Override
  public RequestEvent createRequestEvent() {
    return new RefillStatWithDiamondsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_REFILL_STAT_WITH_DIAMONDS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    RefillStatWithDiamondsRequestProto reqProto = ((RefillStatWithDiamondsRequestEvent)event).getRefillStatWithDiamondsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    StatType statType = reqProto.getStatType();

    RefillStatWithDiamondsResponseProto.Builder resBuilder = RefillStatWithDiamondsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());      

      boolean legitRefill = checkLegitRefill(resBuilder, user, statType);

      RefillStatWithDiamondsResponseEvent resEvent = new RefillStatWithDiamondsResponseEvent(senderProto.getUserId());
      resEvent.setRefillStatWithDiamondsResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitRefill) {
        writeChangesToDB(user, statType);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in RefillStatWithDiamondsController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }

  }

  private void writeChangesToDB(User user, StatType statType) {
    int diamondChange = 0;
    if (statType == StatType.ENERGY) {
      diamondChange = DIAMOND_COST_FOR_ENERGY_REFILL;
    } else if (statType == StatType.STAMINA) {
      diamondChange = DIAMOND_COST_FOR_STAMINA_REFILL;
    }
    if (!user.updateRelativeDiamondsRestoreStat(diamondChange*-1, statType)) {
      log.error("problem with using diamonds to restore stats");
    }
  }

  private boolean checkLegitRefill(Builder resBuilder, User user,
      StatType statType) {
    if (user == null || statType == null) {
      resBuilder.setStatus(RefillStatStatus.OTHER_FAIL);
      return false;
    }
    if (statType == StatType.ENERGY) {
      if (user.getDiamonds() < DIAMOND_COST_FOR_ENERGY_REFILL) {
        resBuilder.setStatus(RefillStatStatus.NOT_ENOUGH_DIAMONDS);
        return false;
      }
    } else if (statType == StatType.STAMINA) {
      if (user.getDiamonds() < DIAMOND_COST_FOR_STAMINA_REFILL) {
        resBuilder.setStatus(RefillStatStatus.NOT_ENOUGH_DIAMONDS);
        return false;
      }      
    } else {
      resBuilder.setStatus(RefillStatStatus.OTHER_FAIL);
      return false;      
    }
    resBuilder.setStatus(RefillStatStatus.SUCCESS);
    return true;  
  }


}
