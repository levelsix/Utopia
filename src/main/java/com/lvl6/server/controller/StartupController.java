package com.lvl6.server.controller;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.StartupRequestEvent;
import com.lvl6.events.response.ArmoryResponseEvent;
import com.lvl6.events.response.RetrieveStaticDataResponseEvent;
import com.lvl6.events.response.StartupResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.BattleDetails;
import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.info.City;
import com.lvl6.info.ClanWallPost;
import com.lvl6.info.Dialogue;
import com.lvl6.info.Equipment;
import com.lvl6.info.MarketplaceTransaction;
import com.lvl6.info.NeutralCityElement;
import com.lvl6.info.PlayerWallPost;
import com.lvl6.info.Quest;
import com.lvl6.info.Structure;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserQuest;
import com.lvl6.leaderboards.LeaderBoardUtil;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.Globals;
import com.lvl6.proto.EventProto.ArmoryResponseProto;
import com.lvl6.proto.EventProto.RetrieveStaticDataResponseProto;
import com.lvl6.proto.EventProto.RetrieveStaticDataResponseProto.RetrieveStaticDataStatus;
import com.lvl6.proto.EventProto.StartupRequestProto;
import com.lvl6.proto.EventProto.StartupResponseProto;
import com.lvl6.proto.EventProto.UpdateClientUserResponseProto;
import com.lvl6.proto.EventProto.StartupResponseProto.Builder;
import com.lvl6.proto.EventProto.StartupResponseProto.DailyBonusInfo;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupStatus;
import com.lvl6.proto.EventProto.StartupResponseProto.TutorialConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.TutorialConstants.FullTutorialQuestProto;
import com.lvl6.proto.EventProto.StartupResponseProto.UpdateStatus;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.proto.InfoProto.FullStructureProto;
import com.lvl6.proto.InfoProto.FullTaskProto;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.BattleDetailsRetrieveUtils;
import com.lvl6.retrieveutils.ClanWallPostRetrieveUtils;
import com.lvl6.retrieveutils.IAPHistoryRetrieveUtils;
import com.lvl6.retrieveutils.MarketplaceTransactionRetrieveUtils;
import com.lvl6.retrieveutils.PlayerWallPostRetrieveUtils;
import com.lvl6.retrieveutils.UnhandledBlacksmithAttemptRetrieveUtils;
import com.lvl6.retrieveutils.UserTaskRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LevelsRequiredExperienceRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.NeutralCityElementsRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.NIOUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class StartupController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public StartupController() {
    numAllocatedThreads = 3;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new StartupRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_STARTUP_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    StartupRequestProto reqProto = ((StartupRequestEvent)event).getStartupRequestProto();
    log.info("Processing startup request event");
    UpdateStatus updateStatus;
    String udid = reqProto.getUdid();
    String apsalarId = reqProto.hasApsalarId() ? reqProto.getApsalarId() : null;

    StartupResponseProto.Builder resBuilder = StartupResponseProto.newBuilder();

    MiscMethods.setMDCProperties(udid, null, MiscMethods.getIPOfPlayer(server, null, udid));

    double tempClientVersionNum = reqProto.getVersionNum() * 10;
    double tempLatestVersionNum = GameServer.clientVersionNumber * 10;

    // Check version number
    if ((int)tempClientVersionNum < (int)tempLatestVersionNum && tempClientVersionNum > 12.5) {
      updateStatus = UpdateStatus.MAJOR_UPDATE;
      log.info("player has been notified of forced update");
    } else if (tempClientVersionNum < tempLatestVersionNum) {
      updateStatus = UpdateStatus.MINOR_UPDATE;
    } else {
      updateStatus = UpdateStatus.NO_UPDATE;
    }

    resBuilder.setUpdateStatus(updateStatus);
    resBuilder.setAppStoreURL(Globals.APP_STORE_URL);

    User user = null;

    // Don't fill in other fields if it is a major update
    StartupStatus startupStatus = StartupStatus.USER_NOT_IN_DB;

    Timestamp now = new Timestamp(new Date().getTime());

    int newNumConsecutiveDaysLoggedIn = 0;

    if (updateStatus != UpdateStatus.MAJOR_UPDATE) {
      user = RetrieveUtils.userRetrieveUtils().getUserByUDID(udid);
      if (user != null) {
        server.lockPlayer(user.getId());
        try {
          startupStatus = StartupStatus.USER_IN_DB;
          log.info("No major update... getting user info");
          newNumConsecutiveDaysLoggedIn = 0;//setDailyBonusInfo(resBuilder, user, now);
          setCitiesAndUserCityInfos(resBuilder, user);
          setInProgressAndAvailableQuests(resBuilder, user);
          setUserEquipsAndEquips(resBuilder, user);
          setAllies(resBuilder, user);
          resBuilder.setExperienceRequiredForNextLevel(
              LevelsRequiredExperienceRetrieveUtils.getRequiredExperienceForLevel(user.getLevel() + 1));
          resBuilder.setExperienceRequiredForCurrentLevel(
              LevelsRequiredExperienceRetrieveUtils.getRequiredExperienceForLevel(user.getLevel()));
          setNotifications(resBuilder, user);
          setWhetherPlayerCompletedInAppPurchase(resBuilder, user);
          setUnhandledForgeAttempts(resBuilder, user);
          setNoticesToPlayers(resBuilder, user);
          setUserClanInfos(resBuilder, user);

          FullUserProto fup = CreateInfoProtoUtils.createFullUserProtoFromUser(user);
          resBuilder.setSender(fup);
        } catch (Exception e) {
          log.error("exception in StartupController processEvent", e);
        } finally {
          server.unlockPlayer(user.getId()); 
        }
      } else {
        log.info("new player with udid " + udid);
      }
      resBuilder.setStartupStatus(startupStatus);
      setConstants(resBuilder, startupStatus);      
    }


    StartupResponseProto resProto = resBuilder.build();
    StartupResponseEvent resEvent = new StartupResponseEvent(udid);
    resEvent.setTag(event.getTag());
    resEvent.setStartupResponseProto(resProto);

//    log.info("Sending struct");
//    sendAllStructs(udid, user);

    log.debug("Writing event response: "+resEvent);
    server.writePreDBEvent(resEvent, udid);
    log.debug("Wrote response event: "+resEvent);
   
    //for things that client doesn't need
    log.debug("After response tasks");
    updateLeaderboard(apsalarId, user, now, newNumConsecutiveDaysLoggedIn);    
  }

	protected void updateLeaderboard(String apsalarId, User user, Timestamp now,int newNumConsecutiveDaysLoggedIn) {
		if (user != null) {
	      log.debug("Updating leaderboard for user "+user.getId());
	      syncApsalaridLastloginConsecutivedaysloggedinResetBadges(user, apsalarId, now, newNumConsecutiveDaysLoggedIn);
	      LeaderBoardUtil leaderboard = AppContext.getApplicationContext().getBean(LeaderBoardUtil.class);
	      leaderboard.updateLeaderboardForUser(user);
	    }
	}

  private void sendAllStructs(String udid, User user) {
    RetrieveStaticDataResponseEvent resEvent1 = new RetrieveStaticDataResponseEvent(0);
    RetrieveStaticDataResponseProto.Builder resProto1 = RetrieveStaticDataResponseProto.newBuilder();
    Map<Integer, Structure> structIdsToStructures = StructureRetrieveUtils.getStructIdsToStructs();
    for (Integer structId :  structIdsToStructures.keySet()) {
      Structure struct = structIdsToStructures.get(structId);
      if (struct != null) {
        resProto1.addStructs(CreateInfoProtoUtils.createFullStructureProtoFromStructure(struct));
      } else {
        resProto1.setStatus(RetrieveStaticDataStatus.SOME_FAIL);
        log.error("problem with retrieving struct with id " + structId);
      }
    }
    resEvent1.setRetrieveStaticDataResponseProto(resProto1.build());
    server.writePreDBEvent(resEvent1, udid);
    log.info("Structs sent");
  }

  private void setUserClanInfos(StartupResponseProto.Builder resBuilder,
      User user) {
    List<UserClan> userClans = RetrieveUtils.userClanRetrieveUtils().getUserClansRelatedToUser(user.getId());
    for (UserClan uc : userClans) {
      resBuilder.addUserClanInfo(CreateInfoProtoUtils.createFullUserClanProtoFromUserClan(uc));
    }
  }

  private void setNoticesToPlayers(Builder resBuilder, User user) {
    if (ControllerConstants.STARTUP__NOTICES_TO_PLAYERS != null) {
      for (int i = 0; i < ControllerConstants.STARTUP__NOTICES_TO_PLAYERS.length; i++) {
        resBuilder.addNoticesToPlayers(ControllerConstants.STARTUP__NOTICES_TO_PLAYERS[i]);
      }
    }
    if (user.getLastLogout() == null && Globals.IDDICTION_ON()) {
      resBuilder.addNoticesToPlayers(ControllerConstants.IDDICTION__NOTICE);
    }
  }

  private void setUnhandledForgeAttempts(Builder resBuilder, User user) {
    List<BlacksmithAttempt> unhandledBlacksmithAttemptsForUser = UnhandledBlacksmithAttemptRetrieveUtils.getUnhandledBlacksmithAttemptsForUser(user.getId());
    if (unhandledBlacksmithAttemptsForUser != null && unhandledBlacksmithAttemptsForUser.size() == 1) {
      resBuilder.setUnhandledForgeAttempt(CreateInfoProtoUtils.createUnhandledBlacksmithAttemptProtoFromBlacksmithAttempt(
          unhandledBlacksmithAttemptsForUser.get(0)));
      resBuilder.setForgeAttemptEquip(CreateInfoProtoUtils.createFullEquipProtoFromEquip(EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(
          unhandledBlacksmithAttemptsForUser.get(0).getEquipId())));
    }
    if (unhandledBlacksmithAttemptsForUser != null && unhandledBlacksmithAttemptsForUser.size() > 1) {
      log.error("user has too many blacksmith attempts, should only have one. blacksmith attempts = " + unhandledBlacksmithAttemptsForUser);
    }
  }

  private void setWhetherPlayerCompletedInAppPurchase(Builder resBuilder,
      User user) {
    boolean hasPurchased = IAPHistoryRetrieveUtils.checkIfUserHasPurchased(user.getId());
    resBuilder.setPlayerHasBoughtInAppPurchase(hasPurchased);
  }

  private int setDailyBonusInfo(Builder resBuilder, User user, Timestamp now) {
    int numConsecDaysPlayed = user.getNumConsecutiveDaysPlayed();

    Calendar curDate = Calendar.getInstance();
    curDate.setTime(new Date(now.getTime()));
    curDate.set(Calendar.HOUR_OF_DAY, 0);
    curDate.set(Calendar.HOUR, 0);
    curDate.set(Calendar.MINUTE, 0);
    curDate.set(Calendar.SECOND, 0);
    curDate.set(Calendar.MILLISECOND, 0);
    curDate.set(Calendar.AM_PM, 0);
    long curTime = curDate.getTimeInMillis();

    Timestamp lastLogin = new Timestamp(user.getLastLogin().getTime());
    Calendar lastDate = Calendar.getInstance();
    lastDate.setTime(new Date(lastLogin.getTime()));
    lastDate.set(Calendar.HOUR_OF_DAY, 0);
    lastDate.set(Calendar.HOUR, 0);
    lastDate.set(Calendar.MINUTE, 0);
    lastDate.set(Calendar.SECOND, 0);
    lastDate.set(Calendar.MILLISECOND, 0);
    lastDate.set(Calendar.AM_PM, 0);
    long lastTime = lastDate.getTimeInMillis();

    if (curTime<lastTime) {
      log.error("ERROR in setDailyBonusInfo, Current login, "+curTime+" is dated before last login "+lastTime);
      return 0;
    }
    //check if already logged in today
    if (curTime==lastTime) {
      resBuilder.setDailyBonusInfo(DailyBonusInfo.newBuilder().setFirstTimeToday(false).build());
      return numConsecDaysPlayed;
    } else { //first time logging in today
      DailyBonusInfo.Builder dbiBuilder = DailyBonusInfo.newBuilder();
      dbiBuilder.setFirstTimeToday(true);

      //check if consecutive login
      lastDate.add(Calendar.DATE,ControllerConstants.STARTUP__DAILY_BONUS_TIME_REQ_BETWEEN_CONSEC_DAYS);
      lastTime = lastDate.getTimeInMillis();
      if (curTime==lastTime) {
        numConsecDaysPlayed++;
        dbiBuilder.setNumConsecutiveDaysPlayed(numConsecDaysPlayed);
        //BIG BONUS
        if (numConsecDaysPlayed >= ControllerConstants.STARTUP__DAILY_BONUS_MIN_CONSEC_DAYS_BIG_BONUS) {

          int idOfEquipToGive = MiscMethods.chooseMysteryBoxEquip(user);
          int levelOfEquipToGive = (int)(Math.random() * ControllerConstants.STARTUP__DAILY_BONUS_MYSTERY_BOX_EQUIP_FORGE_LEVEL_MAX) + 1;
          int userEquipId = InsertUtils.get().insertUserEquip(user.getId(), idOfEquipToGive, levelOfEquipToGive);
          if (userEquipId <= 0) {
            log.error("failed in giving user " + user + " equip with id " + idOfEquipToGive);
            return 0;
          } else {
            UserEquip ue = new UserEquip (userEquipId, user.getId(), idOfEquipToGive, levelOfEquipToGive);
            dbiBuilder.setUserEquipBonus(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(ue));
          }

          if (numConsecDaysPlayed>=ControllerConstants.STARTUP__DAILY_BONUS_MAX_CONSEC_DAYS_BIG_BONUS) {
            numConsecDaysPlayed = 0; //set to day 0 because at max consecutive days
          }
          //SMALL BONUS
        } else if (numConsecDaysPlayed>=ControllerConstants.STARTUP__DAILY_BONUS_MIN_CONSEC_DAYS_SMALL_BONUS) {
          int coinBonus = ControllerConstants.STARTUP__DAILY_BONUS_SMALL_BONUS_COIN_QUANTITY*numConsecDaysPlayed*user.getLevel();
          dbiBuilder.setCoinBonus(coinBonus);
          if (!user.updateRelativeCoinsNaive(coinBonus)) {
            log.error("problem with giving silver bonus of " + coinBonus + " to user " + user);
          }
        }
      } else { //more than a day since last login
        if (lastTime>curTime) {
          log.error("ERROR in setDailyBonusInfo, lastDate, "+lastTime+" is not before curDate, " + curTime);
        }
        numConsecDaysPlayed = 1;
        dbiBuilder.setNumConsecutiveDaysPlayed(numConsecDaysPlayed);
        dbiBuilder.setCoinBonus(ControllerConstants.STARTUP__DAILY_BONUS_SMALL_BONUS_COIN_QUANTITY*numConsecDaysPlayed*user.getLevel());					
      }
      resBuilder.setDailyBonusInfo(dbiBuilder.build());
      return numConsecDaysPlayed;
    }
  }

  private void syncApsalaridLastloginConsecutivedaysloggedinResetBadges(User user, String apsalarId, Timestamp loginTime, int newNumConsecutiveDaysLoggedIn) {
    if (user.getApsalarId() != null && apsalarId == null) {
      apsalarId = user.getApsalarId();
    }
    if (!user.updateAbsoluteApsalaridLastloginBadgesNumConsecutiveDaysLoggedIn(apsalarId, loginTime, 0, newNumConsecutiveDaysLoggedIn)) {
      log.error("problem with updating apsalar id to " + 
          apsalarId + ", last login to " + loginTime + ", and badge count to 0 for " + user + " and newNumConsecutiveDaysLoggedIn is " + newNumConsecutiveDaysLoggedIn);
    }
    if (!InsertUtils.get().insertLastLoginLastLogoutToUserSessions(user.getId(), loginTime, null)) {
      log.error("problem with inserting last login time for user " + user + ", loginTime=" + loginTime);
    }

    if (user.getNumBadges() != 0) {
      if (user.getDeviceToken() != null) { 
        /*
         * handled locally?
         */
        //        ApnsServiceBuilder builder = APNS.newService().withCert(APNSProperties.PATH_TO_CERT, APNSProperties.CERT_PASSWORD);
        //        if (Globals.IS_SANDBOX()) {
        //          builder.withSandboxDestination();
        //        }
        //        ApnsService service = builder.build();
        //        service.push(newDeviceToken, APNS.newPayload().badge(0).build());
        //        service.stop();
      }
    }
  }

  private void setNotifications(Builder resBuilder, User user) {
    List <Integer> userIds = new ArrayList<Integer>();

    List<MarketplaceTransaction> marketplaceTransactions = 
        MarketplaceTransactionRetrieveUtils.getMostRecentMarketplaceTransactionsForPoster(user.getId(), ControllerConstants.STARTUP__MAX_NUM_OF_STARTUP_NOTIFICATION_TYPE_TO_SEND);
    if (marketplaceTransactions != null && marketplaceTransactions.size() > 0) {
      for (MarketplaceTransaction mt : marketplaceTransactions) {
        userIds.add(mt.getBuyerId());
      }
    }

    Timestamp earliestBattleNotificationTimeToRetrieve = new Timestamp(new Date().getTime() - ControllerConstants.STARTUP__HOURS_OF_BATTLE_NOTIFICATIONS_TO_SEND*3600000);
    List<BattleDetails> battleDetails = BattleDetailsRetrieveUtils.getMostRecentBattleDetailsForDefenderAfterTime(user.getId(), ControllerConstants.STARTUP__MAX_NUM_OF_STARTUP_NOTIFICATION_TYPE_TO_SEND, earliestBattleNotificationTimeToRetrieve);
    if (battleDetails != null && battleDetails.size() > 0) {
      for (BattleDetails bd : battleDetails) {
        userIds.add(bd.getAttackerId());
      }        
    }

    List<PlayerWallPost> wallPosts = PlayerWallPostRetrieveUtils.getMostRecentPlayerWallPostsForWallOwner(ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP, user.getId());
    if (wallPosts != null && wallPosts.size() > 0) {
      for (PlayerWallPost p : wallPosts) {
        userIds.add(p.getPosterId());
      }
    }

    List<ClanWallPost> clanWallPosts = null;
    if (user.getClanId() > 0) {
      clanWallPosts = ClanWallPostRetrieveUtils.getMostRecentClanWallPostsForClan(ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP, user.getClanId());
      for (ClanWallPost p : clanWallPosts) {
        userIds.add(p.getPosterId());
      }
    }


    Map<Integer, User> usersByIds = null;
    if (userIds.size() > 0) {
      usersByIds = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIds);
    }

    if (marketplaceTransactions != null && marketplaceTransactions.size() > 0) {
      for (MarketplaceTransaction mt : marketplaceTransactions) {
        resBuilder.addMarketplacePurchaseNotifications(CreateInfoProtoUtils.createMarketplacePostPurchasedNotificationProtoFromMarketplaceTransaction(mt, usersByIds.get(mt.getBuyerId()), user));
      }
    }
    if (battleDetails != null && battleDetails.size() > 0) {
      for (BattleDetails bd : battleDetails) {
        resBuilder.addAttackNotifications(CreateInfoProtoUtils.createAttackedNotificationProtoFromBattleHistory(bd, usersByIds.get(bd.getAttackerId())));
      }        
    } 
    if (wallPosts != null && wallPosts.size() > 0) {
      for (PlayerWallPost p : wallPosts) {
        resBuilder.addPlayerWallPostNotifications(CreateInfoProtoUtils.createPlayerWallPostProtoFromPlayerWallPost(p, usersByIds.get(p.getPosterId())));
      }
    }
    if (clanWallPosts != null && clanWallPosts.size() > 0) {
      
    }
  }

  private void setAllies(Builder resBuilder, User user) {
    List<UserType> userTypes = new ArrayList<UserType>();
    if (MiscMethods.checkIfGoodSide(user.getType())) {
      userTypes.add(UserType.GOOD_ARCHER);
      userTypes.add(UserType.GOOD_MAGE);
      userTypes.add(UserType.GOOD_WARRIOR);      
    } else {
      userTypes.add(UserType.BAD_ARCHER);
      userTypes.add(UserType.BAD_MAGE);
      userTypes.add(UserType.BAD_WARRIOR);
    }

    List<Integer> forbiddenUserIds = new ArrayList<Integer>();
    forbiddenUserIds.add(user.getId());

    List<User> allies = RetrieveUtils.userRetrieveUtils().getUsers(userTypes, ControllerConstants.STARTUP__APPROX_NUM_ALLIES_TO_SEND, user.getLevel(), user.getId(), false, 
        null, null, null, null, false, forbiddenUserIds);
    if (allies != null && allies.size() > 0) {
      for (User ally : allies) {
        resBuilder.addAllies(CreateInfoProtoUtils.createMinimumUserProtoWithLevelFromUser(ally));
      }
    }
  }

  private void setUserEquipsAndEquips(Builder resBuilder, User user) {
    List<UserEquip> userEquips = RetrieveUtils.userEquipRetrieveUtils().getUserEquipsForUser(user.getId());
    if (userEquips != null) {
      Map<Integer, Equipment> equipIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
      Set<Integer> equipsGivenAlready = new HashSet<Integer>();
      for (UserEquip ue : userEquips) {
        resBuilder.addUserEquips(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(ue));
        if (!equipsGivenAlready.contains(ue.getEquipId())) {
          resBuilder.addEquips(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipIdsToEquipment.get(ue.getEquipId())));
          equipsGivenAlready.add(ue.getEquipId());
        }
      }
    }
  }

  private void setInProgressAndAvailableQuests(Builder resBuilder, User user) {
    List<UserQuest> inProgressAndRedeemedUserQuests = RetrieveUtils.userQuestRetrieveUtils().getUnredeemedAndRedeemedUserQuestsForUser(user.getId());
    List<Integer> inProgressQuestIds = new ArrayList<Integer>();
    List<Integer> redeemedQuestIds = new ArrayList<Integer>();

    Map<Integer, Quest> questIdToQuests = QuestRetrieveUtils.getQuestIdsToQuests();
    for (UserQuest uq : inProgressAndRedeemedUserQuests) {
      if (uq.isRedeemed()) {
        redeemedQuestIds.add(uq.getQuestId());
      } else {
        Quest quest = QuestRetrieveUtils.getQuestForQuestId(uq.getQuestId());

        if (quest.getDefeatBadGuysJobsRequired() == null && quest.getDefeatGoodGuysJobsRequired() == null && !uq.isDefeatTypeJobsComplete()) {
          if (!UpdateUtils.get().updateUserQuestsSetCompleted(user.getId(), quest.getId(), false, true)) {
            log.error("problem with updating user quest data by marking defeat type jobs completed for user quest " + uq);
          }
        }
        if (quest.getTasksRequired() == null && !uq.isTasksComplete()) {
          if (!UpdateUtils.get().updateUserQuestsSetCompleted(user.getId(), quest.getId(), true, false)) {
            log.error("problem with updating user quest data by marking tasks completed for user quest " + uq);
          }
        }

        inProgressQuestIds.add(uq.getQuestId());  
        if (uq.isComplete()) {
          resBuilder.addInProgressCompleteQuests(CreateInfoProtoUtils.createFullQuestProtoFromQuest(user.getType(), questIdToQuests.get(uq.getQuestId())));
        } else {
          resBuilder.addInProgressIncompleteQuests(CreateInfoProtoUtils.createFullQuestProtoFromQuest(user.getType(), questIdToQuests.get(uq.getQuestId())));          
        }
      }
    }

    List<Integer> availableQuestIds = QuestUtils.getAvailableQuestsForUser(redeemedQuestIds, inProgressQuestIds);
    if (availableQuestIds != null) {
      for (Integer questId : availableQuestIds) {
        resBuilder.addAvailableQuests(CreateInfoProtoUtils.createFullQuestProtoFromQuest(user.getType(), questIdToQuests.get(questId)));
      }
    }
  }

  private void setCitiesAndUserCityInfos(Builder resBuilder, User user) {
    Map<Integer, Integer> cityIdsToUserCityRanks = RetrieveUtils.userCityRetrieveUtils().getCityIdToUserCityRank(user.getId());
    Map<Integer, Integer> taskIdToNumTimesActedInRank = UserTaskRetrieveUtils.getTaskIdToNumTimesActedInRankForUser(user.getId());


    Map<Integer, City> cities = CityRetrieveUtils.getCityIdsToCities();
    for (Integer cityId : cities.keySet()) {
      City city = cities.get(cityId);
      resBuilder.addAllCities(CreateInfoProtoUtils.createFullCityProtoFromCity(city));
      if (user.getLevel() >= city.getMinLevel()) {

        if (!cityIdsToUserCityRanks.containsKey(city.getId())) {
          if (!UpdateUtils.get().incrementCityRankForUserCity(user.getId(), cityId, 1)) {
            log.error("problem with unlocking city for user, city Id is " + cityId + ", and user is " + user);
          }
        }
        int numTasksComplete = getNumTasksCompleteForUserCity(user, city, taskIdToNumTimesActedInRank);
        resBuilder.addUserCityInfos(CreateInfoProtoUtils.createFullUserCityProto(user.getId(), city.getId(), 
            cityIdsToUserCityRanks.get(city.getId()), numTasksComplete));
      }
    }
  }

  private int getNumTasksCompleteForUserCity(User user, City city, Map<Integer, Integer> taskIdToNumTimesActedInRank) {
    List<Task> tasks = TaskRetrieveUtils.getAllTasksForCityId(city.getId());
    int numCompletedTasks = 0;
    if (tasks != null) {
      for (Task t : tasks) {
        if (taskIdToNumTimesActedInRank.containsKey(t.getId()) && taskIdToNumTimesActedInRank.get(t.getId()) >= t.getNumForCompletion()) {
          numCompletedTasks++;
        }
      }
    }
    return numCompletedTasks;
  }

  private void setConstants(Builder startupBuilder, StartupStatus startupStatus) {
    startupBuilder.setStartupConstants(MiscMethods.createStartupConstantsProto());
    if (startupStatus == StartupStatus.USER_NOT_IN_DB) {
      setTutorialConstants(startupBuilder);
    }
  }

  private void setTutorialConstants(Builder resBuilder) {
    Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();

    UserType aGoodType = UserType.GOOD_ARCHER;
    UserType aBadType = UserType.BAD_ARCHER;

    Task task = TaskRetrieveUtils.getTaskForTaskId(ControllerConstants.TUTORIAL__FIRST_TASK_ID);
    task.setPotentialLootEquipIds(new ArrayList<Integer>());
    FullTaskProto ftpGood = CreateInfoProtoUtils.createFullTaskProtoFromTask(aGoodType, task);
    FullTaskProto ftpBad = CreateInfoProtoUtils.createFullTaskProtoFromTask(aBadType, task);

    Dialogue goodAcceptDialogue = MiscMethods.createDialogue(ControllerConstants.TUTORIAL__FAKE_QUEST_GOOD_ACCEPT_DIALOGUE);
    Dialogue badAcceptDialogue = MiscMethods.createDialogue(ControllerConstants.TUTORIAL__FAKE_QUEST_BAD_ACCEPT_DIALOGUE);

    FullTutorialQuestProto tqbp = FullTutorialQuestProto.newBuilder()
        .setGoodName(ControllerConstants.TUTORIAL__FAKE_QUEST_GOOD_NAME)
        .setBadName(ControllerConstants.TUTORIAL__FAKE_QUEST_BAD_NAME)
        .setGoodDescription(ControllerConstants.TUTORIAL__FAKE_QUEST_GOOD_DESCRIPTION)
        .setBadDescription(ControllerConstants.TUTORIAL__FAKE_QUEST_BAD_DESCRIPTION)
        .setGoodDoneResponse(ControllerConstants.TUTORIAL__FAKE_QUEST_GOOD_DONE_RESPONSE)
        .setBadDoneResponse(ControllerConstants.TUTORIAL__FAKE_QUEST_BAD_DONE_RESPONSE)
        .setGoodAcceptDialogue(CreateInfoProtoUtils.createDialogueProtoFromDialogue(goodAcceptDialogue))
        .setBadAcceptDialogue(CreateInfoProtoUtils.createDialogueProtoFromDialogue(badAcceptDialogue))
        .setAssetNumWithinCity(ControllerConstants.TUTORIAL__FAKE_QUEST_ASSET_NUM_WITHIN_CITY)
        .setCoinsGained(ControllerConstants.TUTORIAL__FAKE_QUEST_COINS_GAINED)
        .setExpGained(ControllerConstants.TUTORIAL__FAKE_QUEST_EXP_GAINED)
        .setFirstTaskGood(ftpGood).setFirstTaskBad(ftpBad)
        .setFirstTaskCompleteCoinGain(MiscMethods.calculateCoinsGainedFromTutorialTask(task))
        .setFirstDefeatTypeJobBattleCoinGain(ControllerConstants.TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_COIN_GAIN)
        .setFirstDefeatTypeJobBattleExpGain(ControllerConstants.TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_EXP_GAIN)
        .setFirstDefeatTypeJobBattleLootAmulet
        (CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_AMULET_LOOT_EQUIP_ID)))
        .build();

    PlayerWallPost pwp = new PlayerWallPost(-1, ControllerConstants.USER_CREATE__ID_OF_POSTER_OF_FIRST_WALL, -1, 
        new Date(), ControllerConstants.USER_CREATE__FIRST_WALL_POST_TEXT);
    User poster = RetrieveUtils.userRetrieveUtils().getUserById(ControllerConstants.USER_CREATE__ID_OF_POSTER_OF_FIRST_WALL);

    TutorialConstants.Builder builder = TutorialConstants.newBuilder()
        .setInitEnergy(ControllerConstants.TUTORIAL__INIT_ENERGY).setInitStamina(ControllerConstants.TUTORIAL__INIT_STAMINA)
        .setInitHealth(ControllerConstants.TUTORIAL__INIT_HEALTH).setStructToBuild(ControllerConstants.TUTORIAL__FIRST_STRUCT_TO_BUILD)
        .setDiamondCostToInstabuildFirstStruct(ControllerConstants.TUTORIAL__DIAMOND_COST_TO_INSTABUILD_FIRST_STRUCT)
        .setArcherInitAttack(ControllerConstants.TUTORIAL__ARCHER_INIT_ATTACK).setArcherInitDefense(ControllerConstants.TUTORIAL__ARCHER_INIT_DEFENSE)
        .setArcherInitWeapon(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__ARCHER_INIT_WEAPON_ID)))
        .setArcherInitArmor(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__ARCHER_INIT_ARMOR_ID)))
        .setMageInitAttack(ControllerConstants.TUTORIAL__MAGE_INIT_ATTACK).setMageInitDefense(ControllerConstants.TUTORIAL__MAGE_INIT_DEFENSE)
        .setMageInitWeapon(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__MAGE_INIT_WEAPON_ID)))
        .setMageInitArmor(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__MAGE_INIT_ARMOR_ID)))
        .setWarriorInitAttack(ControllerConstants.TUTORIAL__WARRIOR_INIT_ATTACK).setWarriorInitDefense(ControllerConstants.TUTORIAL__WARRIOR_INIT_DEFENSE)
        .setWarriorInitWeapon(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__WARRIOR_INIT_WEAPON_ID)))
        .setWarriorInitArmor(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment.get(ControllerConstants.TUTORIAL__WARRIOR_INIT_ARMOR_ID)))
        .setTutorialQuest(tqbp).setMinNameLength(ControllerConstants.USER_CREATE__MIN_NAME_LENGTH)
        .setTutorialQuest(tqbp).setMaxNameLength(ControllerConstants.USER_CREATE__MAX_NAME_LENGTH)
        .setCoinRewardForBeingReferred(ControllerConstants.USER_CREATE__COIN_REWARD_FOR_BEING_REFERRED)
        .setInitDiamonds(ControllerConstants.TUTORIAL__INIT_DIAMONDS)
        .setInitCoins(ControllerConstants.TUTORIAL__INIT_COINS)
        .setExpRequiredForLevelTwo(LevelsRequiredExperienceRetrieveUtils.getLevelsToRequiredExperienceForLevels().get(2))
        .setExpRequiredForLevelThree(LevelsRequiredExperienceRetrieveUtils.getLevelsToRequiredExperienceForLevels().get(3))
        .setFirstWallPost(CreateInfoProtoUtils.createPlayerWallPostProtoFromPlayerWallPost(pwp, poster));

    List<NeutralCityElement> neutralCityElements = NeutralCityElementsRetrieveUtils.getNeutralCityElementsForCity(ControllerConstants.TUTORIAL__FIRST_NEUTRAL_CITY_ID);
    if (neutralCityElements != null) {
      for (NeutralCityElement nce : neutralCityElements) {
        builder.addFirstCityElementsForGood(CreateInfoProtoUtils.createNeutralCityElementProtoFromNeutralCityElement(nce, aGoodType));
        builder.addFirstCityElementsForBad(CreateInfoProtoUtils.createNeutralCityElementProtoFromNeutralCityElement(nce, aBadType));
      }
    }

    Map<Integer, Structure> structIdsToStructs = StructureRetrieveUtils.getStructIdsToStructs();
    for (Structure struct : structIdsToStructs.values()) {
      if (struct != null) {
        FullStructureProto fsp = CreateInfoProtoUtils.createFullStructureProtoFromStructure(struct);
        builder.addCarpenterStructs(fsp);
        if (struct.getMinLevel() == 2) {
          builder.addNewlyAvailableStructsAfterLevelup(CreateInfoProtoUtils.createFullStructureProtoFromStructure(struct));
        } 
      }
    }

    List<City> availCities = MiscMethods.getCitiesAvailableForUserLevel(ControllerConstants.USER_CREATE__START_LEVEL);
    for (City city : availCities) {
      if (city.getMinLevel() == ControllerConstants.USER_CREATE__START_LEVEL) {
        builder.addCitiesNewlyAvailableToUserAfterLevelup(CreateInfoProtoUtils.createFullCityProtoFromCity(city));
      }
    }

    Map<Integer, Equipment> equipIdToEquips = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
    if (equipIdToEquips != null) {
      for (Equipment e : equipIdToEquips.values()) {
        if (e != null && e.getMinLevel() == ControllerConstants.USER_CREATE__START_LEVEL && (e.getRarity() == Rarity.EPIC || e.getRarity() == Rarity.LEGENDARY)) {
          builder.addNewlyEquippableEpicsAndLegendariesForAllClassesAfterLevelup(CreateInfoProtoUtils.createFullEquipProtoFromEquip(e));
        }
      }
    }
    resBuilder.setTutorialConstants(builder.build());
  }


}
