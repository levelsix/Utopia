package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.UseSkillPointRequestEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.UseSkillPointResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.UseSkillPointRequestProto;
import com.lvl6.proto.EventProto.UseSkillPointRequestProto.BoostType;
import com.lvl6.proto.EventProto.UseSkillPointResponseProto;
import com.lvl6.proto.EventProto.UseSkillPointResponseProto.Builder;
import com.lvl6.proto.EventProto.UseSkillPointResponseProto.UseSkillPointStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class UseSkillPointController extends EventController {

  private static final int ATTACK_BASE_GAIN = 1;
  private static final int DEFENSE_BASE_GAIN = 1;
  private static final int ENERGY_BASE_GAIN = 1;
  private static final int HEALTH_BASE_GAIN = 10;
  private static final int STAMINA_BASE_GAIN = 1;

  private static final int ATTACK_BASE_COST = 1;
  private static final int DEFENSE_BASE_COST = 1;
  private static final int ENERGY_BASE_COST = 1;
  private static final int HEALTH_BASE_COST = 1;
  private static final int STAMINA_BASE_COST = 2;


  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());    
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
  protected void processRequestEvent(RequestEvent event) {
    UseSkillPointRequestProto reqProto = ((UseSkillPointRequestEvent)event).getUseSkillPointRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    BoostType boostType = reqProto.getBoostType();
    UseSkillPointResponseProto.Builder resBuilder = UseSkillPointResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    // Lock this player's ID
    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

      int gain = 0;
      int cost = 0;
      if (boostType == BoostType.ATTACK) {
        gain = ATTACK_BASE_GAIN;
        cost = ATTACK_BASE_COST;
      } else if (boostType == BoostType.DEFENSE) {
        gain = DEFENSE_BASE_GAIN;
        cost = DEFENSE_BASE_COST;
      } else if (boostType == BoostType.ENERGY) {
        gain = ENERGY_BASE_GAIN;
        cost = ENERGY_BASE_COST;
      } else if (boostType == BoostType.HEALTH) {
        gain = HEALTH_BASE_GAIN;
        cost = HEALTH_BASE_COST;
      } else if (boostType == BoostType.STAMINA) {
        gain = STAMINA_BASE_GAIN;
        cost = STAMINA_BASE_COST;
      } 

      boolean legitBoost = checkLegitBoost(resBuilder, gain, cost, user);

      UseSkillPointResponseEvent resEvent = new UseSkillPointResponseEvent(senderProto.getUserId());
      resEvent.setUseSkillPointResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitBoost) {
        writeChangesToDB(user, boostType, gain, cost);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in UseSkillPointController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private void writeChangesToDB(User user, BoostType boostType, int gain,
      int cost) {
    if (boostType == BoostType.ATTACK) {
      if (!user.updateRelativeAttackDefenseSkillPoints(gain, 0, cost*-1)) {
        log.error("error in changing attack stats");
      }
    } else if (boostType == BoostType.DEFENSE) {
      if (!user.updateRelativeAttackDefenseSkillPoints(0, gain, cost*-1)) {
        log.error("error in changing defense stats");
      }
    } else if (boostType == BoostType.ENERGY) {
      if (!user.updateRelativeEnergyEnergymaxHealthHealthmaxStaminaStaminamaxSkillPoints(gain, gain, 0, 0, 0, 0, cost*-1)){
        log.error("error in changing energy stats");
      }
    } else if (boostType == BoostType.HEALTH) {
      if (!user.updateRelativeEnergyEnergymaxHealthHealthmaxStaminaStaminamaxSkillPoints(0, 0, gain, gain, 0, 0, cost*-1)){
        log.error("error in changing health stats");
      }
    } else if (boostType == BoostType.STAMINA) {
      if (!user.updateRelativeEnergyEnergymaxHealthHealthmaxStaminaStaminamaxSkillPoints(0, 0, 0, 0, gain, gain, cost*-1)){
        log.error("error in changing stamina stats");
      }
    } 
  }

  private boolean checkLegitBoost(Builder resBuilder, int gain, int cost, User user) {
    if (gain == 0 && cost == 0) {
      resBuilder.setStatus(UseSkillPointStatus.OTHER_FAIL);
      return false;
    }
    if (cost > user.getSkillPoints()) {
      resBuilder.setStatus(UseSkillPointStatus.NOT_ENOUGH_SKILL_POINTS);      
      return false;
    }
    resBuilder.setStatus(UseSkillPointStatus.SUCCESS);
    return true;
  }

}
