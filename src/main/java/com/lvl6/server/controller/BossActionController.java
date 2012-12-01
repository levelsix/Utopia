package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BossActionRequestEvent;
import com.lvl6.events.response.BossActionResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Boss;
import com.lvl6.info.User;
import com.lvl6.info.UserBoss;
import com.lvl6.proto.EventProto.BossActionRequestProto;
import com.lvl6.proto.EventProto.BossActionResponseProto;
import com.lvl6.proto.EventProto.BossActionResponseProto.BossActionStatus;
import com.lvl6.proto.EventProto.BossActionResponseProto.Builder;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserBossRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BossRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class BossActionController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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
    			//this also determines the loot the user gets and updates the user boss after an attack
    			updateUserBoss(resBuilder, aUserBoss, aUser, aBoss);
    			
    			//update user's stamina
    			aUser.updateStaminaAfterAttackingBoss(-aBoss.getStaminaCost());
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
			  //user can attack, update cur_health and maybe num_times_killed
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
   * Since user "attacked," change the user_boss to reflect it.
   */
  private void updateUserBoss(Builder resBuilder, UserBoss aUserBoss, User aUser, Boss aBoss) {
	  int damageTaken = generateDamage(aBoss);
	  resBuilder.setDamageDone(damageTaken);
	  
	  int currentHealth = aUserBoss.getCurrentHealth() - damageTaken;
	  int numTimesKilled = aUserBoss.getNumTimesKilled();
	  if (0 >= currentHealth) {
		  //boss killed
		  currentHealth = 0;
		  numTimesKilled++;
		  
		  //determine what user gets for killing boss
		  giveLoot(resBuilder, aUserBoss);
	  }
	  //TODO: call method to update the database, create method to update the database
	  if (!UpdateUtils.get().decrementUserBossHealthAndMaybeIncrementNumTimesKilled(
		aUser.getId(), aBoss.getId(), aUserBoss.getStartTime(), currentHealth, numTimesKilled)) {
		  log.error("either updated no rows after boss attack or updated more than expected");
	  }
  }
  
  /*
   * determine how much damage user dealt to boss
   */
  private int generateDamage(Boss aBoss) {
	  int maxPossibleDamage = aBoss.getMaxDamage();
	  int minPossibleDamage = aBoss.getMinDamage();
	  
	  Random rand = new Random();
	  return rand.nextInt(maxPossibleDamage - minPossibleDamage + 1) + minPossibleDamage;
  }
  
  //TODO: FIGURE OUT WHAT REWARDS TO GIVE FOR KILLING BOSS
  private void giveLoot(Builder resBuilder, UserBoss aUserBoss) { 
	  
  }
  
}
