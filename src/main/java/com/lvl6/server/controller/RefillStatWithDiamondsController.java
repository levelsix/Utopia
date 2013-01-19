package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RefillStatWithDiamondsRequestEvent;
import com.lvl6.events.response.RefillStatWithDiamondsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsRequestProto;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsRequestProto.StatType;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsResponseProto;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsResponseProto.Builder;
import com.lvl6.proto.EventProto.RefillStatWithDiamondsResponseProto.RefillStatStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

  @Component @DependsOn("gameServer") public class RefillStatWithDiamondsController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RefillStatWithDiamondsController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new RefillStatWithDiamondsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_REFILL_STAT_WITH_DIAMONDS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RefillStatWithDiamondsRequestProto reqProto = ((RefillStatWithDiamondsRequestEvent)event).getRefillStatWithDiamondsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    StatType statType = reqProto.getStatType();

    RefillStatWithDiamondsResponseProto.Builder resBuilder = RefillStatWithDiamondsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());      

      boolean legitRefill = checkLegitRefill(resBuilder, user, statType);

      RefillStatWithDiamondsResponseEvent resEvent = new RefillStatWithDiamondsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRefillStatWithDiamondsResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitRefill) {
        writeChangesToDB(user, statType);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        writeToUserCurrencyHistory(user, statType);
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
      diamondChange = ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_ENERGY_REFILL;
    } else if (statType == StatType.STAMINA) {
      diamondChange = ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_STAMINA_REFILL;
    }
    if (!user.updateRelativeDiamondsRestoreStat(diamondChange*-1, statType)) {
      log.error("problem with using diamonds to restore stat. diamondChange=" + diamondChange
          + ", statType=" + statType);
    }
  }

  private boolean checkLegitRefill(Builder resBuilder, User user, StatType statType) {
    if (user == null || statType == null) {
      resBuilder.setStatus(RefillStatStatus.OTHER_FAIL);
      log.error("parameter passed in is null. user=" + user + ", statType=" + statType);
      return false;
    }
    if (statType == StatType.ENERGY) {
      if (user.getEnergy() >= user.getEnergyMax()) {
        resBuilder.setStatus(RefillStatStatus.ALREADY_MAX);
        log.error("user energy already at his max. user's energy=" + user.getEnergy()
            + ", user's energyMax=" + user.getEnergyMax());
        return false;
      }
      if (user.getDiamonds() < ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_ENERGY_REFILL) {
        resBuilder.setStatus(RefillStatStatus.NOT_ENOUGH_DIAMONDS);
        log.error("user doesn't have enough diamonds to refill energy. has "
            + user.getDiamonds() + ", needs " + ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_ENERGY_REFILL);
        return false;
      }
    } else if (statType == StatType.STAMINA) {
      if (user.getStamina() >= user.getStaminaMax()) {
        resBuilder.setStatus(RefillStatStatus.ALREADY_MAX);
        log.error("user stamina already at his max. user's stamina=" + user.getStamina()
            + ", user's staminaMax=" + user.getStaminaMax());
        return false;
      }
      if (user.getDiamonds() < ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_STAMINA_REFILL) {
        resBuilder.setStatus(RefillStatStatus.NOT_ENOUGH_DIAMONDS);
        log.error("user doesn't have enough diamonds to refill stamina. has "
            + user.getDiamonds() + ", needs " + ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_STAMINA_REFILL);
        return false;
      }      
    } else {
      resBuilder.setStatus(RefillStatStatus.OTHER_FAIL);
      log.error("unknown stattype. stattype=" + statType);
      return false;      
    }
    resBuilder.setStatus(RefillStatStatus.SUCCESS);
    return true;  
  }
  
  public void writeToUserCurrencyHistory(User aUser, StatType aStatType) {
    //try, catch just a precaution
    try {
      int userId = aUser.getId();
      Timestamp date = new Timestamp((new Date()).getTime());
      int isSilver = 0;
      int currencyChange = 0;
      int currencyBefore = 0;
      String reasonForChange = ControllerConstants.UCHRFC__REFILL_STAT; 

      if (StatType.ENERGY == aStatType) {
        currencyChange = ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_ENERGY_REFILL * -1;
      } else if (StatType.STAMINA == aStatType) {
        currencyChange = ControllerConstants.REFILL_STAT_WITH_DIAMONDS__DIAMOND_COST_FOR_STAMINA_REFILL * -1;
      }

      currencyBefore = aUser.getDiamonds() - currencyChange;

      int numInserted = InsertUtils.get().insertIntoUserCurrencyHistory(userId, date, isSilver, 
          currencyChange, currencyBefore, reasonForChange);
      log.info("Should be 1. Rows inserted into user_currency_history: " + numInserted);
    } catch (Exception e) {
      log.error("Maybe table's not there or duplicate keys? ", e);
    }
  }

}
