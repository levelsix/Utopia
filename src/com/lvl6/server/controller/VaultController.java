package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.VaultRequestEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.VaultResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.VaultRequestProto;
import com.lvl6.proto.EventProto.VaultRequestProto.VaultRequestType;
import com.lvl6.proto.EventProto.VaultResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class VaultController extends EventController {

  private static final int DEPOSIT_PERCENT_CUT = 10;

  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());    
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

    // Lock this player's ID
    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      VaultResponseProto.Builder resBuilder = VaultResponseProto.newBuilder();
      resBuilder.setSender(senderProto);

      if (amount > 0 && requestType == VaultRequestType.WITHDRAW && amount < user.getVaultBalance()) {
        if (!user.updateRelativeCoinsVault(amount, -1*amount)) {
          log.error("problem with vault transaction");
        }
      } else if (amount > 0 && requestType == VaultRequestType.DEPOSIT && amount < user.getCoins()){
        if (!user.updateRelativeCoinsVault(-1*amount, (int)Math.floor((1-DEPOSIT_PERCENT_CUT/100.0)*amount))) {
          log.error("problem with vault transaction");          
        }
      }
      resBuilder.setCoinAmount(user.getCoins());
      resBuilder.setVaultAmount(user.getVaultBalance());
      VaultResponseProto resProto = resBuilder.build();

      VaultResponseEvent resEvent = new VaultResponseEvent(senderProto.getUserId());
      resEvent.setVaultResponseProto(resProto);

      server.writeEvent(resEvent);

      UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
      server.writeEvent(resEventUpdate);
    } catch (Exception e) {
      log.error("exception in VaultController processEvent", e);
    } finally {
      // Unlock this player
      server.unlockPlayer(senderProto.getUserId());
    }
  }

}
