package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BeginClanTowerWarRequestEvent;
import com.lvl6.events.response.BeginClanTowerWarResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanTower;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.BeginClanTowerWarRequestProto;
import com.lvl6.proto.EventProto.BeginClanTowerWarResponseProto;
import com.lvl6.proto.EventProto.BeginClanTowerWarResponseProto.BeginClanTowerWarStatus;
import com.lvl6.proto.EventProto.BeginClanTowerWarResponseProto.Builder;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.ClanTowerRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class BeginClanTowerWarController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public BeginClanTowerWarController() {
    numAllocatedThreads = 2;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new BeginClanTowerWarRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_BEGIN_CLAN_TOWER_WAR;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {

    BeginClanTowerWarRequestProto reqProto = ((BeginClanTowerWarRequestEvent)event).getBeginClanTowerWarRequestProto();

    //get the values sent by the client
    MinimumUserProto senderProto = reqProto.getSender();
    int towerId = reqProto.getTowerId();
    Timestamp curTime = new Timestamp(reqProto.getCurTime());
    
    //response to the request setup
    BeginClanTowerWarResponseProto.Builder resBuilder = BeginClanTowerWarResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    
    Clan clan = ClanRetrieveUtils.getClanWithId(senderProto.getClan().getClanId());
    ClanTower aTower = ClanTowerRetrieveUtils.getClanTower(towerId);
    
    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legit = checkLegitBeginClanTowerWarRequest(resBuilder, user, clan, aTower, curTime);
      
      BeginClanTowerWarResponseEvent resEvent = new BeginClanTowerWarResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setBeginClanTowerWarResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legit) {
    	boolean isOwner = isUserOwnerOfTower(user, aTower);
        writeChangesToDB(aTower, user, curTime, isOwner);
      }
    } catch (Exception e) {
      log.error("exception in BeginClanTowerWarController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }
  
  private boolean isUserOwnerOfTower(User aUser, ClanTower aClanTower) {
	  return aUser.getId() == aClanTower.getClanOwnerId();
  }

  //aTower can be modified to store some new data, as in an owner, or attacker id
  private boolean checkLegitBeginClanTowerWarRequest(Builder resBuilder, 
		  User user, Clan clan, ClanTower aTower, Timestamp curTime) {
    if (user == null) {
      resBuilder.setStatus(BeginClanTowerWarStatus.OTHER_FAIL);
      log.error("user is null");
      return false;
    }

    //check if the tower is valid
    if (null == aTower) {
    	//empty tower
    	resBuilder.setStatus(BeginClanTowerWarStatus.OTHER_FAIL);
    	log.error("tower requested is null.");
    	return false;
    }

    if (!MiscMethods.checkClientTimeAroundApproximateNow(curTime)) {
      resBuilder.setStatus(BeginClanTowerWarStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + curTime + ", servertime~="
          + new Date());
      return false;
    }
    
    //check if the request sender is the clan leader or in a clan
    if(null == clan || clan.getOwnerId() != user.getId()) {
    	//non-clan-leader, non-clanned person sent request
    	resBuilder.setStatus(BeginClanTowerWarStatus.NOT_CLAN_LEADER);
    	log.error("user is not the clan leader or not in a clan. user=" + user);
    	return false;
    }
    
    int clanId = clan.getId();
    //check if clan has enough members
    if (ControllerConstants.MIN_CLAN_MEMBERS_TO_HOLD_CLAN_TOWER >
        RetrieveUtils.userClanRetrieveUtils().getUserClanMembersInClan(clanId).size() ){
    	//not enough clan members
    	resBuilder.setStatus(BeginClanTowerWarStatus.NOT_ENOUGH_CLAN_MEMBERS);
    	log.error("clan does not have enough members. clan=" + clan);
    	return false;
    }
    
    //check if the clan tower has an owner
    if (ControllerConstants.NOT_SET == aTower.getClanOwnerId()) {
    	///no owner for tower
    	//TODO: FIGURE OUT WHAT TO RETURN WHEN SETTING THE OWNER OF A TOWER
    	//logic here now will suffice
    	resBuilder.setStatus(BeginClanTowerWarStatus.SUCCESS);
    	aTower.setClanOwnerId(user.getId()); //set the owner id to use when writing to db
    	return true;
    }
    
    //check if there already is a clan attacking the tower
    if (ControllerConstants.NOT_SET == aTower.getClanAttackerId()) {
    	//no clan attacking tower
    	//TODO: FIGURE OUT WHAT TO RETURN WHEN SETTING THE OWNER OF A TOWER
    	//logic here now will suffice
    	resBuilder.setStatus(BeginClanTowerWarStatus.SUCCESS);
    	aTower.setClanAttackerId(user.getId());
    	return true;
    }
    
    //tower has an owner and an attacker, so deny request
    resBuilder.setStatus(BeginClanTowerWarStatus.TOWER_ALREADY_IN_BATTLE);
    return false;
  }

  private void writeChangesToDB(ClanTower aClanTower, User aUser, Timestamp curTime, boolean isOwner) {
//    int goldCost = reset ? ControllerConstants.GOLDMINE__GOLD_COST_TO_RESTART : 0;
//    Timestamp newStamp = reset ? null : curTime;
//    if (!user.updateLastGoldmineRetrieval(-goldCost, newStamp)) {
//      log.error("problem with adding diamonds for goldmine, adding " + ControllerConstants.GOLDMINE__GOLD_AMOUNT_FROM_PICK_UP);
//    }
	  if (!UpdateUtils.get().updateClanTowerOwnerOrAttackerId(aClanTower.getId(), aUser.getId(), curTime, isOwner)) {
		  log.error("problem with updating a clan tower during a BeginClanTowerWarRequest." +
				  " clan tower=" + aClanTower +
				  " user=" + aUser +
				  " time of request=" + curTime);
	  }
  }
}
