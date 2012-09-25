package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.CreateClanRequestEvent;
import com.lvl6.events.response.CreateClanResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.CreateClanRequestProto;
import com.lvl6.proto.EventProto.CreateClanResponseProto;
import com.lvl6.proto.EventProto.CreateClanResponseProto.Builder;
import com.lvl6.proto.EventProto.CreateClanResponseProto.CreateClanStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

@Component @DependsOn("gameServer") public class CreateClanController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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
    
    CreateClanResponseProto.Builder resBuilder = CreateClanResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legitCreate = checkLegitCreate(resBuilder, user, clanName, tag);

      int clanId = ControllerConstants.NOT_SET;
      if (legitCreate) {
        Timestamp createTime = new Timestamp(new Date().getTime());
        String description = "Welcome to " + clanName + "!";
        clanId = InsertUtils.get().insertClan(clanName, user.getId(), createTime, description, tag, MiscMethods.checkIfGoodSide(user.getType()));
        if (clanId <= 0) {
          legitCreate = false;
          resBuilder.setStatus(CreateClanStatus.OTHER_FAIL);
        } else {
          resBuilder.setClanInfo(CreateInfoProtoUtils.createMinimumClanProtoFromClan(new Clan(clanId, clanName, user.getId(), createTime, description, tag, MiscMethods.checkIfGoodSide(user.getType()))));
        }
      }
      
      CreateClanResponseEvent resEvent = new CreateClanResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setCreateClanResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitCreate) {
        writeChangesToDB(user, clanId);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in CreateClan processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private void writeChangesToDB(User user, int clanId) {
    if (!user.updateRelativeDiamondsAbsoluteClan(-1*ControllerConstants.CREATE_CLAN__DIAMOND_PRICE_TO_CREATE_CLAN, clanId)) {
      log.error("problem with decreasing user diamonds for creating clan");
    }
    if (!InsertUtils.get().insertUserClan(user.getId(), clanId, UserClanStatus.MEMBER, new Timestamp(new Date().getTime()))) {
      log.error("problem with inserting user clan data for user " + user + ", and clan id " + clanId);
    }
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
}
