package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.events.request.RequestJoinClanRequestEvent;
import com.lvl6.events.response.RequestJoinClanResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.misc.MiscMethods;
import com.lvl6.proto.EventProto.RequestJoinClanRequestProto;
import com.lvl6.proto.EventProto.RequestJoinClanResponseProto;
import com.lvl6.proto.EventProto.RequestJoinClanResponseProto.Builder;
import com.lvl6.proto.EventProto.RequestJoinClanResponseProto.RequestJoinClanStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.QuestUtils;

@Component @DependsOn("gameServer") public class RequestJoinClanController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RequestJoinClanController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RequestJoinClanRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_REQUEST_JOIN_CLAN_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RequestJoinClanRequestProto reqProto = ((RequestJoinClanRequestEvent)event).getRequestJoinClanRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int clanId = reqProto.getClanId();

    RequestJoinClanResponseProto.Builder resBuilder = RequestJoinClanResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setClanId(clanId);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      Clan clan = ClanRetrieveUtils.getClanWithId(clanId);

      boolean legitRequest = checkLegitRequest(resBuilder, user, clan);

      resBuilder.setRequester(CreateInfoProtoUtils.createMinimumUserProtoForClans(user, UserClanStatus.REQUESTING));

      RequestJoinClanResponseEvent resEvent = new RequestJoinClanResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRequestJoinClanResponseProto(resBuilder.build());
      //in case user is not online write an apns
      server.writeAPNSNotificationOrEvent(resEvent);
      //server.writeEvent(resEvent);

      if (legitRequest) {
        server.writeClanEvent(resEvent, clan.getId());

        writeChangesToDB(user, clanId);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, SpecialQuestAction.REQUEST_JOIN_CLAN, true);
      }
    } catch (Exception e) {
      log.error("exception in RequestJoinClan processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private boolean checkLegitRequest(Builder resBuilder, User user, Clan clan) {
    if (user == null || clan == null) {
      resBuilder.setStatus(RequestJoinClanStatus.OTHER_FAIL);
      log.error("user is " + user + ", clan is " + clan);
      return false;      
    }
    if (user.getClanId() > 0) {
      resBuilder.setStatus(RequestJoinClanStatus.ALREADY_IN_CLAN);
      log.error("user is already in clan with id " + user.getClanId());
      return false;      
    }
    if (clan.isGood() != MiscMethods.checkIfGoodSide(user.getType())) {
      resBuilder.setStatus(RequestJoinClanStatus.WRONG_SIDE);
      log.error("user is good " + user.getType() + ", user type is good " + user.getType());
      return false;      
    }
    UserClan uc = RetrieveUtils.userClanRetrieveUtils().getSpecificUserClan(user.getId(), clan.getId());
    if (uc != null) {
      resBuilder.setStatus(RequestJoinClanStatus.REQUEST_ALREADY_FILED);
      log.error("user clan already exists for this: " + uc);
      return false;      
    }
    resBuilder.setStatus(RequestJoinClanStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, int clanId) {
    if (!InsertUtils.get().insertUserClan(user.getId(), clanId, UserClanStatus.REQUESTING, new Timestamp(new Date().getTime()))) {
      log.error("problem with inserting user clan data for user " + user + ", and clan id " + clanId);
    }
  }
}
