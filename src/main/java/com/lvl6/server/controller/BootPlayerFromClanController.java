package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BootPlayerFromClanRequestEvent;
import com.lvl6.events.response.BootPlayerFromClanResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.BootPlayerFromClanRequestProto;
import com.lvl6.proto.EventProto.BootPlayerFromClanResponseProto;
import com.lvl6.proto.EventProto.BootPlayerFromClanResponseProto.Builder;
import com.lvl6.proto.EventProto.BootPlayerFromClanResponseProto.BootPlayerFromClanStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class BootPlayerFromClanController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public BootPlayerFromClanController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new BootPlayerFromClanRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_BOOT_PLAYER_FROM_CLAN_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    BootPlayerFromClanRequestProto reqProto = ((BootPlayerFromClanRequestEvent)event).getBootPlayerFromClanRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int playerToBootId = reqProto.getPlayerToBoot();

    BootPlayerFromClanResponseProto.Builder resBuilder = BootPlayerFromClanResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setPlayerToBoot(playerToBootId);

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      User playerToBoot = RetrieveUtils.userRetrieveUtils().getUserById(playerToBootId);

      boolean legitBoot = checkLegitBoot(resBuilder, user, playerToBoot);

      BootPlayerFromClanResponseEvent resEvent = new BootPlayerFromClanResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setBootPlayerFromClanResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitBoot) {
        writeChangesToDB(user, playerToBoot);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in BootPlayerFromClan processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private boolean checkLegitBoot(Builder resBuilder, User user,
      User playerToBoot) {
    if (user == null || playerToBoot == null) {
      resBuilder.setStatus(BootPlayerFromClanStatus.OTHER_FAIL);
      log.error("user is " + user + ", playerToBoot is " + playerToBoot);
      return false;      
    }
    Clan clan = ClanRetrieveUtils.getClanWithId(user.getClanId());
    if (clan.getOwnerId() != user.getId()) {
      resBuilder.setStatus(BootPlayerFromClanStatus.NOT_OWNER_OF_CLAN);
      log.error("clan owner isn't this guy, clan owner id is " + clan.getOwnerId());
      return false;      
    }
    if (playerToBoot.getClanId() != user.getClanId()) {
      resBuilder.setStatus(BootPlayerFromClanStatus.BOOTED_NOT_IN_CLAN);
      log.error("playerToBoot is not in user clan. playerToBoot is in " + playerToBoot.getClanId());
      return false;
    }
    resBuilder.setStatus(BootPlayerFromClanStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, User playerToBoot) {
    if (!playerToBoot.updateRelativeDiamondsAbsoluteClan(0, ControllerConstants.NOT_SET)) {
      log.error("problem with change playerToBoot " + playerToBoot + " clan id to nothing");
    }
    if (!DeleteUtils.get().deleteUserClan(playerToBoot.getId(), playerToBoot.getClanId())) {
      log.error("problem with deleting user clan info for playerToBoot with id " + playerToBoot.getId() + " and clan id " + playerToBoot.getClanId()); 
    }
  }
}
