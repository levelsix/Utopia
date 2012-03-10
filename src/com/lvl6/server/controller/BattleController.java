package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class BattleController extends EventController {

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
  protected void processRequestEvent(RequestEvent event) {
    BattleRequestProto reqProto = ((BattleRequestEvent) event)
        .getBattleRequestProto();

    MinimumUserProto attackerProto = reqProto.getAttacker();
    MinimumUserProto defenderProto = reqProto.getDefender();
    BattleResult result = reqProto.getBattleResult();

    Timestamp battleTime = new Timestamp(reqProto.getClientTime());

    Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils
        .getEquipmentIdsToEquipment();

    server.lockPlayers(attackerProto.getUserId(), defenderProto.getUserId());

    try {
      User attacker = UserRetrieveUtils.getUserById(attackerProto.getUserId());
      User defender = UserRetrieveUtils.getUserById(defenderProto.getUserId());

      BattleResponseProto.Builder resBuilder = BattleResponseProto.newBuilder();

      resBuilder.setAttacker(attackerProto);
      resBuilder.setDefender(defenderProto);
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
              defender);
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
        lostCoins = calculateLostCoins(loser, random, (result == BattleResult.ATTACKER_FLEE));
        resBuilder.setCoinsGained(lostCoins);

        if (result == BattleResult.ATTACKER_WIN) {
          expGained = ControllerConstants.BATTLE__MIN_EXP_GAIN
              + random.nextInt(ControllerConstants.BATTLE__MAX_EXP_GAIN
                  - ControllerConstants.BATTLE__MIN_EXP_GAIN + 1);
          resBuilder.setExpGained(expGained);
        }
        resBuilder.setStatus(BattleStatus.SUCCESS);
      }
      BattleResponseProto resProto = resBuilder.build();

      resEvent.setBattleResponseProto(resProto);

      log.info(resEvent + " is resevent");
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
          if (reqProto.hasNeutralCityId() && reqProto.getNeutralCityId() > 0) {
            server.unlockPlayer(defenderProto.getUserId());
            checkQuestsPostBattle(winner, defenderProto.getUserType(),
                attackerProto, reqProto.getNeutralCityId(), lostEquip);
          } else if (lostEquip != null) {
            QuestUtils.checkAndSendQuestsCompleteBasic(server, attacker.getId(), attackerProto, null, null, null, lostEquip.getEquipId(), 1);
          }
        }

        if (attacker != null && defender != null){
          server.unlockPlayers(attackerProto.getUserId(), defenderProto.getUserId());
          if (result == BattleResult.ATTACKER_WIN && !defender.isFake()) {
            BattleResponseEvent resEvent2 = new BattleResponseEvent(defender.getId());
            resEvent2.setBattleResponseProto(resProto);
            server.writeAPNSNotificationOrEvent(resEvent2);
          }
          int stolenEquipId = (lostEquip == null) ? ControllerConstants.NOT_SET : lostEquip.getEquipId();
          if (!InsertUtils.insertBattleHistory(attacker.getId(), defender.getId(), result, battleTime, lostCoins, stolenEquipId, expGained)) {
            log.error("problem with adding battle history into the db");
          }
        }
      }
    } catch (Exception e) {
      log.error("exception in BattleController processEvent", e);
    } finally {
      server
      .unlockPlayers(attackerProto.getUserId(), defenderProto.getUserId());
    }
  }

  private boolean checkLegitBattle(Builder resBuilder, BattleResult result, User attacker, User defender) {
    if (attacker == null || defender == null) {
      resBuilder.setStatus(BattleStatus.OTHER_FAIL);
      return false;
    }
    resBuilder.setStatus(BattleStatus.SUCCESS);
    return true;
  }

  private void checkQuestsPostBattle(User attacker, UserType enemyType,
      MinimumUserProto attackerProto, int cityId, UserEquip lostEquip) {
    boolean goodSide = MiscMethods.checkIfGoodSide(attacker.getType());

    List<UserQuest> inProgressUserQuests = UserQuestRetrieveUtils
        .getInProgressUserQuestsForUser(attacker.getId());
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
                  if (remainingDTJ.getCityId() == cityId && enemyType == remainingDTJ.getEnemyType()) {
                    if (questIdToDefeatTypeJobIdsToNumDefeated == null) {
                      questIdToDefeatTypeJobIdsToNumDefeated = UserQuestsDefeatTypeJobProgressRetrieveUtils.getQuestIdToDefeatTypeJobIdsToNumDefeated(userQuest.getUserId());
                    }
                    Map<Integer, Integer> userJobIdToNumDefeated = questIdToDefeatTypeJobIdsToNumDefeated.get(userQuest.getQuestId()); 
                    if (userJobIdToNumDefeated == null) userJobIdToNumDefeated = new HashMap<Integer, Integer>();
                    if (userJobIdToNumDefeated.get(remainingDTJ.getId()) != null && 
                        userJobIdToNumDefeated.get(remainingDTJ.getId()) + 1 == remainingDTJ.getNumEnemiesToDefeat()) {
                      //TODO: note: not SUPER necessary to delete/update them, but they do capture wrong data if complete (the one that completes is not factored in)
                      if (InsertUtils.insertCompletedDefeatTypeJobIdForUserQuest(attacker.getId(), remainingDTJ.getId(), quest.getId())) {
                        userCompletedDefeatTypeJobsForQuest.add(remainingDTJ.getId());
                        if (userCompletedDefeatTypeJobsForQuest.containsAll(defeatTypeJobsRequired)) {
                          if (UpdateUtils.updateUserQuestsSetCompleted(attacker.getId(), quest.getId(), false, true)) {
                            userQuest.setDefeatTypeJobsComplete(true);
                            questCompletedAndSent = QuestUtils.checkQuestCompleteAndMaybeSend(server, quest, userQuest, attackerProto, true, null, null, null, null, null);
                          } else {
                            log.error("problem with marking defeat type jobs completed for a user quest");
                          }
                        }
                      } else {
                        log.error("problem with adding defeat type jobs to user's completed tasks for quest");
                      }
                    } else {
                      if (!UpdateUtils.incrementUserQuestDefeatTypeJobProgress(attacker.getId(), quest.getId(), remainingDTJ.getId(), 1)) {
                        log.error("problem with updating user quest defeat type job progress");
                      }
                    }
                  }
                }
              }
            }
            if (lostEquip != null && !questCompletedAndSent) {
              QuestUtils.checkQuestCompleteAndMaybeSend(server, quest, userQuest, attackerProto, true,
                  null, null, null, lostEquip.getEquipId(), 1);
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
      if (!UpdateUtils.decrementUserEquip(loser.getId(),
          lostEquip.getEquipId(), lostEquip.getQuantity(), 1)) {
        log.error("problem with decrementUserEquip in battle");
      }
      if (lostEquip.getQuantity() == 1) {
        MiscMethods.unequipUserEquip(loser, lostEquip.getEquipId());
      }
      if (!UpdateUtils.incrementUserEquip(winner.getId(),
          lostEquip.getEquipId(), 1)) {
        log.error("problem with incrementUserEquip in battle");
      }
    }

    boolean simulateStaminaRefill = (attacker.getStamina() == attacker.getStaminaMax());
    if (winner == attacker) {
      attacker.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(-1,
          expGained, lostCoins, 1, 0, 0, simulateStaminaRefill, false, battleTime);
      defender.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(0,
          0, lostCoins * -1, 0, 1, 0, false, true, battleTime);
    } else if (winner == defender) {
      if (isFlee) {
        attacker.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(-1,
            0, lostCoins * -1, 0, 1, 1, simulateStaminaRefill, false, battleTime);
        defender.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(0,
            0, lostCoins, 1, 0, 0, false, false, battleTime);
      } else {
        attacker.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(-1,
            0, lostCoins * -1, 0, 1, 0, simulateStaminaRefill, false, battleTime);
        defender.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(0,
            0, lostCoins, 1, 0, 0, false, true, battleTime);        
      }
    }
  }

  private int calculateLostCoins(User loser, Random random, boolean isFlee) {
    int lostCoins = (int) Math.rint(Math.min(loser.getCoins() * (Math.random() + 1)
        / ControllerConstants.BATTLE__A, loser.getLevel()
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
      Map<Integer, Equipment> equipmentIdsToEquipment, User defender) {
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
        return potentialLosses.get((int) Math.rint(Math.random()
            * (potentialLosses.size() - 1)));
      }
    }
    return null;
  }

}
