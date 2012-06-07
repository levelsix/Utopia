package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BattleRequestEvent;
import com.lvl6.events.response.BattleResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.Quest;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserQuest;
import com.lvl6.info.jobs.DefeatTypeJob;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.BattleRequestProto;
import com.lvl6.proto.EventProto.BattleResponseProto;
import com.lvl6.proto.EventProto.BattleResponseProto.BattleStatus;
import com.lvl6.proto.EventProto.BattleResponseProto.Builder;
import com.lvl6.proto.InfoProto.BattleResult;
import com.lvl6.proto.InfoProto.DefeatTypeJobProto.DefeatTypeJobEnemyType;
import com.lvl6.proto.InfoProto.FullUserEquipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsCompletedDefeatTypeJobsRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsDefeatTypeJobProgressRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.DefeatTypeJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class BattleController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  
  @Autowired
  protected InsertUtil insertUtils;

  public void setInsertUtils(InsertUtil insertUtils) {
	this.insertUtils = insertUtils;
  }

  public BattleController() {
    numAllocatedThreads = 10;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new BattleRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_BATTLE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    BattleRequestProto reqProto = ((BattleRequestEvent) event)
        .getBattleRequestProto();

    MinimumUserProto attackerProto = reqProto.getAttacker();
    MinimumUserProto defenderProto = reqProto.getDefender();
    BattleResult result = reqProto.getBattleResult();

    Timestamp battleTime = new Timestamp(reqProto.getClientTime());

    Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils
        .getEquipmentIdsToEquipment();

    List<FullUserEquipProto> oldDefenderUserEquipsList = reqProto.getDefenderUserEquipsList();

    server.lockPlayers(attackerProto.getUserId(), defenderProto.getUserId());

    try {
      User attacker = UserRetrieveUtils.getUserById(attackerProto.getUserId());
      User defender = UserRetrieveUtils.getUserById(defenderProto.getUserId());

      BattleResponseProto.Builder resBuilder = BattleResponseProto.newBuilder();

      resBuilder.setAttacker(attackerProto);
      resBuilder.setDefender(defenderProto);
      resBuilder.setBattleResult(result);

      BattleResponseEvent resEvent = new BattleResponseEvent(attacker.getId());
      resEvent.setTag(event.getTag());

      UserEquip lostEquip = null;
      int expGained = ControllerConstants.NOT_SET;
      int lostCoins = ControllerConstants.NOT_SET;
      User winner = null;
      User loser = null;

      boolean legitBattle = checkLegitBattle(resBuilder, result, attacker, defender);

      if (legitBattle) {
        if (result == BattleResult.ATTACKER_WIN) {
          winner = attacker;
          loser = defender;
          List<UserEquip> defenderEquips = UserEquipRetrieveUtils
              .getUserEquipsForUser(defender.getId());
          lostEquip = chooseLostEquip(defenderEquips, equipmentIdsToEquipment,
              defender, oldDefenderUserEquipsList);
          if (lostEquip != null) {
            Equipment equip = equipmentIdsToEquipment.get(lostEquip.getEquipId());
            resBuilder.setEquipGained(CreateInfoProtoUtils
                .createFullEquipProtoFromEquip(equip));
          }
        } else if (result == BattleResult.DEFENDER_WIN || result == BattleResult.ATTACKER_FLEE){
          winner = defender;
          loser = attacker;
        }

        Random random = new Random();
        lostCoins = calculateLostCoins(winner, loser, random, (result == BattleResult.ATTACKER_FLEE));
        resBuilder.setCoinsGained(lostCoins);

        if (result == BattleResult.ATTACKER_WIN) {
          expGained = calculateExpGain(loser);
          resBuilder.setExpGained(expGained);
        }
        resBuilder.setStatus(BattleStatus.SUCCESS);
      }
      BattleResponseProto resProto = resBuilder.build();

      resEvent.setBattleResponseProto(resProto);

      server.writeEvent(resEvent);

      if (legitBattle) {
        writeChangesToDB(lostEquip, winner, loser, attacker,
            defender, expGained, lostCoins, battleTime, result==BattleResult.ATTACKER_FLEE);

        UpdateClientUserResponseEvent resEventAttacker = MiscMethods
            .createUpdateClientUserResponseEvent(attacker);
        resEventAttacker.setTag(event.getTag());
        UpdateClientUserResponseEvent resEventDefender = MiscMethods
            .createUpdateClientUserResponseEvent(defender);

        server.writeEvent(resEventAttacker);
        server.writeEvent(resEventDefender);

        if (winner != null && attacker != null && winner == attacker) {
          if (reqProto.hasNeutralCityId() && reqProto.getNeutralCityId() >= 0) {
            server.unlockPlayer(defenderProto.getUserId());
            checkQuestsPostBattle(winner, defender.getType(),
                attackerProto, reqProto.getNeutralCityId(), lostEquip);
          } else if (lostEquip != null) {
            QuestUtils.checkAndSendQuestsCompleteBasic(server, attacker.getId(), attackerProto, null, false);
          }
        }

        if (attacker != null && defender != null){
          server.unlockPlayers(attackerProto.getUserId(), defenderProto.getUserId());
          if (!defender.isFake()) {
            BattleResponseEvent resEvent2 = new BattleResponseEvent(defender.getId());
            resEvent2.setBattleResponseProto(resProto);
            server.writeAPNSNotificationOrEvent(resEvent2);
          }
          int stolenEquipId = (lostEquip == null) ? ControllerConstants.NOT_SET : lostEquip.getEquipId();
          if (!insertUtils.insertBattleHistory(attacker.getId(), defender.getId(), result, battleTime, lostCoins, stolenEquipId, expGained)) {
            log.error("problem with adding battle history into the db for attacker " + attacker.getId() + " and defender " + defender.getId() 
                + " at " + battleTime);
          }
        }
      }
    } catch (Exception e) {
      log.error("exception in BattleController processEvent", e);
    } finally {
      server.unlockPlayers(attackerProto.getUserId(), defenderProto.getUserId());
    }
  }

  private int calculateExpGain(User loser) {
    int expGain = (int)((ControllerConstants.BATTLE__EXP_GAIN_LOWER_BOUND + 
        (ControllerConstants.BATTLE__EXP_GAIN_UPPER_BOUND-ControllerConstants.BATTLE__EXP_GAIN_LOWER_BOUND)
        *Math.random()) * loser.getLevel() * ControllerConstants.BATTLE__EXP_GAIN_MULTIPLIER);
    return Math.max(1, expGain);
  }

  private boolean checkLegitBattle(Builder resBuilder, BattleResult result, User attacker, User defender) {
    if (attacker == null || defender == null) {
      resBuilder.setStatus(BattleStatus.OTHER_FAIL);
      log.error("problem with battle- attacker or defender is null. attacker is " + attacker + " and defender is " + defender);
      return false;
    }
    resBuilder.setStatus(BattleStatus.SUCCESS);
    return true;
  }

  private void checkQuestsPostBattle(User attacker, UserType enemyType,
      MinimumUserProto attackerProto, int cityId, UserEquip lostEquip) {
    boolean goodSide = MiscMethods.checkIfGoodSide(attacker.getType());

    List<UserQuest> inProgressUserQuests = UserQuestRetrieveUtils
        .getIncompleteUserQuestsForUser(attacker.getId());
    if (inProgressUserQuests != null) {
      Map<Integer, List<Integer>> questIdToUserDefeatTypeJobsCompletedForQuestForUser = null;
      Map<Integer, Map<Integer, Integer>> questIdToDefeatTypeJobIdsToNumDefeated = null;

      for (UserQuest userQuest : inProgressUserQuests) {
        boolean questCompletedAndSent = false;
        if (!userQuest.isDefeatTypeJobsComplete()) {
          Quest quest = QuestRetrieveUtils.getQuestForQuestId(userQuest
              .getQuestId());
          if (quest != null) {
            List<Integer> defeatTypeJobsRequired = null;
            if (goodSide) {
              defeatTypeJobsRequired = quest.getDefeatBadGuysJobsRequired();
            } else {
              defeatTypeJobsRequired = quest.getDefeatGoodGuysJobsRequired();
            }
            if (defeatTypeJobsRequired != null) {
              if (questIdToUserDefeatTypeJobsCompletedForQuestForUser == null) {
                questIdToUserDefeatTypeJobsCompletedForQuestForUser = UserQuestsCompletedDefeatTypeJobsRetrieveUtils.getQuestIdToUserDefeatTypeJobsCompletedForQuestForUser(attacker.getId());
              }
              List<Integer> userCompletedDefeatTypeJobsForQuest = questIdToUserDefeatTypeJobsCompletedForQuestForUser.get(quest.getId());
              if (userCompletedDefeatTypeJobsForQuest == null) userCompletedDefeatTypeJobsForQuest = new ArrayList<Integer>();
              List<Integer> defeatTypeJobsRemaining = new ArrayList<Integer>(defeatTypeJobsRequired);
              defeatTypeJobsRemaining.removeAll(userCompletedDefeatTypeJobsForQuest);
              Map<Integer, DefeatTypeJob> remainingDTJMap = DefeatTypeJobRetrieveUtils
                  .getDefeatTypeJobsForDefeatTypeJobIds(defeatTypeJobsRemaining);
              if (remainingDTJMap != null && remainingDTJMap.size() > 0) {
                for (DefeatTypeJob remainingDTJ : remainingDTJMap.values()) {
                  if (remainingDTJ.getCityId() == cityId) {
                    if (remainingDTJ.getEnemyType() == DefeatTypeJobEnemyType.ALL_TYPES_FROM_OPPOSING_SIDE || 
                        enemyType == MiscMethods.getUserTypeFromDefeatTypeJobUserType(remainingDTJ.getEnemyType())) {

                      if (questIdToDefeatTypeJobIdsToNumDefeated == null) {
                        questIdToDefeatTypeJobIdsToNumDefeated = UserQuestsDefeatTypeJobProgressRetrieveUtils.getQuestIdToDefeatTypeJobIdsToNumDefeated(userQuest.getUserId());
                      }
                      Map<Integer, Integer> userJobIdToNumDefeated = questIdToDefeatTypeJobIdsToNumDefeated.get(userQuest.getQuestId()); 
                      int numDefeatedForJob = (userJobIdToNumDefeated != null && userJobIdToNumDefeated.containsKey(remainingDTJ.getId())) ?
                          userJobIdToNumDefeated.get(remainingDTJ.getId()) : 0;

                          if (numDefeatedForJob + 1 == remainingDTJ.getNumEnemiesToDefeat()) {
                            //TODO: note: not SUPER necessary to delete/update them, but they do capture wrong data if complete (the one that completes is not factored in)
                            if (insertUtils.insertCompletedDefeatTypeJobIdForUserQuest(attacker.getId(), remainingDTJ.getId(), quest.getId())) {
                              userCompletedDefeatTypeJobsForQuest.add(remainingDTJ.getId());
                              if (userCompletedDefeatTypeJobsForQuest.containsAll(defeatTypeJobsRequired)) {
                                if (UpdateUtils.updateUserQuestsSetCompleted(attacker.getId(), quest.getId(), false, true)) {
                                  userQuest.setDefeatTypeJobsComplete(true);
                                  questCompletedAndSent = QuestUtils.checkQuestCompleteAndMaybeSendIfJustCompleted(server, quest, userQuest, attackerProto, true, null);
                                } else {
                                  log.error("problem with marking all defeat type jobs in quest " 
                                      + quest.getId() + " completed for a user " + attacker.getId() + " after completing defeat type job with id "
                                      + remainingDTJ.getId());
                                }
                              }
                            } else {
                              log.error("problem with adding defeat type job " + remainingDTJ.getId() + " as a completed job for user " + attacker.getId() 
                                  + " and quest " + quest.getId());
                            }
                          } else {
                            if (!UpdateUtils.incrementUserQuestDefeatTypeJobProgress(attacker.getId(), quest.getId(), remainingDTJ.getId(), 1)) {
                              log.error("problem with incrementing user quest defeat type job progress for user "
                                  + attacker.getId() + ", quest " + quest.getId() + ", defeat type job " + remainingDTJ.getId());
                            }
                          }
                    }
                  }
                }
              }
            }
            if (lostEquip != null && !questCompletedAndSent) {
              QuestUtils.checkQuestCompleteAndMaybeSendIfJustCompleted(server, quest, userQuest, attackerProto, true, null);
            }
          }
        }
      }
    }

  }

  private void writeChangesToDB(UserEquip lostEquip,
      User winner, User loser, User attacker, User defender, int expGained,
      int lostCoins, Timestamp battleTime, boolean isFlee) {
    if (lostEquip != null) {
      if (!loser.isFake() && !UpdateUtils.decrementUserEquip(loser.getId(),
          lostEquip.getEquipId(), lostEquip.getQuantity(), 1)) {
        log.error("problem with decreasing 1 of equip " + lostEquip.getEquipId() + " from user " + loser.getId()
            + " who currently has " + lostEquip.getQuantity() + " of them");
      } else if (lostEquip.getQuantity() == 1) {
        if (!MiscMethods.unequipUserEquipIfEquipped(loser, lostEquip.getEquipId())) {
          log.error("problem with unequipping " + lostEquip.getEquipId() + " from " + loser);
        }
      }
      if (!UpdateUtils.incrementUserEquip(winner.getId(),
          lostEquip.getEquipId(), 1)) {
        log.error("problem with giving user " + winner + " one of " + lostEquip.getEquipId());
      }
    }

    boolean simulateStaminaRefill = (attacker.getStamina() == attacker.getStaminaMax());

    if (winner == attacker) {
      if (!attacker.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(-1,
          expGained, lostCoins, 1, 0, 0, simulateStaminaRefill, false, battleTime)) {
        log.error("problem with updating info for winner/attacker " + attacker.getId() + " in battle at " 
            + battleTime + " vs " + loser.getId());
      }
      if (!defender.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(0,
          0, (defender.isFake()) ? 0 : lostCoins * -1, 0, 1, 0, false, true, battleTime)) {
        log.error("problem with updating info for defender/loser " + defender.getId() + " in battle at " 
            + battleTime + " vs " + winner.getId());
      }
    } else if (winner == defender) {
      if (isFlee) {
        if (!attacker.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(-1,
            0, lostCoins * -1, 0, 1, 1, simulateStaminaRefill, false, battleTime)) {
          log.error("problem with updating info for loser/attacker/flee-er " + attacker.getId() + " in battle at " 
              + battleTime + " vs " + winner.getId());
        }
        if (!defender.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(0,
            0, (defender.isFake()) ? 0 : lostCoins, 1, 0, 0, false, false, battleTime)) {
          log.error("problem with updating info for winner/defender " + defender.getId() + " in battle at " 
              + battleTime + " vs " + loser.getId() + " who fled");
        }
      } else {
        if (!attacker.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(-1,
            0, lostCoins * -1, 0, 1, 0, simulateStaminaRefill, false, battleTime)) {
          log.error("problem with updating info for loser/attacker " + attacker.getId() + " in battle at " 
              + battleTime + " vs " + winner.getId());
        }
        if (!defender.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(0,
            0, (defender.isFake()) ? 0 : lostCoins, 1, 0, 0, false, true, battleTime)) {
          log.error("problem with updating info for winner/defender " + defender.getId() + " in battle at " 
              + battleTime + " vs " + loser.getId());
        }
      }
    }
  }

  private int calculateLostCoins(User winner, User loser, Random random, boolean isFlee) {
    User player = (loser.isFake() && !winner.isFake()) ? winner : loser;

    int lostCoins = (int) Math.rint(Math.min(player.getCoins() * Math.random()
        * ControllerConstants.BATTLE__A, player.getLevel()
        * ControllerConstants.BATTLE__B));
    if (isFlee) {
      return lostCoins/2;
    }
    return lostCoins;
  }

  /*
   * returns null if no item lost this time /* - defender losers lose items
   * (better items have a lower chance of being stolen) to the attacker winner -
   * items with only a diamond price cannot be stolen - only items with
   * min_level below the player's level can be stolen - attackers can't lose
   * items - min level applies for usage, not for holding
   */
  private UserEquip chooseLostEquip(List<UserEquip> defenderEquips,
      Map<Integer, Equipment> equipmentIdsToEquipment, User defender, List<FullUserEquipProto> oldDefenderUserEquipsList) {
    List<UserEquip> potentialLosses = new ArrayList<UserEquip>();
    if (defenderEquips != null) {
      for (UserEquip defenderEquip : defenderEquips) {
        int equipId = defenderEquip.getEquipId();
        Equipment equip = equipmentIdsToEquipment.get(equipId);
        if (equip.getDiamondPrice() == Equipment.NOT_SET
            && equip.getMinLevel() < defender.getLevel()) {
          double rand = Math.random();
          if (rand <= equip.getChanceOfLoss()) {
            potentialLosses.add(defenderEquip);
          }
        }
      }
      if (potentialLosses.size() > 0) {
        UserEquip lostUserEquip = potentialLosses.get((int) Math.rint(Math.random()
            * (potentialLosses.size() - 1)));
        if (oldDefenderUserEquipsList != null) {
          for (FullUserEquipProto oldUserEquip : oldDefenderUserEquipsList) {
            if (oldUserEquip.getEquipId() == lostUserEquip.getEquipId()) {
              return lostUserEquip;
            }
          }
        }
        return null;
      }
    }
    return null;
  }

}
