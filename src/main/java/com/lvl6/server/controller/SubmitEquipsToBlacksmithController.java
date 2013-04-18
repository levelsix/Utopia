package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.SubmitEquipsToBlacksmithRequestEvent;
import com.lvl6.events.response.SubmitEquipsToBlacksmithResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.SubmitEquipsToBlacksmithRequestProto;
import com.lvl6.proto.EventProto.SubmitEquipsToBlacksmithResponseProto;
import com.lvl6.proto.EventProto.SubmitEquipsToBlacksmithResponseProto.Builder;
import com.lvl6.proto.EventProto.SubmitEquipsToBlacksmithResponseProto.SubmitEquipsToBlacksmithStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UnhandledBlacksmithAttemptRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

@Component @DependsOn("gameServer") public class SubmitEquipsToBlacksmithController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public SubmitEquipsToBlacksmithController() {
    numAllocatedThreads = 3;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new SubmitEquipsToBlacksmithRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_SUBMIT_EQUIPS_TO_BLACKSMITH;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    SubmitEquipsToBlacksmithRequestProto reqProto = ((SubmitEquipsToBlacksmithRequestEvent)event).getSubmitEquipsToBlacksmithRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userEquipOne = reqProto.getUserEquipOne();
    int userEquipTwo = reqProto.getUserEquipTwo();
    boolean paidToGuarantee = reqProto.getPaidToGuarantee();
    Timestamp startTime = new Timestamp(reqProto.getStartTime());
    int forgeSlotNumber = reqProto.getForgeSlotNumber();
    
    SubmitEquipsToBlacksmithResponseProto.Builder resBuilder = SubmitEquipsToBlacksmithResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      int previousGold = 0;
      List<Integer> userEquipIds = new ArrayList<Integer>();
      userEquipIds.add(userEquipOne);
      userEquipIds.add(userEquipTwo);
      List<UserEquip> userEquips = RetrieveUtils.userEquipRetrieveUtils().getSpecificUserEquips(userEquipIds);
      Equipment equip = (userEquips != null && userEquips.size() >= 1) ? EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(userEquips.get(0).getEquipId()) : null;

      boolean legitSubmit = checkLegitSubmit(resBuilder, user, paidToGuarantee, userEquips, equip, startTime, forgeSlotNumber);

      int goalLevel = 0; 
      int diamondCost = 0;
      if (legitSubmit) {
        goalLevel = userEquips.get(0).getLevel() + 1;
        diamondCost = calculateDiamondCostForGuarantee(equip, goalLevel, paidToGuarantee);
        //need to keep track of enhancements on weapons
        int enhancementPercentOne = userEquips.get(0).getEnhancementPercentage();
        int enhancementPercentTwo = userEquips.get(1).getEnhancementPercentage();
        
        int blacksmithId = InsertUtils.get().insertForgeAttemptIntoBlacksmith(user.getId(), 
            equip.getId(), goalLevel, paidToGuarantee, startTime, diamondCost, null, false,
            enhancementPercentOne, enhancementPercentTwo, forgeSlotNumber);
        
        if (blacksmithId <= 0) {
          resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.OTHER_FAIL);
          log.error("problem with trying to give user blacksmith attempt");
          legitSubmit = false;
        } else {
          BlacksmithAttempt ba = new BlacksmithAttempt(blacksmithId, user.getId(), equip.getId(),
              goalLevel, paidToGuarantee, startTime, diamondCost, null, false, enhancementPercentOne,
              enhancementPercentTwo, forgeSlotNumber);
          resBuilder.setUnhandledBlacksmithAttempt(
              CreateInfoProtoUtils.createUnhandledBlacksmithAttemptProtoFromBlacksmithAttempt(ba)
              );
        }
      }

      SubmitEquipsToBlacksmithResponseEvent resEvent = 
          new SubmitEquipsToBlacksmithResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setSubmitEquipsToBlacksmithResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitSubmit) {
        previousGold = user.getDiamonds();
        
        Map<String, Integer> money = new HashMap<String, Integer>();
        writeChangesToDB(user, diamondCost, userEquips, money);
        if (diamondCost > 0) {
          UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
          resEventUpdate.setTag(event.getTag());
          server.writeEvent(resEventUpdate);
        }
        writeToUserCurrencyHistory(user, startTime, money, previousGold);
      }

    } catch (Exception e) {
      log.error("exception in SubmitEquipsToBlacksmith processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private void writeChangesToDB(User user, int diamondCostForGuarantee, List<UserEquip> userEquips,
      Map<String, Integer> money) {
    if (userEquips != null) {
      for (UserEquip ue : userEquips) {
        if (!MiscMethods.unequipUserEquipIfEquipped(user, ue)) {
          log.error("problem with unequipping userequip" + ue.getId());
          return;
        }
        if (!DeleteUtils.get().deleteUserEquip(ue.getId())) {
          log.error("problem with removing user equip post forge from user, user equip= " + ue);
        }
      }
      if (diamondCostForGuarantee > 0) {
        if (!user.updateRelativeDiamondsNaive(diamondCostForGuarantee*-1)) {
          log.error("problem with taking away diamonds post forge guarantee attempt, taking away " + diamondCostForGuarantee + ", user only has " + user.getDiamonds());
        } else {
          money.put(MiscMethods.gold, -1 * diamondCostForGuarantee);
        }
      }
    }
  }

  private int calculateDiamondCostForGuarantee(Equipment equip, int goalLevel, boolean paidToGuarantee) {
    if (!paidToGuarantee) return 0;

    float chanceOfSuccess = MiscMethods.calculateChanceOfSuccessForForge(equip, goalLevel);
    int goldCostToSpeedup = MiscMethods.calculateDiamondCostToSpeedupForgeWaittime(equip, goalLevel);
    
    int x = (int) (goldCostToSpeedup/chanceOfSuccess);
    return x;
  }

  private boolean checkLegitSubmit(Builder resBuilder, User user, boolean paidToGuarantee, List<UserEquip> userEquips,
      Equipment equip, Timestamp startTime, int forgeSlotNumber) {
    if (user == null || userEquips == null || userEquips.size() != 2 || equip == null || startTime == null) {
      resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.OTHER_FAIL);
      log.error("parameter passed in is null. user=" + user + ", userEquips=" + userEquips + ", equip=" + equip);
      return false;
    }

    if (!MiscMethods.checkClientTimeAroundApproximateNow(startTime)) {
      resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + startTime + ", servertime~="
          + new Date());
      return false;
    }

    UserEquip ue1 = userEquips.get(0);
    UserEquip ue2 = userEquips.get(1);

    if (ue1.getEquipId() != ue2.getEquipId()) {
      resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.OTHER_FAIL);
      log.error("user equips passed in refer to different equips, equipId of first is " + ue1.getEquipId() + 
          " and equipId of second is " + ue2.getEquipId());
      return false;
    }

    if (ue1.getLevel() != ue2.getLevel()) {
      resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.SUBMITTED_EQUIPS_NOT_SAME_LEVEL);
      log.error("user equips passed in have diff levels, level of first is " + ue1.getLevel() + 
          " and level of second is " + ue2.getLevel());
      return false;
    }

    if (ue1.getLevel() >= ControllerConstants.FORGE_MAX_EQUIP_LEVEL) {
      resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.TRYING_TO_SURPASS_MAX_LEVEL);
      log.error("forged weapon levels are already >= max. max is " + ControllerConstants.FORGE_MAX_EQUIP_LEVEL
          + ", weapon levels = " + ue1.getLevel());
      return false;
    }

    if (user.getDiamonds() < calculateDiamondCostForGuarantee(equip, ue1.getLevel() + 1, paidToGuarantee)) {
      resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.NOT_ENOUGH_DIAMONDS_FOR_GUARANTEE);
      log.error("not enough diamonds to guarantee. has " + user.getDiamonds() + ", needs " + calculateDiamondCostForGuarantee(equip, ue1.getLevel() + 1, paidToGuarantee));
      return false;
    }

    //check if user has at the maximum limit of equips being forged
    Map<Integer, BlacksmithAttempt> blacksmithIdToBlacksmithAttempt = 
        UnhandledBlacksmithAttemptRetrieveUtils.getUnhandledBlacksmithAttemptsForUser(user.getId());
    if (null == blacksmithIdToBlacksmithAttempt) {
      blacksmithIdToBlacksmithAttempt = new HashMap<Integer, BlacksmithAttempt>();
    }
    
    int numEquipsBeingForged = blacksmithIdToBlacksmithAttempt.size();
    int numEquipsUserCanForge = ControllerConstants.FORGE_DEFAULT_NUMBER_OF_FORGE_SLOTS
        + user.getNumAdditionalForgeSlots();
    if (numEquipsBeingForged >= numEquipsUserCanForge) {
      resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.ALREADY_FORGING_MAX_NUM_OF_EQUIPS);
      log.error("already at max forges. limit=" + numEquipsUserCanForge + ", equipsBeingForged="
      + blacksmithIdToBlacksmithAttempt + ", user=" + user);
      return false;
    }
    if (!validForgeSlotNumber(numEquipsUserCanForge, forgeSlotNumber)) {
      resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.FORGE_SLOT_NOT_YET_UNLOCKED);
      log.error("user error: forge slot number not unlocked or is invalid. forgeSlotNumber=" + forgeSlotNumber
          + ", equipsBeingForged=" + blacksmithIdToBlacksmithAttempt + ", user=" + user);
      return false;
    }
    if (clashingForgeSlotNumber(forgeSlotNumber, blacksmithIdToBlacksmithAttempt)) {
      resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.FORGE_SLOT_IN_USE);
      log.error("user error: forge slot number already in use. forgeSlotNumber=" + forgeSlotNumber
          + ", equipsBeingForged=" + blacksmithIdToBlacksmithAttempt);
      return false;
    }
    
    resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.SUCCESS);
    return true;
  }
  
  private boolean validForgeSlotNumber(int numEquipsUserCanForge, int forgeSlotNumber) {
    //forge slot numbers should start at 1
    if (forgeSlotNumber <= 0 || forgeSlotNumber > numEquipsUserCanForge) {
      return false;
    } else {
      return true;
    }
  }
  
  private boolean clashingForgeSlotNumber(int forgeSlotNumber, 
      Map<Integer, BlacksmithAttempt> blacksmithIdToBlacksmithAttempt) {
    for (BlacksmithAttempt ba: blacksmithIdToBlacksmithAttempt.values()) {
      if (ba.getForgeSlotNumber() == forgeSlotNumber) {
        return true;
      }
    }
    
    return false;
  }
  
  public void writeToUserCurrencyHistory(User aUser, Timestamp date, Map<String, Integer> money,
      int previousGold) {
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String gold = MiscMethods.gold;
    String reasonForChange = ControllerConstants.UCHRFC__SUBMIT_EQUIPS_TO_BLACKSMITH;

    previousGoldSilver.put(gold, previousGold);
    reasonsForChanges.put(gold, reasonForChange);
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, money, previousGoldSilver, reasonsForChanges);
  }
}
