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
import com.lvl6.info.City;
import com.lvl6.info.Equipment;
import com.lvl6.info.Quest;
import com.lvl6.info.Task;
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
import com.lvl6.retrieveutils.UserQuestsDefeatTypeJobProgressRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.DefeatTypeJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.InsertUtils;
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
      User attacker = RetrieveUtils.userRetrieveUtils().getUserById(attackerProto.getUserId());
      User defender = RetrieveUtils.userRetrieveUtils().getUserById(defenderProto.getUserId());

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

      boolean stolenEquipIsLastOne = false;

      if (legitBattle) {
        if (result == BattleResult.ATTACKER_WIN) {
          winner = attacker;
          loser = defender;
          List<UserEquip> defenderEquips = RetrieveUtils.userEquipRetrieveUtils().getUserEquipsForUser(defender.getId());
          lostEquip = chooseLostEquip(defenderEquips, equipmentIdsToEquipment,
              defender, oldDefenderUserEquipsList);
          if (lostEquip != null) {
            lostEquip = setLostEquip(resBuilder, lostEquip, winner, loser);
          }
        } else if (result == BattleResult.DEFENDER_WIN || result == BattleResult.ATTACKER_FLEE){
          winner = defender;
          loser = attacker;
        }

        Random random = new Random();
        lostCoins = calculateLostCoins(loser, random, (result == BattleResult.ATTACKER_FLEE));
        resBuilder.setCoinsGained(lostCoins);

        if (result == BattleResult.ATTACKER_WIN) {
          expGained = calculateExpGain(winner, loser);
          resBuilder.setExpGained(expGained);
        }
        resBuilder.setStatus(BattleStatus.SUCCESS);
      }
      BattleResponseProto resProto = resBuilder.build();

      resEvent.setBattleResponseProto(resProto);

      server.writeEvent(resEvent);

      if (legitBattle) {
        writeChangesToDB(stolenEquipIsLastOne, winner, loser, attacker,
            defender, expGained, lostCoins, battleTime, result==BattleResult.ATTACKER_FLEE);

        UpdateClientUserResponseEvent resEventAttacker = MiscMethods
            .createUpdateClientUserResponseEvent(attacker);
        resEventAttacker.setTag(event.getTag());
        UpdateClientUserResponseEvent resEventDefender = MiscMethods
            .createUpdateClientUserResponseEvent(defender);

        server.writeEvent(resEventAttacker);
        server.writeEvent(resEventDefender);

        if (attacker.getId() == 13756) {
          log.info("ricktest- neutral city id is " + reqProto.getNeutralCityId());
        }
        if (winner != null && attacker != null && winner == attacker) {
          if (attacker.getId() == 13756) {
            log.info("ricktest- a");
          }
          if (reqProto.hasNeutralCityId() && reqProto.getNeutralCityId() >= 0) {
            if (attacker.getId() == 13756) {
              log.info("ricktest- b");
            }
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
          int stolenEquipLevel = (lostEquip == null) ? ControllerConstants.NOT_SET : lostEquip.getLevel();

          if (!insertUtils.insertBattleHistory(attacker.getId(), defender.getId(), result, battleTime, lostCoins, stolenEquipId, expGained, stolenEquipLevel)) {
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

  private UserEquip setLostEquip(BattleResponseProto.Builder resBuilder,
      UserEquip lostEquip, User winner, User loser) {
    if (!loser.isFake()) { //real, unequip and transfer
      if (!MiscMethods.unequipUserEquipIfEquipped(loser, lostEquip)) {
        log.error("problem with unequipping userequip" + lostEquip.getId());
        lostEquip = null;
      } else {
        if (!(UpdateUtils.get().updateUserEquipOwner(lostEquip.getId(), winner.getId()))) {
          log.error("problem with giving equip " + lostEquip.getEquipId() + " to user " + winner.getId());
          lostEquip = null;
        } else {
          resBuilder.setUserEquipGained(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
              new UserEquip(lostEquip.getId(), winner.getId(), lostEquip.getEquipId(), lostEquip.getLevel())));
        }
      }
    } else {  //fake, just insert
      int userEquipId = InsertUtils.get().insertUserEquip(winner.getId(), lostEquip.getEquipId(), lostEquip.getLevel());
      if (userEquipId < 0) {
        log.error("problem with giving 1 of equip " + lostEquip.getEquipId() + " to winner " + winner.getId());
        lostEquip = null;
      } else {
        resBuilder.setUserEquipGained(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
            new UserEquip(lostEquip.getId(), winner.getId(), lostEquip.getEquipId(), lostEquip.getLevel())));
        resBuilder.setEquipGained(CreateInfoProtoUtils.createFullEquipProtoFromEquip(EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(lostEquip.getEquipId())));
      }
    }
    return lostEquip;
  }

  private int calculateExpGain(User winner, User loser) {
	  int baseExp = (int) (loser.getLevel()*ControllerConstants.BATTLE__EXP_BASE_MULTIPLIER);
	  int levelDifference = (int) (baseExp * ((loser.getLevel() - winner.getLevel()) 
			  * ControllerConstants.BATTLE__EXP_LEVEL_DIFF_WEIGHT));
	  int randomness = (int)((Math.random() + 1.0) * (loser.getLevel() / 10));
	  
	  int expGain = Math.max(ControllerConstants.BATTLE__EXP_MIN, baseExp + levelDifference + randomness);
	  return expGain;
  }

  private boolean checkLegitBattle(Builder resBuilder, BattleResult result, User attacker, User defender) {
    if (attacker == null || defender == null || attacker.getStamina() <= 0) {
      resBuilder.setStatus(BattleStatus.OTHER_FAIL);
      log.error("problem with battle- attacker or defender is null. attacker is " + attacker + " and defender is " + defender);
      return false;
    }
    if (MiscMethods.checkIfGoodSide(attacker.getType()) == MiscMethods.checkIfGoodSide(defender.getType())) {
      resBuilder.setStatus(BattleStatus.SAME_SIDE);
      log.error("problem with battle- attacker and defender same side. attacker=" + attacker + ", defender=" + defender);
      return false;
    }
    resBuilder.setStatus(BattleStatus.SUCCESS);
    return true;
  }

  private void checkQuestsPostBattle(User attacker, UserType enemyType,
      MinimumUserProto attackerProto, int cityId, UserEquip lostEquip) {
    boolean goodSide = MiscMethods.checkIfGoodSide(attacker.getType());

    if (attacker.getId() == 13756) {
      log.info("ricktest- c");
    }
    List<UserQuest> inProgressUserQuests = RetrieveUtils.userQuestRetrieveUtils()
        .getIncompleteUserQuestsForUser(attacker.getId());
    if (inProgressUserQuests != null) {
      Map<Integer, List<Integer>> questIdToUserDefeatTypeJobsCompletedForQuestForUser = null;
      Map<Integer, Map<Integer, Integer>> questIdToDefeatTypeJobIdsToNumDefeated = null;

      if (attacker.getId() == 13756) {
        log.info("ricktest- d. inProgressUserQuests=" + inProgressUserQuests);
      }
      
      for (UserQuest userQuest : inProgressUserQuests) {
        boolean questCompletedAndSent = false;
        if (!userQuest.isDefeatTypeJobsComplete()) {
          Quest quest = QuestRetrieveUtils.getQuestForQuestId(userQuest
              .getQuestId());
          if (attacker.getId() == 13756) {
            log.info("ricktest- e. quest = " + quest);
          }
          if (quest != null) {
            List<Integer> defeatTypeJobsRequired = null;
            if (goodSide) {
              defeatTypeJobsRequired = quest.getDefeatBadGuysJobsRequired();
            } else {
              defeatTypeJobsRequired = quest.getDefeatGoodGuysJobsRequired();
            }
            if (attacker.getId() == 13756) {
              log.info("ricktest- f. defeat jobs required = " + defeatTypeJobsRequired);
            }

            if (defeatTypeJobsRequired != null) {
              if (questIdToUserDefeatTypeJobsCompletedForQuestForUser == null) {
                questIdToUserDefeatTypeJobsCompletedForQuestForUser = RetrieveUtils.userQuestsCompletedDefeatTypeJobsRetrieveUtils().getQuestIdToUserDefeatTypeJobsCompletedForQuestForUser(attacker.getId());
                if (attacker.getId() == 13756) {
                  log.info("ricktest- g. questIdToUserDefeatTypeJobsCompletedForQuestForUser = " + questIdToUserDefeatTypeJobsCompletedForQuestForUser);
                }
              }
              List<Integer> userCompletedDefeatTypeJobsForQuest = questIdToUserDefeatTypeJobsCompletedForQuestForUser.get(quest.getId());
              if (userCompletedDefeatTypeJobsForQuest == null) userCompletedDefeatTypeJobsForQuest = new ArrayList<Integer>();
              
              if (attacker.getId() == 13756) {
                log.info("ricktest- h. userCompletedDefeatTypeJobsForQuest = " + userCompletedDefeatTypeJobsForQuest);
              }

              List<Integer> defeatTypeJobsRemaining = new ArrayList<Integer>(defeatTypeJobsRequired);
              defeatTypeJobsRemaining.removeAll(userCompletedDefeatTypeJobsForQuest);
              
              if (attacker.getId() == 13756) {
                log.info("ricktest- i. defeatTypeJobsRemaining = " + defeatTypeJobsRemaining);
              }

              Map<Integer, DefeatTypeJob> remainingDTJMap = DefeatTypeJobRetrieveUtils
                  .getDefeatTypeJobsForDefeatTypeJobIds(defeatTypeJobsRemaining);
              if (remainingDTJMap != null && remainingDTJMap.size() > 0) {
                for (DefeatTypeJob remainingDTJ : remainingDTJMap.values()) {
                  
                  if (attacker.getId() == 13756) {
                    log.info("ricktest- j. remainingDTJ = " + remainingDTJ);
                  }
                  
                  if (remainingDTJ.getCityId() == cityId) {
                    if (attacker.getId() == 13756) {
                      log.info("ricktest- k. cityId = " + cityId + ", enemyType=" + enemyType);
                    }
                    
                    
                    if (remainingDTJ.getEnemyType() == DefeatTypeJobEnemyType.ALL_TYPES_FROM_OPPOSING_SIDE || 
                        enemyType == MiscMethods.getUserTypeFromDefeatTypeJobUserType(remainingDTJ.getEnemyType())) {

                      if (questIdToDefeatTypeJobIdsToNumDefeated == null) {
                        questIdToDefeatTypeJobIdsToNumDefeated = UserQuestsDefeatTypeJobProgressRetrieveUtils.getQuestIdToDefeatTypeJobIdsToNumDefeated(userQuest.getUserId());
                      }
                      if (attacker.getId() == 13756) {
                        log.info("ricktest- l. questIdToDefeatTypeJobIdsToNumDefeated = " + questIdToDefeatTypeJobIdsToNumDefeated);
                      }

                      Map<Integer, Integer> userJobIdToNumDefeated = questIdToDefeatTypeJobIdsToNumDefeated.get(userQuest.getQuestId()); 
                      
                      if (attacker.getId() == 13756) {
                        log.info("ricktest- m. userJobIdToNumDefeated = " + userJobIdToNumDefeated);
                      }

                      int numDefeatedForJob = (userJobIdToNumDefeated != null && userJobIdToNumDefeated.containsKey(remainingDTJ.getId())) ?
                          userJobIdToNumDefeated.get(remainingDTJ.getId()) : 0;
                          
                          if (attacker.getId() == 13756) {
                            log.info("ricktest- n. numDefeatedForJob = " + numDefeatedForJob);
                          }

                          if (numDefeatedForJob + 1 >= remainingDTJ.getNumEnemiesToDefeat()) {
                            //TODO: note: not SUPER necessary to delete/update them, but they do capture wrong data if complete (the one that completes is not factored in)
                            if (insertUtils.insertCompletedDefeatTypeJobIdForUserQuest(attacker.getId(), remainingDTJ.getId(), quest.getId())) {
                              userCompletedDefeatTypeJobsForQuest.add(remainingDTJ.getId());
                              if (userCompletedDefeatTypeJobsForQuest.containsAll(defeatTypeJobsRequired)) {
                                if (UpdateUtils.get().updateUserQuestsSetCompleted(attacker.getId(), quest.getId(), false, true)) {
                                  userQuest.setDefeatTypeJobsComplete(true);
                                  questCompletedAndSent = QuestUtils.checkQuestCompleteAndMaybeSendIfJustCompleted(server, quest, userQuest, attackerProto, true, null);
                                } else {
                                  log.error("problem with marking all defeat type jobs in quest " 
                                      + quest.getId() + " completed for a user " + attacker.getId() + " after completing defeat type job with id "
                                      + remainingDTJ.getId());
                                }
                              }
                            } else {
                              log.warn("problem with adding defeat type job " + remainingDTJ.getId() + " as a completed job for user " + attacker.getId() 
                                  + " and quest " + quest.getId());
                            }
                          } else {
                            if (attacker.getId() == 13756) {
                              log.info("ricktest- o. numDefeatedForJob = " + numDefeatedForJob);
                            }
                            if (!UpdateUtils.get().incrementUserQuestDefeatTypeJobProgress(attacker.getId(), quest.getId(), remainingDTJ.getId(), 1)) {
                              if (attacker.getId() == 13756) {
                                log.info("ricktest- p. numDefeatedForJob = " + numDefeatedForJob);
                              }
                              log.error("problem with incrementing user quest defeat type job progress for user "
                                  + attacker.getId() + ", quest " + quest.getId() + ", defeat type job " + remainingDTJ.getId());
                            } else {
                              if (attacker.getId() == 13756) {
                                log.info("ricktest- q. numDefeatedForJob = " + numDefeatedForJob);
                              }
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

  private void writeChangesToDB(boolean stolenEquipIsLastOne, User winner, User loser, User attacker, User defender, int expGained,
      int lostCoins, Timestamp battleTime, boolean isFlee) {

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

  private int calculateLostCoins(User loser, Random random, boolean isFlee) {
    if (loser.isFake()) {
      if (Math.random() < ControllerConstants.BATTLE__CHANCE_OF_ZERO_GAIN_FOR_SILVER) {
        return 0;
      }
      return (int)(Math.random() * loser.getLevel() * ControllerConstants.BATTLE__FAKE_PLAYER_COIN_GAIN_MULTIPLIER);
    }
    
    int lostCoins = (int) Math.rint(Math.min(loser.getCoins() * Math.random()
        * ControllerConstants.BATTLE__A, loser.getLevel()
        * ControllerConstants.BATTLE__B));
    if (lostCoins < ControllerConstants.BATTLE__MIN_COINS_FROM_WIN && 
        ControllerConstants.BATTLE__MIN_COINS_FROM_WIN<=loser.getCoins()) {
      lostCoins = ControllerConstants.BATTLE__MIN_COINS_FROM_WIN;
    }
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
    if (Math.random() > ControllerConstants.BATTLE__CHANCE_OF_EQUIP_LOOT_INITIAL_WALL) {
      return null;
    }
    
    List<UserEquip> potentialLosses = new ArrayList<UserEquip>();
    if (defenderEquips != null) {
      for (UserEquip defenderEquip : defenderEquips) {
        if (defenderEquip.getId() == defender.getWeaponEquippedUserEquipId() || defenderEquip.getId() == defender.getArmorEquippedUserEquipId()
            || defenderEquip.getId() == defender.getAmuletEquippedUserEquipId()) {
          continue;
        }

        int equipId = defenderEquip.getEquipId();
        Equipment equip = equipmentIdsToEquipment.get(equipId);
        if (equip.getDiamondPrice() == Equipment.NOT_SET
            && equip.getMinLevel() < defender.getLevel()) {
          double rand = Math.random();
          if (rand <= equip.getChanceOfLoss() / defenderEquip.getLevel()) {
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
              List<UserEquip> userEquipsWithEquipId = new ArrayList<UserEquip>();
              for (UserEquip defenderEquip : defenderEquips) {
                if (defenderEquip.getEquipId() == lostUserEquip.getEquipId()) userEquipsWithEquipId.add(defenderEquip);
              }
              return MiscMethods.chooseUserEquipWithEquipIdPreferrablyNonEquippedIgnoreLevel(defender, userEquipsWithEquipId);
            }
          }
        }
        return null;
      }
    }
    return null;
  }

}
