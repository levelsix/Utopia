package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.SubmitEquipsToBlacksmithRequestEvent;
import com.lvl6.events.response.SubmitEquipsToBlacksmithResponseEvent;
import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
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
import com.lvl6.utils.utilmethods.MiscMethods;

@Component @DependsOn("gameServer") public class SubmitEquipsToBlacksmithController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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

    SubmitEquipsToBlacksmithResponseProto.Builder resBuilder = SubmitEquipsToBlacksmithResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      List<Integer> userEquipIds = new ArrayList<Integer>();
      userEquipIds.add(userEquipOne);
      userEquipIds.add(userEquipTwo);
      List<UserEquip> userEquips = RetrieveUtils.userEquipRetrieveUtils().getSpecificUserEquips(userEquipIds);
      Equipment equip = (userEquips != null && userEquips.size() >= 1) ? EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(userEquips.get(0).getEquipId()) : null;

      boolean legitSubmit = checkLegitSubmit(resBuilder, user, paidToGuarantee, userEquips, equip, startTime);

      int goalLevel = 0;
      if (legitSubmit) {
        goalLevel = userEquips.get(0).getLevel() + 1;

        int blacksmithId = InsertUtils.get().insertForgeAttemptIntoBlacksmith(user.getId(), equip.getId(), goalLevel, 
            paidToGuarantee, startTime, 
            calculateDiamondCostForGuarantee(equip, goalLevel, paidToGuarantee), null, false);

        if (blacksmithId <= 0) {
          resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.OTHER_FAIL);
          log.error("problem with trying to give user blacksmith attempt");
          legitSubmit = false;
        } else {
          BlacksmithAttempt ba = new BlacksmithAttempt(blacksmithId, user.getId(), equip.getId(), goalLevel, 
            paidToGuarantee, startTime, 
            calculateDiamondCostForGuarantee(equip, goalLevel, paidToGuarantee), null, false);
          resBuilder.setUnhandledBlacksmithAttempt(CreateInfoProtoUtils.createUnhandledBlacksmithAttemptProtoFromBlacksmithAttempt(ba));
        }
      }

      SubmitEquipsToBlacksmithResponseEvent resEvent = new SubmitEquipsToBlacksmithResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setSubmitEquipsToBlacksmithResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitSubmit) {
        writeChangesToDB(user, calculateDiamondCostForGuarantee(equip, goalLevel, paidToGuarantee), userEquips);
      }

    } catch (Exception e) {
      log.error("exception in SubmitEquipsToBlacksmith processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, int diamondCostForGuarantee, List<UserEquip> userEquips) {
    if (userEquips != null) {
      for (UserEquip ue : userEquips) {
        if (!DeleteUtils.get().deleteUserEquip(ue.getId())) {
          log.error("problem with removing user equip post forge from user, user equip= " + ue);
        }
      }
      if (diamondCostForGuarantee > 0) {
        if (!user.updateRelativeDiamondsNaive(diamondCostForGuarantee*-1)) {
          log.error("problem with taking away diamonds post forge guarantee attempt, taking away " + diamondCostForGuarantee + ", user only has " + user.getDiamonds());
        }
      }
    }
  }

  private int calculateDiamondCostForGuarantee(Equipment equip, int goalLevel, boolean paidToGuarantee) {
    if (!paidToGuarantee) return 0;
    //TODO:
    return 0;
  }

  private boolean checkLegitSubmit(Builder resBuilder, User user, boolean paidToGuarantee, List<UserEquip> userEquips, Equipment equip, Timestamp startTime) {
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

    if (ue1.getLevel() >= ControllerConstants.SUBMIT_EQUIPS_TO_BLACKSMITH__MAX_EQUIP_LEVEL) {
      resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.TRYING_TO_SURPASS_MAX_LEVEL);
      log.error("forged weapon levels are already >= max. max is " + ControllerConstants.SUBMIT_EQUIPS_TO_BLACKSMITH__MAX_EQUIP_LEVEL
          + ", weapon levels = " + ue1.getLevel());
      return false;
    }

    if (user.getDiamonds() < calculateDiamondCostForGuarantee(equip, ue1.getLevel() + 1, paidToGuarantee)) {
      resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.NOT_ENOUGH_DIAMONDS_FOR_GUARANTEE);
      log.error("not enough diamonds to guarantee. has " + user.getDiamonds() + ", needs " + calculateDiamondCostForGuarantee(equip, ue1.getLevel() + 1, paidToGuarantee));
      return false;
    }

    List<BlacksmithAttempt> unhandledBlacksmithAttemptsForUser = UnhandledBlacksmithAttemptRetrieveUtils.getUnhandledBlacksmithAttemptsForUser(user.getId());
    if (unhandledBlacksmithAttemptsForUser != null && unhandledBlacksmithAttemptsForUser.size() > 0) {
      resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.ALREADY_FORGING);
      log.error("already have unhandled forges: " + unhandledBlacksmithAttemptsForUser);
      return false;
    }

    resBuilder.setStatus(SubmitEquipsToBlacksmithStatus.SUCCESS);
    return true;
  }
}
