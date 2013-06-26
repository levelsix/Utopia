package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RequestJoinClanRequestEvent;
import com.lvl6.events.response.RequestJoinClanResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Clan;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.misc.MiscMethods;
import com.lvl6.misc.Notification;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.MenteeFinishedQuestResponseProto.MenteeQuestType;
import com.lvl6.proto.EventProto.RequestJoinClanRequestProto;
import com.lvl6.proto.EventProto.RequestJoinClanResponseProto;
import com.lvl6.proto.EventProto.RequestJoinClanResponseProto.Builder;
import com.lvl6.proto.EventProto.RequestJoinClanResponseProto.RequestJoinClanStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.MinimumUserProtoForClans;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.proto.InfoProto.UserClanStatus;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.ClanTierLevelRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
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
      
      boolean requestToJoinRequired = clan.isRequestToJoinRequired();
      
      boolean successful = false;
      if (legitRequest) {
        //setting minimum user proto for clans based on clan join type
        if (requestToJoinRequired) {
          MinimumUserProtoForClans mupfc = CreateInfoProtoUtils.createMinimumUserProtoForClans(
              user, UserClanStatus.REQUESTING);
          resBuilder.setRequester(mupfc);
        } else {
          MinimumUserProtoForClans mupfc = CreateInfoProtoUtils.createMinimumUserProtoForClans(
              user, UserClanStatus.MEMBER);
          resBuilder.setRequester(mupfc);
        }
        successful = writeChangesToDB(resBuilder, user, clan);
      }
      
      if (successful) {
        resBuilder.setMinClan(CreateInfoProtoUtils.createMinimumClanProtoFromClan(clan));
        resBuilder.setFullClan(CreateInfoProtoUtils.createFullClanProtoWithClanSize(clan));
      }
      RequestJoinClanResponseEvent resEvent = new RequestJoinClanResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRequestJoinClanResponseProto(resBuilder.build());
      /* I think I meant write to the clan leader if leader is not on
       
      //in case user is not online write an apns
      server.writeAPNSNotificationOrEvent(resEvent);
      //server.writeEvent(resEvent);
       */
      server.writeEvent(resEvent);
      
      if (successful) {
        server.writeClanEvent(resEvent, clan.getId());
        
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        notifyClan(user, clan, requestToJoinRequired); //write to clan leader or clan
        QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, SpecialQuestAction.REQUEST_JOIN_CLAN, true);
        
        //checkMenteeFinishedQuests(senderProto, requestToJoinRequired);
      }
    } catch (Exception e) {
      log.error("exception in RequestJoinClan processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    }
  }

  private boolean checkLegitRequest(Builder resBuilder, User user, Clan clan) {
    int clanId = clan.getId();
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
    UserClan uc = RetrieveUtils.userClanRetrieveUtils().getSpecificUserClan(user.getId(), clanId);
    if (uc != null) {
      resBuilder.setStatus(RequestJoinClanStatus.REQUEST_ALREADY_FILED);
      log.error("user clan already exists for this: " + uc);
      return false;      
    }
    //level limit does not apply to people who have prestiged 
    //(reached lvl 60 or something and went back down to 1 or something)
    int minLevel = ControllerConstants.STARTUP__CLAN_HOUSE_MIN_LEVEL;
    if (user.getLevel() < minLevel && user.getPrestigeLevel() <= 0) {
      resBuilder.setStatus(RequestJoinClanStatus.OTHER_FAIL);
      log.error("user error: Attemped to send join request to clan, but too low level and not prestiged. "
          + "min level to join clan=" + minLevel + ", user=" + user);
      return false;
    }
    if (ControllerConstants.CLAN__ALLIANCE_CLAN_ID_THAT_IS_EXCEPTION_TO_LIMIT == clanId ||
        ControllerConstants.CLAN__LEGION_CLAN_ID_THAT_IS_EXCEPTION_TO_LIMIT == clanId) {
      return true;
    }
    List<UserClan> ucs = RetrieveUtils.userClanRetrieveUtils().getUserClanMembersInClan(clanId);
    int maxSize = ClanTierLevelRetrieveUtils.getClanTierLevel(clan.getCurrentTierLevel()).getMaxClanSize();
    if (ucs.size() >= maxSize) {
      resBuilder.setStatus(RequestJoinClanStatus.CLAN_IS_FULL);
      log.warn("user error: trying to join full clan with id " + clanId);
      return false;      
    }
    //resBuilder.setStatus(RequestJoinClanStatus.SUCCESS);
    return true;
  }

  private boolean writeChangesToDB(Builder resBuilder, User user, Clan clan) {
    //clan can be open, or user needs to send a request to join the clan
    boolean requestToJoinRequired = clan.isRequestToJoinRequired();
    int userId = user.getId();
    int clanId = clan.getId(); //user.getClanId(); //this is null still...
    UserClanStatus userClanStatus;
    if (requestToJoinRequired) {
      userClanStatus = UserClanStatus.REQUESTING;
      resBuilder.setStatus(RequestJoinClanStatus.REQUEST_SUCCESS);
    } else {
      userClanStatus = UserClanStatus.MEMBER;
      resBuilder.setStatus(RequestJoinClanStatus.JOIN_SUCCESS);
    }
    
    if (!InsertUtils.get().insertUserClan(userId, clanId, userClanStatus, new Timestamp(new Date().getTime()))) {
      log.error("unexpected error: problem with inserting user clan data for user " + user + ", and clan id " + clanId);
      resBuilder.setStatus(RequestJoinClanStatus.OTHER_FAIL);
      return false;
    } 
    
    boolean deleteUserClanInserted = false;
    //update user to reflect he joined clan if the clan does not require a request to join
    if (!requestToJoinRequired) {
      if (!user.updateRelativeDiamondsAbsoluteClan(0, clanId)) {
        //could not change clan_id for user
        log.error("unexpected error: could not change clan id for requester " + user + " to " + clanId 
            + ". Deleting user clan that was just created.");
        deleteUserClanInserted = true;
      } else {
        //successfully changed clan_id in current user
        //get rid of all other join clan requests
        //don't know if this next line will always work...
        DeleteUtils.get().deleteUserClansForUserExceptSpecificClan(userId, clanId);
      }
    }
    
    boolean successful = true;
    //in case things above didn't work out
    if (deleteUserClanInserted) {
      if (!DeleteUtils.get().deleteUserClan(userId, clanId)){
        log.error("unexpected error: could not delete user clan inserted.");
      }
      resBuilder.setStatus(RequestJoinClanStatus.OTHER_FAIL);
      successful = false;
    }
    return successful;
  }
  
  private void notifyClan(User aUser, Clan aClan, boolean requestToJoinRequired) {
    int clanOwnerId = aClan.getOwnerId();
    
    int level = aUser.getLevel();
    String requester = aUser.getName();
    Notification aNote = new Notification();
    
    if (requestToJoinRequired) {
      //notify leader someone requested to join clan
      aNote.setAsUserRequestedToJoinClan(level, requester);
    } else {
      //notify whole clan someone joined the clan <- too annoying, just leader
      //TODO: Maybe exclude the guy who joined from receiving the notification?
      aNote.setAsUserJoinedClan(level, requester);
    }
    MiscMethods.writeNotificationToUser(aNote, server, clanOwnerId);
    
//    GeneralNotificationResponseProto.Builder notificationProto =
//        aNote.generateNotificationBuilder();
//    GeneralNotificationResponseEvent aNotification =
//        new GeneralNotificationResponseEvent(clanOwnerId);
//    aNotification.setGeneralNotificationResponseProto(notificationProto.build());
//    
//    server.writeAPNSNotificationOrEvent(aNotification);
  }
  
  private void checkMenteeFinishedQuests(MinimumUserProto mup, boolean requestToJoinRequired) {
    if (requestToJoinRequired) {
      return;
    }
    
    MenteeQuestType type = MenteeQuestType.JOINED_A_CLAN;
    MiscMethods.sendMenteeFinishedQuests(mup, type, server);
   
  }
  
}
