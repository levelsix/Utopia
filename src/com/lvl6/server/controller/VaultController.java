package com.lvl6.server.controller;

import org.apache.log4j.Logger;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.VaultRequestEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.VaultResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.VaultRequestProto;
import com.lvl6.proto.EventProto.VaultRequestProto.VaultRequestType;
import com.lvl6.proto.EventProto.VaultResponseProto;
import com.lvl6.proto.EventProto.VaultResponseProto.Builder;
import com.lvl6.proto.EventProto.VaultResponseProto.VaultStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class VaultController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public VaultController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new VaultRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_VAULT_EVENT;
  }

  /*
   * db stuff done before sending event to eventwriter/client because the client's not waiting 
   * on it immediately anyways
   */
  @Override
  protected void processRequestEvent(RequestEvent event) {
    VaultRequestProto reqProto = ((VaultRequestEvent)event).getVaultRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    VaultRequestType requestType = reqProto.getRequestType();
    int amount = reqProto.getAmount();

    VaultResponseProto.Builder resBuilder = VaultResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    // Lock this player's ID
    server.lockPlayer(senderProto.getUserId());
    
    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

      boolean legitTransaction = checkLegitTransaction(resBuilder, user, amount, requestType);
            
      if (legitTransaction) {
        if (requestType == VaultRequestType.WITHDRAW) {
          if (!user.updateRelativeCoinsVault(amount, -1*amount)) {
            log.error("problem with vault transaction");
          }
        } else if (requestType == VaultRequestType.DEPOSIT) {
          if (!user.updateRelativeCoinsVault(-1*amount, (int)Math.floor((1-ControllerConstants.VAULT__DEPOSIT_PERCENT_CUT)*amount))) {
            log.error("problem with vault transaction");          
          }
        }
        resBuilder.setCoinAmount(user.getCoins());
        resBuilder.setVaultAmount(user.getVaultBalance());
      }
      
      VaultResponseProto resProto = resBuilder.build();
      VaultResponseEvent resEvent = new VaultResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setVaultResponseProto(resProto);

      server.writeEvent(resEvent);

      UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
      resEventUpdate.setTag(event.getTag());
      resEventUpdate.setTag(event.getTag());
      server.writeEvent(resEventUpdate);
    } catch (Exception e) {
      log.error("exception in VaultController processEvent", e);
    } finally {
      // Unlock this player
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private boolean checkLegitTransaction(Builder resBuilder, User user, int amount, VaultRequestType requestType) {
    if (amount <= 0 || user == null) {
      resBuilder.setStatus(VaultStatus.OTHER_FAIL);
      return false;
    }
    if (requestType == VaultRequestType.WITHDRAW) {
      if (amount > user.getVaultBalance()) {
        resBuilder.setStatus(VaultStatus.OTHER_FAIL);
        return false;
      }
    }
    if (requestType == VaultRequestType.DEPOSIT) {
      if (amount > user.getCoins()) {
        resBuilder.setStatus(VaultStatus.OTHER_FAIL);
        return false;
      }
    }    
    resBuilder.setStatus(VaultStatus.SUCCESS);
    return true;
  }

}
