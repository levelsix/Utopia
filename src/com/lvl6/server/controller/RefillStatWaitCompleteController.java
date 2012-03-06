package com.lvl6.server.controller;

import java.sql.Timestamp;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RefillStatWaitCompleteRequestEvent;
import com.lvl6.events.response.RefillStatWaitCompleteResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RefillStatWaitCompleteRequestProto;
import com.lvl6.proto.EventProto.RefillStatWaitCompleteRequestProto.RefillStatWaitCompleteType;
import com.lvl6.proto.EventProto.RefillStatWaitCompleteResponseProto;
import com.lvl6.proto.EventProto.RefillStatWaitCompleteResponseProto.Builder;
import com.lvl6.proto.EventProto.RefillStatWaitCompleteResponseProto.RefillStatWaitCompleteStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class RefillStatWaitCompleteController extends EventController{

  public RefillStatWaitCompleteController() {
    numAllocatedThreads = 5;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new RefillStatWaitCompleteRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_REFILL_STAT_WAIT_COMPLETE_EVENT;
  }
  
  @Override
  protected void processRequestEvent(RequestEvent event) {
    
    RefillStatWaitCompleteRequestProto reqProto = ((RefillStatWaitCompleteRequestEvent)event).getRefillStatWaitCompleteRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    Timestamp clientTime = new Timestamp(reqProto.getCurTime());
    RefillStatWaitCompleteType type = reqProto.getType();
    
    RefillStatWaitCompleteResponseProto.Builder resBuilder = RefillStatWaitCompleteResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      boolean legitWaitComplete = checkLegitWaitComplete(resBuilder, user, clientTime, type);

      RefillStatWaitCompleteResponseEvent resEvent = new RefillStatWaitCompleteResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      
      if (legitWaitComplete) {
        writeChangesToDB(user, type, clientTime);
      }
      server.writeEvent(resEvent);
      UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
      resEventUpdate.setTag(event.getTag());
      server.writeEvent(resEventUpdate);
    } catch (Exception e) {
      log.error("exception in RefillStatWaitCompleteController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, RefillStatWaitCompleteType type, Timestamp clientTime) {
    if (type == RefillStatWaitCompleteType.ENERGY) {
      int energyChange = Math.min(user.getEnergyMax()-user.getEnergy(), 
          (int)((clientTime.getTime() - user.getLastEnergyRefillTime().getTime()) / (60000*ControllerConstants.REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_ENERGY)));
      Timestamp newLastEnergyRefillTime = new Timestamp(user.getLastEnergyRefillTime().getTime() + 60000*energyChange*ControllerConstants.REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_ENERGY);
      if (!user.updateLastenergyrefilltimeEnergy(newLastEnergyRefillTime, energyChange)) {
        log.error("problem with updating user's energy and lastenergyrefill time");
      }
    } else if (type == RefillStatWaitCompleteType.STAMINA) {
      int staminaChange = Math.min(user.getStaminaMax()-user.getStamina(), 
          (int)((clientTime.getTime() - user.getLastStaminaRefillTime().getTime()) / (60000*ControllerConstants.REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_STAMINA)));
      Timestamp newLastStaminaRefillTime = new Timestamp(user.getLastStaminaRefillTime().getTime() + 60000*staminaChange*ControllerConstants.REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_STAMINA);
      if (!user.updateLaststaminarefilltimeStaminaIslaststaminastatefull(newLastStaminaRefillTime, staminaChange)) {
        log.error("problem with updating user's stamina and laststaminarefill time");
      }
    }
  }

  private boolean checkLegitWaitComplete(Builder resBuilder,
      User user, Timestamp clientTime, RefillStatWaitCompleteType type) {
    if (user == null || clientTime == null || type == null ) {
      resBuilder.setStatus(RefillStatWaitCompleteStatus.OTHER_FAIL);
      return false;
    }
    if (!MiscMethods.checkClientTimeBeforeApproximateNow(clientTime)) {
      resBuilder.setStatus(RefillStatWaitCompleteStatus.CLIENT_TOO_AHEAD_OF_SERVER_TIME);
      return false;
    }
    if (type == RefillStatWaitCompleteType.ENERGY) {
      if (user.getLastEnergyRefillTime().getTime() + 60000*ControllerConstants.REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_ENERGY > clientTime.getTime()) {
        resBuilder.setStatus(RefillStatWaitCompleteStatus.NOT_READY_YET);
        return false;
      }
      if (user.getEnergy() == user.getEnergyMax()) {
        resBuilder.setStatus(RefillStatWaitCompleteStatus.ALREADY_MAX);
        return false;        
      }
    } else if (type == RefillStatWaitCompleteType.STAMINA) { 
      if (user.getLastStaminaRefillTime().getTime() + 60000*ControllerConstants.REFILL_STAT_WAIT_COMPLETE__MINUTES_FOR_STAMINA > clientTime.getTime()) {
        resBuilder.setStatus(RefillStatWaitCompleteStatus.NOT_READY_YET);
        return false;
      }    
      if (user.getStamina() == user.getStaminaMax()) {
        resBuilder.setStatus(RefillStatWaitCompleteStatus.ALREADY_MAX);
        return false;        
      }
    } else {
      resBuilder.setStatus(RefillStatWaitCompleteStatus.OTHER_FAIL);
      return false;      
    }
    resBuilder.setStatus(RefillStatWaitCompleteStatus.SUCCESS);
    return true;  

  }
}
