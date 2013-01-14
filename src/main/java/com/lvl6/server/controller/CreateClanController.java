package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.CreateClanRequestEvent;
import com.lvl6.events.response.CreateClanResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.misc.Notification;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.CreateClanRequestProto;
import com.lvl6.proto.EventProto.CreateClanResponseProto;
import com.lvl6.proto.EventProto.CreateClanResponseProto.Builder;
import com.lvl6.proto.EventProto.CreateClanResponseProto.CreateClanStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

@Component @DependsOn("gameServer") public class CreateClanController extends EventController {

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
  
  public CreateClanController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new CreateClanRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_CREATE_CLAN_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    CreateClanRequestProto reqProto = ((CreateClanRequestEvent)event).getCreateClanRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    String clanName = reqProto.getName();
    String tag = reqProto.getTag();
    int initialClanLevel = ControllerConstants.CREATE_CLAN__INITIAL_CLAN_LEVEL; 
    
    CreateClanResponseProto.Builder resBuilder = CreateClanResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      Timestamp createTime = new Timestamp(new Date().getTime());

      boolean legitCreate = checkLegitCreate(resBuilder, user, clanName, tag);

      int clanId = ControllerConstants.NOT_SET;
      if (legitCreate) {
        String description = "Welcome to " + clanName + "!";
        clanId = InsertUtils.get().insertClan(clanName, user.getId(), createTime, description, tag, MiscMethods.checkIfGoodSide(user.getType()));
        if (clanId <= 0) {
          legitCreate = false;
          resBuilder.setStatus(CreateClanStatus.OTHER_FAIL);
        } else {
          resBuilder.setClanInfo(CreateInfoProtoUtils.createMinimumClanProtoFromClan(
              new Clan(
        		  clanId, clanName, user.getId(), createTime, description, tag, 
        		  MiscMethods.checkIfGoodSide(user.getType()), initialClanLevel)
              ));
        }
      }
      
      CreateClanResponseEvent resEvent = new CreateClanResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setCreateClanResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      if (legitCreate) {
        Map<String, Integer> money = new HashMap<String, Integer>();
        writeChangesToDB(user, clanId, money);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        sendGeneralNotification(user.getName(), clanName);
        
        writeToUserCurrencyHistory(user, createTime, money);
      }
    } catch (Exception e) {
      log.error("exception in CreateClan processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private void writeChangesToDB(User user, int clanId, Map<String, Integer> money) {
    int goldChange = -1*ControllerConstants.CREATE_CLAN__DIAMOND_PRICE_TO_CREATE_CLAN;
    if (!user.updateRelativeDiamondsAbsoluteClan(goldChange, clanId)) {
      log.error("problem with decreasing user diamonds for creating clan");
    } else {
      //everything went well
      money.put(MiscMethods.gold, goldChange);
    }
    if (!InsertUtils.get().insertUserClan(user.getId(), clanId, UserClanStatus.MEMBER, new Timestamp(new Date().getTime()))) {
      log.error("problem with inserting user clan data for user " + user + ", and clan id " + clanId);
    }
    DeleteUtils.get().deleteUserClansForUserExceptSpecificClan(user.getId(), clanId);
  }

  private boolean checkLegitCreate(Builder resBuilder, User user, String clanName, String tag) {
    if (user == null || clanName == null || clanName.length() <= 0 || tag == null || tag.length() <= 0) {
      resBuilder.setStatus(CreateClanStatus.OTHER_FAIL);
      log.error("user is null");
      return false;      
    }
    if (user.getDiamonds() < ControllerConstants.CREATE_CLAN__DIAMOND_PRICE_TO_CREATE_CLAN) {
      resBuilder.setStatus(CreateClanStatus.NOT_ENOUGH_DIAMONDS);
      log.error("user only has " + user.getDiamonds() + ", needs " + ControllerConstants.CREATE_CLAN__DIAMOND_PRICE_TO_CREATE_CLAN);
      return false;
    }
    if (clanName.length() > ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_NAME) {
      resBuilder.setStatus(CreateClanStatus.OTHER_FAIL);
      log.error("clan name " + clanName + " is more than " + ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_NAME + " characters");
      return false;
    }
    
    if (tag.length() > ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_TAG) {
      resBuilder.setStatus(CreateClanStatus.INVALID_TAG_LENGTH);
      log.error("clan tag " + tag + " is more than " + ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_TAG + " characters");
      return false;
    }
    
    if (user.getClanId() > 0) {
      resBuilder.setStatus(CreateClanStatus.ALREADY_IN_CLAN);
      log.error("user already in clan with id " + user.getClanId());
      return false;
    }
    Clan clan = ClanRetrieveUtils.getClanWithNameOrTag(clanName, tag);
    if (clan != null) {
      if (clan.getName().equalsIgnoreCase(clanName)) {
        resBuilder.setStatus(CreateClanStatus.NAME_TAKEN);
        log.error("clan name already taken with name " + clanName);
        return false;
      }
      if (clan.getTag().equalsIgnoreCase(tag)) {
        resBuilder.setStatus(CreateClanStatus.TAG_TAKEN);
        log.error("clan tag already taken with tag " + tag);
        return false;
      }
    }
    resBuilder.setStatus(CreateClanStatus.SUCCESS);
    return true;
  }
  
  private void sendGeneralNotification (String userName, String clanName) {
	  Notification createClanNotification = new Notification ();
	  createClanNotification.setAsClanCreated(userName, clanName);
	  
	  MiscMethods.writeGlobalNotification(createClanNotification, server);
  }
  
  private void writeToUserCurrencyHistory(User aUser, Timestamp date, Map<String, Integer> money) {
    try {
      if(money.isEmpty()) {
        return;
      }
      int userId = aUser.getId();
      int isSilver = 0;
      int currencyChange = money.get(MiscMethods.gold);
      int currencyBefore = aUser.getDiamonds() - currencyChange;
      String reasonForChange = ControllerConstants.UCHRFC__CREATE_CLAN;
      int inserted = InsertUtils.get().insertIntoUserCurrencyHistory(userId, date, isSilver,
          currencyChange, currencyBefore, reasonForChange);

      log.info("Should be 1. Rows inserted into user_currency_history: " + inserted);
    } catch (Exception e) {
      log.error("Maybe table's not there or duplicate keys? " + e.toString());
    }
  }
}
