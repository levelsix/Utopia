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
    String description = reqProto.getDescription();
    
    CreateClanResponseProto.Builder resBuilder = CreateClanResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legitCreate = checkLegitCreate(resBuilder, user, clanName, description);

      if (legitCreate) {
        Timestamp createTime = new Timestamp(new Date().getTime());
        int clanId = InsertUtils.get().insertClan(clanName, user.getId(), createTime, description);
        if (clanId <= 0) {
          legitCreate = false;
        } else {
          resBuilder.setClanInfo(CreateInfoProtoUtils.createFullClanProtoFromClan(new Clan(clanId, clanName, user.getId(), createTime, description)));
        }
      }
      
      CreateClanResponseEvent resEvent = new CreateClanResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setCreateClanResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitCreate) {
        writeChangesToDB(user);
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

  private void writeChangesToDB(User user) {
    if (!user.updateRelativeDiamondsNaive(-1*ControllerConstants.CREATE_CLAN__DIAMOND_PRICE)) {
      log.error("problem with decreasing user diamonds for creating clan");
    }
  }

  private boolean checkLegitCreate(Builder resBuilder, User user, String clanName, String description) {
    if (user == null || clanName == null || clanName.length() <= 0 || description == null || description.length() <= 0) {
      resBuilder.setStatus(CreateClanStatus.OTHER_FAIL);
      log.error("user is null");
      return false;      
    }
    if (user.getDiamonds() < ControllerConstants.CREATE_CLAN__DIAMOND_PRICE) {
      resBuilder.setStatus(CreateClanStatus.NOT_ENOUGH_DIAMONDS);
      log.error("user only has " + user.getDiamonds() + ", needs " + ControllerConstants.CREATE_CLAN__DIAMOND_PRICE);
      return false;
    }
    if (clanName.length() > ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH) {
      resBuilder.setStatus(CreateClanStatus.OTHER_FAIL);
      log.error("clan name " + clanName + " is more than " + ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH + " characters");
      return false;
    }
    if (user.getClanId() > 0) {
      resBuilder.setStatus(CreateClanStatus.ALREADY_IN_CLAN);
      log.error("user already in clan with id " + user.getClanId());
      return false;
    }
    Clan clan = ClanRetrieveUtils.getClanWithName(clanName);
    if (clan != null) {
      resBuilder.setStatus(CreateClanStatus.NAME_TAKEN);
      log.error("clan name already taken with name " + clanName);
      return false;
    }
    resBuilder.setStatus(CreateClanStatus.SUCCESS);
    return true;
  }
}
