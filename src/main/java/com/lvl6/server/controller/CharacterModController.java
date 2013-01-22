package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.CharacterModRequestEvent;
import com.lvl6.events.response.CharacterModResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.CharacterModRequestProto;
import com.lvl6.proto.EventProto.CharacterModResponseProto;
import com.lvl6.proto.EventProto.CharacterModResponseProto.Builder;
import com.lvl6.proto.EventProto.CharacterModResponseProto.CharacterModStatus;
import com.lvl6.proto.InfoProto.CharacterModType;
import com.lvl6.proto.InfoProto.EquipClassType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

@Component @DependsOn("gameServer") public class CharacterModController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public CharacterModController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new CharacterModRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_CHARACTER_MOD_EVENT;
  }

  /*
   * db stuff done before sending event to eventwriter/client because the client's not waiting 
   * on it immediately anyways
   */
  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    CharacterModRequestProto reqProto = ((CharacterModRequestEvent)event).getCharacterModRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    CharacterModType modType = reqProto.getModType();
    UserType newUserType = reqProto.getFutureUserType(); //could be null
    String newName = reqProto.getFutureName(); //could be null
    CharacterModResponseProto.Builder resBuilder = CharacterModResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setModType(modType);

    // Lock this player's ID
    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      int diamondCost = 0;
      if (modType == CharacterModType.CHANGE_CHARACTER_TYPE) {
        diamondCost = ControllerConstants.CHARACTER_MOD__DIAMOND_COST_OF_CHANGE_CHARACTER_TYPE;
      } else if (modType == CharacterModType.CHANGE_NAME) {
        diamondCost = ControllerConstants.CHARACTER_MOD__DIAMOND_COST_OF_CHANGE_NAME;
      } else if (modType == CharacterModType.NEW_PLAYER) {
        diamondCost = ControllerConstants.CHARACTER_MOD__DIAMOND_COST_OF_NEW_PLAYER;
      } else if (modType == CharacterModType.RESET_SKILL_POINTS) {
        diamondCost = ControllerConstants.CHARACTER_MOD__DIAMOND_COST_OF_RESET_SKILL_POINTS;
      } 

      boolean legitMod = checkLegitMod(resBuilder, diamondCost, user, newUserType, newName, modType);

      if (legitMod) {
        writeChangesToDB(user, modType, diamondCost, newUserType, newName);
        resBuilder.setSkillPointsNew(user.getSkillPoints());
        resBuilder.setAttackNew(user.getAttack());
        resBuilder.setDefenseNew(user.getDefense());
        resBuilder.setStaminaNew(user.getStamina());
        resBuilder.setEnergyNew(user.getEnergy());
      }

      CharacterModResponseEvent resEvent = new CharacterModResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setCharacterModResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitMod) {
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        writeToUserCurrencyHistory(user, modType, diamondCost);
      }


    } catch (Exception e) {
      log.error("exception in CharacterModController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private void writeChangesToDB(User user, CharacterModType modType, int diamondCost, UserType newUserType,
      String newName) {
    if (modType == CharacterModType.CHANGE_CHARACTER_TYPE) {
      if (!user.updateNameUserTypeUdid(newUserType, null, null, diamondCost*-1)) {
        log.error("error in changing user character type from "+user.getType()+" to "+newUserType+
            " with diamond cost "+diamondCost);
      }
      unequipNewlyUnequipppableEquips(user, newUserType);
    } else if (modType == CharacterModType.CHANGE_NAME) {
      if (!user.updateNameUserTypeUdid(null, newName, null, diamondCost*-1)) {
        log.error("error in changing user name from "+user.getName()+" to "+newName+
            " with diamond cost "+diamondCost);
      }
    } else if (modType == CharacterModType.NEW_PLAYER) {
      String newUdid = user.getUdid()+"_reset"; //randomly add number between 0-99 to the end of udid
      if (!user.updateNameUserTypeUdid(null, null, newUdid, diamondCost*-1)) {
        log.error("error in updating user UDID from "+user.getUdid()+" to "+newUdid+
            " with diamond cost "+diamondCost);
      }
    } else if (modType == CharacterModType.RESET_SKILL_POINTS) {
      if (!user.resetSkillPoints(user.getEnergy(), user.getStamina(), diamondCost*-1)) {
        log.error("error in reseting skill points");
      }
    } 
  }

  private void unequipNewlyUnequipppableEquips(User user, UserType newUserType) {
    List<Integer> userEquipIds = new ArrayList<Integer>();
    if (user.getWeaponEquippedUserEquipId() > 0)
      userEquipIds.add(user.getWeaponEquippedUserEquipId());
    if (user.getArmorEquippedUserEquipId() > 0)
      userEquipIds.add(user.getArmorEquippedUserEquipId());
    if (user.getAmuletEquippedUserEquipId() > 0)
      userEquipIds.add(user.getAmuletEquippedUserEquipId());

    List<UserEquip> specificUserEquips = RetrieveUtils.userEquipRetrieveUtils().getSpecificUserEquips(userEquipIds);
    Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();

    boolean unequipWeapon = false, unequipArmor = false, unequipAmulet = false;
    EquipClassType userClass = MiscMethods.getClassTypeFromUserType(newUserType);

    for (UserEquip ue : specificUserEquips) {
      Equipment equip = equipmentIdsToEquipment.get(ue.getEquipId());
      if (ue.getId() == user.getWeaponEquippedUserEquipId()) {
        unequipWeapon =  (userClass != equip.getClassType() && equip.getClassType() != EquipClassType.ALL_AMULET);
      }
      if (ue.getId() == user.getArmorEquippedUserEquipId()) {
        unequipArmor =  (userClass != equip.getClassType() && equip.getClassType() != EquipClassType.ALL_AMULET);
      }
      if (ue.getId() == user.getAmuletEquippedUserEquipId()) {
        unequipAmulet =  (userClass != equip.getClassType() && equip.getClassType() != EquipClassType.ALL_AMULET);
      }
    }
    if (!user.updateUnequip(unequipWeapon, unequipArmor, unequipAmulet)) {
      log.error("problem with unequipping equips for user after type change. user=" + user + ", unequipWeapon=" + 
          unequipWeapon + ", unequipArmor=" + unequipArmor + ", unequipAmulet=" + unequipAmulet);
    }
  }

  private boolean checkLegitMod(Builder resBuilder, int diamondCost, User user, UserType newUserType, 
      String newName, CharacterModType modType) {
    if (modType == CharacterModType.CHANGE_CHARACTER_TYPE && (newUserType == null || newUserType==user.getType())) {
      resBuilder.setStatus(CharacterModStatus.OTHER_FAIL);
      log.error("tried to change character type with invalid newUserType, newUserType is "+newUserType);
      return false;
    }
    if (modType == CharacterModType.NEW_PLAYER && user.getClanId() > 0) {
      resBuilder.setStatus(CharacterModStatus.CANNOT_CHANGE_TO_OPPOSING_SIDE_WHEN_IN_CLAN);
      log.error("tried to reset account while still in clan with id " + user.getClanId());
      return false;
    }
    if (modType == CharacterModType.CHANGE_CHARACTER_TYPE && 
        (MiscMethods.checkIfGoodSide(user.getType()) != MiscMethods.checkIfGoodSide(newUserType))) {
      if (user.getClanId() > 0) {
        resBuilder.setStatus(CharacterModStatus.CANNOT_CHANGE_TO_OPPOSING_SIDE_WHEN_IN_CLAN);
        log.error("tried to change to opposing side while still in clan with id " + user.getClanId());
        return false;
      }
    }
    if (modType == CharacterModType.CHANGE_NAME && newName == null) {
      resBuilder.setStatus(CharacterModStatus.OTHER_FAIL);
      log.error("tried to change character name without providing newName, newName is "+newName);
      return false;
    }
    if (diamondCost < 0) {
      resBuilder.setStatus(CharacterModStatus.OTHER_FAIL);
      log.error("in character mod, diamondCost "+diamondCost+" is less than 0");
      return false;
    }
    if (diamondCost > user.getDiamonds()) {
      resBuilder.setStatus(CharacterModStatus.NOT_ENOUGH_DIAMONDS);  
      log.error("user does not have enough gold. has " + user.getDiamonds() + ", needs " + diamondCost);
      return false;
    }
    //temporarily stop people from resetting their game, to prevent transfering initial gold
    if (CharacterModType.NEW_PLAYER == modType) {
      resBuilder.setStatus(CharacterModStatus.OTHER_FAIL);
      log.error("user tried resetting their character. user=" + user);
      return false;
    }
    //TODO
    //add check to make sure name is legit
    //add check to make sure character is not in a clan
    resBuilder.setStatus(CharacterModStatus.SUCCESS);
    return true;
  }

  private void writeToUserCurrencyHistory(User aUser, CharacterModType modType, int diamondCost) {
    try {
      int userId = aUser.getId();
      Timestamp date = new Timestamp((new Date()).getTime());
      int isSilver = 0;
      int currencyBefore = aUser.getDiamonds() - diamondCost;
      String reasonForChange = "character mod controller";
      if (modType == CharacterModType.CHANGE_CHARACTER_TYPE) {
        reasonForChange = ControllerConstants.UCHRFC__CHARACTER_MOD_TYPE;
      } else if (modType == CharacterModType.CHANGE_NAME) {
        reasonForChange = ControllerConstants.UCHRFC__CHARACTER_MOD_NAME;
      } else if (modType == CharacterModType.NEW_PLAYER) {
        reasonForChange = ControllerConstants.UCHRFC__CHARACTER_MOD_RESET;
      } else if (modType == CharacterModType.RESET_SKILL_POINTS) {
        reasonForChange = ControllerConstants.UCHRFC__CHARACTER_MOD_SKILL_POINTS;
      }
      
      int numInserted = InsertUtils.get().insertIntoUserCurrencyHistory(userId, date, 
          isSilver, diamondCost, currencyBefore, reasonForChange);
      log.info("Should be 1. Rows inserted into user_currency_history: " + numInserted);
    } catch (Exception e) {
      log.error("Maybe table's not there or duplicate keys? ", e);
    }
  }
  
}