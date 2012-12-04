package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BossActionRequestEvent;
import com.lvl6.events.response.BossActionResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Boss;
import com.lvl6.info.BossReward;
import com.lvl6.info.User;
import com.lvl6.info.UserBoss;
import com.lvl6.info.UserEquip;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.BossActionRequestProto;
import com.lvl6.proto.EventProto.BossActionResponseProto;
import com.lvl6.proto.EventProto.BossActionResponseProto.BossActionStatus;
import com.lvl6.proto.EventProto.BossActionResponseProto.Builder;
import com.lvl6.proto.InfoProto.FullUserEquipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserBossRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BossRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BossRewardRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class BossActionController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static String silver = "silver";
  private static String gold = "gold";

  public BossActionController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new BossActionRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_BOSS_ACTION_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    BossActionRequestProto reqProto = ((BossActionRequestEvent)event).getBossActionRequestProto();

    //get values sent from the client (the request proto)
    MinimumUserProto senderProto = reqProto.getSender();
    int userId = senderProto.getUserId();
    int bossId = reqProto.getBossId();
    Timestamp curTime = new Timestamp(reqProto.getCurTime());

    //set some values to send to the client (the response proto)
    BossActionResponseProto.Builder resBuilder = BossActionResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setBossId(bossId);

    server.lockPlayer(senderProto.getUserId());
    try {
      User aUser = RetrieveUtils.userRetrieveUtils().getUserById(userId);
      Boss aBoss = BossRetrieveUtils.getBossForBossId(bossId);
      resBuilder.setStatus(BossActionStatus.USER_NOT_ENOUGH_STAMINA);

      if(userHasSufficientStamina(aUser, aBoss)) {
        UserBoss aUserBoss = UserBossRetrieveUtils.getSpecificUserBoss(userId, bossId);

        if(null == aUserBoss) {
          aUserBoss = createUserBoss(aUser, aBoss, curTime);
        }

        //set the BossActionStatus to return. Determine if user can attack
        boolean userCanAttack = canAttack(resBuilder, aUserBoss, aUser, aBoss, curTime);

        if(userCanAttack) {    	
          int damageDone = attackBoss(resBuilder, aUserBoss, aUser, aBoss);
          resBuilder.setDamageDone(damageDone);

          List<BossReward> brList = determineLoot(aUserBoss);

          List<Integer> allSilver = new ArrayList<Integer>();
          List<Integer> allGold = new ArrayList<Integer>();
          List<Integer> allEquipIds = new ArrayList<Integer>();
          Map<String, Integer> money = new HashMap<String, Integer>();
          List<Integer> allUserEquipIds = new ArrayList<Integer>();

          //set silver, gold, equips
          separateSilverGoldAndEquips(resBuilder, brList, allSilver, allGold, allEquipIds, money);
          resBuilder.addAllCoinsGained(allSilver);
          resBuilder.addAllDiamondsGained(allGold);

          //generate levels for each equip
          List<Integer> levels = generateLevelsForEquips(allEquipIds);

          writeChangesToDB(resBuilder, aUserBoss, aUser, aBoss, money, allEquipIds, levels, allUserEquipIds, curTime);
          //send stuff back to client
          List<FullUserEquipProto> ueList = setUserEquipRewards(
              resBuilder, allUserEquipIds, aUser.getId(), allEquipIds, levels);
          resBuilder.addAllLootUserEquip(ueList);
        }
      }
      BossActionResponseEvent resEvent = new BossActionResponseEvent(userId);
      resEvent.setTag(event.getTag());
      resEvent.setBossActionResponseProto(resBuilder.build());
      server.writeEvent(resEvent);

      UpdateClientUserResponseEvent resEventUpdate = MiscMethods
          .createUpdateClientUserResponseEventAndUpdateLeaderboard(aUser);
      resEventUpdate.setTag(event.getTag());
      server.writeEvent(resEventUpdate);
    } catch (Exception e) {
      log.error("exception in BossActionController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  /* 
   * Return true if user has stamina >= to stamina cost to attack boss
   */
  private boolean userHasSufficientStamina(User u, Boss b) {
    int userStamina = u.getStamina();
    int bossStaminaCost = b.getStaminaCost();
    return userStamina >= bossStaminaCost;
  }

  /*
   * Make a new UserBoss: full health, 0 for numTimesKilled, now for startTime.
   */
  private UserBoss createUserBoss(User u, Boss b, Timestamp curTime) {
    int userId = u.getId();
    int bossId = b.getId();
    int currentHealth = b.getBaseHealth();
    int numTimesKilled = 0;
    Date now = new Date(curTime.getTime());
    return new UserBoss(userId, bossId, currentHealth, numTimesKilled, now);
  }

  /*
   * Returns true if the user can attack, false otherwise. The user can attack if the boss has positive
   * nonzero health and the time the user attacks is between start_time (in kingdom.user_bosses table)
   * and start_time + minutes_to_kill (in kingdom.bosses table). 
   * Resets user_boss's start_time and cur_health if boss can respawn.
   * Sets BossActionStatus to one of the following:
   * 	BOSS_HAS_NOT_SPAWNED
   * 	CLIENT_TOO_APART_FROM_SERVER_TIME
   * 	OTHER_FAIL
   * 	SUCCESS
   */
  private boolean canAttack(Builder resBuilder, UserBoss aUserBoss, User aUser, Boss aBoss, Timestamp curTime) {
    //copy pasted from PickLockBoxController.java checkLegitPick()
    if (!MiscMethods.checkClientTimeAroundApproximateNow(curTime)) {
      log.error("client time too apart of server time. client time =" + curTime + ", servertime~="
          + new Date());
      resBuilder.setStatus(BossActionStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      return false;
    } 


    //CHECK IF USER CAN ATTACK BOSS. DETERMINED BY THE TIME INTERVAL: 
    // - start_time in kingdom.user_bosses table 
    // - minutes_to_kill in kingdom.bosses table
    // so time interval is [start_time, start_time + minutes_to_kill]
    long startTime = aUserBoss.getStartTime().getTime(); //the time the user initially attacked boss
    long lastPossibleTimeToAttack = startTime + 60000*aBoss.getMinutesToKill(); //the last possible time user can attack boss
    long timeOfAttack = curTime.getTime();

    long timeBossRespawns = startTime + 60000*aBoss.getMinutesToRespawn();

    if (startTime <= timeOfAttack && timeOfAttack < lastPossibleTimeToAttack) {
      boolean bossHasHealth = (aUserBoss.getCurrentHealth() > 0);

      if(bossHasHealth) {
        //user can attack
        resBuilder.setStatus(BossActionStatus.SUCCESS);
        return true;
      }   else {
        //boss is already dead
        log.error("client is attacking a dead boss. \n" + 
            "time user launched attack = " + curTime + ". \n" +
            "user is " + aUser + ". \n" +
            "boss is " + aBoss + ". \n " + 
            "user_boss is " + aUserBoss + ". \n");
        resBuilder.setStatus(BossActionStatus.BOSS_HAS_NOT_SPAWNED);
        return false;
      }

    }	else if (lastPossibleTimeToAttack <= timeOfAttack && timeOfAttack < timeBossRespawns) {
      //user can't attack
      log.error("boss has not respawned yet. time boss first attacked = " +  aUserBoss.getStartTime()
          + "; last possible time to attack boss = " + new Date(lastPossibleTimeToAttack)
      + "; time user launched attack = " + curTime);
      resBuilder.setStatus(BossActionStatus.BOSS_HAS_NOT_SPAWNED);
      return false;
    } else if (timeBossRespawns <= timeOfAttack) {
      //boss has respawned, update UserBoss: start_time and cur_health
      resBuilder.setStatus(BossActionStatus.SUCCESS);
      aUserBoss.setCurrentHealth(aBoss.getBaseHealth());
      aUserBoss.setStartTime(new Date());
      return true;
    }

    resBuilder.setStatus(BossActionStatus.OTHER_FAIL);
    return false;
  }


  /*
   * Since user "attacked," change the user_boss object to reflect it. 
   */
  private int attackBoss(Builder resBuilder, UserBoss aUserBoss, User aUser, Boss aBoss) {
    int damageTaken = generateNumInRange(aBoss.getMinDamage(), aBoss.getMaxDamage());

    int currentHealth = aUserBoss.getCurrentHealth() - damageTaken;
    int numTimesKilled = aUserBoss.getNumTimesKilled();
    if (0 >= currentHealth) {
      //boss killed
      currentHealth = 0;
      numTimesKilled++;
    }

    aUserBoss.setCurrentHealth(currentHealth);
    aUserBoss.setNumTimesKilled(numTimesKilled);
    return damageTaken;
  }


  /*
   * generate a random number in specified range, ends are inclusive
   */
  private int generateNumInRange(int lowerBound, int upperBound) {
    Random rand = new Random();
    return rand.nextInt(upperBound - lowerBound + 1) + lowerBound;
  }

  private List<BossReward> determineLoot(UserBoss aUserBoss) { 
    List<BossReward> rewardsAwarded = new ArrayList<BossReward>();

    if (aUserBoss.getCurrentHealth() <= 0) {
      //get the BossRewards related to boss id
      List<BossReward> brList = BossRewardRetrieveUtils.getAllBossRewardsForBossId(aUserBoss.getBossId());

      Map<Integer, List<BossReward>> groupedBR = groupByRewardGroup(brList);

      //process reward group 0, the special group where all rewards are possible to be awarded
      pickLootFromSpecialRewardGroup(groupedBR.remove(0), rewardsAwarded);

      //process the other reward groups
      pickLootFromRewardGroups(groupedBR, rewardsAwarded);
    }
    log.info("boss's health=" + aUserBoss.getCurrentHealth());
    return rewardsAwarded;
  }



  private Map<Integer, List<BossReward>> groupByRewardGroup(List<BossReward> brList) {
    Map<Integer, List<BossReward>> groupedBRList = new HashMap<Integer, List<BossReward>>();
    for(BossReward br : brList) {
      int rewardGroup = br.getRewardGroup();
      if(!groupedBRList.containsKey(rewardGroup)) {
        groupedBRList.put(rewardGroup, new ArrayList<BossReward>());
      }
      groupedBRList.get(rewardGroup).add(br);
    }
    return groupedBRList;
  }

  /**
   * Every BossReward can be awarded.
   * @param brList - all BossRewards with the same and specific reward group
   * @param rewards - contains the BossRewards that have been awarded
   */
  private void pickLootFromSpecialRewardGroup(List<BossReward> brList,
      List<BossReward> rewards) {
    Random rand = new Random();
    for(BossReward br: brList) {
      float percent = rand.nextFloat();
      float rewardPercent = br.getProbabilityToBeAwarded();
      if(percent < rewardPercent) {
        rewards.add(br);
      }
    }
  }

  private void pickLootFromRewardGroups(Map<Integer, List<BossReward>> groupedBR,
      List<BossReward> rewards) {
    for(List<BossReward> brList: groupedBR.values()) {
      float sumOfProbabilities = sumProbabilities(brList);
      log.info("sum of percent_chance_to_drop: " + sumOfProbabilities);

      if(0 >= sumOfProbabilities) {
        //choose one of the rewards, all with equal probability to be chosen
        BossReward reward = fairlyPickReward(brList); 
        rewards.add(reward);
      } else {
        BossReward reward = unfairlyPickReward(
            brList, sumOfProbabilities);
        rewards.add(reward);
      }

    }
  }

  private float sumProbabilities(List<BossReward> brList) {
    float returnValue = 0.0f;
    for(BossReward br : brList) {
      returnValue += br.getProbabilityToBeAwarded();
    }
    return returnValue;
  }

  // all rewards have an equal chance of being selected
  private BossReward fairlyPickReward(List<BossReward> brList) {
    int numRewards = brList.size();
    Random rand = new Random();
    int rewardIndex = rand.nextInt(numRewards);
    return brList.get(rewardIndex);
  }

  //each reward has it's own probability to be selected, so normalize then pick
  private BossReward unfairlyPickReward(List<BossReward> brList, float highestProbability){
    Random rand = new Random();
    float f = rand.nextFloat();

    float probabilitySoFar = 0.0f; 
    float bound = 0.0f; 

    //for each reward, calculate its normalized probability and 
    //determine if it is the reward to be returned 
    for(int index = 0; index < brList.size(); index++) {
      BossReward br = brList.get(index);
      probabilitySoFar += br.getProbabilityToBeAwarded();
      bound = probabilitySoFar / highestProbability;

      if (f < bound) {
        log.info("brList=" + brList + ", index=" + index + ", f=" + f + ",");
        return br;
      }
    }
    return null;
  }

  private void separateSilverGoldAndEquips(Builder resBuilder, List<BossReward> brList, List<Integer> allSilver,
      List<Integer> allGold, List<Integer> allEquipIds, Map<String, Integer> money) {
    int silverTotal = 0;
    int goldTotal = 0;

    for(BossReward reward: brList) {
      if(isSilverReward(reward)) {
        int minSilver = reward.getMinSilver();
        int maxSilver = reward.getMaxSilver();
        int silverGenerated = generateNumInRange(minSilver, maxSilver);

        allSilver.add(silverGenerated);
        silverTotal += silverGenerated;
      }
      else if(isGoldReward(reward)) {
        int minGold = reward.getMinGold();
        int maxGold = reward.getMaxGold();
        int goldGenerated = generateNumInRange(minGold, maxGold);

        allGold.add(goldGenerated);
        goldTotal += goldGenerated;
      } 
      else if(isEquipReward(reward)) {
        allEquipIds.add(reward.getEquipId());
      }
    }

    money.put(silver, silverTotal);
    money.put(gold, goldTotal);
  }

  private boolean isSilverReward(BossReward br) {
    int minSilver = br.getMinSilver();
    int maxSilver = br.getMaxSilver();
    if(ControllerConstants.NOT_SET == minSilver || -1 >= minSilver
        || ControllerConstants.NOT_SET == maxSilver || minSilver > maxSilver) {
      log.info("BossReward is not silver reward. br=" + br);
      return false;
    }
    else {
      return true;
    }	  
  }

  private boolean isGoldReward(BossReward br) {
    int minGold = br.getMinGold();
    int maxGold = br.getMaxGold();
    if(ControllerConstants.NOT_SET == minGold || -1 >= minGold
        || ControllerConstants.NOT_SET == maxGold || minGold > maxGold) {
      log.info("BossReward is not gold reward. br=" + br);
      return false;
    }
    else {
      return true;
    }	
  }

  private boolean isEquipReward(BossReward br) {
    int equipId = br.getEquipId();
    if(ControllerConstants.NOT_SET == equipId || -1 >= equipId) {
      log.info("BossReward is not equip reward. br=" + br);
      return false;
    }
    else {
      return true;
    }
  }

  private List<Integer> generateLevelsForEquips(List<Integer> allEquips) {
    //just make the equips level 1
    List<Integer> levels = new ArrayList<Integer>();
    int defaultEquipLevel = 1;
    for(int i = 0; i < allEquips.size(); i++) {
      levels.add(defaultEquipLevel);
    }
    return levels;
  }

  private void writeChangesToDB(Builder resBuilder, UserBoss aUserBoss, User aUser, Boss aBoss,
      Map<String, Integer> money, List<Integer> allEquipIds, List<Integer> levels,
      List<Integer> allUserEquipIds, Timestamp clientTime) {
    //update user_boss table
    if (!UpdateUtils.get().decrementUserBossHealthAndMaybeIncrementNumTimesKilled(
        aUserBoss.getUserId(), aUserBoss.getBossId(), aUserBoss.getStartTime(), aUserBoss.getCurrentHealth(), 
        aUserBoss.getNumTimesKilled())) {
      log.error("either updated no rows after boss attack or updated more than expected");
      return;
    }
    int silverChange = money.get(silver);
    int goldChange = money.get(gold);

    //update users table regarding silver and gold
    boolean simulateStaminaRefill = aUser.getStamina() == aUser.getStaminaMax();
    if(!aUser.updateUserAfterAttackingBoss(-aBoss.getStaminaCost(), silverChange, goldChange, simulateStaminaRefill, clientTime) ){
      log.error("Error in updating user after attacking a boss.");
      return;
    }

    if (allEquipIds != null && allEquipIds.size() > 0) {
      //update user_equips table with equipment rewards.
      List<Integer> createdUserEquipIds = InsertUtils.get().insertUserEquips(
          aUser.getId(), allEquipIds, levels);

      allUserEquipIds.addAll(createdUserEquipIds);
    }

  }

  private List<FullUserEquipProto> setUserEquipRewards(BossActionResponseProto.Builder resBuilder,
      List<Integer> allUserEquipIds, int userId, List<Integer> allEquipIds, List<Integer> levels) {
    List<FullUserEquipProto> fullUserEquipProtos = new ArrayList<FullUserEquipProto>();

    for(int i = 0; i < allEquipIds.size(); i++) {
      int userEquipId = allUserEquipIds.get(i);
      int equipId = allEquipIds.get(i);
      int level = levels.get(i);
      UserEquip ue = new UserEquip(userEquipId, userId, equipId, level);
      FullUserEquipProto fuep = CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(ue);
      fullUserEquipProtos.add(fuep);
    }

    return fullUserEquipProtos;
  }
}
