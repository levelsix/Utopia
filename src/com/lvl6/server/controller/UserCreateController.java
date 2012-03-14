package com.lvl6.server.controller;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.LocationProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.AvailableReferralCodeRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.NIOUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class UserCreateController extends EventController {

  public UserCreateController() {
    numAllocatedThreads = 3;
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
  protected void processRequestEvent(RequestEvent event) {
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

    boolean legitUserCreate = checkLegitUserCreate(resBuilder, udid, name, 
        loc, type, attack, defense, energy, health, stamina, timeOfStructPurchase, timeOfStructBuild, structCoords);

    User referrer = null;
    User user = null;
    int userId = ControllerConstants.NOT_SET;
    List<Integer> equipIds = new ArrayList<Integer>();
    Task taskCompleted = null;
    
    if (legitUserCreate) {
      referrer = (referrerCode != null && referrerCode.length() > 0) ? UserRetrieveUtils.getUserByReferralCode(referrerCode) : null;

      String newReferCode = grabNewReferCode();

      taskCompleted = TaskRetrieveUtils.getTaskForTaskId(ControllerConstants.TUTORIAL__FIRST_TASK_ID);

      int playerExp = taskCompleted.getExpGained() * taskCompleted.getNumForCompletion() + ControllerConstants.TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_EXP_GAIN + ControllerConstants.TUTORIAL__FAKE_QUEST_EXP_GAINED;
      int playerCoins = ControllerConstants.TUTORIAL__INIT_COINS + MiscMethods.calculateCoinsGainedFromTutorialTask(taskCompleted) + ControllerConstants.TUTORIAL__FIRST_DEFEAT_TYPE_JOB_BATTLE_COIN_GAIN + ControllerConstants.TUTORIAL__FAKE_QUEST_COINS_GAINED; 

      int playerDiamonds = ControllerConstants.TUTORIAL__INIT_DIAMONDS - ControllerConstants.TUTORIAL__DIAMOND_COST_TO_INSTABUILD_FIRST_STRUCT;
      if (referrer != null) playerDiamonds += ControllerConstants.USER_CREATE__DIAMOND_REWARD_FOR_BEING_REFERRED;
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

      userId = InsertUtils.insertUser(udid, name, type, loc, deviceToken, newReferCode, ControllerConstants.USER_CREATE__START_LEVEL, 
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

    NIOUtils.channelWrite(sc, writeBuffer);

    if (legitUserCreate && userId > 0) {
      server.lockPlayer(userId);
      try {
        writeUserStruct(userId, ControllerConstants.TUTORIAL__FIRST_STRUCT_TO_BUILD, timeOfStructPurchase, timeOfStructBuild, structCoords);
        writeUserCritstructs(user.getId());
        writeUserEquips(user.getId(), equipIds);
        writeTaskCompleted(user.getId(), taskCompleted);
        if (!UpdateUtils.incrementCityRankForUserCity(user.getId(), 1, 1)) {
          log.error("problem with giving user access to first city");
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

  private void writeUserStruct(int userId, int structId, Timestamp timeOfStructPurchase, Timestamp timeOfStructBuild, CoordinatePair structCoords) {
    if (InsertUtils.insertUserStructJustBuilt(userId, structId, timeOfStructPurchase, timeOfStructBuild, structCoords)) {
      log.error("problem in giving user the user struct");
    }
  }

  private void writeTaskCompleted(int userId, Task taskCompleted) {
    if (taskCompleted != null) {
      if (!UpdateUtils.incrementTimesCompletedInRankForUserTask(userId, taskCompleted.getId(), taskCompleted.getNumForCompletion())) {
        log.error("problem with incrementing user times completed in rank in tutorial");
      }
    }
  }

  private void writeUserEquips(int userId, List<Integer> equipIds) {
    if (equipIds.size() > 0) {
      if (!InsertUtils.insertUserEquips(userId, equipIds, 1)) {
        log.error("problem with giving user initial user equips for user " + userId);
      }
    }
  }

  private void writeUserCritstructs(int userId) {
    if (!InsertUtils.insertAviaryAndCarpenterCoords(userId, ControllerConstants.AVIARY_COORDS, ControllerConstants.CARPENTER_COORDS)) {
      log.error("problem with giving user his critical structs");
    }
  }

  private String grabNewReferCode() {
    String newReferCode = AvailableReferralCodeRetrieveUtils.getAvailableReferralCode();
    if (newReferCode != null && newReferCode.length() > 0) {
      while (!DeleteUtils.deleteAvailableReferralCode(newReferCode)) {
        newReferCode = AvailableReferralCodeRetrieveUtils.getAvailableReferralCode();
      }
    }
    return newReferCode;
  }

  private void rewardReferrer(User referrer, User user) {
    if (!referrer.isFake()) {
      server.lockPlayer(referrer.getId());
      try {
        if (!referrer.updateRelativeDiamondsNumreferrals(ControllerConstants.USER_CREATE__DIAMOND_REWARD_FOR_REFERRER, 1)) {
          log.error("problem with rewarding the referrer");
        } else {
          InsertUtils.insertReferral(referrer.getId(), user.getId());

          ReferralCodeUsedResponseEvent resEvent = new ReferralCodeUsedResponseEvent(referrer.getId());
          ReferralCodeUsedResponseProto resProto = ReferralCodeUsedResponseProto.newBuilder()
              .setSender(CreateInfoProtoUtils.createMinimumUserProtoFromUser(referrer))
              .setReferredPlayer(CreateInfoProtoUtils.createMinimumUserProtoFromUser(user)).build();
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
      Timestamp timeOfStructPurchase, Timestamp timeOfDiamondInstabuild, CoordinatePair coordinatePair) {
    if (udid == null || name == null || timeOfStructPurchase == null || coordinatePair == null || type == null || timeOfDiamondInstabuild == null) {
      resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
      return false;
    }
    if (UserRetrieveUtils.getUserByUDID(udid) != null) {
      resBuilder.setStatus(UserCreateStatus.USER_WITH_UDID_ALREADY_EXISTS);
      return false;
    }
    if (!MiscMethods.checkClientTimeBeforeApproximateNow(timeOfDiamondInstabuild) || !MiscMethods.checkClientTimeBeforeApproximateNow(timeOfStructPurchase)) {
      resBuilder.setStatus(UserCreateStatus.CLIENT_TOO_AHEAD_OF_SERVER_TIME);
      return false;
    }
    if (loc.getLatitude() < ControllerConstants.LATITUDE_MIN || loc.getLatitude() > ControllerConstants.LATITUDE_MAX || 
        loc.getLongitude() < ControllerConstants.LONGITUDE_MIN || loc.getLongitude() > ControllerConstants.LONGITUDE_MAX) {
      resBuilder.setStatus(UserCreateStatus.INVALID_LOCATION);
      return false;
    }
    if (name.length() < ControllerConstants.USER_CREATE__MIN_NAME_LENGTH || 
        name.length() > ControllerConstants.USER_CREATE__MAX_NAME_LENGTH) {
      resBuilder.setStatus(UserCreateStatus.INVALID_NAME);
      return false;
    }
    if (type == UserType.GOOD_ARCHER || type == UserType.BAD_ARCHER) {
      if (attack < ControllerConstants.TUTORIAL__ARCHER_INIT_ATTACK) {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        return false;
      }
      if (defense < ControllerConstants.TUTORIAL__ARCHER_INIT_DEFENSE) {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        return false;
      }
    }
    if (type == UserType.GOOD_WARRIOR || type == UserType.BAD_WARRIOR) {
      if (attack < ControllerConstants.TUTORIAL__WARRIOR_INIT_ATTACK) {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        return false;
      }
      if (defense < ControllerConstants.TUTORIAL__WARRIOR_INIT_DEFENSE) {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        return false;
      }      
    }
    if (type == UserType.GOOD_MAGE || type == UserType.BAD_MAGE) {
      if (attack < ControllerConstants.TUTORIAL__MAGE_INIT_ATTACK) {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        return false;
      }
      if (defense < ControllerConstants.TUTORIAL__MAGE_INIT_DEFENSE) {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
        return false;
      }            
    } else {
      resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
      return false;
    }
    if (energy < ControllerConstants.TUTORIAL__INIT_ENERGY) {
      resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
      return false;      
    }
    if (health < ControllerConstants.TUTORIAL__INIT_HEALTH) {
      resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
      return false;
    }
    if (stamina < ControllerConstants.TUTORIAL__INIT_STAMINA) {
      resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
      return false;
    }

    resBuilder.setStatus(UserCreateStatus.SUCCESS);
    return true;
  }

}
