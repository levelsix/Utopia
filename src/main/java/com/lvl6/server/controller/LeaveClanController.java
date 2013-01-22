package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.LeaveClanRequestEvent;
import com.lvl6.events.response.LeaveClanResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanTower;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.misc.MiscMethods;
import com.lvl6.proto.EventProto.ChangedClanTowerResponseProto.ReasonForClanTowerChange;
import com.lvl6.proto.EventProto.LeaveClanRequestProto;
import com.lvl6.proto.EventProto.LeaveClanResponseProto;
import com.lvl6.proto.EventProto.LeaveClanResponseProto.Builder;
import com.lvl6.proto.EventProto.LeaveClanResponseProto.LeaveClanStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.ClanTowerRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;

@Component @DependsOn("gameServer") public class LeaveClanController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  //For sending messages to online people, NOTIFICATION FEATURE
  @Resource(name = "outgoingGameEventsHandlerExecutor")
  protected TaskExecutor executor;
  public TaskExecutor getExecutor() {
    return executor;
  }
  public void setExecutor(TaskExecutor executor) {
    this.executor = executor;
  }
  @Resource(name = "playersByPlayerId")
  protected Map<Integer, ConnectedPlayer> playersByPlayerId;
  public Map<Integer, ConnectedPlayer> getPlayersByPlayerId() {
    return playersByPlayerId;
  }
  public void setPlayersByPlayerId(
      Map<Integer, ConnectedPlayer> playersByPlayerId) {
    this.playersByPlayerId = playersByPlayerId;
  }

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

    LeaveClanResponseProto.Builder resBuilder = LeaveClanResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      int clanId = (user == null) ? 0 : user.getClanId();
      Clan clan = ClanRetrieveUtils.getClanWithId(clanId);

      boolean legitLeave = checkLegitLeave(resBuilder, user, clan);

      LeaveClanResponseEvent resEvent = new LeaveClanResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setLeaveClanResponseProto(resBuilder.build());
      server.writeClanEvent(resEvent, clan.getId());

      if (legitLeave) {
        writeChangesToDB(user, clan);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);

        //clan tower stuff
        if(server.lockClanTowersTable()) {
        	try {
        		sendTowersAndNotifications(clan);
        	}catch(Exception e) {
        		log.error("Error leaving clan", e);
        		throw e;
        	}finally {
        		server.unlockClanTowersTable();
        	}
        }

      }
    } catch (Exception e) {
      log.error("exception in LeaveClan processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    }
  }

  private void sendTowersAndNotifications(Clan clan) {
    //get the ids of the towers that the clan owned, and attacked
    Map<String, List<Integer>> towersClanOwnedAndAttacked = 
        MiscMethods.updateClanTowersAfterClanSizeDecrease(clan);
    log.info("the towers that changed: " + towersClanOwnedAndAttacked 
        + ". The clan who lost a member: " + clan);

    if(null != towersClanOwnedAndAttacked && 0 < towersClanOwnedAndAttacked.size()) {
      List<Integer> towersAttacked = new ArrayList<Integer>();
      List<Integer> towersOwned = new ArrayList<Integer>();
      Map<Integer, ClanTower> clanTowerIdsToClanTowers =
          getClanTowerIdsToClanTowers(towersClanOwnedAndAttacked, 
              towersAttacked, towersOwned);

      //send the towers that changed
      MiscMethods.sendClanTowerProtosToClient(clanTowerIdsToClanTowers.values(), 
          server, ReasonForClanTowerChange.NOT_ENOUGH_MEMBERS);

      //send notifications to everyone online that clan towers changed
      MiscMethods.sendClanTowerWarNotEnoughMembersNotification(
          clanTowerIdsToClanTowers, towersAttacked, towersOwned, 
          clan, executor, playersByPlayerId.values(), server);
    }
  }

  private Map<Integer, ClanTower> getClanTowerIdsToClanTowers(
      Map<String, List<Integer>> towersClanOwnedAndAttacked,
      List<Integer> towersAttacked, List<Integer> towersOwned) {
    if(null != towersClanOwnedAndAttacked && !towersClanOwnedAndAttacked.isEmpty()) {
      String attacked = MiscMethods.clanTowersClanAttacked;
      String owned = MiscMethods.clanTowersClanOwned;

      towersAttacked.addAll(towersClanOwnedAndAttacked.get(attacked));
      towersOwned.addAll(towersClanOwnedAndAttacked.get(owned));

      //get all the clan tower objects that were reset
      List<Integer> towerIds = new ArrayList<Integer>();
      towerIds.addAll(towersAttacked);
      towerIds.addAll(towersOwned);
      return ClanTowerRetrieveUtils.getClanTowersForClanTowerIds(towerIds);
    }
    return null;
  }

  private void writeChangesToDB(User user, Clan clan) {
    int userId = user.getId();
    int clanId = clan.getId();

    if (userId == clan.getOwnerId()) {
      List<Integer> userIds = RetrieveUtils.userClanRetrieveUtils().getUserIdsRelatedToClan(clanId);
      deleteClan(clan, userIds, user);
    } else {
      if (!DeleteUtils.get().deleteUserClan(userId, clanId)) {
        log.error("problem with deleting user clan for " + user + " and clan " + clan);
      }
      if (!user.updateRelativeDiamondsAbsoluteClan(0, null)) {
        log.error("problem with making clanid for user null");
      }
    }
  }

  private void deleteClan(Clan clan, List<Integer> userIds, User user) {
    if (!user.updateRelativeDiamondsAbsoluteClan(0, null)) {
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

  private boolean checkLegitLeave(Builder resBuilder, User user, Clan clan) {
    if (user == null || clan == null) {
      resBuilder.setStatus(LeaveClanStatus.OTHER_FAIL);
      log.error("user is null");
      return false;      
    }
    if (user.getClanId() != clan.getId()) {
      resBuilder.setStatus(LeaveClanStatus.NOT_IN_CLAN);
      log.error("user's clan id is " + user.getClanId() + ", clan id is " + clan.getId());
      return false;
    }

    if (clan.getOwnerId() == user.getId()) {
      List<UserClan> userClanMembersInClan = RetrieveUtils.userClanRetrieveUtils().getUserClanMembersInClan(clan.getId());
      if (userClanMembersInClan.size() > 1) {
        resBuilder.setStatus(LeaveClanStatus.OWNER_OF_CLAN_WITH_OTHERS_STILL_IN);
        log.error("user is owner and he's not alone in clan, can't leave without switching ownership. user clan members are " 
            + userClanMembersInClan);
        return false;
      }
    }
    resBuilder.setStatus(LeaveClanStatus.SUCCESS);
    return true;
  }
}
