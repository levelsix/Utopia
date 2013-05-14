package com.lvl6.server.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ApproveOrRejectRequestToJoinClanRequestEvent;
import com.lvl6.events.response.ApproveOrRejectRequestToJoinClanResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanRequestProto;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanResponseProto;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanResponseProto.ApproveOrRejectRequestToJoinClanStatus;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanResponseProto.Builder;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.ClanTierLevelRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class ApproveOrRejectRequestToJoinClanController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    server.lockPlayer(requesterId, this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      User requester = RetrieveUtils.userRetrieveUtils().getUserById(requesterId);

      boolean legitDecision = checkLegitDecision(resBuilder, user, requester, accept);
      
      if (legitDecision) {
        Clan clan = ClanRetrieveUtils.getClanWithId(user.getClanId());
        resBuilder.setMinClan(CreateInfoProtoUtils.createMinimumClanProtoFromClan(clan));
        resBuilder.setFullClan(CreateInfoProtoUtils.createFullClanProtoWithClanSize(clan));
      }

      ApproveOrRejectRequestToJoinClanResponseEvent resEvent = new ApproveOrRejectRequestToJoinClanResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setApproveOrRejectRequestToJoinClanResponseProto(resBuilder.build());  

      if (legitDecision) {
        server.writeClanEvent(resEvent, user.getClanId());

        // Send message to the new guy
        ApproveOrRejectRequestToJoinClanResponseEvent resEvent2 = new ApproveOrRejectRequestToJoinClanResponseEvent(requesterId);
        resEvent2.setApproveOrRejectRequestToJoinClanResponseProto(resBuilder.build());
        //in case user is not online write an apns
        server.writeAPNSNotificationOrEvent(resEvent2);
        //server.writeEvent(resEvent2);

        writeChangesToDB(user, requester, accept);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      } else {
        server.writeEvent(resEvent);
      }
    } catch (Exception e) {
      log.error("exception in ApproveOrRejectRequestToJoinClan processEvent", e);
    } finally {
      server.unlockPlayer(requesterId, this.getClass().getSimpleName());
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
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
      DeleteUtils.get().deleteUserClansForUserExceptSpecificClan(requester.getId(), user.getClanId());
    } else {
      if (!DeleteUtils.get().deleteUserClan(requester.getId(), user.getClanId())) {
        log.error("problem with deleting user clan info for requester with id " + requester.getId() + " and clan id " + user.getClanId()); 
      }
    }
  }

  private boolean checkLegitDecision(Builder resBuilder, User user, User requester, boolean accept) {
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
    //check if requester is already in a clan
    if (0 < requester.getClanId()) {
    	resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.ALREADY_IN_A_CLAN);
    	log.error("trying to accept a user that is already in a clan");
    	//the other requests in user_clans table that have a status of 2 (requesting to join clan)
    	//are deleted later on in writeChangesToDB
    	return false;
    }
    int clanId = clan.getId();
    UserClan uc = RetrieveUtils.userClanRetrieveUtils().getSpecificUserClan(requester.getId(), clanId);
    if (uc == null || uc.getStatus() != UserClanStatus.REQUESTING) {
      resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.NOT_A_REQUESTER);
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
      resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.OTHER_FAIL);
      log.warn("user error: trying to add user into already full clan with id " + user.getClanId());
      return false;      
    }
    resBuilder.setStatus(ApproveOrRejectRequestToJoinClanStatus.SUCCESS);
    return true;
  }


}
