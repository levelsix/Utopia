package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.UpgradeClanTierLevelRequestEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.UpgradeClanTierLevelResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanTierLevel;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.UpgradeClanTierLevelRequestProto;
import com.lvl6.proto.EventProto.UpgradeClanTierLevelResponseProto;
import com.lvl6.proto.EventProto.UpgradeClanTierLevelResponseProto.Builder;
import com.lvl6.proto.EventProto.UpgradeClanTierLevelResponseProto.UpgradeClanTierLevelStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.ClanTierLevelRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class UpgradeClanTierLevelController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public UpgradeClanTierLevelController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new UpgradeClanTierLevelRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_UPGRADE_CLAN_TIER_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    //get values sent from the client (the request proto)
    UpgradeClanTierLevelRequestProto reqProto = ((UpgradeClanTierLevelRequestEvent)event).getUpgradeClanTierRequestProto();    
    MinimumUserProto senderProto = reqProto.getSender();
    int userId = senderProto.getUserId();
    int clanId = reqProto.getClanId();

    //set some values to send to the client (the response proto)
    UpgradeClanTierLevelResponseProto.Builder resBuilder = UpgradeClanTierLevelResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User possibleClanOwner = RetrieveUtils.userRetrieveUtils().getUserById(userId);
      List<Integer> currencyChange = new ArrayList<Integer>();
      int previousGold = possibleClanOwner.getDiamonds();
      
      boolean validRequest = 
          isValidUpdateClanTierLevelRequest(resBuilder, possibleClanOwner, clanId, currencyChange);
      boolean successfulUpdate = false;
      if (validRequest) {
        successfulUpdate = writeChangesToDB(resBuilder, possibleClanOwner, currencyChange, clanId);
      }
      if (successfulUpdate) {
        Clan clan = ClanRetrieveUtils.getClanWithId(clanId);
        resBuilder.setMinClan(CreateInfoProtoUtils.createMinimumClanProtoFromClan(clan));
        resBuilder.setFullClan(CreateInfoProtoUtils.createFullClanProtoWithClanSize(clan));
      }
      
      UpgradeClanTierLevelResponseEvent resEvent = 
          new UpgradeClanTierLevelResponseEvent(userId);
      resEvent.setTag(event.getTag());
      resEvent.setUpgradeClanTierResponseProto(resBuilder.build());

      if(successfulUpdate) {
        //notify everyone in the clan that their clan's tier level has increased
        server.writeClanEvent(resEvent, clanId);

        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(possibleClanOwner);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        writeToUserCurrencyHistory(possibleClanOwner, currencyChange, previousGold);
      } else {
        server.writeEvent(resEvent);
      }

    } catch (Exception e) {
      log.error("exception in UpgradeClanTierLevelController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    }
  }

  /*
   * Returns true if the user who requested to upgrade a clan tier level can indeed upgrade
   * the tier level. If this is the case, then the user's gold and the clan's 
   * currentTierLevel is modified accordingly. 
   * Returns false otherwise.
   */
  private boolean isValidUpdateClanTierLevelRequest(Builder aBuilder, User aUser, int clanId,
      List<Integer> currencyChange) {
    //check if user who sent request is the owner of the clan
    //then check if user has enough money
    //then check if the clan is at the highest level

    int clanIdOfUser = aUser.getClanId();

    if(clanIdOfUser != clanId) {
      aBuilder.setStatus(UpgradeClanTierLevelStatus.NOT_CLAN_LEADER);
      return false;
    }

    //user is clan leader, now check the gold the user has with 
    //the gold cost to upgrade to next tier lvl
    int usersGold = aUser.getDiamonds();

    Clan aClan = ClanRetrieveUtils.getClanWithId(clanId);
    int currentTierLevel = aClan.getCurrentTierLevel();
    ClanTierLevel aClanTierLevel = ClanTierLevelRetrieveUtils.getClanTierLevel(currentTierLevel); 
    int upgradeCost = aClanTierLevel.getGoldCostToUpgradeToNextTierLevel();

    if(usersGold < upgradeCost) {
      aBuilder.setStatus(UpgradeClanTierLevelStatus.NOT_ENOUGH_GOLD);
      return false;
    }

    //clan leader has enough gold, now check if clan is already at the highest tier level
    int highestClanTierLevel = ClanTierLevelRetrieveUtils.getHighestClanTierLevel(); 
    if (highestClanTierLevel <= currentTierLevel) {
      aBuilder.setStatus(UpgradeClanTierLevelStatus.ALREADY_AT_MAX_TIER);
      return false;
    }

    currencyChange.add(upgradeCost);
    aBuilder.setStatus(UpgradeClanTierLevelStatus.SUCCESS);
    return true;  
  }

  private boolean writeChangesToDB(Builder aBuilder, User aUser, List<Integer> currencyChange, int clanId) {
    if(currencyChange.isEmpty()) {
      return false;
    }
    int before = aUser.getDiamonds();
    int upgradeCost = -1 * currencyChange.get(0);
    int refund = -1 * upgradeCost;

    //passed all checks
    //UPDATE USER'S GOLD AND THE CLAN'S CURRENT TIER LEVEL
    if (aUser.updateRelativeDiamondsNaive(upgradeCost)) {
      if(UpdateUtils.get().incrementCurrentTierLevelForClan(clanId)) {
        //upgraded clan tier
        log.info("successfully changed clan tier level for clan: " + clanId);
        aBuilder.setStatus(UpgradeClanTierLevelStatus.SUCCESS);
        return true;
      }
      else {
        int after = aUser.getDiamonds();
        log.error("problem with creating new clan. user diamonds before: " + before + ", after: " + after);
        //if(!aUser.updateRelativeDiamondsNaive(refund)) {
          //give back money to user because of failure;
        log.error("give back " + refund + " diamonds to user: " + aUser + "?");
        //}
        aBuilder.setStatus(UpgradeClanTierLevelStatus.OTHER_FAIL);
      }
    } else {
      log.error("could not take " + refund + " diamonds form user: " + aUser);
      aBuilder.setStatus(UpgradeClanTierLevelStatus.OTHER_FAIL);
    }
    currencyChange.clear();
    return false;
  }
  
  private void writeToUserCurrencyHistory(User aUser, List<Integer> money, int previousGold) {
    try {
      if(money.isEmpty()) {
        return;
      }
      int userId = aUser.getId();
      Timestamp date = new Timestamp((new Date()).getTime());
      int isSilver = 0;
      int currencyChange = money.get(0) * -1; //forgot to make it negative before, but it is negative in writetodb
      int currencyAfter = aUser.getDiamonds();
      String reasonForChange = ControllerConstants.UCHRFC__UPGRADE_CLAN_TIER_LEVEL;
      
      InsertUtils.get().insertIntoUserCurrencyHistory(userId, date, isSilver, 
          currencyChange, previousGold, currencyAfter, reasonForChange);
      
      //log.info("Should be 1. Rows inserted into user_currency_history: " + numInserted);
    } catch (Exception e) {
      log.error("Maybe table's not there or duplicate keys? ", e);
    }
  }
}
