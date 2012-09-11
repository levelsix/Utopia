package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

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
import com.lvl6.retrieveutils.UserClanRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

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
    LeaveClanRequestProto reqProto = ((LeaveClanRequestEvent)event).getLeaveClanRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int clanId = reqProto.getClanId();
    int newOwner = reqProto.getNewOwner();
    boolean deleteClan = !reqProto.hasNewOwner();

    LeaveClanResponseProto.Builder resBuilder = LeaveClanResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      Clan clan = ClanRetrieveUtils.getClanWithId(clanId);

      User newClanOwner = null;
      if (newOwner > 0) {
        newClanOwner = RetrieveUtils.userRetrieveUtils().getUserById(newOwner);        
      }

      boolean legitLeave = checkLegitLeave(resBuilder, user, clan, clanId, newClanOwner, deleteClan);

      LeaveClanResponseEvent resEvent = new LeaveClanResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setLeaveClanResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitLeave) {
        writeChangesToDB(user, clan, deleteClan, newClanOwner);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in LeaveClan processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private void writeChangesToDB(User user, Clan clan, boolean deleteClan, User newClanOwner) {
    List<Integer> userIds = RetrieveUtils.userClanRetrieveUtils().getUserIdsRelatedToClan(clan.getId());
    if (!UpdateUtils.get().updateUsersClanId(null, userIds)) {
      log.error("problem with marking clan id null for users with ids in " + userIds);
    } else {
      if (!DeleteUtils.get().deleteUserClanDataRelatedToClanId(clan.getId(), userIds.size())) {
        log.error("problem with deleting user clan data for clan with id " + clan.getId());
      } else {
        if (!DeleteUtils.get().deleteClanWithClanId(clan.getId())) {
          log.error("problem with deleting clan with id " + clan.getId());
        }
      }
    }
  }

  private boolean checkLegitLeave(Builder resBuilder, User user, Clan clan, int clanId, User newClanOwner, boolean deleteClan) {
    if (user == null) {
      resBuilder.setStatus(LeaveClanStatus.OTHER_FAIL);
      log.error("user is null");
      return false;      
    }
    if (clan == null) {
      resBuilder.setStatus(LeaveClanStatus.OTHER_FAIL);
      log.error("clan is null, passed in clan id was " + clanId);
      return false;     
    }
    if (user.getClanId() != clan.getId()) {
      resBuilder.setStatus(LeaveClanStatus.NOT_IN_CLAN);
      log.error("user's clan id is " + user.getClanId() + ", clan id is " + clanId);
      return false;
    }
    if (!deleteClan && (newClanOwner == null || newClanOwner.getClanId() != clan.getId())) {
      resBuilder.setStatus(LeaveClanStatus.NEW_OWNER_NOT_IN_CLAN);
      log.error("problem with new clan owner, who is " + newClanOwner + ", clan is " + clan);
      return false;     
    }
    resBuilder.setStatus(LeaveClanStatus.SUCCESS);
    return true;
  }
}
