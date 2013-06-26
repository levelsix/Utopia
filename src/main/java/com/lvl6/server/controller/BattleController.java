package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BattleRequestEvent;
import com.lvl6.events.response.BattleResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.ClanTower;
import com.lvl6.info.Equipment;
import com.lvl6.info.LeaderboardEvent;
import com.lvl6.info.LockBoxEvent;
import com.lvl6.info.Quest;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserLeaderboardEvent;
import com.lvl6.info.UserQuest;
import com.lvl6.info.jobs.DefeatTypeJob;
import com.lvl6.leaderboards.LeaderBoardUtil;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.BattleRequestProto;
import com.lvl6.proto.EventProto.BattleResponseProto;
import com.lvl6.proto.EventProto.BattleResponseProto.BattleStatus;
import com.lvl6.proto.EventProto.BattleResponseProto.Builder;
import com.lvl6.proto.EventProto.ChangedClanTowerResponseProto.ReasonForClanTowerChange;
import com.lvl6.proto.InfoProto.BattleResult;
import com.lvl6.proto.InfoProto.DefeatTypeJobProto.DefeatTypeJobEnemyType;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.proto.InfoProto.FullUserEquipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.ClanTowerRetrieveUtils;
import com.lvl6.retrieveutils.UserLeaderboardEventRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsDefeatTypeJobProgressRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.DefeatTypeJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LeaderboardEventRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LockBoxEventRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class BattleController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  @Autowired
  protected InsertUtil insertUtils;

  public void setInsertUtils(InsertUtil insertUtils) {
    this.insertUtils = insertUtils;
  }

  @Autowired
  protected LeaderBoardUtil leaderUtil;
  public void setLeaderUtil(LeaderBoardUtil leaderUtil) {
    this.leaderUtil = leaderUtil;
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

    boolean isTutorialBattle = reqProto.getIsTutorialBattle();
    
    if( server.lockPlayers(attackerProto.getUserId(), defenderProto.getUserId(), this.getClass().getSimpleName())) {

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
        int lockBoxEventId = ControllerConstants.NOT_SET;
        User winner = null;
        User loser = null;

        boolean legitBattle = checkLegitBattle(resBuilder, result, attacker, defender);

        boolean stolenEquipIsLastOne = false;

        int attackerPreviousSilver = 0;
        int defenderPreviousSilver = 0;
        
        if (legitBattle) {
          attackerPreviousSilver = attacker.getCoins() + attacker.getVaultBalance();
          defenderPreviousSilver = defender.getCoins() + defender.getVaultBalance();
          if (result == BattleResult.ATTACKER_WIN) {
            winner = attacker;
            loser = defender;
            List<UserEquip> defenderEquips = RetrieveUtils.userEquipRetrieveUtils().getUserEquipsForUser(defender.getId());
            lostEquip = chooseLostEquip(defenderEquips, equipmentIdsToEquipment,
                defender, oldDefenderUserEquipsList);
            if (lostEquip != null) {
              lostEquip = setLostEquip(resBuilder, lostEquip, winner, loser, battleTime);
            }
          } else if (result == BattleResult.DEFENDER_WIN || result == BattleResult.ATTACKER_FLEE){
            winner = defender;
            loser = attacker;
          }

          Random random = new Random();
          lostCoins = calculateLostCoins(loser, random, (result == BattleResult.ATTACKER_FLEE));
          resBuilder.setCoinsGained(lostCoins);

          lockBoxEventId = checkIfUserAcquiresLockBox(attacker, result, battleTime);
          if (lockBoxEventId != ControllerConstants.NOT_SET) {
            resBuilder.setEventIdOfLockBoxGained(lockBoxEventId);
          }

          resBuilder.setShouldGiveKiipReward(checkIfUserGetsKiipReward(result));

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
          Map<String, Integer> attackerCurrencyChange = new HashMap<String, Integer>();
          Map<String, Integer> defenderCurrencyChange = new HashMap<String, Integer>();
          boolean attackerFled = result==BattleResult.ATTACKER_FLEE;
          writeChangesToDB(stolenEquipIsLastOne, winner, loser, attacker,
              defender, expGained, lostCoins, battleTime, attackerFled,
              lockBoxEventId, attackerCurrencyChange, defenderCurrencyChange);

          //clan towers
          log.debug("BattleController... locking all clan towers");
          if (server.lockClanTowersTable()) {
        	  try {
        		  writeChangesToDBForClanTowers(winner, loser, attacker, defender);
        	  }catch(Exception e) {
        		  log.error("Failed to write clanTower changes to DB", e);
        		  throw e;
        	  }finally {
        		  server.unlockClanTowersTable();		  
        	  }
          }

          //user leaderboard event stuff
          leaderBoardEventStuff(winner, loser, attacker, defender, attackerFled);

          UpdateClientUserResponseEvent resEventAttacker = MiscMethods
              .createUpdateClientUserResponseEventAndUpdateLeaderboard(attacker);
          resEventAttacker.setTag(event.getTag());
          UpdateClientUserResponseEvent resEventDefender = MiscMethods
              .createUpdateClientUserResponseEventAndUpdateLeaderboard(defender);

          server.writeEvent(resEventAttacker);
          server.writeEvent(resEventDefender);

          if (winner != null && attacker != null && winner == attacker) {
            if (reqProto.hasNeutralCityId() && reqProto.getNeutralCityId() >= 0) {
              //server.unlockPlayer(defenderProto.getUserId());
              checkQuestsPostBattle(winner, defender.getType(),
                  attackerProto, reqProto.getNeutralCityId(), lostEquip);
            } else if (lostEquip != null) {
              QuestUtils.checkAndSendQuestsCompleteBasic(server, attacker.getId(), attackerProto, null, false);
            }
          }

          if (attacker != null && defender != null){
            //server.unlockPlayers(attackerProto.getUserId(), defenderProto.getUserId());
            if (!defender.isFake()) {
              BattleResponseEvent resEvent2 = new BattleResponseEvent(defender.getId());
              resEvent2.setBattleResponseProto(resProto);
              server.writeAPNSNotificationOrEvent(resEvent2);
            }
            int stolenEquipId = (lostEquip == null) ? ControllerConstants.NOT_SET : lostEquip.getEquipId();
            int stolenEquipLevel = (lostEquip == null) ? ControllerConstants.NOT_SET : lostEquip.getLevel();
            
            //don't record the loss forced upon players
            log.info("!!!!!!!!!!!!!!isTutorialBattle=" + isTutorialBattle);
            if (!isTutorialBattle) {
              //since real/nontutorial, battle record it
              if (!insertUtils.insertBattleHistory(attacker.getId(), defender.getId(), result, battleTime, lostCoins, stolenEquipId, expGained, stolenEquipLevel)) {
                log.error("problem with adding battle history into the db for attacker " + attacker.getId() + " and defender " + defender.getId() 
                    + " at " + battleTime);
              }
            }
            writeToUserCurrencyHistory(winner, attacker, defender, battleTime, attackerCurrencyChange, 
                defenderCurrencyChange, attackerPreviousSilver, defenderPreviousSilver);
          }
        }
      } catch (Exception e) {
        log.error("exception in BattleController processEvent", e);
      } finally {
    	  server.unlockPlayers(attackerProto.getUserId(), defenderProto.getUserId(), this.getClass().getSimpleName());
      }
    }else {
      log.warn("Failed to obtain lock in BattleController processEvent");
    }
  }

  private UserEquip setLostEquip(BattleResponseProto.Builder resBuilder,
      UserEquip lostEquip, User winner, User loser, Timestamp battleTime) {
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
              new UserEquip(lostEquip.getId(), winner.getId(), lostEquip.getEquipId(), lostEquip.getLevel(), 0)));
          resBuilder.setEquipGained(CreateInfoProtoUtils.createFullEquipProtoFromEquip(
              EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(lostEquip.getEquipId())));
        }
      }
    } else {  //fake, just insert
      int userEquipId = InsertUtils.get().insertUserEquip(winner.getId(), lostEquip.getEquipId(), lostEquip.getLevel(),
          ControllerConstants.DEFAULT_USER_EQUIP_ENHANCEMENT_PERCENT, battleTime);
      if (userEquipId < 0) {
        log.error("problem with giving 1 of equip " + lostEquip.getEquipId() + " to winner " + winner.getId());
        lostEquip = null;
      } else {
        resBuilder.setUserEquipGained(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
            new UserEquip(lostEquip.getId(), winner.getId(), lostEquip.getEquipId(), lostEquip.getLevel(), 0)));
        resBuilder.setEquipGained(CreateInfoProtoUtils.createFullEquipProtoFromEquip(
            EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(lostEquip.getEquipId())));
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
    if (defender.isHasActiveShield()) {
      resBuilder.setStatus(BattleStatus.OPPONENT_HAS_ACTIVE_SHIELD);
      log.error("user error: user trying to attack a defender who has an active" +
      		" shield. attacker=" + attacker + "\t defender=" + defender);
      return false;
    }
    resBuilder.setStatus(BattleStatus.SUCCESS);
    return true;
  }

  private void checkQuestsPostBattle(User attacker, UserType enemyType,
      MinimumUserProto attackerProto, int cityId, UserEquip lostEquip) {
    boolean goodSide = MiscMethods.checkIfGoodSide(attacker.getType());

    List<UserQuest> inProgressUserQuests = RetrieveUtils.userQuestRetrieveUtils()
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
                questIdToUserDefeatTypeJobsCompletedForQuestForUser = RetrieveUtils.userQuestsCompletedDefeatTypeJobsRetrieveUtils().getQuestIdToUserDefeatTypeJobsCompletedForQuestForUser(attacker.getId());
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
                            if (!UpdateUtils.get().incrementUserQuestDefeatTypeJobProgress(attacker.getId(), quest.getId(), remainingDTJ.getId(), 1)) {
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

  private void writeChangesToDB(boolean stolenEquipIsLastOne, User winner, User loser, User attacker, User defender, 
      int expGained, int lostCoins, Timestamp battleTime, boolean isFlee, int lockBoxEventId,
      Map<String, Integer> attackerCurrencyChange, Map<String, Integer> defenderCurrencyChange) {

    boolean simulateStaminaRefill = (attacker.getStamina() == attacker.getStaminaMax());
    
    boolean attackerDeactivateShield = false;
    boolean defenderDeactivateShield = false;
    //turn off attacker's shield if defender is real
    if (!defender.isFake()) {
      attackerDeactivateShield = true;
    }
    
    if (winner == attacker) {
      if (!attacker.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(-1,
          expGained, lostCoins, 1, 0, 0, simulateStaminaRefill, false, battleTime, attackerDeactivateShield)) {
        log.error("problem with updating info for winner/attacker " + attacker.getId() + " in battle at " 
            + battleTime + " vs " + loser.getId());
      } else {//for user currency history
        attackerCurrencyChange.put(MiscMethods.silver, lostCoins);
      }
      if (!defender.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(0,
          0, (defender.isFake()) ? 0 : lostCoins * -1, 0, 1, 0, false, true, battleTime, defenderDeactivateShield)) {
        log.error("problem with updating info for defender/loser " + defender.getId() + " in battle at " 
            + battleTime + " vs " + winner.getId());
      } else { //for user currency history
        if(!defender.isFake()) {
          defenderCurrencyChange.put(MiscMethods.silver, lostCoins * -1);
        }
      }
    } else if (winner == defender) {
      if (isFlee) {
        if (!attacker.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(-1,
            0, lostCoins * -1, 0, 1, 1, simulateStaminaRefill, false, battleTime, attackerDeactivateShield)) {
          log.error("problem with updating info for loser/attacker/flee-er " + attacker.getId() + " in battle at " 
              + battleTime + " vs " + winner.getId());
        } else {//for user currency history
          attackerCurrencyChange.put(MiscMethods.silver, lostCoins * -1);
        }
        if (!defender.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(0,
            0, (defender.isFake()) ? 0 : lostCoins, 1, 0, 0, false, false, battleTime, defenderDeactivateShield)) {
          log.error("problem with updating info for winner/defender " + defender.getId() + " in battle at " 
              + battleTime + " vs " + loser.getId() + " who fled");
        } else {//for user currency history
          if (!defender.isFake()) {
            defenderCurrencyChange.put(MiscMethods.silver, lostCoins);
          }
        }
      } else {
        if (!attacker.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(-1,
            0, lostCoins * -1, 0, 1, 0, simulateStaminaRefill, false, battleTime, attackerDeactivateShield)) {
          log.error("problem with updating info for loser/attacker " + attacker.getId() + " in battle at " 
              + battleTime + " vs " + winner.getId());
        } else {//for user currency history
          attackerCurrencyChange.put(MiscMethods.silver, lostCoins * -1);
        }
        if (!defender.updateRelativeStaminaExperienceCoinsBattleswonBattleslostFleesSimulatestaminarefill(0,
            0, (defender.isFake()) ? 0 : lostCoins, 1, 0, 0, false, true, battleTime, defenderDeactivateShield)) {
          log.error("problem with updating info for winner/defender " + defender.getId() + " in battle at " 
              + battleTime + " vs " + loser.getId());
        } else {//for user currency history
          if (!defender.isFake()) {
            defenderCurrencyChange.put(MiscMethods.silver, lostCoins);
          }
        }
      }
    }

    if (lockBoxEventId != ControllerConstants.NOT_SET) {
      if (!UpdateUtils.get().incrementNumberOfLockBoxesForLockBoxEvent(attacker.getId(), lockBoxEventId, 1))
        log.error("problem incrementing user lock boxes for user = "+attacker+" lock box event id ="+lockBoxEventId);
    }
  }

  //if incrementOwnerBattleWins is true then the the owner's battle wins is incremented
  //else the attacker's battle wins is incremented, does nothing if list of towers is null/empty
  //returns changed towers
  private List<ClanTower> incrementBattleWins(List<ClanTower> towers, boolean incrementOwnerBattleWins, int ownerUserId,
      int attackerUserId, int pointsGained) {
    List<ClanTower> changedTowers = new ArrayList<ClanTower>();
    String ownerOrAttacker = incrementOwnerBattleWins ? "owner" : "attacker";
    for(ClanTower aTower : towers) {
      if(!UpdateUtils.get().updateClanTowerBattleWins(
          aTower.getId(), aTower.getClanOwnerId(), aTower.getClanAttackerId(),
          incrementOwnerBattleWins, pointsGained, aTower.getCurrentBattleId(), ownerUserId, attackerUserId)) {
        log.error("(no rows updated) problem with updating tower's battle wins. " +
            "tower=" + aTower + " The " + ownerOrAttacker + "'s " +
            "battle wins were not incremented.");
      } else {
        if (incrementOwnerBattleWins) {
          aTower.setOwnerBattleWins(aTower.getOwnerBattleWins()+pointsGained);
        } else {
          aTower.setAttackerBattleWins(aTower.getAttackerBattleWins()+pointsGained);
        }
      }
    }
    return changedTowers;
  }

  private void writeChangesToDBForClanTowers(User winner, User loser, User attacker, User defender) {
    List<ClanTower> attackerIsClanTowerOwner;
    List<ClanTower> defenderIsClanTowerOwner;
    int attackerId = attacker.getClanId();
    int defenderId = defender.getClanId();
    boolean ownerAndAttackerAreEnemies = true;
    int pointsGained = MiscMethods.pointsGainedForClanTowerUserBattle(winner, loser);
    
    if (attackerId == ControllerConstants.NOT_SET || defenderId == ControllerConstants.NOT_SET) {
      return;
    }

    //attacker is owner of tower
    attackerIsClanTowerOwner = ClanTowerRetrieveUtils.getAllClanTowersWithSpecificOwnerAndOrAttackerId(
        attackerId, defenderId, ownerAndAttackerAreEnemies);    
    //defender is owner of tower
    defenderIsClanTowerOwner = ClanTowerRetrieveUtils.getAllClanTowersWithSpecificOwnerAndOrAttackerId(
        defenderId, attackerId, ownerAndAttackerAreEnemies);

    if (winner == attacker) {
      incrementBattleWins(attackerIsClanTowerOwner, true, attacker.getId(), defender.getId(), pointsGained);//increment clan tower's owner battle wins
      incrementBattleWins(defenderIsClanTowerOwner, false, defender.getId(), attacker.getId(), pointsGained);//increment clan tower's attacker battle wins
    } else if (winner == defender) {
      incrementBattleWins(defenderIsClanTowerOwner, true, defender.getId(), attacker.getId(), pointsGained);//increment clan tower's owner battle wins
      incrementBattleWins(attackerIsClanTowerOwner, false, attacker.getId(), defender.getId(), pointsGained);//increment clan tower's attacker battle wins
    }
    //defenderIsClanTowerOwner contains half of the towers, so add into it the other half
    defenderIsClanTowerOwner.addAll(attackerIsClanTowerOwner);
    if(!defenderIsClanTowerOwner.isEmpty()) { //send towers only if there are towers
      MiscMethods.sendClanTowerProtosToClient(new HashSet<ClanTower>(defenderIsClanTowerOwner), server, ReasonForClanTowerChange.NUM_BATTLE_WINS_CHANGED, 
          attacker, defender, winner==attacker, pointsGained);
    }
  }

  private void leaderBoardEventStuff(User winner, User loser, User attacker, 
      User defender, boolean attackerFled) {
    //get all leaderboard events that have not ended this is assuming all events care about wins/losses/flees
    Collection<LeaderboardEvent> events = LeaderboardEventRetrieveUtils.getIdsToLeaderboardEvents(false).values();
    List<LeaderboardEvent> activeEvents = new ArrayList<LeaderboardEvent>();

    for (LeaderboardEvent event : events) {
      long curTime = new Date().getTime();
      if (curTime > event.getStartDate().getTime() && curTime < event.getEndDate().getTime()) {
        activeEvents.add(event);
      }
    }

    boolean attackerWon = winner == attacker;
    boolean defenderWon = winner == defender;

    for(LeaderboardEvent e: activeEvents) {
      int attackerId = attacker.getId();
      int defenderId = defender.getId();
      int leaderboardEventId = e.getId();
      if(attackerWon) {

        //need to increment attacker's wins
        InsertUtils.get().insertIntoUserLeaderboardEvent(leaderboardEventId, attackerId, 1, 0, 0);
        //need to increment defender's losses
        InsertUtils.get().insertIntoUserLeaderboardEvent(leaderboardEventId, defenderId, 0, 1, 0);
      } else if(defenderWon) {

        //need to increment defender's wins
        InsertUtils.get().insertIntoUserLeaderboardEvent(leaderboardEventId, defenderId, 1, 0, 0);
        if(attackerFled) {

          //need to increment attacker's flees
          InsertUtils.get().insertIntoUserLeaderboardEvent(leaderboardEventId, attackerId, 0, 0, 1);
        } else {
          //need to increment attacker's losses
          InsertUtils.get().insertIntoUserLeaderboardEvent(leaderboardEventId, attackerId, 0, 1, 0);
        }
      }

      updateLeaderboard(attackerId, leaderboardEventId);
      updateLeaderboard(defenderId, leaderboardEventId);
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

    Set<Integer> equippedEquips = MiscMethods.getEquippedEquips(defender);
    
    //get equips that can be stolen
    List<UserEquip> potentialLosses = new ArrayList<UserEquip>();
    if (defenderEquips != null) {
      for (UserEquip defenderEquip : defenderEquips) {
        int defenderEquipId = defenderEquip.getId();
        boolean equipped = equippedEquips.contains(defenderEquipId); //check if user has it equipped
        if (equipped) { //can't steal equipped equips
          continue;
        }
//        if (defenderEquip.getId() == defender.getWeaponEquippedUserEquipId() || defenderEquip.getId() == defender.getArmorEquippedUserEquipId()
//            || defenderEquip.getId() == defender.getAmuletEquippedUserEquipId()) {
//          continue;
//        }

        int equipId = defenderEquip.getEquipId();
        Equipment equip = equipmentIdsToEquipment.get(equipId);
        if (equip.getMinLevel() < defender.getLevel() 
            && defenderEquip.getLevel() < ControllerConstants.BATTLE__MAX_LEVEL_TO_STEAL
            && equip.getRarity().getNumber() < Rarity.RARE_VALUE) {
          double rand = Math.random();
          if (rand <= equip.getChanceOfLoss() / defenderEquip.getLevel()) {
            potentialLosses.add(defenderEquip);
          }
        }
      }
      //find out which equip will be stolen
      if (potentialLosses.size() > 0) {
        UserEquip lostUserEquip = potentialLosses.get((int) Math.rint(Math.random()
            * (potentialLosses.size() - 1)));
        if (oldDefenderUserEquipsList != null) {
          for (FullUserEquipProto oldUserEquip : oldDefenderUserEquipsList) {
            if (oldUserEquip.getEquipId() == lostUserEquip.getEquipId()) {
              List<UserEquip> userEquipsWithEquipId = new ArrayList<UserEquip>();
              for (UserEquip defenderEquip : defenderEquips) {
                if (defenderEquip.getId() == lostUserEquip.getId()) userEquipsWithEquipId.add(defenderEquip);
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

  private int checkIfUserAcquiresLockBox(User user, BattleResult result, Timestamp curTime) {
    if (result != BattleResult.ATTACKER_WIN) return ControllerConstants.NOT_SET;

    Map<Integer, LockBoxEvent> events = LockBoxEventRetrieveUtils.getLockBoxEventIdsToLockBoxEvents();
    LockBoxEvent curEvent = null;
    for (LockBoxEvent event : events.values()) {
      if (curTime.getTime() > event.getStartDate().getTime() && curTime.getTime() < event.getEndDate().getTime()) {
        curEvent = event;
        break;
      }
    }

    if (curEvent != null) {
      float chanceToAttainBox = ControllerConstants.LOCK_BOXES__CHANCE_TO_ACQUIRE_FROM_BATTLE;
      if (Math.random() < chanceToAttainBox) return curEvent.getId();
    }
    return ControllerConstants.NOT_SET;
  }

  private boolean checkIfUserGetsKiipReward(BattleResult result) {
    if (result != BattleResult.ATTACKER_WIN) return false;
    if (Math.random() < ControllerConstants.CHANCE_TO_GET_KIIP_ON_BATTLE_WIN) return true;
    return false;
  }
  
  private void updateLeaderboard(int userId, int eventId) {
    UserLeaderboardEvent ule = UserLeaderboardEventRetrieveUtils.getSpecificUserLeaderboardEvent(eventId, userId);
    
    if (ule != null) {
      int winsScore = ule.getBattlesWon()*ControllerConstants.LEADERBOARD_EVENT__WINS_WEIGHT;
      int lossesScore = ule.getBattlesLost()*ControllerConstants.LEADERBOARD_EVENT__LOSSES_WEIGHT;
      int fleesScore = ule.getBattlesFled()*ControllerConstants.LEADERBOARD_EVENT__FLEES_WEIGHT;
      int totalScore = winsScore+lossesScore+fleesScore;
      leaderUtil.setScoreForEventAndUser(eventId, userId, (double) totalScore);
    }
  }
  
  private void writeToUserCurrencyHistory(User winner, User attacker, User defender, Timestamp date, 
      Map<String, Integer> attackerCurrencyChange, Map<String, Integer> defenderCurrencyChange,
      int attackerPreviousSilver, int defenderPreviousSilver) {
    try {
      if(null != winner) {
        int isSilver = 1;
        String silver = MiscMethods.silver;
        int attackerSilverChange = 0;
        if(attackerCurrencyChange.containsKey(silver)) {
          attackerSilverChange = attackerCurrencyChange.get(silver);
        }
        int attackerCurrentSilver = attacker.getCoins() + attacker.getVaultBalance();
        int defenderSilverChange = 0;
        if(defenderCurrencyChange.containsKey(silver)) {
          defenderSilverChange = defenderCurrencyChange.get(silver);
        }
        int defenderCurrentSilver = defender.getCoins() + defender.getVaultBalance(); 
        String won = ControllerConstants.UCHRFC__BATTLE_WON;
        String lost = ControllerConstants.UCHRFC__BATTLE_LOST;

        List<Integer> userIds = new ArrayList<Integer>();
        List<Timestamp> dates = new ArrayList<Timestamp>();
        List<Integer> areSilver = new ArrayList<Integer>();
        List<Integer> currenciesChange = new ArrayList<Integer>();
        List<Integer> currenciesBefore = new ArrayList<Integer>();
        List<Integer> currentCurrencies = new ArrayList<Integer>();
        List<String> reasonsForChanges = new ArrayList<String>();

        boolean attackerWon = winner == attacker;

        if (0 != attackerSilverChange) {
          userIds.add(attacker.getId());
          dates.add(date);
          areSilver.add(isSilver);
          currenciesChange.add(attackerSilverChange);
          currenciesBefore.add(attackerPreviousSilver);
          currentCurrencies.add(attackerCurrentSilver);
          if(attackerWon) {
            reasonsForChanges.add(won);
          } else {
            reasonsForChanges.add(lost);
          }
        }
        if (0 != defenderSilverChange) {
          userIds.add(defender.getId());
          dates.add(date);
          areSilver.add(isSilver);
          currenciesChange.add(defenderSilverChange);
          currenciesBefore.add(defenderPreviousSilver);
          currentCurrencies.add(defenderCurrentSilver);
          if (attackerWon) {
            reasonsForChanges.add(lost);
          } else {
            reasonsForChanges.add(won);
          }
        }
        
        if(!userIds.isEmpty()) {
          InsertUtils.get().insertIntoUserCurrencyHistoryMultipleRows(userIds, dates, areSilver,
              currenciesChange, currenciesBefore, currentCurrencies, reasonsForChanges);
        }
      } 
    } catch(Exception e) {
      log.error("can't write into user_currency_history", e);
    }
  }
}
