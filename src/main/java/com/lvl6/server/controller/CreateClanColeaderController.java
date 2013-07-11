package com.lvl6.server.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.CreateClanColeaderRequestEvent;
import com.lvl6.events.response.CreateClanColeaderResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.CreateClanColeaderRequestProto;
import com.lvl6.proto.EventProto.CreateClanColeaderResponseProto;
import com.lvl6.proto.EventProto.CreateClanColeaderResponseProto.Builder;
import com.lvl6.proto.EventProto.CreateClanColeaderResponseProto.CreateClanColeaderStatus;
import com.lvl6.proto.EventProto.MenteeFinishedQuestResponseProto.MenteeQuestType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.ClanTierLevelRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class CreateClanColeaderController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public CreateClanColeaderController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new CreateClanColeaderRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_APPROVE_OR_REJECT_REQUEST_TO_JOIN_CLAN_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    CreateClanColeaderRequestProto reqProto = ((CreateClanColeaderRequestEvent)event).getCreateClanColeaderRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userId = reqProto.getUserId();
    boolean makeColeader = reqProto.getMakeColeader();

    CreateClanColeaderResponseProto.Builder resBuilder = CreateClanColeaderResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setUserId(userId);
    resBuilder.setMakeColeader(makeColeader);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    server.lockPlayer(userId, this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      User candidate = RetrieveUtils.userRetrieveUtils().getUserById(userId);
      Clan clan = ClanRetrieveUtils.getClanWithId(user.getClanId());
      //resBuilder.setMinClan(CreateInfoProtoUtils.createMinimumClanProtoFromClan(clan));
      //resBuilder.setFullClan(CreateInfoProtoUtils.createFullClanProtoWithClanSize(clan));
      
      boolean legitDecision = checkLegitDecision(resBuilder, user, candidate, makeColeader);

      CreateClanColeaderResponseEvent resEvent = new CreateClanColeaderResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setCreateClanColeaderResponseProto(resBuilder.build());  
      server.writeClanEvent(resEvent, clan.getId());

      if (legitDecision) {
        writeChangesToDB(user, candidate, makeColeader);
      } else {
        server.writeEvent(resEvent);
      }
    } catch (Exception e) {
      log.error("exception in CreateClanColeader processEvent", e);
    } finally {
      server.unlockPlayer(userId, this.getClass().getSimpleName());
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    }
  }

  private void writeChangesToDB(User user, User candidate, boolean makeColeader) {
    if (makeColeader) {
      if (!UpdateUtils.get().updateUserClanStatus(candidate.getId(), user.getClanId(), UserClanStatus.COLEADER)) {
        log.error("problem with updating user clan status to coleader for candidate " + candidate + " and clan id "+ user.getClanId());
      } 
    } else {
      if (!UpdateUtils.get().updateUserClanStatus(candidate.getId(), user.getClanId(), UserClanStatus.MEMBER)) {
        log.error("problem with updating user clan status to member for candidate with id " + candidate.getId() + " and clan id " + user.getClanId()); 
      }
    }
  }

  private boolean checkLegitDecision(Builder resBuilder, User user, User candidate, boolean accept) {
    if (user == null || candidate == null) {
      resBuilder.setStatus(CreateClanColeaderStatus.OTHER_FAIL);
      log.error("user is " + user + ", candidate is " + candidate);
      return false;      
    }
    Clan clan = ClanRetrieveUtils.getClanWithId(user.getClanId());
    //user making decision is leader
    int clanId = clan.getId();
    UserClan uc = RetrieveUtils.userClanRetrieveUtils().getSpecificUserClan(candidate.getId(), clanId);
    UserClan uc2 = RetrieveUtils.userClanRetrieveUtils().getSpecificUserClan(user.getId(), clanId);
    
    if (clan.getOwnerId() != user.getId()) {
      resBuilder.setStatus(CreateClanColeaderStatus.NOT_OWNER);
      log.error("clan owner isn't this guy, clan owner id is " + clan.getOwnerId());
      return false;      
    }
    
    if (clan.getOwnerId() == candidate.getId()) {
      resBuilder.setStatus(CreateClanColeaderStatus.IS_LEADER);
      log.error("candidate is clan owner, clan owner id is " + clan.getOwnerId());
      return false;      
    }
    
    //check if candidate is in same clan
    if (user.getClanId() != candidate.getClanId()) {
    	resBuilder.setStatus(CreateClanColeaderStatus.NOT_SAME_CLAN);
    	log.error("trying to make non clan member coleader");
    	//the other requests in user_clans table that have a status of 2 (requesting to join clan)
    	//are deleted later on in writeChangesToDB
    	return false;
    }
    //check if max number of coleaders already
    List<UserClan> ucs = RetrieveUtils.userClanRetrieveUtils().getUserClanMembersInClan(clanId);
    int maxSize = ClanTierLevelRetrieveUtils.getClanTierLevel(clan.getCurrentTierLevel()).getMaxClanSize();	
    int maxColeaders = (int) (maxSize * ControllerConstants.MAX_NUMBER_OF_CLAN_COLEADERS);
    int numOfColeaders=0;
    for(int i=0; i<ucs.size();i++) {
    	if(ucs.get(i).getStatus() == UserClanStatus.COLEADER) {
    		numOfColeaders++;
    	}
    }
    
    if(numOfColeaders > maxColeaders) {
    	resBuilder.setStatus(CreateClanColeaderStatus.OTHER_FAIL);
    	log.error("clan has more than max coleaders");
    	return false;
    }
    	
    
    if(numOfColeaders == maxColeaders && accept) {
    	resBuilder.setStatus(CreateClanColeaderStatus.MAX_COLEADERS);
    	log.error("clan already has max coleaders");
    	return false;
    }
    
//    if(uc.getStatus() == UserClanStatus.COLEADER && accept) {
//    	resBuilder.setStatus(CreateClanColeaderStatus.ALREADY_COLEADER);
//    	log.error("candidate is already coleader");
//    	return false;
//    }
    
    if(uc.getStatus() == UserClanStatus.COLEADER && uc2.getStatus() == UserClanStatus.COLEADER) {
    	resBuilder.setStatus(CreateClanColeaderStatus.OTHER_FAIL);
    	log.error("coleader trying to demote coleader");
    	return false;
    }
    
    resBuilder.setStatus(CreateClanColeaderStatus.SUCCESS);
    return true;
  }

}
