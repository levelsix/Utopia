package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ChangeClanDescriptionRequestEvent;
import com.lvl6.events.response.ChangeClanDescriptionResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.proto.EventProto.ChangeClanDescriptionRequestProto;
import com.lvl6.proto.EventProto.ChangeClanDescriptionResponseProto;
import com.lvl6.proto.EventProto.ChangeClanDescriptionResponseProto.Builder;
import com.lvl6.proto.EventProto.ChangeClanDescriptionResponseProto.ChangeClanDescriptionStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

@Component @DependsOn("gameServer") public class ChangeClanDescriptionController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public ChangeClanDescriptionController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new ChangeClanDescriptionRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRACT_REQUEST_JOIN_CLAN_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
//    ChangeClanDescriptionRequestProto reqProto = ((ChangeClanDescriptionRequestEvent)event).getChangeClanDescriptionRequestProto();
//
//    MinimumUserProto senderProto = reqProto.getSender();
//    int clanId = reqProto.getClanId();
//
//    ChangeClanDescriptionResponseProto.Builder resBuilder = ChangeClanDescriptionResponseProto.newBuilder();
//    resBuilder.setSender(senderProto);
//
//    server.lockPlayer(senderProto.getUserId());
//    try {
//      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
//      Clan clan = ClanRetrieveUtils.getClanWithId(clanId);
//
//      boolean legitRetract = checkLegitRequest(resBuilder, user, clan);
//
//      ChangeClanDescriptionResponseEvent resEvent = new ChangeClanDescriptionResponseEvent(senderProto.getUserId());
//      resEvent.setTag(event.getTag());
//      resEvent.setChangeClanDescriptionResponseProto(resBuilder.build());  
//      server.writeEvent(resEvent);
//
//      if (legitRetract) {
//        writeChangesToDB(user, clanId);
//        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
//        resEventUpdate.setTag(event.getTag());
//        server.writeEvent(resEventUpdate);
//      }
//    } catch (Exception e) {
//      log.error("exception in ChangeClanDescription processEvent", e);
//    } finally {
//      server.unlockPlayer(senderProto.getUserId());
//    }
//  }
//
//  private boolean checkLegitRequest(Builder resBuilder, User user, Clan clan) {
//    if (user == null || clan == null) {
//      resBuilder.setStatus(ChangeClanDescriptionStatus.OTHER_FAIL);
//      log.error("user is " + user + ", clan is " + clan);
//      return false;      
//    }
//    if (user.getClanId() > 0) {
//      resBuilder.setStatus(ChangeClanDescriptionStatus.ALREADY_IN_CLAN);
//      log.error("user is already in clan with id " + user.getClanId());
//      return false;      
//    }
//    UserClan uc = RetrieveUtils.userClanRetrieveUtils().getSpecificUserClan(user.getId(), clan.getId());
//    if (uc == null || uc.getStatus() != UserClanStatus.REQUESTING) {
//      resBuilder.setStatus(ChangeClanDescriptionStatus.DID_NOT_REQUEST);
//      log.error("user clan request has not been filed");
//      return false;      
//    }
//    resBuilder.setStatus(ChangeClanDescriptionStatus.SUCCESS);
//    return true;
//  }
//
//  private void writeChangesToDB(User user, int clanId) {
//    if (!DeleteUtils.get().deleteUserClan(user.getId(), clanId)) {
//      log.error("problem with deleting user clan data for user " + user + ", and clan id " + clanId);
//    }
  }
}
