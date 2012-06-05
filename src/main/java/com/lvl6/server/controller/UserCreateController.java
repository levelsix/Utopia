package com.lvl6.server.controller;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.UserCreateRequestEvent;
import com.lvl6.events.response.ReferralCodeUsedResponseEvent;
import com.lvl6.events.response.UserCreateResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Location;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.Globals;
import com.lvl6.proto.EventProto.ReferralCodeUsedResponseProto;
import com.lvl6.proto.EventProto.UserCreateRequestProto;
import com.lvl6.proto.EventProto.UserCreateResponseProto;
import com.lvl6.proto.EventProto.UserCreateResponseProto.Builder;
import com.lvl6.proto.EventProto.UserCreateResponseProto.UserCreateStatus;
import com.lvl6.proto.InfoProto.FullEquipProto.ClassType;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.LocationProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.AvailableReferralCodeRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.spring.AppContext;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.NIOUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class UserCreateController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  protected InsertUtil insertUtils;
  
  public UserCreateController() {
    numAllocatedThreads = 3;
    insertUtils = (InsertUtils) AppContext.getApplicationContext().getBean("insertUtils");
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new UserCreateRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_USER_CREATE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    UserCreateRequestProto reqProto = ((UserCreateRequestEvent)event).getUserCreateRequestProto();
    String udid = reqProto.getUdid();
    String name = reqProto.getName();
    UserType type = reqProto.getType();

    LocationProto locationProto = (reqProto.hasUserLocation()) ? reqProto.getUserLocation() : null;
    String referrerCode = (reqProto.hasReferrerCode()) ? reqProto.getReferrerCode() : null;
    String deviceToken = (reqProto.hasDeviceToken()) ? reqProto.getDeviceToken() : null;

    Timestamp timeOfStructPurchase = new Timestamp(reqProto.getTimeOfStructPurchase());
    Timestamp timeOfStructBuild = new Timestamp(reqProto.getTimeOfStructBuild());
    CoordinatePair structCoords = new CoordinatePair(reqProto.getStructCoords().getX(), reqProto.getStructCoords().getY());
    
    int attack = reqProto.getAttack();
    int defense = reqProto.getDefense();
    int energy = reqProto.getEnergy();
    int health = reqProto.getHealth();
    int stamina = reqProto.getStamina();
    
    boolean usedDiamondsToBuild = reqProto.getUsedDiamondsToBuilt();
    

    UserCreateResponseProto.Builder resBuilder = UserCreateResponseProto.newBuilder();

    Location loc = (locationProto == null) ? MiscMethods.getRandomValidLocation() : new Location(locationProto.getLatitude(), locationProto.getLongitude());

    User referrer = (referrerCode != null && referrerCode.length() > 0) ? UserRetrieveUtils.getUserByReferralCode(referrerCode) : null;;

    boolean legitUserCreate = checkLegitUserCreate(resBuilder, udid, name, 
        loc, type, attack, defense, energy, health, stamina, timeOfStructPurchase, timeOfStructBuild, structCoords, 
        referrer, reqProto.hasReferrerCode());

    User user = null;
    int userId = ControllerConstants.NOT_SET;
    List<Integer> equipIds = new ArrayList<Integer>();
    Task taskCompleted = null;
    
    if (legitUserCreate) {
      String newReferCode = grabNewReferCode();

      taskCompleted = TaskRetrieveUtils.getTaskForTaskId(ControllerConstants.TUTORIAL__FIRST_TASK_ID);

      int playerExp = taskCompleted.getExpGained() * taskCompleted.getNumForCompletion() + ControllerConstants.TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_EXP_GAIN + ControllerConstants.TUTORIAL__FAKE_QUEST_EXP_GAINED;
      int playerCoins = ControllerConstants.TUTORIAL__INIT_COINS + MiscMethods.calculateCoinsGainedFromTutorialTask(taskCompleted) + ControllerConstants.TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_COIN_GAIN + ControllerConstants.TUTORIAL__FAKE_QUEST_COINS_GAINED
          - EquipmentRetrieveUtils.getEquipmentIdsToEquipment()
          .get(ControllerConstants.TUTORIAL__FIRST_STRUCT_TO_BUILD).getCoinPrice(); 
      if (referrer != null) playerCoins += ControllerConstants.USER_CREATE__COIN_REWARD_FOR_BEING_REFERRED;

      int playerDiamonds = ControllerConstants.TUTORIAL__INIT_DIAMONDS;
      if (usedDiamondsToBuild) playerDiamonds -= ControllerConstants.TUTORIAL__DIAMOND_COST_TO_INSTABUILD_FIRST_STRUCT;
      
      Integer amuletEquipped = ControllerConstants.TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_AMULET_LOOT_EQUIP_ID;
      Integer weaponEquipped = null, armorEquipped = null;
      if (type == UserType.GOOD_ARCHER || type == UserType.BAD_ARCHER) {
        weaponEquipped = ControllerConstants.TUTORIAL__ARCHER_INIT_WEAPON_ID;
        armorEquipped = ControllerConstants.TUTORIAL__ARCHER_INIT_ARMOR_ID;
      }
      if (type == UserType.GOOD_WARRIOR || type == UserType.BAD_WARRIOR) {
        weaponEquipped = ControllerConstants.TUTORIAL__WARRIOR_INIT_WEAPON_ID;
        armorEquipped = ControllerConstants.TUTORIAL__WARRIOR_INIT_ARMOR_ID;
      }
      if (type == UserType.GOOD_MAGE || type == UserType.BAD_MAGE) {
        weaponEquipped = ControllerConstants.TUTORIAL__MAGE_INIT_WEAPON_ID;
        armorEquipped = ControllerConstants.TUTORIAL__MAGE_INIT_ARMOR_ID;
      }

      if (weaponEquipped > 0) equipIds.add(weaponEquipped);
      if (armorEquipped > 0) equipIds.add(armorEquipped);
      if (amuletEquipped > 0) equipIds.add(amuletEquipped);

      userId = insertUtils.insertUser(udid, name, type, loc, deviceToken, newReferCode, ControllerConstants.USER_CREATE__START_LEVEL, 
          attack, defense, energy, health, stamina, playerExp, playerCoins, playerDiamonds, 
          weaponEquipped, armorEquipped, amuletEquipped, false);
      if (userId > 0) {
        server.lockPlayer(userId);
        try {
          user = UserRetrieveUtils.getUserById(userId);
          FullUserProto userProto = CreateInfoProtoUtils.createFullUserProtoFromUser(user);
          resBuilder.setSender(userProto);
        } catch (Exception e) {
          log.error("exception in UserCreateController processEvent", e);
        } finally {
          server.unlockPlayer(userId); 
        }
      } else {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        log.error("problem with trying to create user. udid=" + udid + ", name=" + name + ", type=" + type
            + ", loc=" + loc + ", deviceToken=" + deviceToken + ", newReferCode=" + newReferCode + ", attack="
            + attack + ", defense=" + defense + ", energy=" + energy + ", health=" + health + ", stamina=" + stamina
            + ", playerExp=" + playerExp + ", playerCoins=" + playerCoins + ", playerDiamonds=" + playerDiamonds
            + ", weaponEquipped=" + weaponEquipped + ", armorEquipped=" + armorEquipped + ", amuletEquipped=" 
            + amuletEquipped); 
      }
    }

    UserCreateResponseProto resProto = resBuilder.build();
    UserCreateResponseEvent resEvent = new UserCreateResponseEvent(udid);
    resEvent.setTag(event.getTag());
    resEvent.setUserCreateResponseProto(resProto);

    log.info("Writing event: " + resEvent);
    // Write event directly since EventWriter cannot handle without userId.
    ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);
    NIOUtils.prepBuffer(resEvent, writeBuffer);

    SocketChannel sc = server.removePreDbPlayer(udid);

    if (user != null) {
      ConnectedPlayer p = new ConnectedPlayer();
      p.setPlayerId(user.getId());
      p.setChannel(sc);
      server.addPlayer(p);
      log.debug("delegate event, new player created and channel set, player:" + 
          p.getPlayerId() + ", channel: " + sc);
    }

    if (user != null) 
      NIOUtils.channelWrite(sc, writeBuffer, user.getId());
    else
      NIOUtils.channelWrite(sc, writeBuffer, ControllerConstants.NOT_SET);

    if (legitUserCreate && userId > 0) {
      server.lockPlayer(userId);
      try {
        writeFirstWallPost(userId);
        writeUserStruct(userId, ControllerConstants.TUTORIAL__FIRST_STRUCT_TO_BUILD, timeOfStructPurchase, timeOfStructBuild, structCoords);
//        writeUserCritstructs(user.getId());
        writeUserEquips(user.getId(), equipIds);
        writeTaskCompleted(user.getId(), taskCompleted);
        if (!UpdateUtils.incrementCityRankForUserCity(user.getId(), 1, 1)) {
          log.error("problem with giving user access to first city (city with id 1)");
        }
        if (referrer != null && user != null) {
          rewardReferrer(referrer, user);        
        }
      } catch (Exception e) {
        log.error("exception in UserCreateController processEvent", e);
      } finally {
        server.unlockPlayer(userId); 
      }
    }    
  }

  private void writeFirstWallPost(int newPlayerId) {
    Timestamp timeOfPost = new Timestamp(new Date().getTime());
    if (insertUtils.insertPlayerWallPost(ControllerConstants.USER_CREATE__ID_OF_POSTER_OF_FIRST_WALL, 
        newPlayerId, ControllerConstants.USER_CREATE__FIRST_WALL_POST_TEXT, timeOfPost) < 0) {
        log.error("problem with writing wall post from user " + ControllerConstants.USER_CREATE__ID_OF_POSTER_OF_FIRST_WALL
            + " for player " + newPlayerId + " at " + timeOfPost);
    }
  }

  private void writeUserStruct(int userId, int structId, Timestamp timeOfStructPurchase, Timestamp timeOfStructBuild, CoordinatePair structCoords) {
    if (!insertUtils.insertUserStructJustBuilt(userId, structId, timeOfStructPurchase, timeOfStructBuild, structCoords)) {
      log.error("problem in giving user the user struct with these properties: userId="
          + userId + ", structId=" + structId + ", timeOfStructPurchase=" + timeOfStructPurchase
          + ", timeOfStructBuild=" + timeOfStructBuild + ", structCoords");
    }
  }

  private void writeTaskCompleted(int userId, Task taskCompleted) {
    if (taskCompleted != null) {
      if (!UpdateUtils.incrementTimesCompletedInRankForUserTask(userId, taskCompleted.getId(), taskCompleted.getNumForCompletion())) {
        log.error("problem with incrementing number of times user completed task in current rank for task " + taskCompleted.getId()
            + " for player " + userId + " by " + taskCompleted.getNumForCompletion());
      }
    }
  }

  private void writeUserEquips(int userId, List<Integer> equipIds) {
    if (equipIds.size() > 0) {
      if (!insertUtils.insertUserEquips(userId, equipIds, 1)) {
        log.error("problem with giving user initial 1 of each user equips for user " + userId + ", equipIds are " + equipIds);
      }
    }
  }
//
//  private void writeUserCritstructs(int userId) {
//    if (!insertUtils.insertAviaryAndCarpenterCoords(userId, ControllerConstants.AVIARY_COORDS, ControllerConstants.CARPENTER_COORDS)) {
//      log.error("problem with giving user his critical structs");
//    }
//  }

  private String grabNewReferCode() {
    String newReferCode = AvailableReferralCodeRetrieveUtils.getAvailableReferralCode();
    if (newReferCode != null && newReferCode.length() > 0) {
      while (!DeleteUtils.deleteAvailableReferralCode(newReferCode)) {
        newReferCode = AvailableReferralCodeRetrieveUtils.getAvailableReferralCode();
      }
    } else {
      log.fatal("no refer codes left");
    }
    return newReferCode;
  }

  private void rewardReferrer(User referrer, User user) {
    if (!referrer.isFake()) {
      server.lockPlayer(referrer.getId());
      try {
        int coinsGivenToReferrer = MiscMethods.calculateCoinsGivenToReferrer(referrer);
        if (!referrer.updateRelativeCoinsNumreferrals(coinsGivenToReferrer, 1)) {
          log.error("problem with rewarding the referrer " + referrer + " with this many coins: " + coinsGivenToReferrer);
        } else {
          if (!insertUtils.insertReferral(referrer.getId(), user.getId(), coinsGivenToReferrer)) {
            log.error("problem with inserting referral into db. referrer is " + referrer.getId() + ", user=" + user.getId()
                + ", coins given to referrer=" + coinsGivenToReferrer);
          }
          ReferralCodeUsedResponseEvent resEvent = new ReferralCodeUsedResponseEvent(referrer.getId());
          ReferralCodeUsedResponseProto resProto = ReferralCodeUsedResponseProto.newBuilder()
              .setSender(CreateInfoProtoUtils.createMinimumUserProtoFromUser(referrer))
              .setReferredPlayer(CreateInfoProtoUtils.createMinimumUserProtoFromUser(user))
              .setCoinsGivenToReferrer(coinsGivenToReferrer).build();
          resEvent.setReferralCodeUsedResponseProto(resProto);
          server.writeAPNSNotificationOrEvent(resEvent);
        }
      } catch (Exception e) {
        log.error("exception in UserCreateController processEvent", e);
      } finally {
        server.unlockPlayer(referrer.getId()); 
      }
    }
  }

  private boolean checkLegitUserCreate(Builder resBuilder, String udid,
      String name, Location loc, UserType type, int attack, int defense, int energy, int health, int stamina, 
      Timestamp timeOfStructPurchase, Timestamp timeOfStructBuild, CoordinatePair coordinatePair, User referrer, 
      boolean hasReferrerCode) {
    
    if (udid == null || name == null || timeOfStructPurchase == null || coordinatePair == null || type == null || timeOfStructBuild == null) {
      resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
      log.error("parameter passed in is null. udid=" + udid + ", name=" + name + ", timeOfStructPurchase=" + timeOfStructPurchase
          + ", coordinatePair=" + coordinatePair + ", type=" + type + ", timeOfStructBuild=" + timeOfStructBuild);
      return false;
    }
    if (hasReferrerCode && referrer == null) {
      resBuilder.setStatus(UserCreateStatus.INVALID_REFER_CODE);
      log.info("refer code passed in is invalid.");
      return false;
    }
    int sumStat = attack + defense + energy + health + stamina;
    int correctBaseSumStat = calculateCorrectSumStat(MiscMethods.getClassTypeFromUserType(type));
    if (sumStat < correctBaseSumStat || sumStat > correctBaseSumStat + ControllerConstants.LEVEL_UP__SKILL_POINTS_GAINED*ControllerConstants.USE_SKILL_POINT__MAX_STAT_GAIN) {
      resBuilder.setStatus(UserCreateStatus.INVALID_SKILL_POINT_ALLOCATION);
      log.error("invalid skill point allocation. sum stat range should be between " + correctBaseSumStat
          + " and " + (correctBaseSumStat + ControllerConstants.LEVEL_UP__SKILL_POINTS_GAINED*ControllerConstants.USE_SKILL_POINT__MAX_STAT_GAIN)
          + ", but it's at " + sumStat + ". attack=" + attack + ", defense=" + defense + ", energy=" + energy
          + ", health=" + health + ", stamina=" + stamina + ", type=" + type);
      return false;
    }
    if (UserRetrieveUtils.getUserByUDID(udid) != null) {
      resBuilder.setStatus(UserCreateStatus.USER_WITH_UDID_ALREADY_EXISTS);
      log.error("user with udid " + udid + " already exists");
      return false;
    }
    if (timeOfStructBuild.getTime() <= timeOfStructPurchase.getTime() || 
        timeOfStructBuild.getTime() > new Date().getTime() + Globals.NUM_MINUTES_DIFFERENCE_LEEWAY_FOR_CLIENT_TIME*60000 ||
        timeOfStructPurchase.getTime() > new Date().getTime() + Globals.NUM_MINUTES_DIFFERENCE_LEEWAY_FOR_CLIENT_TIME*60000) {
      resBuilder.setStatus(UserCreateStatus.TIME_ISSUE);
      log.error("time issue. time now is " + new Date() + ". timeOfStructBuild=" + timeOfStructBuild 
          + ", timeOfStructPurchase=" + timeOfStructPurchase);
      return false;
    }
    if (loc.getLatitude() < ControllerConstants.LATITUDE_MIN || loc.getLatitude() > ControllerConstants.LATITUDE_MAX || 
        loc.getLongitude() < ControllerConstants.LONGITUDE_MIN || loc.getLongitude() > ControllerConstants.LONGITUDE_MAX) {
      resBuilder.setStatus(UserCreateStatus.INVALID_LOCATION);
      log.error("location is out of bounds. location=" + loc + ". latitude is between " + ControllerConstants.LATITUDE_MIN
          + " and " + ControllerConstants.LATITUDE_MAX + ". longitude is between " + ControllerConstants.LONGITUDE_MIN
          + " and " + ControllerConstants.LONGITUDE_MAX);
      return false;
    }
    if (name.length() < ControllerConstants.USER_CREATE__MIN_NAME_LENGTH || 
        name.length() > ControllerConstants.USER_CREATE__MAX_NAME_LENGTH) {
      resBuilder.setStatus(UserCreateStatus.INVALID_NAME);
      log.error("name length is off. length is " + name.length() + ", should be in between " + ControllerConstants.USER_CREATE__MIN_NAME_LENGTH
          + " and " + ControllerConstants.USER_CREATE__MAX_NAME_LENGTH);
      return false;
    }
    if (type == UserType.GOOD_ARCHER || type == UserType.BAD_ARCHER) {
      if (attack < ControllerConstants.TUTORIAL__ARCHER_INIT_ATTACK) {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        log.error("archer attack too low. attack is " + attack + ", init attack is " + ControllerConstants.TUTORIAL__ARCHER_INIT_ATTACK);
        return false;
      }
      if (defense < ControllerConstants.TUTORIAL__ARCHER_INIT_DEFENSE) {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        log.error("archer defense too low. defense is " + defense + ", init defense is " + ControllerConstants.TUTORIAL__ARCHER_INIT_DEFENSE);
        return false;
      }
    } else if (type == UserType.GOOD_WARRIOR || type == UserType.BAD_WARRIOR) {
      if (attack < ControllerConstants.TUTORIAL__WARRIOR_INIT_ATTACK) {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        log.error("warrior attack too low. attack is " + attack + ", init attack is " + ControllerConstants.TUTORIAL__WARRIOR_INIT_ATTACK);
        return false;
      }
      if (defense < ControllerConstants.TUTORIAL__WARRIOR_INIT_DEFENSE) {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        log.error("warrior defense too low. defense is " + defense + ", init defense is " + ControllerConstants.TUTORIAL__WARRIOR_INIT_DEFENSE);
        return false;
      }      
    } else if (type == UserType.GOOD_MAGE || type == UserType.BAD_MAGE) {
      if (attack < ControllerConstants.TUTORIAL__MAGE_INIT_ATTACK) {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        log.error("mage attack too low. attack is " + attack + ", init attack is " + ControllerConstants.TUTORIAL__MAGE_INIT_ATTACK);
        return false;
      }
      if (defense < ControllerConstants.TUTORIAL__MAGE_INIT_DEFENSE) {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        log.error("mage defense too low. defense is " + defense + ", init defense is " + ControllerConstants.TUTORIAL__MAGE_INIT_DEFENSE);
        return false;
      }            
    } else {
      resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
      log.error("unkown user type. type=" + type);
      return false;
    }
    if (energy < ControllerConstants.TUTORIAL__INIT_ENERGY) {
      resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
      log.error("energy too low. energy is " + energy + ", init energy is " + ControllerConstants.TUTORIAL__INIT_ENERGY);
      return false;      
    }
    if (health < ControllerConstants.TUTORIAL__INIT_HEALTH) {
      resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
      log.error("health too low. health is " + health + ", init health is " + ControllerConstants.TUTORIAL__INIT_HEALTH);
      return false;
    }
    if (stamina < ControllerConstants.TUTORIAL__INIT_STAMINA) {
      resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
      log.error("stamina too low. stamina is " + stamina + ", init stamina is " + ControllerConstants.TUTORIAL__INIT_STAMINA);
      return false;
    }

    resBuilder.setStatus(UserCreateStatus.SUCCESS);
    return true;
  }

  private int calculateCorrectSumStat(ClassType classType) {
    int sumStat = ControllerConstants.TUTORIAL__INIT_HEALTH + ControllerConstants.TUTORIAL__INIT_ENERGY 
        + ControllerConstants.TUTORIAL__INIT_STAMINA;
    if (classType == ClassType.WARRIOR) {
      return sumStat + ControllerConstants.TUTORIAL__WARRIOR_INIT_ATTACK + ControllerConstants.TUTORIAL__WARRIOR_INIT_DEFENSE;
    } else if (classType == ClassType.ARCHER) {
      return sumStat + ControllerConstants.TUTORIAL__ARCHER_INIT_ATTACK + ControllerConstants.TUTORIAL__ARCHER_INIT_DEFENSE;
    } else if (classType == ClassType.MAGE) {
      return sumStat + ControllerConstants.TUTORIAL__MAGE_INIT_ATTACK + ControllerConstants.TUTORIAL__MAGE_INIT_DEFENSE;
    }
    return sumStat;
  }

}
