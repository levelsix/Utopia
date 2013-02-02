package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.events.request.VaultRequestEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.VaultResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.VaultRequestProto;
import com.lvl6.proto.EventProto.VaultRequestProto.VaultRequestType;
import com.lvl6.proto.EventProto.VaultResponseProto;
import com.lvl6.proto.EventProto.VaultResponseProto.Builder;
import com.lvl6.proto.EventProto.VaultResponseProto.VaultStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.QuestUtils;

  @Component @DependsOn("gameServer") public class VaultController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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
  protected void processRequestEvent(RequestEvent event) throws Exception {
    VaultRequestProto reqProto = ((VaultRequestEvent)event).getVaultRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    VaultRequestType requestType = reqProto.getRequestType();
    int amount = reqProto.getAmount();

    VaultResponseProto.Builder resBuilder = VaultResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    // Lock this player's ID
    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      int previousSilver = 0;

      boolean legitTransaction = checkLegitTransaction(resBuilder, user, amount, requestType);
            
      if (legitTransaction) {
        previousSilver = user.getCoins() + user.getVaultBalance();
        
        if (requestType == VaultRequestType.WITHDRAW) {
          if (!user.updateRelativeCoinsVault(amount, -1*amount)) {
            log.error("problem with vault transaction. coinChange=" + amount + ", vaultChange=" + amount*-1);
            legitTransaction = false;
          }
        } else if (requestType == VaultRequestType.DEPOSIT) {
          if (!user.updateRelativeCoinsVault(-1*amount, (int)Math.floor((1-ControllerConstants.VAULT__DEPOSIT_PERCENT_CUT)*amount))) {
            log.error("problem with vault transaction. coinChange=" + -1*amount + ", vaultChange="
                + (int)Math.floor((1-ControllerConstants.VAULT__DEPOSIT_PERCENT_CUT)*amount));
            legitTransaction = false;
          }
        }
      }
      if (legitTransaction) {
        resBuilder.setCoinAmount(user.getCoins());
        resBuilder.setVaultAmount(user.getVaultBalance());
      }
      
      VaultResponseProto resProto = resBuilder.build();
      VaultResponseEvent resEvent = new VaultResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setVaultResponseProto(resProto);

      server.writeEvent(resEvent);

      UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
      resEventUpdate.setTag(event.getTag());
      server.writeEvent(resEventUpdate);
      
      if (legitTransaction) {
        if (requestType == VaultRequestType.WITHDRAW) {
          QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, SpecialQuestAction.WITHDRAW_FROM_VAULT, true);
        } else if (requestType == VaultRequestType.DEPOSIT) {
          QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, SpecialQuestAction.DEPOSIT_IN_VAULT, true);
          
          writeToUserCurrencyHistory(user, -1*amount, previousSilver); 
        }
      }
    } catch (Exception e) {
      log.error("exception in VaultController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    }
  }

  private boolean checkLegitTransaction(Builder resBuilder, User user, int amount, VaultRequestType requestType) {
    if (amount <= 0 || user == null) {
      resBuilder.setStatus(VaultStatus.OTHER_FAIL);
      log.error("amount <= 0 or user=null. user=" + user + ", amount=" + amount + ", request type=" +  requestType);
      return false;
    }
    if (requestType == VaultRequestType.WITHDRAW) {
      if (amount > user.getVaultBalance()) {
        resBuilder.setStatus(VaultStatus.OTHER_FAIL);
        log.error("user tried to withdraw " + amount + " coins but only has (in vault) " + user.getVaultBalance());
        return false;
      }
    }
    if (requestType == VaultRequestType.DEPOSIT) {
      if (amount > user.getCoins()) {
        resBuilder.setStatus(VaultStatus.OTHER_FAIL);
        log.error("user tried to deposit " + amount + " coins but only has " + user.getCoins());
        return false;
      }
    }    
    resBuilder.setStatus(VaultStatus.SUCCESS);
    return true;
  }

  public void writeToUserCurrencyHistory(User aUser, int coinChange, int previousSilver) {
    Timestamp date = new Timestamp((new Date()).getTime());

    Map<String, Integer> goldSilverChange = new HashMap<String, Integer>();
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String silver = MiscMethods.silver;
    String reasonForChange = ControllerConstants.UCHRFC__VAULT_DEPOSIT;
    
    goldSilverChange.put(silver, coinChange);
    previousGoldSilver.put(silver, previousSilver);
    reasonsForChanges.put(silver, reasonForChange);
    
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, goldSilverChange,
        previousGoldSilver, reasonsForChanges);
  }
}
