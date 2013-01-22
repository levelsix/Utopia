package com.lvl6.server.controller;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.events.request.ChangeClanDescriptionRequestEvent;
import com.lvl6.events.response.ChangeClanDescriptionResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.ChangeClanDescriptionRequestProto;
import com.lvl6.proto.EventProto.ChangeClanDescriptionResponseProto;
import com.lvl6.proto.EventProto.ChangeClanDescriptionResponseProto.Builder;
import com.lvl6.proto.EventProto.ChangeClanDescriptionResponseProto.ChangeClanDescriptionStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class ChangeClanDescriptionController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public ChangeClanDescriptionController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new ChangeClanDescriptionRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_CHANGE_CLAN_DESCRIPTION_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    ChangeClanDescriptionRequestProto reqProto = ((ChangeClanDescriptionRequestEvent)event).getChangeClanDescriptionRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    String description = reqProto.getDescription();

    ChangeClanDescriptionResponseProto.Builder resBuilder = ChangeClanDescriptionResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      Clan clan = ClanRetrieveUtils.getClanWithId(user.getClanId());
      
      boolean legitChange = checkLegitChange(resBuilder, user, description, clan);

      if (legitChange) {
        writeChangesToDB(user, description);
        Clan newClan = ClanRetrieveUtils.getClanWithId(clan.getId());
        resBuilder.setMinClan(CreateInfoProtoUtils.createMinimumClanProtoFromClan(newClan));
        resBuilder.setFullClan(CreateInfoProtoUtils.createFullClanProtoWithClanSize(newClan));
      }
      
      ChangeClanDescriptionResponseEvent resEvent = new ChangeClanDescriptionResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setChangeClanDescriptionResponseProto(resBuilder.build());  
      server.writeClanEvent(resEvent, clan.getId());

      if (legitChange) {
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in ChangeClanDescription processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    }
  }

  private boolean checkLegitChange(Builder resBuilder, User user, String description, Clan clan) {
    if (user == null || description == null || description.length() <= 0 || clan == null) {
      resBuilder.setStatus(ChangeClanDescriptionStatus.OTHER_FAIL);
      log.error("user is " + user + ", description is " + description);
      return false;      
    }
    if (description.length() > ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_DESCRIPTION) {
      resBuilder.setStatus(ChangeClanDescriptionStatus.TOO_LONG);
      log.error("description is " + description + ", and length of that is " + description.length() + ", max size is " + 
          ControllerConstants.CREATE_CLAN__MAX_CHAR_LENGTH_FOR_CLAN_DESCRIPTION);
      return false;      
    }
    if (user.getClanId() <= 0) {
      resBuilder.setStatus(ChangeClanDescriptionStatus.NOT_IN_CLAN);
      log.error("user not in clan");
      return false;      
    }
    if (clan.getOwnerId() != user.getId()) {
      resBuilder.setStatus(ChangeClanDescriptionStatus.NOT_OWNER);
      log.error("clan owner isn't this guy, clan owner id is " + clan.getOwnerId());
      return false;      
    }
    resBuilder.setStatus(ChangeClanDescriptionStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, String description) {
    if (!UpdateUtils.get().updateClanOwnerDescriptionForClan(user.getClanId(), ControllerConstants.NOT_SET, description)) {
      log.error("problem with updating clan description for clan with id " + user.getClanId());
    }
  }
}
