package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ApproveOrRejectRequestToJoinClanRequestEvent;
import com.lvl6.events.response.ApproveOrRejectRequestToJoinClanResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanRequestProto;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanResponseProto;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanResponseProto.Builder;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanResponseProto.ApproveOrRejectRequestToJoinClanStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class ApproveOrRejectRequestToJoinClanController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public ApproveOrRejectRequestToJoinClanController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new ApproveOrRejectRequestToJoinClanRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_APPROVE_OR_REJECT_REQUEST_TO_JOIN_CLAN_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
//    ApproveOrRejectRequestToJoinClanRequestProto reqProto = ((ApproveOrRejectRequestToJoinClanRequestEvent)event).getApproveOrRejectRequestToJoinClanRequestProto();
//
//    MinimumUserProto senderProto = reqProto.getSender();
//    String description = reqProto.getDescription();
//
//    ApproveOrRejectRequestToJoinClanResponseProto.Builder resBuilder = ApproveOrRejectRequestToJoinClanResponseProto.newBuilder();
//    resBuilder.setSender(senderProto);
//
//    server.lockPlayer(senderProto.getUserId());
//    try {
//      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
//
//      boolean legitChange = checkLegitChange(resBuilder, user, description);
//
//      ApproveOrRejectRequestToJoinClanResponseEvent resEvent = new ApproveOrRejectRequestToJoinClanResponseEvent(senderProto.getUserId());
//      resEvent.setTag(event.getTag());
//      resEvent.setApproveOrRejectRequestToJoinClanResponseProto(resBuilder.build());  
//      server.writeEvent(resEvent);
//
//      if (legitChange) {
//        writeChangesToDB(user, description);
//        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
//        resEventUpdate.setTag(event.getTag());
//        server.writeEvent(resEventUpdate);
//      }
//    } catch (Exception e) {
//      log.error("exception in ApproveOrRejectRequestToJoinClan processEvent", e);
//    } finally {
//      server.unlockPlayer(senderProto.getUserId());
//    }
//  }
//
//  private boolean checkLegitChange(Builder resBuilder, User user, String description) {
//    if (user == null || description == null || description.length() <= 0) {
//      resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.OTHER_FAIL);
//      log.error("user is " + user + ", description is " + description);
//      return false;      
//    }
//    if (description.length() > ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_DESCRIPTION) {
//      resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.TOO_LONG);
//      log.error("description is " + description + ", and length of that is " + description.length() + ", max size is " + 
//          ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_DESCRIPTION);
//      return false;      
//    }
//    if (user.getClanId() <= 0) {
//      resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.NOT_IN_CLAN);
//      log.error("user not in clan");
//      return false;      
//    }
//    Clan clan = ClanRetrieveUtils.getClanWithId(user.getClanId());
//    if (clan.getOwnerId() != user.getId()) {
//      resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.NOT_OWNER);
//      log.error("clan owner isn't this guy, clan owner id is " + clan.getOwnerId());
//      return false;      
//    }
//    resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.SUCCESS);
//    return true;
//  }
//
//  private void writeChangesToDB(User user, String description) {
//    if (!UpdateUtils.get().updateClanOwnerDescriptionForClan(user.getClanId(), ControllerConstants.NOT_SET, description)) {
//      log.error("problem with updating clan description for clan with id " + user.getClanId());
//    }
  }
}
