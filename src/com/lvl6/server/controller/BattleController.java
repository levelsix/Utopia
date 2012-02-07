package com.lvl6.server.controller;

import java.util.ArrayList;
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
import com.lvl6.proto.EventProto.BattleRequestProto.BattleResult;
import com.lvl6.proto.EventProto.BattleResponseProto;
import com.lvl6.proto.EventProto.BattleResponseProto.BattleStatus;
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

    Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils
        .getEquipmentIdsToEquipment();

    server.lockPlayers(attackerProto.getUserId(), defenderProto.getUserId());

    try {
      User attacker = UserRetrieveUtils.getUserById(attackerProto.getUserId());
      User defender = UserRetrieveUtils.getUserById(defenderProto.getUserId());

      BattleResponseProto.Builder resBuilder = BattleResponseProto.newBuilder();

      resBuilder.setAttacker(attackerProto);
      resBuilder.setDefender(defenderProto);

      UserEquip lostEquip = null;
      int expGained = ControllerConstants.NOT_SET;
      int lostCoins = ControllerConstants.NOT_SET;
      User winner = null;
      User loser = null;
      boolean legitBattle = false;

      BattleResponseEvent resEvent = new BattleResponseEvent();
      int[] recipients = { attacker.getId(), defender.getId() };
      ;
      resEvent.setRecipients(recipients);

      if (result == BattleResult.ATTACKER_WIN) {
        winner = attacker;
        loser = defender;
        List<UserEquip> defenderEquips = UserEquipRetrieveUtils
            .getUserEquipsForUser(defender.getId());
        lostEquip = chooseLostEquip(defenderEquips, equipmentIdsToEquipment,
            defender.getLevel());
        if (lostEquip != null) {
          Equipment equip = equipmentIdsToEquipment.get(lostEquip.getEquipId());
          resBuilder.setEquipGained(CreateInfoProtoUtils
              .createFullEquipProtoFromEquip(equip));
        }
      } else {
        winner = defender;
        loser = attacker;
      }

      Random random = new Random();
      lostCoins = calculateLostCoins(loser, random);
      resBuilder.setCoinsGained(lostCoins);
      expGained = ControllerConstants.BATTLE__MIN_EXP_GAIN
          + random.nextInt(ControllerConstants.BATTLE__MAX_EXP_GAIN
              - ControllerConstants.BATTLE__MIN_EXP_GAIN + 1);
      resBuilder.setExpGained(expGained);
      resBuilder.setStatus(BattleStatus.SUCCESS);

      BattleResponseProto resProto = resBuilder.build();

      resEvent.setBattleResponseProto(resProto);

      log.info(resEvent + " is resevent");
      server.writeEvent(resEvent);

      writeChangesToDB(legitBattle, lostEquip, winner, loser, attacker,
          defender, expGained, lostCoins);
      // TODO: should these send new response? or package inside battles?
      // TODO: AchievementCheck.checkBattle();
      // TODO: LevelCheck.checkUser();

      UpdateClientUserResponseEvent resEventAttacker = MiscMethods
          .createUpdateClientUserResponseEvent(attacker);
      UpdateClientUserResponseEvent resEventDefender = MiscMethods
          .createUpdateClientUserResponseEvent(defender);

      server.writeEvent(resEventAttacker);
      server.writeEvent(resEventDefender);

      if (winner != null && attacker != null && winner == attacker
          && reqProto.hasNeutralCityId() && reqProto.getNeutralCityId() > 0) {
        server.unlockPlayer(defenderProto.getUserId());
        checkQuestsPostBattle(winner, defenderProto.getUserType(),
            attackerProto, reqProto.getNeutralCityId());

      }

    } catch (Exception e) {
      log.error("exception in BattleController processEvent", e);
    } finally {
      server
      .unlockPlayers(attackerProto.getUserId(), defenderProto.getUserId());
    }
  }

  private void checkQuestsPostBattle(User attacker, UserType enemyType,
      MinimumUserProto attackerProto, int cityId) {

    boolean goodSide = MiscMethods.checkIfGoodSide(attacker.getType());
    
    List<UserQuest> inProgressUserQuests = UserQuestRetrieveUtils
        .getInProgressUserQuestsForUser(attacker.getId());
    if (inProgressUserQuests != null) {
      for (UserQuest userQuest : inProgressUserQuests) {
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
              List<Integer> userCompletedDefeatTypeJobsForQuest = UserQuestsCompletedDefeatTypeJobsRetrieveUtils
                  .getUserDefeatTypeJobsCompletedForQuest(attacker.getId(), quest.getId());
              List<Integer> defeatTypeJobsRemaining = new ArrayList<Integer>(defeatTypeJobsRequired);
              defeatTypeJobsRemaining.removeAll(userCompletedDefeatTypeJobsForQuest);
              Map<Integer, DefeatTypeJob> remainingDTJMap = DefeatTypeJobRetrieveUtils
                  .getDefeatTypeJobsForDefeatTypeJobIds(defeatTypeJobsRemaining);
              if (remainingDTJMap != null && remainingDTJMap.size() > 0) {
                Map<Integer, Integer> userJobIdToNumDefeated = null;
                for (DefeatTypeJob remainingDTJ : remainingDTJMap.values()) {
                  if (remainingDTJ.getCityId() == cityId && enemyType == remainingDTJ.getEnemyType()) {
                    if (userJobIdToNumDefeated == null) {
                      userJobIdToNumDefeated = UserQuestsDefeatTypeJobProgressRetrieveUtils.getDefeatTypeJobIdsToNumDefeatedForUserQuest(attacker.getId(), quest.getId());
                    }
                    if (userJobIdToNumDefeated.get(remainingDTJ.getId()) != null && 
                        userJobIdToNumDefeated.get(remainingDTJ.getId()) + 1 == remainingDTJ.getNumEnemiesToDefeat()) {
                    //TODO: note: not SUPER necessary to delete/update them, but they do capture wrong data if complete (the one that completes is not factored in)
                      if (InsertUtils.insertCompletedDefeatTypeJobIdForUserQuest(attacker.getId(), remainingDTJ.getId(), quest.getId())) {
                        userCompletedDefeatTypeJobsForQuest.add(remainingDTJ.getId());
                        if (userCompletedDefeatTypeJobsForQuest.containsAll(defeatTypeJobsRequired)) {
                          if (UpdateUtils.updateUserQuestsSetCompleted(attacker.getId(), quest.getId(), false, true)) {
                            userQuest.setDefeatTypeJobsComplete(true);
                            QuestUtils.checkAndSendQuestComplete(server, quest, userQuest, attackerProto);
                          } else {
                            log.error("problem with marking defeat type jobs completed for a user quest");
                          }
                        }
                      } else {
                        log.error("problem with adding defeat type jobs to user's completed tasks for quest");
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

  }

  private void writeChangesToDB(boolean legitBattle, UserEquip lostEquip,
      User winner, User loser, User attacker, User defender, int expGained,
      int lostCoins) {
    if (legitBattle) {
      if (lostEquip != null) {
        if (!UpdateUtils.decrementUserEquip(loser.getId(),
            lostEquip.getEquipId(), lostEquip.getQuantity(), 1)) {
          log.error("problem with decrementUserEquip in battle");
        }
        if (!UpdateUtils.incrementUserEquip(winner.getId(),
            lostEquip.getEquipId(), 1)) {
          log.error("problem with incrementUserEquip in battle");
        }
      }

      if (winner == attacker) {
        attacker.updateRelativeStaminaExperienceCoinsBattleswonBattleslost(-1,
            expGained, lostCoins, 1, 0);
        defender.updateRelativeStaminaExperienceCoinsBattleswonBattleslost(0,
            0, lostCoins * -1, 0, 1);
      } else if (winner == defender) {
        attacker.updateRelativeStaminaExperienceCoinsBattleswonBattleslost(-1,
            0, lostCoins * -1, 0, 1);
        defender.updateRelativeStaminaExperienceCoinsBattleswonBattleslost(0,
            0, lostCoins, 1, 0);
      }

      /*
       * TODO: write to battle_history id, attackerId, defenderId, winnerId,
       * winnerHealthLoss, loserHealthLoss, coinTransfer, lostEquipId, expGain,
       * timestamp
       */
    }

  }

  private int calculateLostCoins(User loser, Random random) {
    return (int) Math.rint(Math.min(loser.getCoins() * (Math.random() + 1)
        / ControllerConstants.BATTLE__A, loser.getLevel()
        * ControllerConstants.BATTLE__B));
  }

  /*
   * returns null if no item lost this time /* - defender losers lose items
   * (better items have a lower chance of being stolen) to the attacker winner -
   * items with only a diamond price cannot be stolen - only items with
   * min_level below the player's level can be stolen - attackers can't lose
   * items - min level applies for usage, not for holding
   */
  private UserEquip chooseLostEquip(List<UserEquip> defenderEquips,
      Map<Integer, Equipment> equipmentIdsToEquipment, int level) {
    List<UserEquip> potentialLosses = new ArrayList<UserEquip>();
    if (defenderEquips != null) {
      for (UserEquip defenderEquip : defenderEquips) {
        Equipment equip = equipmentIdsToEquipment.get(defenderEquip
            .getEquipId());
        if (equip.getDiamondPrice() == Equipment.NOT_SET
            && equip.getMinLevel() < level) {
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
