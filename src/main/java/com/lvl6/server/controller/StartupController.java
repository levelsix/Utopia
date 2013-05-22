package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.hazelcast.core.IList;
import com.kabam.apiclient.KabamApi;
import com.kabam.apiclient.MobileNaidResponse;
import com.kabam.apiclient.ResponseCode;
import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.StartupRequestEvent;
import com.lvl6.events.response.RetrieveStaticDataResponseEvent;
import com.lvl6.events.response.StartupResponseEvent;
import com.lvl6.info.BattleDetails;
import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.info.BoosterItem;
import com.lvl6.info.BoosterPack;
import com.lvl6.info.City;
import com.lvl6.info.Clan;
import com.lvl6.info.ClanChatPost;
import com.lvl6.info.ClanTower;
import com.lvl6.info.DailyBonusReward;
import com.lvl6.info.Dialogue;
import com.lvl6.info.EquipEnhancement;
import com.lvl6.info.EquipEnhancementFeeder;
import com.lvl6.info.Equipment;
import com.lvl6.info.GoldSale;
import com.lvl6.info.MarketplaceTransaction;
import com.lvl6.info.NeutralCityElement;
import com.lvl6.info.PlayerWallPost;
import com.lvl6.info.PrivateChatPost;
import com.lvl6.info.Quest;
import com.lvl6.info.Structure;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.info.UserDailyBonusRewardHistory;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserLockBoxEvent;
import com.lvl6.info.UserQuest;
import com.lvl6.leaderboards.LeaderBoardUtil;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.Globals;
import com.lvl6.properties.KabamProperties;
import com.lvl6.proto.EventProto.RetrieveStaticDataResponseProto;
import com.lvl6.proto.EventProto.RetrieveStaticDataResponseProto.RetrieveStaticDataStatus;
import com.lvl6.proto.EventProto.StartupRequestProto;
import com.lvl6.proto.EventProto.StartupResponseProto;
import com.lvl6.proto.EventProto.StartupResponseProto.Builder;
import com.lvl6.proto.EventProto.StartupResponseProto.DailyBonusInfo;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupStatus;
import com.lvl6.proto.EventProto.StartupResponseProto.TutorialConstants;
import com.lvl6.proto.EventProto.StartupResponseProto.TutorialConstants.FullTutorialQuestProto;
import com.lvl6.proto.EventProto.StartupResponseProto.UpdateStatus;
import com.lvl6.proto.InfoProto.BoosterPackProto;
import com.lvl6.proto.InfoProto.EquipEnhancementProto;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.proto.InfoProto.FullStructureProto;
import com.lvl6.proto.InfoProto.FullTaskProto;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.GoldSaleProto;
import com.lvl6.proto.InfoProto.GroupChatMessageProto;
import com.lvl6.proto.InfoProto.LockBoxEventProto;
import com.lvl6.proto.InfoProto.PrivateChatPostProto;
import com.lvl6.proto.InfoProto.RareBoosterPurchaseProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.BattleDetailsRetrieveUtils;
import com.lvl6.retrieveutils.ClanChatPostRetrieveUtils;
import com.lvl6.retrieveutils.ClanRetrieveUtils;
import com.lvl6.retrieveutils.ClanTowerRetrieveUtils;
import com.lvl6.retrieveutils.EquipEnhancementFeederRetrieveUtils;
import com.lvl6.retrieveutils.EquipEnhancementRetrieveUtils;
import com.lvl6.retrieveutils.FirstTimeUsersRetrieveUtils;
import com.lvl6.retrieveutils.IAPHistoryRetrieveUtils;
import com.lvl6.retrieveutils.LoginHistoryRetrieveUtils;
import com.lvl6.retrieveutils.MarketplaceTransactionRetrieveUtils;
import com.lvl6.retrieveutils.PlayerWallPostRetrieveUtils;
import com.lvl6.retrieveutils.PrivateChatPostRetrieveUtils;
import com.lvl6.retrieveutils.UnhandledBlacksmithAttemptRetrieveUtils;
import com.lvl6.retrieveutils.UserBoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.UserDailyBonusRewardHistoryRetrieveUtils;
import com.lvl6.retrieveutils.UserLockBoxEventRetrieveUtils;
import com.lvl6.retrieveutils.UserTaskRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterItemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BoosterPackRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.DailyBonusRewardRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.GoldSaleRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LevelsRequiredExperienceRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.NeutralCityElementsRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.scriptsjava.generatefakeusers.NameGeneratorElven;
import com.lvl6.server.GameServer;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component
@DependsOn("gameServer")
public class StartupController extends EventController {
  private static String nameRulesFile = "namerulesElven.txt";
  private static int syllablesInName1 = 2;
  private static int syllablesInName2 = 3;

	@Autowired
	protected NameGeneratorElven nameGeneratorElven;

	public NameGeneratorElven getNameGeneratorElven() {
		return nameGeneratorElven;
	}

	public void setNameGeneratorElven(NameGeneratorElven nameGeneratorElven) {
		this.nameGeneratorElven = nameGeneratorElven;
	}
  private static Logger log = LoggerFactory.getLogger(new Object() {
  }.getClass().getEnclosingClass());

  public StartupController() {
    numAllocatedThreads = 3;
  }

  @Resource(name = "goodEquipsRecievedFromBoosterPacks")
  protected IList<RareBoosterPurchaseProto> goodEquipsRecievedFromBoosterPacks;

  public IList<RareBoosterPurchaseProto> getGoodEquipsRecievedFromBoosterPacks() {
    return goodEquipsRecievedFromBoosterPacks;
  }

  public void setGoodEquipsRecievedFromBoosterPacks(
      IList<RareBoosterPurchaseProto> goodEquipsRecievedFromBoosterPacks) {
    this.goodEquipsRecievedFromBoosterPacks = goodEquipsRecievedFromBoosterPacks;
  }

  @Resource(name = "globalChat")
  protected IList<GroupChatMessageProto> chatMessages;

  public IList<GroupChatMessageProto> getChatMessages() {
    return chatMessages;
  }

  public void setChatMessages(IList<GroupChatMessageProto> chatMessages) {
    this.chatMessages = chatMessages;
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
    StartupRequestProto reqProto = ((StartupRequestEvent) event).getStartupRequestProto();
    log.info("Processing startup request event");
    UpdateStatus updateStatus;
    String udid = reqProto.getUdid();
    String apsalarId = reqProto.hasApsalarId() ? reqProto.getApsalarId() : null;

    StartupResponseProto.Builder resBuilder = StartupResponseProto.newBuilder();

    MiscMethods.setMDCProperties(udid, null, MiscMethods.getIPOfPlayer(server, null, udid));

    double tempClientVersionNum = reqProto.getVersionNum() * 10;
    double tempLatestVersionNum = GameServer.clientVersionNumber * 10;

    // Check version number
    if ((int) tempClientVersionNum < (int) tempLatestVersionNum && tempClientVersionNum > 12.5) {
      updateStatus = UpdateStatus.MAJOR_UPDATE;
      log.info("player has been notified of forced update");
    } else if (tempClientVersionNum < tempLatestVersionNum) {
      updateStatus = UpdateStatus.MINOR_UPDATE;
    } else {
      updateStatus = UpdateStatus.NO_UPDATE;
    }

    resBuilder.setUpdateStatus(updateStatus);
    resBuilder.setAppStoreURL(Globals.APP_STORE_URL());
    resBuilder.setReviewPageURL(Globals.REVIEW_PAGE_URL());
    resBuilder.setReviewPageConfirmationMessage(Globals.REVIEW_PAGE_CONFIRMATION_MESSAGE);

    User user = null;

    // Don't fill in other fields if it is a major update
    StartupStatus startupStatus = StartupStatus.USER_NOT_IN_DB;

    Timestamp now = new Timestamp(new Date().getTime());
    boolean isLogin = true;

    int newNumConsecutiveDaysLoggedIn = 0;

    if (updateStatus != UpdateStatus.MAJOR_UPDATE) {
      user = RetrieveUtils.userRetrieveUtils().getUserByUDID(udid);
      if (user != null) {
        server.lockPlayer(user.getId(), this.getClass().getSimpleName());
        try {
          startupStatus = StartupStatus.USER_IN_DB;
          log.info("No major update... getting user info");
          newNumConsecutiveDaysLoggedIn = setDailyBonusInfo(resBuilder, user, now);
          setCitiesAndUserCityInfos(resBuilder, user);
          setInProgressAndAvailableQuests(resBuilder, user);
          setUserEquipsAndEquips(resBuilder, user);
          resBuilder.setExperienceRequiredForNextLevel(LevelsRequiredExperienceRetrieveUtils
              .getRequiredExperienceForLevel(user.getLevel() + 1));
          resBuilder.setExperienceRequiredForCurrentLevel(LevelsRequiredExperienceRetrieveUtils
              .getRequiredExperienceForLevel(user.getLevel()));
          setNotifications(resBuilder, user);
          setWhetherPlayerCompletedInAppPurchase(resBuilder, user);
          setUnhandledForgeAttempts(resBuilder, user);
          setNoticesToPlayers(resBuilder, user);
          setUserClanInfos(resBuilder, user);
          setLockBoxEvents(resBuilder, user);
          setMarketplaceSearchEquips(resBuilder);
          setStaticEquipsAndStructs(resBuilder);
          setChatMessages(resBuilder, user);
          setBoosterPurchases(resBuilder);
          setGoldSales(resBuilder, user);
          resBuilder.addAllClanTierLevels(MiscMethods.getAllClanTierLevelProtos());
          // if(server.lockClanTowersTable()) {
          setClanTowers(resBuilder);
          // }
          resBuilder.addAllBossEvents(MiscMethods.currentBossEvents());
          setLeaderboardEventStuff(resBuilder);
          setEquipEnhancementStuff(resBuilder, user);
          setAllies(resBuilder, user);
          setPrivateChatPosts(resBuilder, user);

          FullUserProto fup = CreateInfoProtoUtils.createFullUserProtoFromUser(user);
          resBuilder.setSender(fup);

          boolean isNewUser = false;
          InsertUtils.get().insertIntoLoginHistory(udid, user.getId(), now, isLogin, isNewUser);
        } catch (Exception e) {
          log.error("exception in StartupController processEvent", e);
        } finally {
          // server.unlockClanTowersTable();
          server.unlockPlayer(user.getId(), this.getClass().getSimpleName());
        }
      } else {
        log.info("tutorial player with udid " + udid);

        boolean userLoggedIn = LoginHistoryRetrieveUtils.userLoggedInByUDID(udid);
        int numOldAccounts = RetrieveUtils.userRetrieveUtils().numAccountsForUDID(udid);
        boolean alreadyInFirstTimeUsers = FirstTimeUsersRetrieveUtils.userExistsWithUDID(udid);
        boolean isFirstTimeUser = false;
        // log.info("userLoggedIn=" + userLoggedIn + ", numOldAccounts="
        // + numOldAccounts
        // + ", alreadyInFirstTimeUsers=" + alreadyInFirstTimeUsers);
        if (!userLoggedIn && 0 >= numOldAccounts && !alreadyInFirstTimeUsers) {
          isFirstTimeUser = true;
        }

        if (isFirstTimeUser) {
          log.info("new player with udid " + udid);
          InsertUtils.get().insertIntoFirstTimeUsers(udid, null,
              reqProto.getMacAddress(), reqProto.getAdvertiserId(), now);
        }

        boolean goingThroughTutorial = true;
        InsertUtils.get().insertIntoLoginHistory(udid, 0, now, isLogin, goingThroughTutorial);
      }
      resBuilder.setStartupStatus(startupStatus);
      setConstants(resBuilder, startupStatus);
    }

    if (Globals.KABAM_ENABLED()) {
      String naid = retrieveKabamNaid(user, udid, reqProto.getMacAddress(),
          reqProto.getAdvertiserId());
      resBuilder.setKabamNaid(naid);
    }

    StartupResponseProto resProto = resBuilder.build();
    StartupResponseEvent resEvent = new StartupResponseEvent(udid);
    resEvent.setTag(event.getTag());
    resEvent.setStartupResponseProto(resProto);

    // log.info("Sending struct");
    // sendAllStructs(udid, user);

    log.debug("Writing event response: " + resEvent);
    server.writePreDBEvent(resEvent, udid);
    log.debug("Wrote response event: " + resEvent);
    // for things that client doesn't need
    log.debug("After response tasks");

    // if app is not in force tutorial execute this function,
    // regardless of whether the user is new or restarting from an account
    // reset
    updateLeaderboard(apsalarId, user, now, newNumConsecutiveDaysLoggedIn);
  }

  private void setPrivateChatPosts(Builder resBuilder, User aUser) {
    int userId = aUser.getId();
    boolean isRecipient = true;
    Map<Integer, Integer> userIdsToPrivateChatPostIds = null;
    Map<Integer, PrivateChatPost> postIdsToPrivateChatPosts = new HashMap<Integer, PrivateChatPost>();
    Map<Integer, User> userIdsToUsers = null;
    Map<Integer, Set<Integer>> clanIdsToUserIdSet = null;
    Map<Integer, Clan> clanIdsToClans = null;
    List<Integer> clanlessUserIds = new ArrayList<Integer>();
    List<Integer> clanIdList = new ArrayList<Integer>();
    List<Integer> privateChatPostIds = new ArrayList<Integer>();

    //get all the most recent posts sent to this user
    Map<Integer, PrivateChatPost> postsUserReceived = 
        PrivateChatPostRetrieveUtils.getMostRecentPrivateChatPostsByOrToUser(
            userId, isRecipient, ControllerConstants.STARTUP__MAX_PRIVATE_CHAT_POSTS_RECEIVED);

    //get all the most recent posts this user sent
    isRecipient = false;
    Map<Integer, PrivateChatPost> postsUserSent = 
        PrivateChatPostRetrieveUtils.getMostRecentPrivateChatPostsByOrToUser(
            userId, isRecipient, ControllerConstants.STARTUP__MAX_PRIVATE_CHAT_POSTS_SENT);

    if ((null == postsUserReceived || postsUserReceived.isEmpty()) &&
        (null == postsUserSent || postsUserSent.isEmpty()) ) {
      log.info("user has no private chats. aUser=" + aUser);
      return;
    }

    //link other users with private chat posts and combine all the posts
    //linking is done to select only the latest post between the duple (userId, otherUserId)
    userIdsToPrivateChatPostIds = aggregateOtherUserIdsAndPrivateChatPost(postsUserReceived, postsUserSent, postIdsToPrivateChatPosts);

    if (null != userIdsToPrivateChatPostIds && !userIdsToPrivateChatPostIds.isEmpty()) {
      //retrieve all users
      List<Integer> userIdList = new ArrayList<Integer>();
      userIdList.addAll(userIdsToPrivateChatPostIds.keySet());
      userIdList.add(userId); //userIdsToPrivateChatPostIds contains userIds other than 'this' userId
      userIdsToUsers = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIdList);
    } else {
      //user did not send any nor received any private chat posts
      log.error("unexpected error: aggregating private chat post ids returned nothing");
      return;
    }
    if (null == userIdsToUsers || userIdsToUsers.isEmpty() ||
        userIdsToUsers.size() == 1) {
      log.error("unexpected error: perhaps user talked to himself. postsUserReceved="
          + MiscMethods.shallowMapToString(postsUserReceived) + ", postsUserSent="
          + MiscMethods.shallowMapToString(postsUserSent) + ", aUser=" + aUser);
      return;
    }

    //get all the clans for the users (a map: clanId->set(userId))
    //put the clanless users in the second argument: userIdsToClanlessUsers
    clanIdsToUserIdSet = determineClanIdsToUserIdSet(userIdsToUsers, clanlessUserIds);
    if (null != clanIdsToUserIdSet && !clanIdsToUserIdSet.isEmpty()) {
      clanIdList.addAll(clanIdsToUserIdSet.keySet());
      //retrieve all clans for the users
      clanIdsToClans = ClanRetrieveUtils.getClansByIds(clanIdList);
    }


    //create the protoList
    privateChatPostIds.addAll(userIdsToPrivateChatPostIds.values());
    List<PrivateChatPostProto> pcppList = CreateInfoProtoUtils.createPrivateChatPostProtoList(
        clanIdsToClans, clanIdsToUserIdSet, userIdsToUsers, clanlessUserIds, privateChatPostIds,
        postIdsToPrivateChatPosts);

    resBuilder.addAllPcpp(pcppList);
  }

  private Map<Integer, Integer> aggregateOtherUserIdsAndPrivateChatPost(
      Map<Integer, PrivateChatPost> postsUserReceived, Map<Integer, PrivateChatPost> postsUserSent,
      Map<Integer, PrivateChatPost> postIdsToPrivateChatPosts) {
    Map<Integer, Integer> userIdsToPrivateChatPostIds = new HashMap<Integer, Integer>();

    //go through the posts specific user received
    if (null != postsUserReceived && !postsUserReceived.isEmpty()) {
      for (int pcpId : postsUserReceived.keySet()) {
        PrivateChatPost postUserReceived = postsUserReceived.get(pcpId);
        int senderId = postUserReceived.getPosterId();

        //record that the other user and specific user chatted
        userIdsToPrivateChatPostIds.put(senderId, pcpId);
      }
      //combine all the posts together
      postIdsToPrivateChatPosts.putAll(postsUserReceived);
    }

    if (null != postsUserSent && !postsUserSent.isEmpty()) {
      //go through the posts user sent
      for (int pcpId: postsUserSent.keySet()) {
        PrivateChatPost postUserSent = postsUserSent.get(pcpId);
        int recipientId = postUserSent.getRecipientId();

        //determine the latest post between other recipientId and specific user
        if (!userIdsToPrivateChatPostIds.containsKey(recipientId)) {
          //didn't see this user id yet, record it
          userIdsToPrivateChatPostIds.put(recipientId, pcpId);

        } else {
          //recipientId sent something to specific user, choose the latest one
          int postIdUserReceived = userIdsToPrivateChatPostIds.get(recipientId);
          //postsUserReceived can't be null here
          PrivateChatPost postUserReceived = postsUserReceived.get(postIdUserReceived);

          Date newDate = postUserSent.getTimeOfPost();
          Date existingDate = postUserReceived.getTimeOfPost();
          if (newDate.getTime() > existingDate.getTime()) {
            //since postUserSent's time is later, choose this post for recipientId
            userIdsToPrivateChatPostIds.put(recipientId, pcpId);
          }
        }
      }

      //combine all the posts together
      postIdsToPrivateChatPosts.putAll(postsUserSent);
    }

    return userIdsToPrivateChatPostIds;
  }

  private Map<Integer, Set<Integer>> determineClanIdsToUserIdSet(Map<Integer, User> userIdsToUsers,
      List<Integer> clanlessUserUserIds) {
    Map<Integer, Set<Integer>> clanIdsToUserIdSet = new HashMap<Integer, Set<Integer>>();
    if (null == userIdsToUsers  || userIdsToUsers.isEmpty()) {
      return clanIdsToUserIdSet;
    }
    //go through users and lump them by clan id
    for (int userId : userIdsToUsers.keySet()) {
      User u = userIdsToUsers.get(userId);
      int clanId = u.getClanId();
      if (ControllerConstants.NOT_SET == clanId) {
        clanlessUserUserIds.add(userId);
        continue;	      
      }

      if (clanIdsToUserIdSet.containsKey(clanId)) {
        //clan id exists, add userId in with others
        Set<Integer> userIdSet = clanIdsToUserIdSet.get(clanId);
        userIdSet.add(userId);
      } else {
        //clan id doesn't exist, create new grouping of userIds
        Set<Integer> userIdSet = new HashSet<Integer>();
        userIdSet.add(userId);

        clanIdsToUserIdSet.put(clanId, userIdSet);
      }
    }
    return clanIdsToUserIdSet;
  }

  private void setEquipEnhancementStuff(StartupResponseProto.Builder resBuilder, User aUser) {
    int userId = aUser.getId();
    List<EquipEnhancement> equipUnderEnhancements = EquipEnhancementRetrieveUtils
        .getEquipEnhancementsForUser(userId);
    if (null == equipUnderEnhancements || equipUnderEnhancements.isEmpty()) {
      return;
    }

    EquipEnhancement equipUnderEnhancement = equipUnderEnhancements.get(0);

    int equipEnhancementId = equipUnderEnhancement.getId();
    List<EquipEnhancementFeeder> feeders = EquipEnhancementFeederRetrieveUtils
        .getEquipEnhancementFeedersForEquipEnhancementId(equipEnhancementId);

    EquipEnhancementProto eeProto = CreateInfoProtoUtils.createEquipEnhancementProto(
        equipUnderEnhancement, feeders);
    resBuilder.setEquipEnhancement(eeProto);
  }

  private void setClanTowers(StartupResponseProto.Builder resBuilder) {
    List<ClanTower> towers = ClanTowerRetrieveUtils.getAllClanTowers();

    for (ClanTower tower : towers) {
      resBuilder.addClanTowers(CreateInfoProtoUtils.createClanTowerProtoFromClanTower(tower));
    }
  }

  // retrieve's the active leaderboard event prizes and rewards for the events
  private void setLeaderboardEventStuff(StartupResponseProto.Builder resBuilder) {
    resBuilder.addAllLeaderboardEvents(MiscMethods.currentLeaderboardEventProtos());
  }

  private String retrieveKabamNaid(User user, String openUdid, String mac, String advertiserId) {
    String host;
    int port = 443;
    int clientId;
    String secret;
    if (Globals.IS_SANDBOX()) {
      host = KabamProperties.SANDBOX_API_URL;
      clientId = KabamProperties.SANDBOX_CLIENT_ID;
      secret = KabamProperties.SANDBOX_SECRET;
    } else {
      host = KabamProperties.PRODUCTION_API_URL;
      clientId = KabamProperties.PRODUCTION_CLIENT_ID;
      secret = KabamProperties.PRODUCTION_SECRET;
    }

    KabamApi kabamApi = new KabamApi(host, port, secret);
    String userId = openUdid;
    String platform = "iphone";

    String biParams = "{\"open_udid\":\"" + userId + "\",\"mac\":\"" + mac
        + "\",\"mac_hash\":\"" + DigestUtils.md5Hex(mac) + "\",\"advertiser_id\":\"" + advertiserId
        + "\"}";

    MobileNaidResponse naidResponse;
    try {
      naidResponse = kabamApi.mobileGetNaid(userId, clientId, platform, biParams,
          new Date().getTime() / 1000);
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }

    if (naidResponse.getReturnCode() == ResponseCode.Success) {
      if (user != null) {
        user.updateSetKabamNaid(naidResponse.getNaid());
      }
      log.info("Successfully got kabam naid.");
      return naidResponse.getNaid()+"";
    } else {
      log.error("Error retrieving kabam naid: " + naidResponse.getReturnCode());
    }
    return "";
  }

  private void setChatMessages(StartupResponseProto.Builder resBuilder, User user) {
    if (user.getClanId() > 0) {
      List<ClanChatPost> activeClanChatPosts;
      activeClanChatPosts = ClanChatPostRetrieveUtils.getMostRecentClanChatPostsForClan(
          ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP, user.getClanId());

      if (activeClanChatPosts != null) {
        if (activeClanChatPosts != null && activeClanChatPosts.size() > 0) {
          List<Integer> userIds = new ArrayList<Integer>();
          for (ClanChatPost p : activeClanChatPosts) {
            userIds.add(p.getPosterId());
          }
          Map<Integer, User> usersByIds = null;
          if (userIds.size() > 0) {
            usersByIds = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIds);
            for (int i = activeClanChatPosts.size() - 1; i >= 0; i--) {
              ClanChatPost pwp = activeClanChatPosts.get(i);
              resBuilder.addClanChats(CreateInfoProtoUtils
                  .createGroupChatMessageProtoFromClanChatPost(pwp,
                      usersByIds.get(pwp.getPosterId())));
            }
          }
        }
      }
    }

    Iterator<GroupChatMessageProto> it = chatMessages.iterator();
    List<GroupChatMessageProto> globalChats = new ArrayList<GroupChatMessageProto>();
    while (it.hasNext()) {
      globalChats.add(it.next());
    }

    Comparator<GroupChatMessageProto> c = new Comparator<GroupChatMessageProto>() {
      @Override
      public int compare(GroupChatMessageProto o1, GroupChatMessageProto o2) {
        if (o1.getTimeOfChat() < o2.getTimeOfChat()) {
          return -1;
        } else if (o1.getTimeOfChat() > o2.getTimeOfChat()) {
          return 1;
        } else {
          return 0;
        }
      }
    };
    Collections.sort(globalChats, c);
    // Need to add them in reverse order
    for (int i = 0; i < globalChats.size(); i++) {
      resBuilder.addGlobalChats(globalChats.get(i));
    }
  }

  private void setBoosterPurchases(StartupResponseProto.Builder resBuilder) {
    Iterator<RareBoosterPurchaseProto> it = goodEquipsRecievedFromBoosterPacks.iterator();
    List<RareBoosterPurchaseProto> boosterPurchases = new ArrayList<RareBoosterPurchaseProto>();
    while (it.hasNext()) {
      boosterPurchases.add(it.next());
    }

    Comparator<RareBoosterPurchaseProto> c = new Comparator<RareBoosterPurchaseProto>() {
      @Override
      public int compare(RareBoosterPurchaseProto o1, RareBoosterPurchaseProto o2) {
        if (o1.getTimeOfPurchase() < o2.getTimeOfPurchase()) {
          return -1;
        } else if (o1.getTimeOfPurchase() > o2.getTimeOfPurchase()) {
          return 1;
        } else {
          return 0;
        }
      }
    };
    Collections.sort(boosterPurchases, c);
    // Need to add them in reverse order
    for (int i = 0; i < boosterPurchases.size(); i++) {
      resBuilder.addRareBoosterPurchases(boosterPurchases.get(i));
    }
  }

  private void setMarketplaceSearchEquips(StartupResponseProto.Builder resBuilder) {
    Collection<Equipment> equips = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().values();
    for (Equipment equip : equips) {
      resBuilder.addMktSearchEquips(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equip));
    }
  }

  private void setStaticEquipsAndStructs(StartupResponseProto.Builder resBuilder) {
    Collection<Equipment> equips = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().values();
    for (Equipment equip : equips) {
      resBuilder.addStaticEquips(CreateInfoProtoUtils.createFullEquipProtoFromEquip(equip));
    }

    Collection<Structure> structs = StructureRetrieveUtils.getStructIdsToStructs().values();
    for (Structure struct : structs) {
      resBuilder.addStaticStructs(CreateInfoProtoUtils.createFullStructureProtoFromStructure(struct));
    }
  }

  private void setLockBoxEvents(StartupResponseProto.Builder resBuilder, User user) {
    resBuilder.addAllLockBoxEvents(MiscMethods.currentLockBoxEvents());
    Map<Integer, UserLockBoxEvent> map = UserLockBoxEventRetrieveUtils
        .getLockBoxEventIdsToLockBoxEventsForUser(user.getId());
    for (LockBoxEventProto p : resBuilder.getLockBoxEventsList()) {
      UserLockBoxEvent e = map.get(p.getLockBoxEventId());
      if (e != null) {
        resBuilder.addUserLockBoxEvents(CreateInfoProtoUtils.createUserLockBoxEventProto(e,
            user.getType()));
      }
    }
  }

  private void setGoldSales(StartupResponseProto.Builder resBuilder, User user) {
    GoldSaleProto sale = MiscMethods.createFakeGoldSaleForNewPlayer(user);
    if (sale != null) {
      resBuilder.addGoldSales(sale);
    }

    List<GoldSale> sales = GoldSaleRetrieveUtils.getCurrentAndFutureGoldSales();
    if (sales != null && sales.size() > 0) {
      for (GoldSale s : sales) {
        resBuilder.addGoldSales(CreateInfoProtoUtils.createGoldSaleProtoFromGoldSale(s));
      }
    }
  }

  protected void updateLeaderboard(String apsalarId, User user, Timestamp now,
      int newNumConsecutiveDaysLoggedIn) {
    if (user != null) {
      log.info("Updating leaderboard for user " + user.getId());
      syncApsalaridLastloginConsecutivedaysloggedinResetBadges(user, apsalarId, now,
          newNumConsecutiveDaysLoggedIn);
      LeaderBoardUtil leaderboard = AppContext.getApplicationContext().getBean(LeaderBoardUtil.class);
      leaderboard.updateLeaderboardForUser(user);
    }
  }

  private void sendAllStructs(String udid, User user) {
    RetrieveStaticDataResponseEvent resEvent1 = new RetrieveStaticDataResponseEvent(0);
    RetrieveStaticDataResponseProto.Builder resProto1 = RetrieveStaticDataResponseProto.newBuilder();
    Map<Integer, Structure> structIdsToStructures = StructureRetrieveUtils.getStructIdsToStructs();
    for (Integer structId : structIdsToStructures.keySet()) {
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

  private void setUserClanInfos(StartupResponseProto.Builder resBuilder, User user) {
    List<UserClan> userClans = RetrieveUtils.userClanRetrieveUtils().getUserClansRelatedToUser(
        user.getId());
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
    Map<Integer, BlacksmithAttempt> blacksmithIdToBlacksmithAttempt = 
        UnhandledBlacksmithAttemptRetrieveUtils.getUnhandledBlacksmithAttemptsForUser(user.getId());
    int numEquipsBeingForged = blacksmithIdToBlacksmithAttempt.size();
    int numEquipsUserCanForge = ControllerConstants.FORGE_DEFAULT_NUMBER_OF_FORGE_SLOTS
        + user.getNumAdditionalForgeSlots();

    if (blacksmithIdToBlacksmithAttempt != null && numEquipsBeingForged <= numEquipsUserCanForge) {
      for (BlacksmithAttempt ba : blacksmithIdToBlacksmithAttempt.values()) {

        int baId = ba.getId();
        resBuilder.addUnhandledForgeAttempt(
            CreateInfoProtoUtils.createUnhandledBlacksmithAttemptProtoFromBlacksmithAttempt(
                blacksmithIdToBlacksmithAttempt.get(baId))
            );

        int equipId = ba.getEquipId();
        Equipment e = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(equipId);
        resBuilder.addForgeAttemptEquip(
            CreateInfoProtoUtils.createFullEquipProtoFromEquip(e)
            );
      }
    }

    if (blacksmithIdToBlacksmithAttempt != null && numEquipsBeingForged > numEquipsUserCanForge) {
      log.error("user has too many blacksmith attempts, should only have " + numEquipsUserCanForge + 
          ". blacksmith attempts = " + blacksmithIdToBlacksmithAttempt);
    }
  }

  private void setWhetherPlayerCompletedInAppPurchase(Builder resBuilder, User user) {
    boolean hasPurchased = IAPHistoryRetrieveUtils.checkIfUserHasPurchased(user.getId());
    resBuilder.setPlayerHasBoughtInAppPurchase(hasPurchased);
  }

  // returns the total number of consecutive days the user logged in,
  // awards user if user logged in for an additional consecutive day
  private int setDailyBonusInfo(Builder resBuilder, User user, Timestamp now) {
    // will keep track of total consecutive days user has logged in, just
    // for funzies
    List<Integer> numConsecDaysList = new ArrayList<Integer>();
    int totalConsecutiveDaysPlayed = 1;
    List<Boolean> rewardUserList = new ArrayList<Boolean>();
    boolean rewardUser = false;

    int consecutiveDaysPlayed = determineCurrentConsecutiveDay(user, now, numConsecDaysList,
        rewardUserList);
    if (!numConsecDaysList.isEmpty()) {
      totalConsecutiveDaysPlayed = numConsecDaysList.get(0);
      rewardUser = rewardUserList.get(0);
    }

    DailyBonusReward rewardForUser = determineRewardForUser(user);
    // function does nothing if null reward was returned from
    // determineRewardForUser
    Map<String, Integer> currentDayReward = selectRewardFromDailyBonusReward(rewardForUser,
        consecutiveDaysPlayed);

    List<Integer> equipIdRewardedList = new ArrayList<Integer>();
    // function does nothing if previous function returned null, or
    // either updates user's money or "purchases" booster pack for user
    boolean successful = writeDailyBonusRewardToDB(user, currentDayReward, rewardUser, now,
        equipIdRewardedList);
    if (successful) {
      int equipIdRewarded = equipIdRewardedList.get(0);
      writeToUserDailyBonusRewardHistory(user, currentDayReward, consecutiveDaysPlayed, now,
          equipIdRewarded);
    }
    setDailyBonusStuff(resBuilder, user, rewardUser, rewardForUser);
    return totalConsecutiveDaysPlayed;
  }

  // totalConsecutiveDaysList will contain one element the actual number of
  // consecutive
  // days the user has logged into our game, not really necessary to keep
  // track...
  private int determineCurrentConsecutiveDay(User user, Timestamp now,
      List<Integer> totalConsecutiveDaysList, List<Boolean> rewardUserList) {
    // SETTING STUFF UP
    int userId = user.getId();
    UserDailyBonusRewardHistory lastReward = UserDailyBonusRewardHistoryRetrieveUtils
        .getLastDailyRewardAwardedForUserId(userId);
    Date nowDate = new Date(now.getTime());
    long nowDateMillis = nowDate.getTime();

    if (null == lastReward) {
      log.info("user has never received a daily bonus reward. Setting consecutive days played to 1.");
      totalConsecutiveDaysList.add(1);
      rewardUserList.add(true);
      return 1;
    }
    // let days = consecutive day amount corresponding to the reward user
    // was given
    // if reward was more than one day ago (in past), return 1
    // else if user was rewarded yesterday return the (1 + days)
    // else reward was today return days
    int nthConsecutiveDay = lastReward.getNthConsecutiveDay();
    Date dateLastAwarded = lastReward.getDateAwarded();
    long dateLastMillis = dateLastAwarded.getTime();
    boolean awardedInThePast = nowDateMillis > dateLastMillis;

    int dayDiff = MiscMethods.dateDifferenceInDays(dateLastAwarded, nowDate);
    // log.info("dateLastAwarded=" + dateLastAwarded + ", nowDate=" +
    // nowDate + ", day difference=" + dayDiff);
    if (1 < dayDiff && awardedInThePast) {
      // log.info("user broke their logging in streak. previous daily bonus reward: "
      // + lastReward
      // + ", now=" + now);
      // been a while since user last logged in
      totalConsecutiveDaysList.add(1);
      rewardUserList.add(true);
      return 1;
    } else if (1 == dayDiff && awardedInThePast) {
      // log.info("awarding user. previous daily bonus reward: " +
      // lastReward + ", now=" + now);
      // user logged in yesterday
      totalConsecutiveDaysList.add(user.getNumConsecutiveDaysPlayed() + 1);
      rewardUserList.add(true);
      return nthConsecutiveDay % ControllerConstants.STARTUP__DAILY_BONUS_MAX_CONSECUTIVE_DAYS + 1;
    } else {
      // either user logged in today or user tried faking time, but who
      // cares...
      totalConsecutiveDaysList.add(user.getNumConsecutiveDaysPlayed());
      rewardUserList.add(false);
      // log.info("user already collected his daily reward. previous daily bonus reward: "
      // + lastReward + ", now=" + now);
      return nthConsecutiveDay;
    }
  }

  private DailyBonusReward determineRewardForUser(User aUser) {
    Map<Integer, DailyBonusReward> allDailyRewards = DailyBonusRewardRetrieveUtils
        .getDailyBonusRewardIdsToDailyBonusRewards();
    // sanity check, exit if it fails
    if (null == allDailyRewards || allDailyRewards.isEmpty()) {
      log.error("unexpected error: There are no daily bonus rewards set up in the daily_bonus_reward table");
      return null;
    }

    int level = aUser.getLevel();
    // determine daily bonus reward for this user's level, exit if there it
    // doesn't exist
    DailyBonusReward reward = selectDailyBonusRewardForLevel(allDailyRewards, level);
    if (null == reward) {
      log.error("unexpected error: no daily bonus rewards available for level=" + level);
    }
    return reward;
  }

  private DailyBonusReward selectDailyBonusRewardForLevel(Map<Integer, DailyBonusReward> allRewards,
      int userLevel) {
    DailyBonusReward returnValue = null;
    for (int id : allRewards.keySet()) {
      DailyBonusReward dbr = allRewards.get(id);
      int minLevel = dbr.getMinLevel();
      int maxLevel = dbr.getMaxLevel();
      if (minLevel <= userLevel && userLevel <= maxLevel) {
        // we found the reward to return
        returnValue = dbr;
        break;
      }
    }
    return returnValue;
  }

  private Map<String, Integer> selectRewardFromDailyBonusReward(DailyBonusReward rewardForUser,
      int numConsecutiveDaysPlayed) {
    if (null == rewardForUser) {
      return null;
    }
    if (5 < numConsecutiveDaysPlayed || 0 >= numConsecutiveDaysPlayed) {
      log.error("unexpected error: number of consecutive days played is not in the range [1,5]. "
          + "numConsecutiveDaysPlayed=" + numConsecutiveDaysPlayed);
      return null;
    }
    Map<String, Integer> reward = getCurrentDailyReward(rewardForUser, numConsecutiveDaysPlayed);
    return reward;
  }

  // sets the rewards the user gets/ will get in the daily bonus info builder
  private Map<String, Integer> getCurrentDailyReward(DailyBonusReward reward, int numConsecutiveDaysPlayed) {
    Map<String, Integer> returnValue = new HashMap<String, Integer>();
    String key = "";
    int value = ControllerConstants.NOT_SET;

    String silver = MiscMethods.silver;
    String gold = MiscMethods.gold;
    String boosterPackIdString = MiscMethods.boosterPackId;

    // mimicking fall through in switch statement, setting reward user just
    // got
    // today and will get in future logins in 5 consecutive day spans
    if (5 == numConsecutiveDaysPlayed) {
      // can't set reward in the builder; currently have booster pack id
      // need equip id
      key = boosterPackIdString;
      List<Integer> boosterPackIds = reward.getDayFiveBoosterPackIds();
      value = MiscMethods.getRandomIntFromList(boosterPackIds);
    }
    if (4 == numConsecutiveDaysPlayed) {
      key = silver;
      value = reward.getDayFourCoins();
    }
    if (3 == numConsecutiveDaysPlayed) {
      key = gold;
      value = reward.getDayThreeDiamonds();
    }
    if (2 == numConsecutiveDaysPlayed) {
      key = silver;
      value = reward.getDayTwoCoins();
    }
    if (1 == numConsecutiveDaysPlayed) {
      key = silver;
      value = reward.getDayOneCoins();
    }
    returnValue.put(key, value);
    return returnValue;
  }

  // returns the equip id user "purchased" by logging in
  // mimics purchase booster pack controller except the argument checking and
  // dealing with money
  private int purchaseBoosterPack(int boosterPackId, User aUser, int numBoosterItemsUserWants, Timestamp now) {
    int equipId = ControllerConstants.NOT_SET;
    try {
      // local vars
      int userId = aUser.getId();
      BoosterPack aPack = BoosterPackRetrieveUtils.getBoosterPackForBoosterPackId(boosterPackId);
      Map<Integer, BoosterItem> boosterItemIdsToBoosterItems = BoosterItemRetrieveUtils
          .getBoosterItemIdsToBoosterItemsForBoosterPackId(boosterPackId);
      Map<Integer, Integer> boosterItemIdsToNumCollected = UserBoosterItemRetrieveUtils
          .getBoosterItemIdsToQuantityForUser(userId);
      Map<Integer, Integer> newBoosterItemIdsToNumCollected = new HashMap<Integer, Integer>();
      List<BoosterItem> itemsUserReceives = new ArrayList<BoosterItem>();
      List<Boolean> collectedBeforeReset = new ArrayList<Boolean>();

      // actually selecting equips
      boolean resetOccurred = MiscMethods.getAllBoosterItemsForUser(boosterItemIdsToBoosterItems,
          boosterItemIdsToNumCollected, numBoosterItemsUserWants, aUser, aPack, itemsUserReceives,
          collectedBeforeReset);
      newBoosterItemIdsToNumCollected = new HashMap<Integer, Integer>(boosterItemIdsToNumCollected);
      boolean successful = writeBoosterStuffToDB(aUser, boosterItemIdsToNumCollected,
          newBoosterItemIdsToNumCollected, itemsUserReceives, collectedBeforeReset, resetOccurred);
      if (successful) {
        //exclude from daily limit check in PurchaseBoosterPackController
        boolean excludeFromLimitCheck = true;
        MiscMethods.writeToUserBoosterPackHistoryOneUser(userId, boosterPackId,
            numBoosterItemsUserWants, now, itemsUserReceives, excludeFromLimitCheck);
        equipId = getEquipId(numBoosterItemsUserWants, itemsUserReceives);
      }

    } catch (Exception e) {
      log.error("unexpected error: ", e);
    }
    return equipId;
  }

  private int getEquipId(int numBoosterItemsUserWants, List<BoosterItem> itemsUserReceives) {
    if (1 != numBoosterItemsUserWants) {
      log.error("unexpected error: trying to buy more than one equip from booster pack. boosterItems="
          + MiscMethods.shallowListToString(itemsUserReceives));
      return ControllerConstants.NOT_SET;
    }
    BoosterItem bi = itemsUserReceives.get(0);
    return bi.getEquipId();
  }

  private boolean writeBoosterStuffToDB(User aUser, Map<Integer, Integer> boosterItemIdsToNumCollected,
      Map<Integer, Integer> newBoosterItemIdsToNumCollected, List<BoosterItem> itemsUserReceives,
      List<Boolean> collectedBeforeReset, boolean resetOccurred) {
    int userId = aUser.getId();
    List<Integer> userEquipIds = MiscMethods.insertNewUserEquips(userId, itemsUserReceives);
    if (null == userEquipIds || userEquipIds.isEmpty() || userEquipIds.size() != itemsUserReceives.size()) {
      log.error("unexpected error: failed to insert equip for user. boosteritems="
          + MiscMethods.shallowListToString(itemsUserReceives));
      return false;
    }

    if (!MiscMethods.updateUserBoosterItems(itemsUserReceives, collectedBeforeReset,
        boosterItemIdsToNumCollected, newBoosterItemIdsToNumCollected, userId, resetOccurred)) {
      // failed to update user_booster_items
      log.error("unexpected error: failed to update user_booster_items for userId: " + userId
          + " attempting to delete equips given: " + MiscMethods.shallowListToString(userEquipIds));
      DeleteUtils.get().deleteUserEquips(userEquipIds);
      return false;
    }
    return true;
  }

  private boolean writeDailyBonusRewardToDB(User aUser, Map<String, Integer> currentDayReward,
      boolean giveToUser, Timestamp now, List<Integer> equipIdRewardedList) {
    int equipId = ControllerConstants.NOT_SET;
    if (!giveToUser || null == currentDayReward || 0 == currentDayReward.size()) {
      return false;
    }
    String key = "";
    int value = ControllerConstants.NOT_SET;
    // sanity check, should only be one reward: gold, silver, equipId
    if (1 == currentDayReward.size()) {
      String[] keys = new String[1];
      currentDayReward.keySet().toArray(keys);
      key = keys[0];
      value = currentDayReward.get(key);
    } else {
      log.error("unexpected error: current day's reward for a user is more than one. rewards="
          + MiscMethods.shallowMapToString(currentDayReward));
      return false;
    }

    int previousSilver = aUser.getCoins() + aUser.getVaultBalance();
    int previousGold = aUser.getDiamonds();
    if (key.equals(MiscMethods.boosterPackId)) {
      // since user got a booster pack id as reward, need to "buy it" for
      // him
      int numBoosterItemsUserWants = 1;
      // calling this will already give the user an equip
      equipId = purchaseBoosterPack(value, aUser, numBoosterItemsUserWants, now);
      if (ControllerConstants.NOT_SET == equipId) {
        log.error("unexpected error: failed to 'buy' booster pack for user. packId=" + value
            + ", user=" + aUser);
        return false;
      }
    }
    if (key.equals(MiscMethods.silver)) {
      if (!aUser.updateRelativeCoinsNaive(value)) {
        log.error("unexpected error: could not give silver bonus of " + value + " to user " + aUser);
        return false;
      } else {// gave user silver
        writeToUserCurrencyHistory(aUser, key, previousSilver, currentDayReward);
      }
    }
    if (key.equals(MiscMethods.gold)) {
      if (!aUser.updateRelativeDiamondsNaive(value)) {
        log.error("unexpected error: could not give silver bonus of " + value + " to user " + aUser);
        return false;
      } else {// gave user gold
        writeToUserCurrencyHistory(aUser, key, previousGold, currentDayReward);
      }
    }
    equipIdRewardedList.add(equipId);
    return true;
  }

  private void writeToUserDailyBonusRewardHistory(User aUser, Map<String, Integer> rewardForUser,
      int nthConsecutiveDay, Timestamp now, int equipIdRewarded) {
    int userId = aUser.getId();
    int currencyRewarded = ControllerConstants.NOT_SET;
    boolean isCoins = false;
    int boosterPackIdRewarded = ControllerConstants.NOT_SET;

    String boosterPackId = MiscMethods.boosterPackId;
    String silver = MiscMethods.silver;
    String gold = MiscMethods.gold;
    if (rewardForUser.containsKey(boosterPackId)) {
      boosterPackIdRewarded = rewardForUser.get(boosterPackId);
    }
    if (rewardForUser.containsKey(silver)) {
      currencyRewarded = rewardForUser.get(silver);
      isCoins = true;
    }
    if (rewardForUser.containsKey(gold)) {
      currencyRewarded = rewardForUser.get(gold);
    }
    int numInserted = InsertUtils.get().insertIntoUserDailyRewardHistory(userId, currencyRewarded,
        isCoins, boosterPackIdRewarded, equipIdRewarded, nthConsecutiveDay, now);
    if (1 != numInserted) {
      log.error("unexpected error: could not record that user got a reward for this day: " + now);
    }
  }

  private void setDailyBonusStuff(Builder resBuilder, User aUser, boolean rewardUser,
      DailyBonusReward rewardForUser) {
    // log.info("rewardUser=" + rewardUser + "rewardForUser=" +
    // rewardForUser + "user=" + aUser);

    int userId = aUser.getId();
    // there should be a reward inserted if things saved sans a hitch
    UserDailyBonusRewardHistory udbrh = UserDailyBonusRewardHistoryRetrieveUtils
        .getLastDailyRewardAwardedForUserId(userId);

    if (null == udbrh || null == rewardForUser) {
      log.error("unexpected error: no daily bonus reward history exists for user=" + aUser);
      return;
    }
    int consecutiveDaysPlayed = udbrh.getNthConsecutiveDay();

    DailyBonusInfo.Builder dbib = DailyBonusInfo.newBuilder();
    if (5 == consecutiveDaysPlayed) {
      // user just got an equip
      int boosterPackId = udbrh.getBoosterPackIdRewarded();
      BoosterPack bp = BoosterPackRetrieveUtils.getBoosterPackForBoosterPackId(boosterPackId);
      Map<Integer, BoosterItem> biMap = BoosterItemRetrieveUtils
          .getBoosterItemIdsToBoosterItemsForBoosterPackId(boosterPackId);
      Collection<BoosterItem> biList = biMap.values();
      BoosterPackProto aBoosterPackProto = CreateInfoProtoUtils.createBoosterPackProto(bp, biList);
      dbib.setBoosterPack(aBoosterPackProto);

      // log.info("setting 5th consecutive day reward");
      int equipId = udbrh.getEquipIdRewarded();
      dbib.setEquipId(equipId);
    }
    if (4 >= consecutiveDaysPlayed) {
      // log.info("setting 4th consecutive day reward");
      dbib.setDayFourCoins(rewardForUser.getDayFourCoins());
    }
    if (3 >= consecutiveDaysPlayed) {
      // log.info("setting 3rd consecutive day reward");
      dbib.setDayThreeDiamonds(rewardForUser.getDayThreeDiamonds());
    }
    if (2 >= consecutiveDaysPlayed) {
      // log.info("setting 2nd consecutive day reward");
      dbib.setDayTwoCoins(rewardForUser.getDayTwoCoins());
    }
    if (1 == consecutiveDaysPlayed) {
      // log.info("setting first consecutive day reward");
      dbib.setDayOneCoins(rewardForUser.getDayOneCoins());
    }
    // log.info("nth consecutive day=" + consecutiveDaysPlayed);
    Date dateAwarded = udbrh.getDateAwarded();
    long dateAwardedMillis = dateAwarded.getTime();
    dbib.setTimeAwarded(dateAwardedMillis);
    dbib.setNumConsecutiveDaysPlayed(consecutiveDaysPlayed);
    resBuilder.setDailyBonusInfo(dbib.build());
  }

  private void syncApsalaridLastloginConsecutivedaysloggedinResetBadges(User user, String apsalarId,
      Timestamp loginTime, int newNumConsecutiveDaysLoggedIn) {
    if (user.getApsalarId() != null && apsalarId == null) {
      apsalarId = user.getApsalarId();
    }
    if (!user.updateAbsoluteApsalaridLastloginBadgesNumConsecutiveDaysLoggedIn(apsalarId, loginTime, 0,
        newNumConsecutiveDaysLoggedIn)) {
      log.error("problem with updating apsalar id to " + apsalarId + ", last login to " + loginTime
          + ", and badge count to 0 for " + user + " and newNumConsecutiveDaysLoggedIn is "
          + newNumConsecutiveDaysLoggedIn);
    }
    if (!InsertUtils.get().insertLastLoginLastLogoutToUserSessions(user.getId(), loginTime, null)) {
      log.error("problem with inserting last login time for user " + user + ", loginTime=" + loginTime);
    }

    if (user.getNumBadges() != 0) {
      if (user.getDeviceToken() != null) {
        /*
         * handled locally?
         */
        // ApnsServiceBuilder builder =
        // APNS.newService().withCert(APNSProperties.PATH_TO_CERT,
        // APNSProperties.CERT_PASSWORD);
        // if (Globals.IS_SANDBOX()) {
        // builder.withSandboxDestination();
        // }
        // ApnsService service = builder.build();
        // service.push(newDeviceToken,
        // APNS.newPayload().badge(0).build());
        // service.stop();
      }
    }
  }

  private void setNotifications(Builder resBuilder, User user) {
    List<Integer> userIds = new ArrayList<Integer>();

    List<MarketplaceTransaction> marketplaceTransactions = MarketplaceTransactionRetrieveUtils
        .getMostRecentMarketplaceTransactionsForPoster(user.getId(),
            ControllerConstants.STARTUP__MAX_NUM_OF_STARTUP_NOTIFICATION_TYPE_TO_SEND);
    if (marketplaceTransactions != null && marketplaceTransactions.size() > 0) {
      for (MarketplaceTransaction mt : marketplaceTransactions) {
        userIds.add(mt.getBuyerId());
      }
    }

    Timestamp earliestBattleNotificationTimeToRetrieve = new Timestamp(new Date().getTime()
        - ControllerConstants.STARTUP__HOURS_OF_BATTLE_NOTIFICATIONS_TO_SEND * 3600000);
    List<BattleDetails> battleDetails = BattleDetailsRetrieveUtils
        .getMostRecentBattleDetailsForDefenderAfterTime(user.getId(),
            ControllerConstants.STARTUP__MAX_NUM_OF_STARTUP_NOTIFICATION_TYPE_TO_SEND,
            earliestBattleNotificationTimeToRetrieve);
    if (battleDetails != null && battleDetails.size() > 0) {
      for (BattleDetails bd : battleDetails) {
        userIds.add(bd.getAttackerId());
      }
    }

    List<PlayerWallPost> wallPosts = PlayerWallPostRetrieveUtils
        .getMostRecentPlayerWallPostsForWallOwner(
            ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP, user.getId());
    if (wallPosts != null && wallPosts.size() > 0) {
      for (PlayerWallPost p : wallPosts) {
        userIds.add(p.getPosterId());
      }
    }

    List<ClanChatPost> clanChatPosts = null;
    if (user.getClanId() > 0) {
      clanChatPosts = ClanChatPostRetrieveUtils.getMostRecentClanChatPostsForClan(
          ControllerConstants.RETRIEVE_PLAYER_WALL_POSTS__NUM_POSTS_CAP, user.getClanId());
      for (ClanChatPost p : clanChatPosts) {
        userIds.add(p.getPosterId());
      }
    }

    Map<Integer, User> usersByIds = null;
    if (userIds.size() > 0) {
      usersByIds = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIds);
    }

    if (marketplaceTransactions != null && marketplaceTransactions.size() > 0) {
      for (MarketplaceTransaction mt : marketplaceTransactions) {
        resBuilder.addMarketplacePurchaseNotifications(CreateInfoProtoUtils
            .createMarketplacePostPurchasedNotificationProtoFromMarketplaceTransaction(mt,
                usersByIds.get(mt.getBuyerId()), user));
      }
    }
    if (battleDetails != null && battleDetails.size() > 0) {
      for (BattleDetails bd : battleDetails) {
        resBuilder.addAttackNotifications(CreateInfoProtoUtils
            .createAttackedNotificationProtoFromBattleHistory(bd,
                usersByIds.get(bd.getAttackerId())));
      }
    }
    if (wallPosts != null && wallPosts.size() > 0) {
      for (PlayerWallPost p : wallPosts) {
        resBuilder.addPlayerWallPostNotifications(CreateInfoProtoUtils
            .createPlayerWallPostProtoFromPlayerWallPost(p, usersByIds.get(p.getPosterId())));
      }
    }
    if (clanChatPosts != null && clanChatPosts.size() > 0) {

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

    boolean realPlayersOnly = false;
    boolean fakePlayersOnly = false;
    boolean offlinePlayersOnly = false;
    boolean prestigePlayersOrBotsOnly = false;
    List<Integer> forbiddenUserIds = new ArrayList<Integer>();
    forbiddenUserIds.add(user.getId());

    List<User> allies = RetrieveUtils.userRetrieveUtils().getUsers(userTypes,
        ControllerConstants.STARTUP__APPROX_NUM_ALLIES_TO_SEND, user.getLevel(),
        user.getId(), false, null, null, null, null, false, realPlayersOnly,
        fakePlayersOnly, offlinePlayersOnly, prestigePlayersOrBotsOnly, forbiddenUserIds);
    if (allies != null && allies.size() > 0) {
      for (User ally : allies) {
        resBuilder.addAllies(CreateInfoProtoUtils.createMinimumUserProtoWithLevelFromUser(ally));
      }
    }
  }

  private void setUserEquipsAndEquips(Builder resBuilder, User user) {
    List<UserEquip> userEquips = RetrieveUtils.userEquipRetrieveUtils()
        .getUserEquipsForUser(user.getId());
    if (userEquips != null) {
      Map<Integer, Equipment> equipIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
      Set<Integer> equipsGivenAlready = new HashSet<Integer>();
      for (UserEquip ue : userEquips) {
        resBuilder.addUserEquips(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(ue));
        if (!equipsGivenAlready.contains(ue.getEquipId())) {
          resBuilder.addEquips(CreateInfoProtoUtils
              .createFullEquipProtoFromEquip(equipIdsToEquipment.get(ue.getEquipId())));
          equipsGivenAlready.add(ue.getEquipId());
        }
      }
    }
  }

  private void setInProgressAndAvailableQuests(Builder resBuilder, User user) {
    List<UserQuest> inProgressAndRedeemedUserQuests = RetrieveUtils.userQuestRetrieveUtils()
        .getUnredeemedAndRedeemedUserQuestsForUser(user.getId());
    List<Integer> inProgressQuestIds = new ArrayList<Integer>();
    List<Integer> redeemedQuestIds = new ArrayList<Integer>();

    Map<Integer, Quest> questIdToQuests = QuestRetrieveUtils.getQuestIdsToQuests();
    for (UserQuest uq : inProgressAndRedeemedUserQuests) {
      if (uq.isRedeemed()) {
        redeemedQuestIds.add(uq.getQuestId());
      } else {
        Quest quest = QuestRetrieveUtils.getQuestForQuestId(uq.getQuestId());

        if (quest.getDefeatBadGuysJobsRequired() == null
            && quest.getDefeatGoodGuysJobsRequired() == null && !uq.isDefeatTypeJobsComplete()) {
          if (!UpdateUtils.get().updateUserQuestsSetCompleted(user.getId(), quest.getId(), false,
              true)) {
            log.error("problem with updating user quest data by marking defeat type jobs completed for user quest "
                + uq);
          }
        }
        if (quest.getTasksRequired() == null && !uq.isTasksComplete()) {
          if (!UpdateUtils.get().updateUserQuestsSetCompleted(user.getId(), quest.getId(), true,
              false)) {
            log.error("problem with updating user quest data by marking tasks completed for user quest "
                + uq);
          }
        }

        inProgressQuestIds.add(uq.getQuestId());
        if (uq.isComplete()) {
          resBuilder.addInProgressCompleteQuests(CreateInfoProtoUtils
              .createFullQuestProtoFromQuest(user.getType(),
                  questIdToQuests.get(uq.getQuestId())));
        } else {
          resBuilder.addInProgressIncompleteQuests(CreateInfoProtoUtils
              .createFullQuestProtoFromQuest(user.getType(),
                  questIdToQuests.get(uq.getQuestId())));
        }
      }
    }

    List<Integer> availableQuestIds = QuestUtils.getAvailableQuestsForUser(redeemedQuestIds,
        inProgressQuestIds);
    if (availableQuestIds != null) {
      for (Integer questId : availableQuestIds) {
        resBuilder.addAvailableQuests(CreateInfoProtoUtils.createFullQuestProtoFromQuest(
            user.getType(), questIdToQuests.get(questId)));
      }
    }
  }

  private void setCitiesAndUserCityInfos(Builder resBuilder, User user) {
    Map<Integer, Integer> cityIdsToUserCityRanks = RetrieveUtils.userCityRetrieveUtils()
        .getCityIdToUserCityRank(user.getId());
    Map<Integer, Integer> taskIdToNumTimesActedInRank = UserTaskRetrieveUtils
        .getTaskIdToNumTimesActedInRankForUser(user.getId());

    Map<Integer, City> cities = CityRetrieveUtils.getCityIdsToCities();
    for (Integer cityId : cities.keySet()) {
      City city = cities.get(cityId);
      resBuilder.addAllCities(CreateInfoProtoUtils.createFullCityProtoFromCity(city));
      if (user.getLevel() >= city.getMinLevel()) {

        if (!cityIdsToUserCityRanks.containsKey(city.getId())) {
          if (!UpdateUtils.get().incrementCityRankForUserCity(user.getId(), cityId, 1)) {
            log.error("problem with unlocking city for user, city Id is " + cityId
                + ", and user is " + user);
          } else {
            cityIdsToUserCityRanks = RetrieveUtils.userCityRetrieveUtils()
                .getCityIdToUserCityRank(user.getId());
          }
        }
        int numTasksComplete = getNumTasksCompleteForUserCity(user, city, taskIdToNumTimesActedInRank);
        resBuilder.addUserCityInfos(CreateInfoProtoUtils.createFullUserCityProto(user.getId(),
            city.getId(), cityIdsToUserCityRanks.get(city.getId()), numTasksComplete));
      }
    }
  }

  private int getNumTasksCompleteForUserCity(User user, City city,
      Map<Integer, Integer> taskIdToNumTimesActedInRank) {
    List<Task> tasks = TaskRetrieveUtils.getAllTasksForCityId(city.getId());
    int numCompletedTasks = 0;
    if (tasks != null) {
      for (Task t : tasks) {
        if (taskIdToNumTimesActedInRank.containsKey(t.getId())
            && taskIdToNumTimesActedInRank.get(t.getId()) >= t.getNumForCompletion()) {
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

    task = TaskRetrieveUtils.getTaskForTaskId(ControllerConstants.TUTORIAL__FAKE_QUEST_TASK_ID);
    task.setPotentialLootEquipIds(new ArrayList<Integer>());
    FullTaskProto questFtpGood = CreateInfoProtoUtils.createFullTaskProtoFromTask(aGoodType, task);
    FullTaskProto questFtpBad = CreateInfoProtoUtils.createFullTaskProtoFromTask(aBadType, task);

    Dialogue goodAcceptDialogue = MiscMethods
        .createDialogue(ControllerConstants.TUTORIAL__FAKE_QUEST_GOOD_ACCEPT_DIALOGUE);
    Dialogue badAcceptDialogue = MiscMethods
        .createDialogue(ControllerConstants.TUTORIAL__FAKE_QUEST_BAD_ACCEPT_DIALOGUE);

    FullTutorialQuestProto tqbp = FullTutorialQuestProto
        .newBuilder()
        .setGoodName(ControllerConstants.TUTORIAL__FAKE_QUEST_GOOD_NAME)
        .setBadName(ControllerConstants.TUTORIAL__FAKE_QUEST_BAD_NAME)
        .setGoodDescription(ControllerConstants.TUTORIAL__FAKE_QUEST_GOOD_DESCRIPTION)
        .setBadDescription(ControllerConstants.TUTORIAL__FAKE_QUEST_BAD_DESCRIPTION)
        .setGoodDoneResponse(ControllerConstants.TUTORIAL__FAKE_QUEST_GOOD_DONE_RESPONSE)
        .setBadDoneResponse(ControllerConstants.TUTORIAL__FAKE_QUEST_BAD_DONE_RESPONSE)
        .setGoodAcceptDialogue(
            CreateInfoProtoUtils.createDialogueProtoFromDialogue(goodAcceptDialogue))
            .setBadAcceptDialogue(CreateInfoProtoUtils.createDialogueProtoFromDialogue(badAcceptDialogue))
            .setAssetNumWithinCity(ControllerConstants.TUTORIAL__FAKE_QUEST_ASSET_NUM_WITHIN_CITY)
            .setCoinsGained(ControllerConstants.TUTORIAL__FAKE_QUEST_COINS_GAINED)
            .setExpGained(ControllerConstants.TUTORIAL__FAKE_QUEST_EXP_GAINED)
            .setTaskGood(questFtpGood)
            .setTaskBad(questFtpBad)
            .setTaskCompleteCoinGain(MiscMethods.calculateCoinsGainedFromTutorialTask(task))
            .setEquipReward(
                CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment
                    .get(ControllerConstants.TUTORIAL__FAKE_QUEST_AMULET_LOOT_EQUIP_ID))).build();

    PlayerWallPost pwp = new PlayerWallPost(-1,
        ControllerConstants.USER_CREATE__ID_OF_POSTER_OF_FIRST_WALL, -1, new Date(),
        ControllerConstants.USER_CREATE__FIRST_WALL_POST_TEXT);
    User poster = RetrieveUtils.userRetrieveUtils().getUserById(
        ControllerConstants.USER_CREATE__ID_OF_POSTER_OF_FIRST_WALL);

    String name = "";
      int syllablesInName = (Math.random() < .5) ? syllablesInName1 : syllablesInName2;
      name = nameGeneratorElven.compose(syllablesInName);
    

    TutorialConstants.Builder builder = TutorialConstants
        .newBuilder()
        .setInitEnergy(ControllerConstants.TUTORIAL__INIT_ENERGY)
        .setInitStamina(ControllerConstants.TUTORIAL__INIT_STAMINA)
        .setInitHealth(ControllerConstants.TUTORIAL__INIT_HEALTH)
        .setStructToBuild(ControllerConstants.TUTORIAL__FIRST_STRUCT_TO_BUILD)
        .setDiamondCostToInstabuildFirstStruct(
            ControllerConstants.TUTORIAL__DIAMOND_COST_TO_INSTABUILD_FIRST_STRUCT)
            .setArcherInitAttack(ControllerConstants.TUTORIAL__ARCHER_INIT_ATTACK)
            .setArcherInitDefense(ControllerConstants.TUTORIAL__ARCHER_INIT_DEFENSE)
            .setArcherInitWeapon(
                CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment
                    .get(ControllerConstants.TUTORIAL__ARCHER_INIT_WEAPON_ID)))
                    .setArcherInitArmor(
                        CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment
                            .get(ControllerConstants.TUTORIAL__ARCHER_INIT_ARMOR_ID)))
                            .setMageInitAttack(ControllerConstants.TUTORIAL__MAGE_INIT_ATTACK)
                            .setMageInitDefense(ControllerConstants.TUTORIAL__MAGE_INIT_DEFENSE)
                            .setMageInitWeapon(
                                CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment
                                    .get(ControllerConstants.TUTORIAL__MAGE_INIT_WEAPON_ID)))
                                    .setMageInitArmor(
                                        CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment
                                            .get(ControllerConstants.TUTORIAL__MAGE_INIT_ARMOR_ID)))
                                            .setWarriorInitAttack(ControllerConstants.TUTORIAL__WARRIOR_INIT_ATTACK)
                                            .setWarriorInitDefense(ControllerConstants.TUTORIAL__WARRIOR_INIT_DEFENSE)
                                            .setWarriorInitWeapon(
                                                CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment
                                                    .get(ControllerConstants.TUTORIAL__WARRIOR_INIT_WEAPON_ID)))
                                                    .setWarriorInitArmor(
                                                        CreateInfoProtoUtils.createFullEquipProtoFromEquip(equipmentIdsToEquipment
                                                            .get(ControllerConstants.TUTORIAL__WARRIOR_INIT_ARMOR_ID)))
                                                            .setTutorialQuest(tqbp)
                                                            .setMinNameLength(ControllerConstants.USER_CREATE__MIN_NAME_LENGTH)
                                                            .setTutorialQuest(tqbp)
                                                            .setMaxNameLength(ControllerConstants.USER_CREATE__MAX_NAME_LENGTH)
                                                            .setCoinRewardForBeingReferred(
                                                                ControllerConstants.USER_CREATE__COIN_REWARD_FOR_BEING_REFERRED)
                                                                .setInitDiamonds(ControllerConstants.TUTORIAL__INIT_DIAMONDS)
                                                                .setInitCoins(ControllerConstants.TUTORIAL__INIT_COINS)
                                                                .setFirstBattleCoinGain(ControllerConstants.TUTORIAL__FIRST_BATTLE_COIN_GAIN)
                                                                .setFirstBattleExpGain(ControllerConstants.TUTORIAL__FIRST_BATTLE_EXP_GAIN)
                                                                .setFirstTaskGood(ftpGood)
                                                                .setFirstTaskBad(ftpBad)
                                                                .setExpRequiredForLevelTwo(
                                                                    LevelsRequiredExperienceRetrieveUtils.getLevelsToRequiredExperienceForLevels().get(2))
                                                                    .setExpRequiredForLevelThree(
                                                                        LevelsRequiredExperienceRetrieveUtils.getLevelsToRequiredExperienceForLevels().get(3))
                                                                        .setFirstWallPost(
                                                                            CreateInfoProtoUtils.createPlayerWallPostProtoFromPlayerWallPost(pwp, poster))
                                                                            .setDefaultName(name);

    List<NeutralCityElement> neutralCityElements = NeutralCityElementsRetrieveUtils
        .getNeutralCityElementsForCity(ControllerConstants.TUTORIAL__FIRST_NEUTRAL_CITY_ID);
    if (neutralCityElements != null) {
      for (NeutralCityElement nce : neutralCityElements) {
        builder.addFirstCityElementsForGood(CreateInfoProtoUtils
            .createNeutralCityElementProtoFromNeutralCityElement(nce, aGoodType));
        builder.addFirstCityElementsForBad(CreateInfoProtoUtils
            .createNeutralCityElementProtoFromNeutralCityElement(nce, aBadType));
      }
    }

    Map<Integer, Structure> structIdsToStructs = StructureRetrieveUtils.getStructIdsToStructs();
    for (Structure struct : structIdsToStructs.values()) {
      if (struct != null) {
        FullStructureProto fsp = CreateInfoProtoUtils.createFullStructureProtoFromStructure(struct);
        builder.addCarpenterStructs(fsp);
        if (struct.getMinLevel() == 2) {
          builder.addNewlyAvailableStructsAfterLevelup(CreateInfoProtoUtils
              .createFullStructureProtoFromStructure(struct));
        }
      }
    }

    List<City> availCities = MiscMethods
        .getCitiesAvailableForUserLevel(ControllerConstants.USER_CREATE__START_LEVEL);
    for (City city : availCities) {
      if (city.getMinLevel() == ControllerConstants.USER_CREATE__START_LEVEL) {
        builder.addCitiesNewlyAvailableToUserAfterLevelup(CreateInfoProtoUtils
            .createFullCityProtoFromCity(city));
      }
    }

    Map<Integer, Equipment> equipIdToEquips = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
    if (equipIdToEquips != null) {
      for (Equipment e : equipIdToEquips.values()) {
        if (e != null && e.getMinLevel() == ControllerConstants.USER_CREATE__START_LEVEL
            && (e.getRarity() == Rarity.EPIC || e.getRarity() == Rarity.LEGENDARY)) {
          builder.addNewlyEquippableEpicsAndLegendariesForAllClassesAfterLevelup(CreateInfoProtoUtils
              .createFullEquipProtoFromEquip(e));
        }
      }
    }
    resBuilder.setTutorialConstants(builder.build());
  }

  public void writeToUserCurrencyHistory(User aUser, String goldSilver, int previousMoney,
      Map<String, Integer> goldSilverChange) {
    String silver = MiscMethods.silver;
    String gold = MiscMethods.gold;

    Timestamp date = new Timestamp((new Date()).getTime());
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String reasonForChange = ControllerConstants.UCHRFC__STARTUP_DAILY_BONUS;

    if (goldSilver.equals(silver)) {
      previousGoldSilver.put(silver, previousMoney);
      reasonsForChanges.put(silver, reasonForChange);
    } else {
      previousGoldSilver.put(gold, previousMoney);
      reasonsForChanges.put(gold, reasonForChange);
    }

    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, goldSilverChange,
        previousGoldSilver, reasonsForChanges);
  }

}
