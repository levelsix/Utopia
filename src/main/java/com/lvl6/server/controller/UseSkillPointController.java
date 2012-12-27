package com.lvl6.server.controller;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.events.request.UseSkillPointRequestEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.UseSkillPointResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.UseSkillPointRequestProto;
import com.lvl6.proto.EventProto.UseSkillPointResponseProto;
import com.lvl6.proto.EventProto.UseSkillPointResponseProto.Builder;
import com.lvl6.proto.EventProto.UseSkillPointResponseProto.UseSkillPointStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;

  @Component @DependsOn("gameServer") public class UseSkillPointController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public UseSkillPointController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new UseSkillPointRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_USE_SKILL_POINT_EVENT;
  }

  /*
   * db stuff done before sending event to eventwriter/client because the client's not waiting 
   * on it immediately anyways
   */
  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    UseSkillPointRequestProto reqProto = ((UseSkillPointRequestEvent)event).getUseSkillPointRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int attackIncrease = reqProto.getAttackIncrease();
    int defenseIncrease = reqProto.getDefenseIncrease();
    int energyIncrease = reqProto.getEnergyIncrease();
    int staminaIncrease = reqProto.getStaminaIncrease();
    UseSkillPointResponseProto.Builder resBuilder = UseSkillPointResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    // Lock this player's ID
    server.lockPlayer(senderProto.getUserId());

    try {
//      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
//
//      int gain = 0;
//      int cost = 0;
//      if (boostType == BoostType.ATTACK) {
//        gain = ControllerConstants.USE_SKILL_POINT__ATTACK_BASE_GAIN;
//        cost = ControllerConstants.USE_SKILL_POINT__ATTACK_BASE_COST;
//      } else if (boostType == BoostType.DEFENSE) {
//        gain = ControllerConstants.USE_SKILL_POINT__DEFENSE_BASE_GAIN;
//        cost = ControllerConstants.USE_SKILL_POINT__DEFENSE_BASE_COST;
//      } else if (boostType == BoostType.ENERGY) {
//        gain = ControllerConstants.USE_SKILL_POINT__ENERGY_BASE_GAIN;
//        cost = ControllerConstants.USE_SKILL_POINT__ENERGY_BASE_COST;
//      } else if (boostType == BoostType.STAMINA) {
//        gain = ControllerConstants.USE_SKILL_POINT__STAMINA_BASE_GAIN;
//        cost = ControllerConstants.USE_SKILL_POINT__STAMINA_BASE_COST;
//      } 
//
//      boolean legitBoost = checkLegitBoost(resBuilder, gain, cost, user);
//
//      UseSkillPointResponseEvent resEvent = new UseSkillPointResponseEvent(senderProto.getUserId());
//      resEvent.setTag(event.getTag());
//      resEvent.setUseSkillPointResponseProto(resBuilder.build());  
//      server.writeEvent(resEvent);
//
//      if (legitBoost) {
//        writeChangesToDB(user, boostType, gain, cost);
//        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
//        resEventUpdate.setTag(event.getTag());
//        server.writeEvent(resEventUpdate);
//      }

    } catch (Exception e) {
      log.error("exception in UseSkillPointController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

//  private void writeChangesToDB(User user, BoostType boostType, int gain,
//      int cost) {
//    if (boostType == BoostType.ATTACK) {
//      if (!user.updateRelativeAttackDefenseSkillPoints(gain, 0, cost*-1)) {
//        log.error("error in taking away " + cost + " skill points and giving " + gain + " attack");
//      }
//    } else if (boostType == BoostType.DEFENSE) {
//      if (!user.updateRelativeAttackDefenseSkillPoints(0, gain, cost*-1)) {
//        log.error("error in taking away " + cost + " skill points and giving " + gain + " defense");
//      }
//    } else if (boostType == BoostType.ENERGY) {
//      if (!user.updateRelativeEnergyEnergymaxStaminaStaminamaxSkillPoints(gain, gain, 0, 0, cost*-1)){
//        log.error("error in taking away " + cost + " skill points and giving " + gain + " energy/energymax");
//      }
//    } else if (boostType == BoostType.STAMINA) {
//      if (!user.updateRelativeEnergyEnergymaxStaminaStaminamaxSkillPoints(0, 0, gain, gain, cost*-1)){
//        log.error("error in taking away " + cost + " skill points and giving " + gain + " stamina/staminamax");
//      }
//    } 
//  }

  private boolean checkLegitBoost(Builder resBuilder, int gain, int cost, User user) {
    if (gain == 0 || cost == 0) {
      resBuilder.setStatus(UseSkillPointStatus.OTHER_FAIL);
      log.error("no gain or no cost in using skill point");
      return false;
    }
    if (cost > user.getSkillPoints()) {
      resBuilder.setStatus(UseSkillPointStatus.NOT_ENOUGH_SKILL_POINTS);  
      log.error("user does not have enough skill points. has " + user.getSkillPoints() + ", needs " + cost);
      return false;
    }
    resBuilder.setStatus(UseSkillPointStatus.SUCCESS);
    return true;
  }

}
