package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joda.time.DateTime;
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
import com.lvl6.info.CityGem;
import com.lvl6.info.User;
import com.lvl6.info.UserBoss;
import com.lvl6.info.UserCityGem;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.BossActionRequestProto;
import com.lvl6.proto.EventProto.BossActionResponseProto;
import com.lvl6.proto.EventProto.BossActionResponseProto.BossActionStatus;
import com.lvl6.proto.EventProto.BossActionResponseProto.Builder;
import com.lvl6.proto.InfoProto.FullUserEquipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserCityGemProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserBossRetrieveUtils;
import com.lvl6.retrieveutils.UserCityGemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BossRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BossRewardRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityGemRetrieveUtils;
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
    resBuilder.setStatus(BossActionStatus.FAIL_OTHER); //default
    resBuilder.setBossId(bossId);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User aUser = RetrieveUtils.userRetrieveUtils().getUserById(userId);
      Boss aBoss = BossRetrieveUtils.getBossForBossId(bossId);
      List<UserBoss> userBossList = new ArrayList<UserBoss>();
      int previousSilver = 0;
      int previousGold = 0;

      //userBossList should be populated if successful
      boolean legit = checkLegit(resBuilder, aUser, userId, aBoss,
          bossId, curTime, isSuperAttack, userBossList);
      
      if(legit) {
        UserBoss aUserBoss = userBossList.get(0);
        previousSilver = aUser.getCoins() + aUser.getVaultBalance();
        previousGold = aUser.getDiamonds();

        //aUserBoss will be modified to account for user's attack
        Map<String, Integer> damageExp = 
            attackBoss(resBuilder, aUserBoss, aUser, aBoss, curTime, isSuperAttack);
        int damageDone = damageExp.get(damage);
        int expGained = damageExp.get(experience);
        
        //get the city gem the user gets
        Map<Integer, UserCityGem> gemIdsToUserCityGems = new HashMap<Integer, UserCityGem>();
        int cityId = aBoss.getCityId();
        CityGem cg = determineIfGemDropped(aUserBoss, userId, cityId,
            gemIdsToUserCityGems);
        List<BossReward> brList = determineLoot(aUserBoss);

        List<Integer> allSilver = new ArrayList<Integer>();
        List<Integer> allGold = new ArrayList<Integer>();
        List<Integer> allEquipIds = new ArrayList<Integer>();
        Map<String, Integer> money = new HashMap<String, Integer>();
        List<Integer> allUserEquipIds = new ArrayList<Integer>();

        //set silver, gold, equips
        separateSilverGoldAndEquips(resBuilder, brList, allSilver, allGold, allEquipIds, money);

        //generate levels for each equip
        List<Integer> levels = generateLevelsForEquips(allEquipIds);

        writeChangesToDB(resBuilder, aUserBoss, aUser, aBoss, isSuperAttack,
            money, allEquipIds, levels, allUserEquipIds, curTime, expGained);
        writeUserCityGems(resBuilder, userId, cityId, cg, gemIdsToUserCityGems);
        
        //send stuff back to client
        resBuilder.setDamageDone(damageDone);
        resBuilder.setExpGained(expGained);
        resBuilder.addAllCoinsGained(allSilver);
        resBuilder.addAllDiamondsGained(allGold);
        List<FullUserEquipProto> ueList = getFullUserEquipProtosForClient(
            resBuilder, allUserEquipIds, aUser.getId(), allEquipIds, levels);
        resBuilder.addAllLootUserEquip(ueList);

        writeToUserCurrencyHistory(aUser, money, curTime, previousSilver, previousGold);
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
   * Return true if user request is valid; false otherwise and set the
   * builder status to the appropriate value.
   */
  private boolean checkLegit(Builder resBuilder, User u, int userId, Boss b,
      int bossId, Timestamp curTime, boolean isSuperAttack,
      List<UserBoss> userBossList) {
    if (null == u || null == b) {
      log.error("unexpected error: user or boss is null. user=" + u
          + "\t boss="+ b);
      return false;
    }
    //copy pasted from PickLockBoxController.java Pick()
    if (!MiscMethods.checkClientTimeAroundApproximateNow(curTime)) {
      log.error("client time too apart of server time. client time =" + curTime + ", servertime~="
          + new Date());
      resBuilder.setStatus(BossActionStatus.FAIL_CLIENT_TOO_APART_FROM_SERVER_TIME);
      return false;
    } 
    if(!userHasSufficientEnergy(u, b, isSuperAttack)) {
      log.error("user error: use does not have enough energy to attack boss" +
          "user energy=" + u.getEnergy() + "\t boss=" + b);
      resBuilder.setStatus(BossActionStatus.FAIL_USER_NOT_ENOUGH_ENERGY);
      return false;
    }
    
    //In order to attack boss, user needs to rank up a city. When ranking
    //up a city, a user_boss entry should be created/updated.
    //Ergo entry in user_bosses should exist when this controller executes.
    UserBoss aUserBoss = UserBossRetrieveUtils.getSpecificUserBoss(userId, bossId);
    if(null == aUserBoss) {
      log.error("unexpected error: user_boss should exist before user can" +
      		" event send BossActionRequest. user=" + u + "\t boss=" + b);
      return false;
    }
    userBossList.add(aUserBoss);
    
    //check if user can attack the boss
    if (!inAttackWindow(aUserBoss, u, b, curTime)) {
      log.error("user error: user is trying to attack a boss outside of the" +
      		" allotted time. boss=" + b + "\t userboss=" + aUserBoss);
      resBuilder.setStatus(BossActionStatus.FAIL_ATTACK_WINDOW_EXPIRED);
      return false;
    }
    
    //check if boss is alive
    if (aUserBoss.getCurrentHealth() <= 0) {
      log.error("user error: user is trying to attack a dead boss. boss=" +
          b + "\t userBoss=" + aUserBoss);
      resBuilder.setStatus(BossActionStatus.FAIL_BOSS_IS_DEAD);
      return false;
    }
    
    return true;
  }
  
  /* 
   * Return true if user has energy >= to energy cost to attack boss
   */
  private boolean userHasSufficientEnergy(User u, Boss b,
      boolean isSuperAttack) {
    int energyCost = b.getRegularAttackEnergyCost();
    if (isSuperAttack) {
      energyCost = b.getSuperAttackEnergyCost();
    }
    int userEnergy = u.getEnergy();
    boolean enoughEnergy = userEnergy >= energyCost;
    return enoughEnergy;
  }

  /*
   * Returns true if the user can attack, false otherwise. The user can attack if
   * the time the user attacks is between start_time (in kingdom.user_bosses table)
   * and start_time + minutes_to_kill (in kingdom.bosses table). 
   */
  private boolean inAttackWindow(UserBoss aUserBoss, User u, Boss b,
      Timestamp curTime) {
    Date timeOfFirstHit = aUserBoss.getStartTime();
    int attackWindow = b.getMinutesToKill();
    DateTime timeForLastHit = new DateTime(timeOfFirstHit);
    timeForLastHit.plusMinutes(attackWindow);
    
    if (timeForLastHit.isBefore(curTime.getTime())) {
      return false;
    }
    return true;
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
		if (0 >= currentHealth) {
			//boss killed
			currentHealth = 0;
		}
	  
		aUserBoss.setCurrentHealth(currentHealth);
		
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
    int minExp = aBoss.getBaseExp();
    int maxExp = aBoss.getBaseExp() * 2;

    if(isSuperAttack) {
      log.info("superattack");
      double superAttack = ControllerConstants.BOSS_EVENT__SUPER_ATTACK;
      int integerPart = (int) superAttack;
      double fractionalPart = superAttack - (double) integerPart;

      int indexOfLastDamage = individualDamages.size() - 1;
      for(int i = 0; i < indexOfLastDamage; i++) {
        int dmgDone = individualDamages.get(i); //not really needed because of new formula
        int exp = generateNumInRange(minExp, maxExp);
        exp = Math.max(1, exp + userLevel);
        expGained += exp;

        log.info("damage=" + dmgDone + ", exp=" + exp);
      }

      int lastDmgDone = individualDamages.get(indexOfLastDamage); //not really needed, just for logging
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
      int exp = generateNumInRange(minExp, maxExp);
      exp = Math.max(1, exp + userLevel);
      expGained += exp;

      log.info("damage=" + dmgDone + ", exp=" + exp);
    }

    return expGained;
  }
  
  private CityGem determineIfGemDropped(UserBoss aUserBoss, int userId,
      int cityId, Map<Integer, UserCityGem> gemIdsToUserCityGems) {
    if (aUserBoss.getCurrentHealth() > 0) {
      return null;
    }
    
    Map<Integer, CityGem> gemIdsToActiveCityGems = 
        CityGemRetrieveUtils.getActiveCityGemIdsToCityGems();
    if (null == gemIdsToActiveCityGems || gemIdsToActiveCityGems.isEmpty()) {
      log.error("unexpected error: no city gems in the db!");
      return null;
    }
    Map<Integer, UserCityGem> gemIdsToUserCityGemsTemp = UserCityGemRetrieveUtils
        .getGemIdsToGemsForUserAndCity(userId, cityId);
    
    //select the next boss gem the user gets
    boolean getBossGem = true;
    boolean allowDuplicates = false;
    List<CityGem> potentialBossGems = MiscMethods.getPotentialGems(
        allowDuplicates, getBossGem, gemIdsToUserCityGemsTemp,
        gemIdsToActiveCityGems);
    
    //assuming only one boss gem
    if (potentialBossGems.isEmpty()) {
      return null;
    }
    
    //if randFloat is between [bossGemDropRate, 1), don't give a gem
    CityGem cg = potentialBossGems.get(0); 
    Random rand = new Random();
    float bossGemDropRate = cg.getDropRate();
    float randFloat = rand.nextFloat();
    
    if (randFloat >= bossGemDropRate) {
      cg = null;
    }
    return cg;
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

  private void writeChangesToDB(Builder resBuilder, UserBoss aUserBoss, User aUser,
      Boss aBoss, boolean isSuperAttack, Map<String, Integer> money,
      List<Integer> allEquipIds, List<Integer> levels,
      List<Integer> allUserEquipIds, Timestamp clientTime, int expChange) {
    
    int bossId = aUserBoss.getBossId();
    int userId = aUserBoss.getUserId();
    //update user_boss table
    if (!UpdateUtils.get().replaceBoss(
        userId, bossId, aUserBoss.getStartTime(), aUserBoss.getCurrentHealth(), 
        aUserBoss.getCurrentLevel())) {
      log.error("either updated no rows after boss attack or updated more than expected");
      return;
    }
    int silverChange = money.get(MiscMethods.silver);
    int goldChange = money.get(MiscMethods.gold);

    //update users table regarding silver and gold
    boolean simulateEnergyRefill = aUser.getEnergy() == aUser.getEnergyMax();
    int energyCost = aBoss.getRegularAttackEnergyCost();
    if (isSuperAttack) {
      energyCost = aBoss.getSuperAttackEnergyCost();
    }
    if(!aUser.updateUserAfterAttackingBoss(-1 * energyCost, silverChange, 
        goldChange, simulateEnergyRefill, clientTime, expChange) ){
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
//      int numUpdated = InsertUtils.get().insertIntoBossEquipDropHistory(bossRewardDropHistoryId, allEquipIds);
//      log.info("number of distinct equips boss dropped=" + numUpdated + ". The equips are: " + allEquipIds);
    }
  }
  
  //increase the user's city gem by 1
  private void writeUserCityGems(Builder resBuilder, int userId, int cityId,
      CityGem cg, Map<Integer, UserCityGem> gemIdsToUserCityGems) {
    if (null == cg) {
      return;
    }
    
    int gemId = cg.getId();
    int quantity = 0;
    UserCityGem ucg = null;
    
    //if the user has this gem already, get the quantity
    if (gemIdsToUserCityGems.containsKey(gemId)) {
      ucg = gemIdsToUserCityGems.get(gemId);
      quantity = ucg.getQuantity();
    } else {
      ucg = new UserCityGem(userId, cityId, gemId, quantity);
    }
    
    int newQuantity = quantity + 1;
    ucg.setQuantity(newQuantity);
    
    //update the quantity for user's gem in the database
    if (!UpdateUtils.get().updateUserCityGem(userId, cityId, gemId, newQuantity) ) {
      log.error("unexpected error: did not update the user's gems. "
          + "userCityGem=" + ucg);
    } else {
      //send this to the client
      UserCityGemProto gem = CreateInfoProtoUtils.createUserCityGemProto(ucg);
      resBuilder.setGemDropped(gem);
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
