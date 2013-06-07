package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.lvl6.misc.MiscMethods;
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
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class BossActionController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static String damage = "damage";
  private static String experience = "experience";

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
    boolean isSuperAttack = reqProto.getIsSuperAttack();
    		
    //set some values to send to the client (the response proto)
    BossActionResponseProto.Builder resBuilder = BossActionResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setBossId(bossId);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User aUser = RetrieveUtils.userRetrieveUtils().getUserById(userId);
      Boss aBoss = BossRetrieveUtils.getBossForBossId(bossId);
      resBuilder.setStatus(BossActionStatus.FAIL_OTHER);
      int previousSilver = 0;
      int previousGold = 0;

      if(userHasSufficientStamina(resBuilder, aUser, aBoss)) {
        UserBoss aUserBoss = UserBossRetrieveUtils.getSpecificUserBoss(userId, bossId);

        if(null == aUserBoss) {
          aUserBoss = createUserBoss(aUser, aBoss, curTime);
        }

        //set the BossActionStatus to return. Determine if user can attack
        boolean userCanAttack = canAttack(resBuilder, aUserBoss, aUser, aBoss, curTime);

        if(userCanAttack) {   
          previousSilver = aUser.getCoins() + aUser.getVaultBalance();
          previousGold = aUser.getDiamonds();
          
          Map<String, Integer> damageExp = 
              attackBoss(resBuilder, aUserBoss, aUser, aBoss, curTime, isSuperAttack);
          int damageDone = damageExp.get(damage);
          int expGained = damageExp.get(experience);
          resBuilder.setDamageDone(damageDone);
          resBuilder.setExpGained(expGained);

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

          writeChangesToDB(resBuilder, aUserBoss, aUser, aBoss, money, 
              allEquipIds, levels, allUserEquipIds, curTime, expGained);
          //send stuff back to client
          List<FullUserEquipProto> ueList = getFullUserEquipProtosForClient(
              resBuilder, allUserEquipIds, aUser.getId(), allEquipIds, levels);
          resBuilder.addAllLootUserEquip(ueList);
          
          writeToUserCurrencyHistory(aUser, money, curTime, previousSilver, previousGold);
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
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    }
  }

  /* 
   * Return true if user has stamina >= to stamina cost to attack boss
   */
  private boolean userHasSufficientStamina(Builder resBuilder, User u, Boss b) {
    if(null != u && null != b) {
      int userStamina = u.getStamina();
      int bossStaminaCost = b.getStaminaCost();
      boolean enough = userStamina >= bossStaminaCost;
      if (!enough) {
        resBuilder.setStatus(BossActionStatus.FAIL_USER_NOT_ENOUGH_ENERGY);
      } 
      return enough;
    } else {
      resBuilder.setStatus(BossActionStatus.FAIL_OTHER);
      return false;
    }
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
    return new UserBoss(userId, bossId, currentHealth, numTimesKilled, now, null);
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
      resBuilder.setStatus(BossActionStatus.FAIL_CLIENT_TOO_APART_FROM_SERVER_TIME);
      return false;
    } 

    //CHECK IF USER CAN ATTACK BOSS. DETERMINED BY THE TIME INTERVALS:
    // case 1:
    // - start_time or last_killed_time in kingdom.user_bosses table 
    // - minutes_to_kill in kingdom.bosses table
    // so time interval is [start_time, start_time + minutes_to_kill] or
    //case 2:
    // let start_time2 = last_killed_time + minutes_to_respawn
    // [start_time2, start_time2 + minutes_to_kill] 

    long timeAllocatedToKill = 60000*aBoss.getMinutesToKill();
    long timeAllocatedToRespawn = 60000*aBoss.getMinutesToRespawn();
    
    //case 1
    long startTime = aUserBoss.getStartTime().getTime(); //the time the user initially attacked boss
    long lastPossibleTimeToAttack = startTime + timeAllocatedToKill; //the last possible time user can attack boss
    long timeOfAttack = curTime.getTime();
    long timeBossRespawns = lastPossibleTimeToAttack + timeAllocatedToRespawn;

    //case 2
    Date lastKilledTime = aUserBoss.getLastTimeKilled();
    if(null != lastKilledTime) {
      //user killed boss 
      long lastKilledTimeMilliseconds = lastKilledTime.getTime();

      startTime = lastKilledTimeMilliseconds; //doesn't matter what this value is
      lastPossibleTimeToAttack = lastKilledTimeMilliseconds; //time should be a value before curTime
      
      timeBossRespawns = lastKilledTimeMilliseconds + timeAllocatedToRespawn;
      
      //values are set like this to ensure "boss needs to respawn" or "boss has respawned" case executes
    }
    
    if (startTime <= timeOfAttack && timeOfAttack < lastPossibleTimeToAttack) {
      boolean bossHasHealth = (aUserBoss.getCurrentHealth() > 0);

      if(bossHasHealth) {
        //user can attack
        resBuilder.setStatus(BossActionStatus.SUCCESS);
        return true;
      }   else {
        //can't attack because boss is already dead
        log.error("client is attacking a dead boss. \n" 
            + "time user launched attack = " + curTime + ". \n" + "user is " + aUser + ". \n"
            + "boss is " + aBoss + ". \n " + "user_boss is " + aUserBoss + ". \n");
        resBuilder.setStatus(BossActionStatus.FAIL_BOSS_HAS_NOT_SPAWNED);
        return false;
      }

    }	else if (lastPossibleTimeToAttack <= timeOfAttack && timeOfAttack < timeBossRespawns) {
      //user can't attack because boss needs to respawn
      log.error("boss has not respawned yet. time boss first attacked = " +  aUserBoss.getStartTime()
          + "; last possible time to attack boss = " + new Date(lastPossibleTimeToAttack)
      + "; time user launched attack = " + curTime);
      resBuilder.setStatus(BossActionStatus.FAIL_BOSS_HAS_NOT_SPAWNED);
      return false;
    } else if (timeBossRespawns <= timeOfAttack) {
      //boss has respawned
      resBuilder.setStatus(BossActionStatus.SUCCESS);
      aUserBoss.setCurrentHealth(aBoss.getBaseHealth());
      aUserBoss.setStartTime(new Date(curTime.getTime()));
      
      aUserBoss.setLastTimeKilled(null);
      return true;
    }

    resBuilder.setStatus(BossActionStatus.FAIL_OTHER);
    return false;
  }


  /*
   * Since user "attacked," change the user_boss object to reflect it,
   * Return map to reflecting the amount of damage user did and exp gained from attacking. 
   */
	private Map<String, Integer> attackBoss(Builder resBuilder, UserBoss aUserBoss, User aUser, 
			Boss aBoss, Timestamp curTime, boolean isSuperAttack) {
		int damageGenerated = 0;
		List<Integer> individualDamages = new ArrayList<Integer>(); //records the damages generated
		int expGained = 0;
		
		damageGenerated = generateDamage(aBoss, isSuperAttack, individualDamages);
		expGained = generateExpGained(aBoss, isSuperAttack, individualDamages, aUser.getLevel());
		
		int currentHealth = aUserBoss.getCurrentHealth() - damageGenerated;
		int numTimesKilled = aUserBoss.getNumTimesKilled();
		Date lastTimeKilled = aUserBoss.getLastTimeKilled();
		if (0 >= currentHealth) {
			//boss killed
			currentHealth = 0;
			numTimesKilled++;
			lastTimeKilled = curTime;
		}
	  
		aUserBoss.setCurrentHealth(currentHealth);
		aUserBoss.setNumTimesKilled(numTimesKilled);
		aUserBoss.setLastTimeKilled(lastTimeKilled);
		
		Map<String, Integer> damageExp = new HashMap<String, Integer>();
		damageExp.put(damage, damageGenerated);
		damageExp.put(experience, expGained);
		
		return damageExp;
  }


  /*
   * generate a random number in specified range, ends are inclusive
   */
  private int generateNumInRange(int lowerBound, int upperBound) {
    Random rand = new Random();
    return rand.nextInt(upperBound - lowerBound + 1) + lowerBound;
  }
  
  private int generateDamage(Boss aBoss, boolean isSuperAttack, List<Integer> individualDamages) {
    int minDamage = aBoss.getMinDamage();
    int maxDamage = aBoss.getMaxDamage();
	  int damageGenerated = 0;

	  if(isSuperAttack) {
  	  double superAttack = ControllerConstants.BOSS_EVENT__SUPER_ATTACK;
  	  int integerPart = (int) superAttack;
  	  double fractionalPart = superAttack - integerPart;
  		
  	  for(int i = 0; i < integerPart; i++) {
  	    int dmg = generateNumInRange(minDamage, maxDamage);
  		  damageGenerated += dmg;
  		  individualDamages.add(dmg);
  	  }
  		
  	  //this should account for when the superAttack value is a non-whole number
  	  if(superAttack != integerPart) { //3.0 does equal 3 
  	    int dmg = generateNumInRange(minDamage, maxDamage);
  		  damageGenerated += dmg * fractionalPart; //damages are not rounded
  	    individualDamages.add(dmg);
  	  }
  	  
	  } else {
	    //not super attack
	    int dmg = generateNumInRange(minDamage, maxDamage);
	    damageGenerated += dmg;
	    individualDamages.add(dmg);
	  }
	  
	  return damageGenerated;
  }

  //  user's experience based on the attack
  //  The formula is:
  //  (minExp) + ((dmgDone - minDmg)/(maxDmg - minDmg)) * (maxExp - minExp)
  //
  //  Forget the top formula, new formula is max(value1, value2) where
  //  value1 = 1; 
  //  value2 = randomNumFrom(aBoss.minExp, aBoss.maxExp) + currentLevel 
  //  Take max because value2 could be negative. 
  //  THE MINIMUM EXP GAINED IS 1! This is for the low level players.
  private int generateExpGained(Boss aBoss, boolean isSuperAttack, 
      List<Integer> individualDamages, int userLevel) {
    int expGained = 1;
    int minExp = aBoss.getMinExp();
    int maxExp = aBoss.getMaxExp();

    if(isSuperAttack) {
      log.info("superattack");
      double superAttack = ControllerConstants.BOSS_EVENT__SUPER_ATTACK;
      int integerPart = (int) superAttack;
      double fractionalPart = superAttack - (double) integerPart;

      int indexOfLastDamage = individualDamages.size() - 1;
      for(int i = 0; i < indexOfLastDamage; i++) {
        int dmgDone = individualDamages.get(i); //not really needed because of new formula
        //int exp = calculateExpGained(minExp, maxExp);
        int exp = generateNumInRange(minExp, maxExp);
        exp = Math.max(1, exp + userLevel);
        expGained += exp;

        log.info("damage=" + dmgDone + ", exp=" + exp);
      }

      int lastDmgDone = individualDamages.get(indexOfLastDamage); //not really needed, just for logging
      //int lastExp = calculateExpGained(minExp, maxExp);
      int lastExp = generateNumInRange(minExp, maxExp);
      
      if(superAttack != integerPart) {
        lastExp = (int) (lastExp * fractionalPart); //truncating some values
        lastExp = Math.max(1, lastExp + userLevel); //once again, minimum exp could be 1 and 1*(float from 0 to 1) is 0
        log.info("super attack not a whole number.");
      }
      log.info("lastDmgDone=" + lastDmgDone + ", the last exp gained=" + lastExp);
      expGained += lastExp;  

    } else {
      int dmgDone = individualDamages.get(0);
      //int exp = calculateExpGained(minExp, maxExp);
      int exp = generateNumInRange(minExp, maxExp);
      exp = Math.max(1, exp + userLevel);
      expGained += exp;

      log.info("damage=" + dmgDone + ", exp=" + exp);
    }

    return expGained;
  }
  /*
  private int calculateExpGained(int minExp, int maxExp) {
    double minDmg = aBoss.getMinDamage();
    double maxDmg = aBoss.getMaxDamage();
    double maxMinDmgDifference = maxDmg - minDmg;
    
    double minExp = aBoss.getMinExp();
    double maxExp = aBoss.getMaxExp();
    double maxMinExpDifference = maxExp - minExp;
    
    double dmgDoneMinDmgDifference = (dmgDone - minDmg);
    double differencesRatio = dmgDoneMinDmgDifference/maxMinDmgDifference;
    
    log.info("differencesRatio=" + differencesRatio);
    return (int) (minExp + differencesRatio*maxMinExpDifference);
    
  }*/
  
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
   * @param rewards - will contain the BossRewards that have been awarded
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
    
    if (0 != silverTotal) {
      money.put(MiscMethods.silver, silverTotal);
    }
    if (0 != goldTotal) {
      money.put(MiscMethods.gold, goldTotal);
    }
  }

  private boolean isSilverReward(BossReward br) {
    int minSilver = br.getMinSilver();
    int maxSilver = br.getMaxSilver();
    if(ControllerConstants.NOT_SET == minSilver || -1 >= minSilver
        || ControllerConstants.NOT_SET == maxSilver || minSilver > maxSilver) {
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
      return false;
    }
    else {
      return true;
    }	
  }

  private boolean isEquipReward(BossReward br) {
    int equipId = br.getEquipId();
    if(ControllerConstants.NOT_SET == equipId || -1 >= equipId) {
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
      List<Integer> allUserEquipIds, Timestamp clientTime, int expChange) {
    
    int bossId = aUserBoss.getBossId();
    int userId = aUserBoss.getUserId();
    //update user_boss table
    if (!UpdateUtils.get().decrementUserBossHealthAndMaybeIncrementNumTimesKilled(
        userId, bossId, aUserBoss.getStartTime(), aUserBoss.getCurrentHealth(), 
        aUserBoss.getNumTimesKilled(), aUserBoss.getLastTimeKilled())) {
      log.error("either updated no rows after boss attack or updated more than expected");
      return;
    }
    int silverChange = money.get(MiscMethods.silver);
    int goldChange = money.get(MiscMethods.gold);

    //update users table regarding silver and gold
    boolean simulateStaminaRefill = aUser.getStamina() == aUser.getStaminaMax();
    if(!aUser.updateUserAfterAttackingBoss(-aBoss.getStaminaCost(), silverChange, 
        goldChange, simulateStaminaRefill, clientTime, expChange) ){
      log.error("Error in updating user after attacking a boss.");
      return;
    }

    if (allEquipIds != null && allEquipIds.size() > 0) {
      //update user_equips table with equipment rewards.
      List<Integer> enhancement = null;
      List<Integer> createdUserEquipIds = InsertUtils.get().insertUserEquips(
          aUser.getId(), allEquipIds, levels, enhancement);

      allUserEquipIds.addAll(createdUserEquipIds);
    }

    //boss reward history stuff
    if(0 >= aUserBoss.getCurrentHealth()) {
      //boss died so record the rewards
      int bossRewardDropHistoryId = InsertUtils.get()
          .insertIntoBossRewardDropHistoryReturnId(bossId, userId, silverChange, goldChange, clientTime);
      log.info("id of new boss reward drop history row: " + bossRewardDropHistoryId);
      int numUpdated = InsertUtils.get().insertIntoBossEquipDropHistory(bossRewardDropHistoryId, allEquipIds);
      log.info("number of distinct equips boss dropped=" + numUpdated + ". The equips are: " + allEquipIds);
    }
  }

  private List<FullUserEquipProto> getFullUserEquipProtosForClient(BossActionResponseProto.Builder resBuilder,
      List<Integer> allUserEquipIds, int userId, List<Integer> allEquipIds, List<Integer> levels) {
    List<FullUserEquipProto> fullUserEquipProtos = new ArrayList<FullUserEquipProto>();

    for(int i = 0; i < allEquipIds.size(); i++) {
      int userEquipId = allUserEquipIds.get(i);
      int equipId = allEquipIds.get(i);
      int level = levels.get(i);
      UserEquip ue = new UserEquip(userEquipId, userId, equipId, level, 0);
      FullUserEquipProto fuep = CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(ue);
      fullUserEquipProtos.add(fuep);
    }

    return fullUserEquipProtos;
  }
  
  private void writeToUserCurrencyHistory(User aUser, Map<String, Integer> money, Timestamp curTime,
      int previousSilver, int previousGold) {
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String reasonForChange = ControllerConstants.UCHRFC__BOSS_ACTION;
    String gold = MiscMethods.gold;
    String silver = MiscMethods.silver;
    
    previousGoldSilver.put(gold, previousGold);
    previousGoldSilver.put(silver, previousSilver);
    reasonsForChanges.put(gold, reasonForChange);
    reasonsForChanges.put(silver, reasonForChange);
    
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, curTime, money, 
        previousGoldSilver, reasonsForChanges);
    
  }
}
