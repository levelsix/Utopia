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
import com.lvl6.info.UserClan;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanRequestProto;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanResponseProto;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanResponseProto.Builder;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanResponseProto.ApproveOrRejectRequestToJoinClanStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
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
    ApproveOrRejectRequestToJoinClanRequestProto reqProto = ((ApproveOrRejectRequestToJoinClanRequestEvent)event).getApproveOrRejectRequestToJoinClanRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int requesterId = reqProto.getRequesterId();
    boolean accept = reqProto.getAccept();
    
    ApproveOrRejectRequestToJoinClanResponseProto.Builder resBuilder = ApproveOrRejectRequestToJoinClanResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setRequesterId(requesterId);
    resBuilder.setAccept(accept);
    
    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      User requester = RetrieveUtils.userRetrieveUtils().getUserById(requesterId);
      
      boolean legitDecision = checkLegitDecision(resBuilder, user, requester);

      ApproveOrRejectRequestToJoinClanResponseEvent resEvent = new ApproveOrRejectRequestToJoinClanResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setApproveOrRejectRequestToJoinClanResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitDecision) {
        writeChangesToDB(user, requester, accept);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in ApproveOrRejectRequestToJoinClan processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private void writeChangesToDB(User user, User requester, boolean accept) {
    if (accept) {
      if (!requester.updateRelativeDiamondsAbsoluteClan(0, user.getClanId())) {
        log.error("problem with change requester " + requester + " clan id to " + user.getClanId());
      }
      if (!UpdateUtils.get().updateUserClanStatus(requester.getId(), user.getClanId(), UserClanStatus.MEMBER)) {
        log.error("problem with updating user clan status to member for requester " + requester + " and clan id "+ user.getClanId());
      }
      DeleteUtils.get().deleteUserClanRequestsForUser(requester.getId());
    } else {
      if (!DeleteUtils.get().deleteUserClan(requester.getId(), user.getClanId())) {
        log.error("problem with deleting user clan info for requester with id " + requester.getId() + " and clan id " + user.getClanId()); 
      }
    }
  }

  private boolean checkLegitDecision(Builder resBuilder, User user, User requester) {
    if (user == null || requester == null) {
      resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.OTHER_FAIL);
      log.error("user is " + user + ", requester is " + requester);
      return false;      
    }
    Clan clan = ClanRetrieveUtils.getClanWithId(user.getClanId());
    if (clan.getOwnerId() != user.getId()) {
      resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.NOT_OWNER);
      log.error("clan owner isn't this guy, clan owner id is " + clan.getOwnerId());
      return false;      
    }
    UserClan uc = RetrieveUtils.userClanRetrieveUtils().getSpecificUserClan(requester.getId(), clan.getId());
    if (uc == null || uc.getStatus() != UserClanStatus.REQUESTING) {
      resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.NOT_A_REQUESTER);
      log.error("requester has not requested for clan with id " + user.getClanId());
      return false;      
    }
    resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.SUCCESS);
    return true;
  }

}
