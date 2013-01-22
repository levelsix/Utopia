package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.UseSkillPointRequestEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.UseSkillPointResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;
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

  //used for skill point stuff
  public static final int cost = 0;
  public static final int gain = 1;

  /*
   * db stuff done before sending event to eventwriter/client because the client's not waiting 
   * on it immediately anyways
   */
  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    UseSkillPointRequestProto reqProto = ((UseSkillPointRequestEvent)event).getUseSkillPointRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    Map<String, Integer> statTypeToStatIncrease = new HashMap<String, Integer>();
    statTypeToStatIncrease.put(DBConstants.USER__ATTACK, reqProto.getAttackIncrease());
    statTypeToStatIncrease.put(DBConstants.USER__DEFENSE, reqProto.getDefenseIncrease());
    statTypeToStatIncrease.put(DBConstants.USER__ENERGY, reqProto.getEnergyIncrease());
    statTypeToStatIncrease.put(DBConstants.USER__STAMINA, reqProto.getStaminaIncrease());

    UseSkillPointResponseProto.Builder resBuilder = UseSkillPointResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    // Lock this player's ID
    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      Map<String, List<Integer>> statTypeToStatCostAndGain = new HashMap<String, List<Integer>>();
      List<Integer> totalCostAndGain = determineAllStatCostAndGain(statTypeToStatIncrease, statTypeToStatCostAndGain);

      int totalSkillPointCost = totalCostAndGain.get(0);
      int totalSkillPointGain = totalCostAndGain.get(1);
      boolean legitBoost = checkLegitBoost(resBuilder, totalSkillPointGain, 
          totalSkillPointCost, user);

      UseSkillPointResponseEvent resEvent = new UseSkillPointResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setUseSkillPointResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitBoost) {
        writeChangesToDB(user, statTypeToStatCostAndGain, -totalSkillPointCost);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in UseSkillPointController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  // returns a list containing two integers: total cost, total gain
  private List<Integer> determineAllStatCostAndGain(Map<String, Integer> statTypeToStatIncrease,
      Map<String, List<Integer>> statTypeToStatCostAndGain) {
    List<Integer> totalCostAndGain = new ArrayList<Integer>(); //return value
    int totalCost = 0;
    int totalGain = 0;

    //placeholder for cost and gain for one stat
    List<Integer> costAndGainForAStat = null; 

    for(String statType: statTypeToStatIncrease.keySet()) {
      int statIncrease = statTypeToStatIncrease.get(statType);
      costAndGainForAStat = determineStatCostAndGain(statType, statIncrease);

      totalCost += costAndGainForAStat.get(cost);
      totalGain += costAndGainForAStat.get(gain);

      //used in writeToDB to determine how much of each user stat is increased
      statTypeToStatCostAndGain.put(statType, costAndGainForAStat);
    }

    totalCostAndGain.add(cost, totalCost);
    totalCostAndGain.add(gain, totalGain);

    return totalCostAndGain;
  }

  //return value is a two Integer list: cost and gain
  private List<Integer> determineStatCostAndGain(String statType, int statIncrease) {
    List<Integer> costAndGain = new ArrayList<Integer>();

    if (DBConstants.USER__ATTACK.equals(statType)) {
      costAndGain.add(cost, statIncrease * ControllerConstants.USE_SKILL_POINT__ATTACK_BASE_COST);
      costAndGain.add(gain, statIncrease * ControllerConstants.USE_SKILL_POINT__ATTACK_BASE_GAIN);

    } else if (DBConstants.USER__DEFENSE.equals(statType)) {
      costAndGain.add(cost, statIncrease * ControllerConstants.USE_SKILL_POINT__DEFENSE_BASE_COST);
      costAndGain.add(gain, statIncrease * ControllerConstants.USE_SKILL_POINT__DEFENSE_BASE_GAIN);

    } else if (DBConstants.USER__ENERGY.equals(statType)) {
      costAndGain.add(cost, statIncrease * ControllerConstants.USE_SKILL_POINT__ENERGY_BASE_COST);
      costAndGain.add(gain, statIncrease * ControllerConstants.USE_SKILL_POINT__ENERGY_BASE_GAIN);

    } else if (DBConstants.USER__STAMINA.equals(statType)) {
      costAndGain.add(cost, statIncrease * ControllerConstants.USE_SKILL_POINT__STAMINA_BASE_COST);
      costAndGain.add(gain, statIncrease * ControllerConstants.USE_SKILL_POINT__STAMINA_BASE_GAIN);
    } else {
      log.error("no stat type with value=" + statType);
      costAndGain.add(cost, 0);
      costAndGain.add(gain, 0);
    }

    return costAndGain;
  }

  private void writeChangesToDB(User user, Map<String, List<Integer>> statTypeToStatCostAndGain, int skillPointsChange) {
    List<Integer> attackCostAndGain = statTypeToStatCostAndGain.get(DBConstants.USER__ATTACK);
    List<Integer> defenseCostAndGain = statTypeToStatCostAndGain.get(DBConstants.USER__DEFENSE);
    List<Integer> energyCostAndGain = statTypeToStatCostAndGain.get(DBConstants.USER__ENERGY);
    List<Integer> staminaCostAndGain = statTypeToStatCostAndGain.get(DBConstants.USER__STAMINA);

    int attackChange = attackCostAndGain.get(gain);
    int defenseChange = defenseCostAndGain.get(gain);
    int energyChange = energyCostAndGain.get(gain);
    int staminaChange = staminaCostAndGain.get(gain);

    int attackCost = attackCostAndGain.get(gain);
    int defenseCost = defenseCostAndGain.get(gain);
    int energyCost = energyCostAndGain.get(gain);
    int staminaCost = staminaCostAndGain.get(gain);

    if(!user.updateRelativeAttackDefenseEnergyEnergyMaxStaminaStaminaMaxSkillPoints(
        attackChange, defenseChange, energyChange, staminaChange, skillPointsChange)) {
      log.error("error in taking away " 
          + attackCost + " skill points and giving " + attackChange + " attack"
          + ", " + defenseCost + " skill points and giving " + defenseChange + " defense"
          + ", " + energyCost + " skill points and giving " + energyChange + " energy/energy max"
          + ", " + staminaCost + " skill points and giving " + staminaChange + " stamina/stamina max");
    }
  }

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
