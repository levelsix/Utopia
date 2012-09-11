package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.LeaveClanRequestEvent;
import com.lvl6.events.response.LeaveClanResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.LeaveClanRequestProto;
import com.lvl6.proto.EventProto.LeaveClanResponseProto;
import com.lvl6.proto.EventProto.LeaveClanResponseProto.Builder;
import com.lvl6.proto.EventProto.LeaveClanResponseProto.LeaveClanStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

@Component @DependsOn("gameServer") public class LeaveClanController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public LeaveClanController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new LeaveClanRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_LEAVE_CLAN_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
//    LeaveClanRequestProto reqProto = ((LeaveClanRequestEvent)event).getLeaveClanRequestProto();
//
//    MinimumUserProto senderProto = reqProto.getSender();
//    int clanId = reqProto.getClanId();
//    int newOwner = reqProto.getNewOwner();
//    
//    LeaveClanResponseProto.Builder resBuilder = LeaveClanResponseProto.newBuilder();
//    resBuilder.setSender(senderProto);
//
//    server.lockPlayer(senderProto.getUserId());
//    try {
//      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
//      Clan clan = ClanRetrieveUtils.getClanWithId(clanId);
//      
//
//      boolean legitCreate = checkLegitCreate(resBuilder, user, clanName, description);
//
//      if (legitCreate) {
//        Timestamp createTime = new Timestamp(new Date().getTime());
//        int clanId = InsertUtils.get().insertClan(clanName, user.getId(), createTime, description);
//        if (clanId <= 0) {
//          legitCreate = false;
//        } else {
//          resBuilder.setClanInfo(CreateInfoProtoUtils.createFullClanProtoFromClan(new Clan(clanId, clanName, user.getId(), createTime, description)));
//        }
//      }
//      
//      LeaveClanResponseEvent resEvent = new LeaveClanResponseEvent(senderProto.getUserId());
//      resEvent.setTag(event.getTag());
//      resEvent.setLeaveClanResponseProto(resBuilder.build());  
//      server.writeEvent(resEvent);
//
//      if (legitCreate) {
//        writeChangesToDB(user);
//        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
//        resEventUpdate.setTag(event.getTag());
//        server.writeEvent(resEventUpdate);
//      }
//    } catch (Exception e) {
//      log.error("exception in LeaveClan processEvent", e);
//    } finally {
//      server.unlockPlayer(senderProto.getUserId());
//    }
//  }
//
//  private void writeChangesToDB(User user) {
//    if (!user.updateRelativeDiamondsNaive(-1*ControllerConstants.CREATE_CLAN__DIAMOND_PRICE_TO_CREATE_CLAN)) {
//      log.error("problem with decreasing user diamonds for creating clan");
//    }
//  }
//
//  private boolean checkLegitCreate(Builder resBuilder, User user, String clanName, String description) {
//    if (user == null || clanName == null || clanName.length() <= 0 || description == null || description.length() <= 0) {
//      resBuilder.setStatus(LeaveClanStatus.OTHER_FAIL);
//      log.error("user is null");
//      return false;      
//    }
//    if (user.getDiamonds() < ControllerConstants.CREATE_CLAN__DIAMOND_PRICE_TO_CREATE_CLAN) {
//      resBuilder.setStatus(LeaveClanStatus.NOT_ENOUGH_DIAMONDS);
//      log.error("user only has " + user.getDiamonds() + ", needs " + ControllerConstants.CREATE_CLAN__DIAMOND_PRICE_TO_CREATE_CLAN);
//      return false;
//    }
//    if (clanName.length() > ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_NAME) {
//      resBuilder.setStatus(LeaveClanStatus.OTHER_FAIL);
//      log.error("clan name " + clanName + " is more than " + ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_NAME + " characters");
//      return false;
//    }
//    
//    if (description.length() > ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_DESCRIPTION) {
//      resBuilder.setStatus(LeaveClanStatus.OTHER_FAIL);
//      log.error("clan description " + description + " is more than " + ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_DESCRIPTION + " characters");
//      return false;
//    }
//    
//    if (user.getClanId() > 0) {
//      resBuilder.setStatus(LeaveClanStatus.ALREADY_IN_CLAN);
//      log.error("user already in clan with id " + user.getClanId());
//      return false;
//    }
//    Clan clan = ClanRetrieveUtils.getClanWithName(clanName);
//    if (clan != null) {
//      resBuilder.setStatus(LeaveClanStatus.NAME_TAKEN);
//      log.error("clan name already taken with name " + clanName);
//      return false;
//    }
//    resBuilder.setStatus(LeaveClanStatus.SUCCESS);
//    return true;
  }
}
