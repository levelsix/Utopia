package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.UpgradeClanTierLevelRequestEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.UpgradeClanTierLevelResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanTierLevel;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.UpgradeClanTierLevelRequestProto;
import com.lvl6.proto.EventProto.UpgradeClanTierLevelResponseProto;
import com.lvl6.proto.EventProto.UpgradeClanTierLevelResponseProto.UpgradeClanTierLevelStatus;
import com.lvl6.proto.EventProto.UpgradeClanTierLevelResponseProto.Builder;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.ClanTierLevelRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class UpgradeClanTierLevelController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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

    server.lockPlayer(senderProto.getUserId());
    try {
      User possibleClanOwner = RetrieveUtils.userRetrieveUtils().getUserById(userId);

      boolean validRequest = 
          isValidUpdateClanTierLevelRequest(resBuilder, possibleClanOwner, clanId);
      
      if (validRequest) {
        Clan clan = ClanRetrieveUtils.getClanWithId(clanId);
        resBuilder.setMinClan(CreateInfoProtoUtils.createMinimumClanProtoFromClan(clan));
        resBuilder.setFullClan(CreateInfoProtoUtils.createFullClanProtoWithClanSize(clan));
      }

      UpgradeClanTierLevelResponseEvent resEvent = 
          new UpgradeClanTierLevelResponseEvent(userId);
      resEvent.setTag(event.getTag());
      resEvent.setUpgradeClanTierResponseProto(resBuilder.build());

      if(validRequest) {
        //notify everyone in the clan that their clan's tier level has increased
        server.writeClanEvent(resEvent, clanId);

        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(possibleClanOwner);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      } else {
        server.writeEvent(resEvent);
      }

    } catch (Exception e) {
      log.error("exception in UpgradeClanTierLevelController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  /*
   * Returns true if the user who requested to upgrade a clan tier level can indeed upgrade
   * the tier level. If this is the case, then the user's gold and the clan's 
   * currentTierLevel is modified accordingly. 
   * Returns false otherwise.
   */
  private boolean isValidUpdateClanTierLevelRequest(Builder aBuilder, User aUser, int clanId) {
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

    //passed all checks
    //UPDATE USER'S GOLD AND THE CLAN'S CURRENT TIER LEVEL
    aUser.updateRelativeDiamondsNaive(-upgradeCost);
    if(UpdateUtils.get().incrementCurrentTierLevelForClan(clanId)) {
      //upgrade clan request has met all requirements
      aBuilder.setStatus(UpgradeClanTierLevelStatus.SUCCESS);
      return true;    			
    }
    else {
      aBuilder.setStatus(UpgradeClanTierLevelStatus.OTHER_FAIL);
      return false;
    }

  }

}
