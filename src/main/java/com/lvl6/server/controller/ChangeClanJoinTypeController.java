package com.lvl6.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ChangeClanJoinTypeRequestEvent;
import com.lvl6.events.response.ChangeClanJoinTypeResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.proto.EventProto.ChangeClanJoinTypeRequestProto;
import com.lvl6.proto.EventProto.ChangeClanJoinTypeResponseProto;
import com.lvl6.proto.EventProto.ChangeClanJoinTypeResponseProto.Builder;
import com.lvl6.proto.EventProto.ChangeClanJoinTypeResponseProto.ChangeClanJoinTypeStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class ChangeClanJoinTypeController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public ChangeClanJoinTypeController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new ChangeClanJoinTypeRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_CHANGE_CLAN_JOIN_TYPE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    ChangeClanJoinTypeRequestProto reqProto = ((ChangeClanJoinTypeRequestEvent)event).getChangeClanJoinTypeRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    boolean requestToJoinRequired = reqProto.getRequestToJoinRequired();

    ChangeClanJoinTypeResponseProto.Builder resBuilder = ChangeClanJoinTypeResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      Clan clan = ClanRetrieveUtils.getClanWithId(user.getClanId());
      
      boolean legitChange = checkLegitChange(resBuilder, user, clan, requestToJoinRequired);

      if (legitChange) {
        writeChangesToDB(user, requestToJoinRequired);
        Clan newClan = ClanRetrieveUtils.getClanWithId(clan.getId());
        resBuilder.setMinClan(CreateInfoProtoUtils.createMinimumClanProtoFromClan(newClan));
        resBuilder.setFullClan(CreateInfoProtoUtils.createFullClanProtoWithClanSize(newClan));
      }
      
      ChangeClanJoinTypeResponseEvent resEvent = new ChangeClanJoinTypeResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setChangeClanJoinTypeResponseProto(resBuilder.build());  
      server.writeClanEvent(resEvent, clan.getId());

      if (legitChange) {
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in ChangeClanJoinType processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    }
  }

  private boolean checkLegitChange(Builder resBuilder, User user, Clan clan,
      boolean requestToJoinRequired) {
    if (user == null || clan == null) {
      resBuilder.setStatus(ChangeClanJoinTypeStatus.OTHER_FAIL);
      log.error("user is " + user + ", clan is " + clan);
      return false;      
    }
    if (user.getClanId() <= 0) {
      resBuilder.setStatus(ChangeClanJoinTypeStatus.NOT_IN_CLAN);
      log.error("user not in clan");
      return false;      
    }
    if (clan.getOwnerId() != user.getId()) {
      resBuilder.setStatus(ChangeClanJoinTypeStatus.NOT_OWNER);
      log.error("clan owner isn't this guy, clan owner id is " + clan.getOwnerId());
      return false;      
    }
    resBuilder.setStatus(ChangeClanJoinTypeStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, boolean requestToJoinRequired) {
    if (!UpdateUtils.get().updateClanJoinTypeForClan(user.getClanId(), requestToJoinRequired)) {
      log.error("problem with updating clan join type for clan with id " + user.getClanId()
          + " requestToJoinRequired=" + requestToJoinRequired);
    }
  }
}
