package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PrestigeRequestEvent;
import com.lvl6.events.response.PrestigeResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.misc.Notification;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.Globals;
import com.lvl6.proto.EventProto.PrestigeRequestProto;
import com.lvl6.proto.EventProto.PrestigeResponseProto;
import com.lvl6.proto.EventProto.PrestigeResponseProto.Builder;
import com.lvl6.proto.EventProto.PrestigeResponseProto.PrestigeStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;


  @Component @DependsOn("gameServer") public class PrestigeController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  
  public PrestigeController() {
    numAllocatedThreads = 1;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new PrestigeRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_PRESTIGE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    PrestigeRequestProto reqProto = ((PrestigeRequestEvent)event).getPrestigeRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userId = senderProto.getUserId();
    
    PrestigeResponseProto.Builder resBuilder = PrestigeResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(userId);
      int preprestigeLevel = 0;
      int preprestigePrestigeLevel = 0;
      int newPrestigeLevel = 0;
      int preprestigeAttackStat = 0;
      int preprestigeDefenseStat = 0;
      int preprestigeStaminaStat = 0;
      int preprestigeEnergyStat = 0;
      Date aDate = new Date();
      int preprestigeExperience = 0;
      int preprestigeSkillPoints = 0;
      List<Notification> notifications = new ArrayList<Notification>();
      List<Integer> costs = new ArrayList<Integer>();
      int previousGold = 0;
      int cost = 0;
      
      boolean legitPrestige = checkLegitPrestige(resBuilder, user,
          notifications, costs);
      boolean success = false;
      if (legitPrestige) {
        preprestigeLevel = user.getLevel();
        preprestigePrestigeLevel = user.getPrestigeLevel();
        preprestigeAttackStat = user.getAttack();
        preprestigeDefenseStat = user.getDefense();
        preprestigeStaminaStat = user.getStamina();
        preprestigeEnergyStat = user.getEnergy();
        preprestigeExperience = user.getExperience();
        preprestigeSkillPoints = user.getSkillPoints();
        
        previousGold = user.getDiamonds();
        if (!costs.isEmpty()) {
          cost = costs.get(0) * -1;
        }
        success = writeChangesToDB(resBuilder, user, cost);
      }
      
      PrestigeResponseEvent resEvent = new PrestigeResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setPrestigeResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      
      if (success) {
        newPrestigeLevel = user.getPrestigeLevel();
        if (Globals.IS_SANDBOX()) {
          log.info("user prestige event, writing to prestige history!");
        }
        //write to prestige history table
        InsertUtils.get().insertIntoPrestigeHistory(userId, preprestigeLevel,
            preprestigePrestigeLevel, newPrestigeLevel, preprestigeAttackStat,
            preprestigeDefenseStat, preprestigeStaminaStat, preprestigeEnergyStat,
            aDate, preprestigeExperience, preprestigeSkillPoints);
        //keep track of gold change if needed
        
        writeToUserCurrencyHistory(user, aDate, cost, previousGold, newPrestigeLevel);
      } else {
        //send notification if there is one
        sendNotification(userId, notifications);
      }
      
    } catch (Exception e) {
      log.error("exception in Prestige processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private boolean writeChangesToDB(Builder resBuilder, User user, int cost) {
    //unequip everything
    //all skill points reset
    //charge diamonds if needed
    if (!user.prestige(user.getEnergy(), user.getStamina(), cost)) {
      log.error("unexpected error: could not reset user back to level 1. user=" + user);
      return false;
    }
    int userId = user.getId();
    
    //reset all quests, tasks, cities
    //user_cities delete all of them
    int numDeleted = DeleteUtils.get().deleteAllUserCitiesForUser(userId);
    if (0 >= numDeleted) {
      log.error("unexpected error: no user cities were deleted for user but continuing" +
      		" processing. numDeleted=" + numDeleted + ", user=" + user);
    }
    //user_quests delete all of them
    numDeleted = DeleteUtils.get().deleteAllUserQuestsForUser(userId);
    if (0 >= numDeleted) {
      log.error("unexpected error: no user quests were deleted for user but continuing" +
      		" processing numDeleted=" + numDeleted + ", user=" + user);
    }
    //user_quests_completed_defeat_type_jobs
    numDeleted = DeleteUtils.get().deleteAllUserQuestsCompletedDefeatTypeJobsForUser(userId);
    if (0 >= numDeleted) {
      log.error("unexpected error: no user quests completed defeat type jobs were deleted" +
      		" for user but continuing processing. numDeleted=" + numDeleted + ", user=" + user);
    }
    //user_quests_completed_tasks
    numDeleted = DeleteUtils.get().deleteAllUserQuestsCompletedTasksForUser(userId);
    if (0 >= numDeleted) {
      log.error("unexpected error: no user quests completed tasks were deleted for user" +
      		" but continuing processing. numDeleted=" + numDeleted + ", user=" + user);
    }
    //user_quests_defeat_type_job_progress
    numDeleted = DeleteUtils.get().deleteAllUserQuestsDefeatTypeJobProgressForUser(userId);
    if (0 >= numDeleted) {
      log.error("unexpected error: no user quests defeat type job progress were deleted for" +
      		" user but continuing processing. numDeleted=" + numDeleted + ", user=" + user);
    }
    //user_quests_task_progress
    numDeleted = DeleteUtils.get().deleteAllUserQuestsTaskProgress(userId);
    if (0 >= numDeleted) {
      log.error("unexpected error: no user quests task progress were deleted for user" +
      		" but continuing processing. numDeleted=" + numDeleted + ", user=" + user);
    }
    //user_tasks
    numDeleted = DeleteUtils.get().deleteAllUserTasksForUser(userId);
    if (0 >= numDeleted) {
      log.error("unexpected error: no user tasks were deleted for user but continuing" +
      		" processing. numDeleted=" + numDeleted + ", user=" + user);
    }
    //don't know what else to do if something goes wrong...
    return true;
  }

  private boolean checkLegitPrestige(Builder resBuilder, User user,
      List<Notification> notifications, List<Integer> costs) {
    if (user == null) {
      resBuilder.setStatus(PrestigeStatus.FAIL_OTHER);
      return false;
    }
    
    int minLevelForPrestige = ControllerConstants
        .PRESTIGE__MIN_LEVEL_FOR_PRESTIGE; 
    int userLevel = user.getLevel();
    if (userLevel < minLevelForPrestige) {
      log.error("user error: minLevelForPrestige=" + minLevelForPrestige + ", user=" + user );
      resBuilder.setStatus(PrestigeStatus.BELOW_MIN_LEVEL_FOR_PRESTIGE);
      return false;
    }
    
    //for prestige level 4 and above, user must have enough gold 
    int maxPrestigeLvlNoCost = ControllerConstants
        .PRESTIGE__MAX_PRESTIGE_LEVEL_WITH_NO_GOLD_COST;
    int userPrestigeLvl = user.getPrestigeLevel();
    int prestigeGoldCost = ControllerConstants
        .PRESTIGE__PRESTIGE_GOLD_COST;
    int userGold = user.getDiamonds();
    
    //see if user should be charged if he is going above prestige lvl 3
    if (userPrestigeLvl >= maxPrestigeLvlNoCost) {
      
      //gold cost
      if (userGold < prestigeGoldCost) {
        Notification n = new Notification();
        n.setAsNotEnoughGoldToPrestige();
        notifications.add(n);
        log.error("user error: user does not have enough gold when going" +
            " above prestige level 3. cost=" + prestigeGoldCost +
            ". userGold=" + userGold);
        return false;
      }
      //user should be charged this much gold
      costs.add(prestigeGoldCost);
    }
    
    return true;  
  }
  
  private void writeToUserCurrencyHistory(User aUser, Date aDate, int cost,
      int previousGold, int newPrestigeLvl) {
    if (0 == cost) {
      return;
    }
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String reasonForChange = ControllerConstants.UCHRFC__PRESTIGE + newPrestigeLvl;
    String gold = MiscMethods.gold;
    
    previousGoldSilver.put(gold, previousGold);
    reasonsForChanges.put(gold, reasonForChange);
    
    Timestamp date = new Timestamp(aDate.getTime());
    Map<String, Integer> money = new HashMap<String, Integer>();
    money.put(gold, cost);
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, money, previousGoldSilver, reasonsForChanges);
  }
  
  private void sendNotification(int userId, List<Notification> nList) {
    for (Notification n : nList) {
      MiscMethods.writeNotificationToUser(n, server, userId);
    }
  }
  
}
