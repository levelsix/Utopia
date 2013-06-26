package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.CreateClanColeaderRequestEvent;
import com.lvl6.events.response.CreateClanColeaderResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.misc.MiscMethods;
import com.lvl6.misc.Notification;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.CreateClanColeaderRequestProto;
import com.lvl6.proto.EventProto.CreateClanColeaderResponseProto;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanResponseProto.CreateClanColeaderStatus;
import com.lvl6.proto.EventProto.CreateClanColeaderResponseProto.Builder;
import com.lvl6.proto.EventProto.CreateClanColeaderResponseProto.CreateClanColeaderStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.ClanTierLevelRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class CreateClanColeaderController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  //For sending messages to online people, NOTIFICATION FEATURE
  @Resource(name = "outgoingGameEventsHandlerExecutor")
  protected TaskExecutor executor;
  public TaskExecutor getExecutor() {
	  return executor;
  }
  public void setExecutor(TaskExecutor executor) {
	  this.executor = executor;
  }
  @Resource(name = "playersByPlayerId")
  protected Map<Integer, ConnectedPlayer> playersByPlayerId;
  public Map<Integer, ConnectedPlayer> getPlayersByPlayerId() {
	  return playersByPlayerId;
  }
  public void setPlayersByPlayerId(
		  Map<Integer, ConnectedPlayer> playersByPlayerId) {
	  this.playersByPlayerId = playersByPlayerId;
  }
  
  public CreateClanColeaderController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new CreateClanColeaderRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_CREATE_CLAN_COLEADER_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    CreateClanColeaderRequestProto reqProto = ((CreateClanColeaderRequestEvent)event).getCreateClanColeaderRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userId = reqProto.getUserId();
    boolean makeClanColeader = reqProto.getMakeColeader();
    
    CreateClanColeaderResponseProto.Builder resBuilder = CreateClanColeaderResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    server.lockPlayer(userId, this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      
      boolean legitCreate = checkLegitCreate(resBuilder, user, clanName, tag);

      int clanId = ControllerConstants.NOT_SET;
      if (legitCreate) {
        String description = "Welcome to " + clanName + "!";
        clanId = InsertUtils.get().insertClan(clanName, user.getId(), createTime, description,
            tag, MiscMethods.checkIfGoodSide(user.getType()), requestToJoinRequired);
        if (clanId <= 0) {
          legitCreate = false;
          resBuilder.setStatus(CreateClanColeaderStatus.OTHER_FAIL);
        } else {
          resBuilder.setClanInfo(CreateInfoProtoUtils.createMinimumClanProtoFromClan(
              new Clan(
        		  clanId, clanName, user.getId(), createTime, description, tag, 
        		  MiscMethods.checkIfGoodSide(user.getType()), initialClanLevel,
        		  requestToJoinRequired)
              ));
        }
      }
      
      CreateClanColeaderResponseEvent resEvent = new CreateClanColeaderResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setCreateClanColeaderResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      if (legitCreate) {
        previousGold = user.getDiamonds();
        
        Map<String, Integer> money = new HashMap<String, Integer>();
        writeChangesToDB(user, clanId, money);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        sendGeneralNotification(user.getName(), clanName);
        
        writeToUserCurrencyHistory(user, createTime, money, previousGold);
      }
    } catch (Exception e) {
      log.error("exception in CreateClanColeader processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
      server.unlockPlayer(userId, this.getClass().getSimpleName());
    }
  }

  private void writeChangesToDB(User user, User candidate, Boolean makeColeader) {
  	if (makeColeader) {
      if (!UpdateUtils.get().updateUserClanStatus(candidate.getId(), user.getClanId(), UserClanStatus.COLEADER)) {
        log.error("problem with updating user clan status to coleader for candidate " + candidate + " and clan id "+ user.getClanId());
      }
    } else {
      if (!UpdateUtils.get().updateUserClanStatus(candidate.getId(), user.getClanId(), UserClanStatus.MEMBER)) {
      	log.error("problem with updating user clan status to member for candidate " + candidate + " and clan id "+ user.getClanId()); 
      }
    }
  }

  private boolean checkLegitDecision(Builder resBuilder, User user, User candidate, boolean makeColeader) {
    if (user == null || candidate == null) {
      resBuilder.setStatus(CreateClanColeaderStatus.OTHER_FAIL);
      log.error("user is " + user + " candidate is " + candidate);
      return false;      
    }
    Clan clan = ClanRetrieveUtils.getClanWithId(user.getClanId());
    
    int clanId = clan.getId();
    //user making decision is leader
    if (clan.getOwnerId() != user.getId()) {
      resBuilder.setStatus(CreateClanColeaderStatus.NOT_OWNER);
      log.error("clan owner isn't this guy, clan owner id is " + clan.getOwnerId());
      return false;      
    }
    //check if requester is already in a clan
    if (0 < requester.getClanId()) {
    	resBuilder.setStatus(CreateClanColeaderStatus.ALREADY_IN_A_CLAN);
    	log.error("trying to accept a user that is already in a clan");
    	//the other requests in user_clans table that have a status of 2 (requesting to join clan)
    	//are deleted later on in writeChangesToDB
    	return false;
    }
    //int clanId = clan.getId();
    UserClan uc = RetrieveUtils.userClanRetrieveUtils().getSpecificUserClan(requester.getId(), clanId);
    if (uc == null || uc.getStatus() != UserClanStatus.REQUESTING) {
      resBuilder.setStatus(CreateClanColeaderStatus.NOT_A_REQUESTER);
      log.error("requester has not requested for clan with id " + user.getClanId());
      return false;
    }
    if (ControllerConstants.CLAN__ALLIANCE_CLAN_ID_THAT_IS_EXCEPTION_TO_LIMIT == clanId ||
        ControllerConstants.CLAN__LEGION_CLAN_ID_THAT_IS_EXCEPTION_TO_LIMIT == clanId) {
      return true;
    }
    List<UserClan> ucs = RetrieveUtils.userClanRetrieveUtils().getUserClanMembersInClan(clanId);
    int maxSize = ClanTierLevelRetrieveUtils.getClanTierLevel(clan.getCurrentTierLevel()).getMaxClanSize();
    if (ucs.size() >= maxSize && accept) {
      resBuilder.setStatus(CreateClanColeaderStatus.OTHER_FAIL);
      log.warn("user error: trying to add user into already full clan with id " + user.getClanId());
      return false;      
    }
    resBuilder.setStatus(CreateClanColeaderStatus.SUCCESS);
    return true;
  }
  
  private void sendGeneralNotification (String userName, String clanName) {
	  Notification createClanNotification = new Notification ();
	  createClanNotification.setAsClanCreated(userName, clanName);
	  
	  MiscMethods.writeGlobalNotification(createClanNotification, server);
  }
  
  private void writeToUserCurrencyHistory(User aUser, Timestamp date, Map<String, Integer> money,
      int previousGold) {
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String gold = MiscMethods.gold;
    String reasonForChange = ControllerConstants.UCHRFC__CREATE_CLAN;
    
    previousGoldSilver.put(gold, previousGold);
    reasonsForChanges.put(gold, reasonForChange);
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, money, previousGoldSilver, reasonsForChanges);
  }
}
