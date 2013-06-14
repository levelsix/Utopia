package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.TaskActionRequestEvent;
import com.lvl6.events.response.TaskActionResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Boss;
import com.lvl6.info.City;
import com.lvl6.info.CityGem;
import com.lvl6.info.LockBoxEvent;
import com.lvl6.info.NeutralCityElement;
import com.lvl6.info.Quest;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.UserBoss;
import com.lvl6.info.UserCityGem;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserQuest;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.TaskActionRequestProto;
import com.lvl6.proto.EventProto.TaskActionResponseProto;
import com.lvl6.proto.EventProto.TaskActionResponseProto.Builder;
import com.lvl6.proto.EventProto.TaskActionResponseProto.TaskActionStatus;
import com.lvl6.proto.InfoProto.FullUserBossProto;
import com.lvl6.proto.InfoProto.FullUserEquipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserCityGemProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserBossRetrieveUtils;
import com.lvl6.retrieveutils.UserCityGemRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsTaskProgressRetrieveUtils;
import com.lvl6.retrieveutils.UserTaskRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.BossRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityGemRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LockBoxEventRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.NeutralCityElementsRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskEquipReqRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component
@DependsOn("gameServer")
public class TaskActionController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() {
  }.getClass().getEnclosingClass());

  @Autowired
  protected InsertUtil insertUtils;

  public void setInsertUtils(InsertUtil insertUtils) {
    this.insertUtils = insertUtils;
  }

  @Resource(name = "outgoingGameEventsHandlerExecutor")
  protected TaskExecutor executor;
  
  public TaskExecutor getExecutor() {
	  return executor;
  }
  
  public void setExecutor(TaskExecutor executor) {
	  this.executor = executor;
  }
  
  @Resource(name = "playersByPlayerId")
  protected Map<Integer, ConnectedPlayer> playersByPlayerId;
  public Map<Integer, ConnectedPlayer> getPlayersByPlayerId() {
	  return playersByPlayerId;
  }
  
  public void setPlayersByPlayerId(
		  Map<Integer, ConnectedPlayer> playersByPlayerId) {
	  this.playersByPlayerId = playersByPlayerId;
  }
  
  public TaskActionController() {
    numAllocatedThreads = 20;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new TaskActionRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_TASK_ACTION_EVENT;
  }

  /*
   * db stuff done before sending event to eventwriter/client because the
   * client's not waiting on it immediately anyways
   */
  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    TaskActionRequestProto reqProto = ((TaskActionRequestEvent) event)
        .getTaskActionRequestProto();
    MinimumUserProto senderProto = reqProto.getSender();
    int userId = senderProto.getUserId();
    int taskId = reqProto.getTaskId();
    int cityId = ControllerConstants.NOT_SET;
    Date clientDate = new Date(reqProto.getCurTime());
    Timestamp clientTime = new Timestamp(reqProto.getCurTime());

    TaskActionResponseProto.Builder resBuilder = TaskActionResponseProto
        .newBuilder();
    resBuilder.setSender(senderProto);
    
    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(userId);
      int previousSilver = 0;
      Task task = TaskRetrieveUtils.getTaskForTaskId(taskId);
      // TODO: check the level of this task's city, use that as multiplier
      // for expGained, energyCost, etc.
      
      //holds user's city rank for task's city
      List<Integer> cityRankList = new ArrayList<Integer>();
      
      //int expGained = ControllerConstants.NOT_SET;
      int coinsGained = ControllerConstants.NOT_SET;
      int lootEquipId = ControllerConstants.NOT_SET;
      int lockBoxEventId = ControllerConstants.NOT_SET;
      UserCityGem ucg = null;
      int coinBonus = ControllerConstants.NOT_SET;
      int expBonus = ControllerConstants.NOT_SET;
      List<Task> tasksInCity = null;
      boolean taskCompleted = false;
      boolean cityRankedUp = false;
      boolean changeNumTimesUserActedInDB = true;

      boolean legitAction = checkLegitAction(user, task, clientTime,
          resBuilder, cityRankList);
      if (legitAction) {
        cityId = task.getCityId();
        City city = CityRetrieveUtils.getCityForCityId(cityId);
        int cityRank = cityRankList.get(0);
        previousSilver = user.getCoins() + user.getVaultBalance();
        
        //determine the stuff that might pop out after tapping building
        resBuilder.setCityId(cityId);
        coinsGained = calculateCoinsGained(resBuilder, task);//set coins returned
        //expGained = calculateExpGained(resBuilder, task);
        lootEquipId = chooseLootEquipId(task);
        lockBoxEventId = checkIfUserAcquiresLockBox(user, task, clientTime);
        
        //if equip dropped give it to him
        lootEquipId = giveEquip(resBuilder, userId, lootEquipId);
        
        //don't limit city ranks because of city boss feature
        //if (cityRank < ControllerConstants.TASK_ACTION__MAX_CITY_RANK) {
        Map<Integer, Integer> taskIdToNumTimesActedInRank = UserTaskRetrieveUtils
            .getTaskIdToNumTimesActedInRankForUser(userId);
        
        //num times the user did this task + 1
        int numTimesActedInRank = determineNumTimesTaskCompleted(taskId,
            taskIdToNumTimesActedInRank);
        
        ucg = checkIfGemDropped(userId, user, cityId, city, cityRank,
            taskIdToNumTimesActedInRank);
        
        //if user already completed this task, don't record
        int numActionsToCompleteTask = task.getNumForCompletion();
        if (numTimesActedInRank > numActionsToCompleteTask) {
          changeNumTimesUserActedInDB = false;
          
        } else if (numTimesActedInRank == numActionsToCompleteTask) {
          //user just completed the task
          tasksInCity = TaskRetrieveUtils.getAllTasksForCityId(cityId);
          taskCompleted = true;
          cityRankedUp = checkCityRankup(cityId, tasksInCity,
              taskIdToNumTimesActedInRank);
        }
        
        if (cityRankedUp) {
          //user gets bonus exp and coins when city ranks up
          coinBonus = coinBonusOnCityRankup(city, cityRank);
          expBonus = expBonusOnCityRankup(city, cityRank);
        }
//        } else {
//          changeNumTimesUserActedInDB = false;
//        }
      } //endif (legitAction)
      
      setBuilderStuff(resBuilder, lockBoxEventId, ucg, coinBonus, expBonus,
          taskCompleted, cityRankedUp);
      
      int totalCoinGain = sumUpCoinsGained(legitAction, coinsGained, coinBonus);
      int totalExpGain = sumUpExpGained(legitAction, task, expBonus);

      //write the stuff to the database
      if (legitAction) {
        writeChangesToDB(user, task, cityRankedUp,
            changeNumTimesUserActedInDB, lootEquipId, lockBoxEventId,
            totalCoinGain, totalExpGain, tasksInCity, clientTime);
        
        //write stuff to user_boss table
        UserBoss newBoss = 
            writeBossStuff(userId, cityId, cityRankedUp, clientDate);
        if (null != newBoss) {
          setBossStuff(resBuilder, user, newBoss, cityId);
        }
      }

      //send stuff to the client
      TaskActionResponseProto resProto = resBuilder.build();
      TaskActionResponseEvent resEvent = new TaskActionResponseEvent(userId);
      resEvent.setTag(event.getTag());
      resEvent.setTaskActionResponseProto(resProto);
      server.writeEvent(resEvent);

      if (legitAction) {
        writeUserCityGems(userId, cityId, ucg);
        
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods
            .createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        checkQuestsPostTaskAction(user, task, senderProto, lootEquipId);
        
        //writeChangesToDB functionality is enclosed within an if(legitAction) block
        writeToUserCurrencyHistory(user, taskId, totalCoinGain, previousSilver);
      }
    } catch (Exception e) {
      log.error("exception in TaskActionController processEvent", e);
      try {
        resBuilder.setStatus(TaskActionStatus.OTHER_FAIL);
        TaskActionResponseProto resProto = resBuilder.build();
        TaskActionResponseEvent resEvent = new TaskActionResponseEvent(userId);
        resEvent.setTag(event.getTag());
        resEvent.setTaskActionResponseProto(resProto);
        server.writeEvent(resEvent);
      } catch (Exception e2) {
        log.error("exception2 in TaskActionController processEvent", e2);
      }
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    }
  }

  private void checkQuestsPostTaskAction(User user, Task task,
      MinimumUserProto senderProto, int lootEquipId) {
    int userId = user.getId();
    int taskId = task.getId();
    //get the user quests that are not complete and have not been redeemed
    List<UserQuest> inProgressUserQuests = RetrieveUtils
        .userQuestRetrieveUtils().getIncompleteUserQuestsForUser(user.getId());

    if (null == inProgressUserQuests) {
      return;
    }
    Map<Integer, List<Integer>> questIdToUserTasksCompletedForQuestForUser = null;

    //for every in progress quest user has, check if it is complete (I think)
    for (UserQuest userQuest : inProgressUserQuests) {
      boolean questCompletedAndSent = false;

      if (userQuest.isTasksComplete()) {
        //something wrong, quest has only completed tasks but isn't complete
        //nor has been redeemed
        continue;
      }
      int questId = userQuest.getQuestId();
      Quest quest = QuestRetrieveUtils.getQuestForQuestId(questId);
      if (null == quest) {
        continue;
      }
      
      //get the Map(questId->List(taskId)) for tasks the user completed
      if (questIdToUserTasksCompletedForQuestForUser == null) {
        //efficiency reasons, in steady state user would have finished all quests
        questIdToUserTasksCompletedForQuestForUser = RetrieveUtils
            .userQuestsCompletedTasksRetrieveUtils()
            .getQuestIdToUserTasksCompletedForQuestForUser(user.getId());
      }
      
      //get the completed tasks for this quest
      List<Integer> userCompletedTasksForQuest = getCompletedTasksForQuest(questId,
          questIdToUserTasksCompletedForQuestForUser);
      
      //ids of tasks for this quest
      List<Integer> tasksRequired = quest.getTasksRequired();
      if (tasksRequired != null) {
        
        //get incomplete tasks for this quest
        List<Integer> tasksRemaining = new ArrayList<Integer>(tasksRequired);
        tasksRemaining.removeAll(userCompletedTasksForQuest);
        
        //find quest for task client sent and see if it is complete
        if (tasksRemaining.contains(taskId)) {
          questCompletedAndSent = seeifTaskJustFinished(senderProto, quest,
              userQuest, userId, taskId, questId, task,
              userCompletedTasksForQuest, tasksRequired);
        }
      }
      if (lootEquipId > ControllerConstants.NOT_SET && !questCompletedAndSent) {
        QuestUtils.checkQuestCompleteAndMaybeSendIfJustCompleted(server, quest, userQuest, senderProto, true, null);
      }
    }
  }
  
  private List<Integer> getCompletedTasksForQuest(int questId,
      Map<Integer, List<Integer>> questIdToUserTasksCompletedForQuestForUser) {
    List<Integer> taskIdList = questIdToUserTasksCompletedForQuestForUser.get(questId);
    if (taskIdList == null) {
      taskIdList = new ArrayList<Integer>();
    }
    return taskIdList;
  }
  
  private boolean seeifTaskJustFinished(MinimumUserProto senderProto, 
      Quest quest, UserQuest userQuest, int userId, int taskId,
      int questId, Task task, List<Integer> userCompletedTasksForQuest,
      List<Integer> tasksRequired) {
    
    boolean questCompletedAndSent = false; //return value
    
    //get Map(questId->Map(taskId->numTimesTaskDone)) for this user
    Map<Integer, Map<Integer, Integer>> questIdToTaskIdsToNumTimesActedInQuest = 
        UserQuestsTaskProgressRetrieveUtils.getQuestIdToTaskIdsToNumTimesActedInQuest(userId);
    
    Map<Integer, Integer> taskIdToNumTimesActed = questIdToTaskIdsToNumTimesActedInQuest.get(questId);
    if (taskIdToNumTimesActed == null) {
      taskIdToNumTimesActed = new HashMap<Integer, Integer>();
    }
    
    //if user did the "task" before, get how many times he did the "task"
    int timesActed = taskIdToNumTimesActed.containsKey(taskId) ? taskIdToNumTimesActed.get(taskId) : 0;

    //update how many times the user did this task
    if (timesActed + 1 >= task.getNumForCompletion()) {
      //user just now completed the "task"
      questCompletedAndSent = recordThatUserJustCompletedTask(senderProto,
          quest, userQuest, userId, taskId, questId, timesActed,
          userCompletedTasksForQuest, tasksRequired);
    } else {
      if (!UpdateUtils.get().incrementUserQuestTaskProgress(userId, quest.getId(), taskId, 1)) {
        log.error("problem with incrementing user quest task progress by 1 for quest id " + quest.getId() + ", task id=" + taskId);
      }
    }
    return questCompletedAndSent;
  }
  
  private boolean recordThatUserJustCompletedTask(MinimumUserProto senderProto, 
      Quest quest, UserQuest userQuest, int userId, int taskId, int questId,
      int timesActed, List<Integer> userCompletedTasksIdsForQuest,
      List<Integer> tasksRequired) {
    boolean questCompletedAndSent = false;
    if (timesActed == 0) {
      // The Delete Util for quest redeem expects a row for every task in both the progress table
      // and the completed task table so this emulates it for the case where the num times required
      // is only 1.
      if (!UpdateUtils.get().incrementUserQuestTaskProgress(userId, questId, taskId, 1)) {
        log.error("problem with incrementing user quest task progress by 1 for quest id " + questId + ", task id=" + taskId);
      }
    }
    if (insertUtils.insertCompletedTaskIdForUserQuest(userId, taskId, questId)) {
      userCompletedTasksIdsForQuest.add(taskId);
      if (userCompletedTasksIdsForQuest.containsAll(tasksRequired)) {
        if (UpdateUtils.get().updateUserQuestsSetCompleted(userId,questId,true, false)) {
          userQuest.setTasksComplete(true);
          questCompletedAndSent = QuestUtils.checkQuestCompleteAndMaybeSendIfJustCompleted(server, quest, userQuest, senderProto, true, null);
        } else {
          log.error("problem with marking tasks completed for a user quest, questId=" + questId);
        }
      }
    } else {
      log.error("problem with adding tasks to user's completed tasks for quest. taskId=" + taskId + ", questId=" + questId);
    }
    return questCompletedAndSent;
  }

  private void writeChangesToDB(User user, Task task, boolean cityRankedUp,
      boolean changeNumTimesUserActedInDB, int lootEquipId,
      int lockBoxEventId, int totalCoinGain, int totalExpGain,
      List<Task> tasksInCity, Timestamp clientTime) {

    //update user_city stuff
    if (cityRankedUp) {
      if (!UpdateUtils.get().incrementCityRankForUserCity(
          user.getId(), task.getCityId(), 1)) {
        log.error("problem with incrementing user city rank post-task by "
            + task.getCityId());
      }
      if (tasksInCity != null) {
        if (!UpdateUtils.get().resetTimesCompletedInRankForUserTasksInCity(
            user.getId(), tasksInCity)) {
          log.error("problem with resetting user times completed in rank post-task");
        }
      }
    } else if (changeNumTimesUserActedInDB) {
      if (!UpdateUtils.get().incrementTimesCompletedInRankForUserTask(
          user.getId(), task.getId(), 1)) {
        log.error("problem with incrementing user times completed in rank post-task for task "
            + task.getId());
      }
    }

    if (lockBoxEventId != ControllerConstants.NOT_SET) {
      if (!UpdateUtils.get().incrementNumberOfLockBoxesForLockBoxEvent(
          user.getId(), lockBoxEventId, 1)) {
        log.error("problem incrementing user lock boxes for user = " +
            user +" lock box event id ="+lockBoxEventId);
      }
    }

    boolean simulateEnergyRefill =
        (user.getEnergy() == user.getEnergyMax());
    
    int energyChange = task.getEnergyCost() * -1;
    if (!user.updateRelativeCoinsExpTaskscompletedEnergySimulateenergyrefill(
        totalCoinGain, totalExpGain, 1, energyChange, simulateEnergyRefill,
        clientTime)) {
      log.error("problem with updating user stats post-task. coinChange="
          + totalCoinGain + ", expGain=" + totalExpGain + ", increased" +
          " tasks completed by 1, energyChange=" + energyChange +
          ", clientTime=" + clientTime + ", simulateEnergyRefill=" +
          simulateEnergyRefill + ", user=" + user);
    }
  }
  
  private UserBoss writeBossStuff(int userId, int cityId,
      boolean cityRankedUp, Date clientDate) {
    if (!cityRankedUp) {
      return null;
    }
    //since city ranked up, spawn boss
    //get the boss for the city
    List<Boss> bossList = BossRetrieveUtils.getBossesForCityId(cityId);
    if(null == bossList || bossList.isEmpty()) {
      log.error("unexpected error: no boss exists for cityId=" + cityId);
      return null;
    }
    Boss aBoss = null;
    //for now assume there exists only one boss per city
    aBoss = bossList.get(0);
    int bossId = aBoss.getId();
    int defaultHealth = aBoss.getBaseHealth(); 
    
    //get the current user boss
    UserBoss ub = UserBossRetrieveUtils.getSpecificUserBoss(userId, bossId);
    
    int newLevel = 1;
    int newHealth = defaultHealth;
    if (null != ub) {
      //if it exists record user boss into history
      Date startDate = ub.getStartTime();
      Timestamp startTime = new Timestamp(startDate.getTime());
      int curHealth = ub.getCurrentHealth();
      int currentLevel = ub.getCurrentLevel();
      InsertUtils.get().insertIntoUserBossHistory(bossId, userId,
          startTime, curHealth, currentLevel);
      
      if (curHealth <= 0) {
        //user slayed the boss, boss should level up
        newLevel = currentLevel + 1;
      } else{
        //user did not slay the boss, level and health stay the same
        newLevel = currentLevel;
        newHealth = curHealth;
      }
    } else {
      //create dummy user boss
      ub = new UserBoss(bossId, userId, 0, 0, null);
    }
    
   updateUserBoss(ub, userId, bossId, newLevel, newHealth, clientDate);
    
    return ub;
  }
  
  private void updateUserBoss(UserBoss ub, int userId, int bossId,
      int newLevel, int newHealth, Date clientDate) {
    Timestamp newSpawnTime = new Timestamp(clientDate.getTime());
    ub.setCurrentHealth(newHealth);
    ub.setStartTime(clientDate);
    ub.setCurrentLevel(newLevel);
    if (!UpdateUtils.get().replaceBoss(userId, bossId, newSpawnTime,
        newHealth, newLevel)) {
      log.error("unexpected error: could not replace userBoss. ub=" + ub);
    }
  }
  
  //setting the user boss and the name of the boss
  private void setBossStuff(Builder resBuilder, User aUser,
      UserBoss newBoss, int cityId) {
    FullUserBossProto fubp = CreateInfoProtoUtils
        .createFullUserBossProtoFromUserBoss(newBoss);
    resBuilder.setBoss(fubp);
    int bossId = newBoss.getBossId();
    Boss aBoss = BossRetrieveUtils.getBossForBossId(bossId);
    if (null == aBoss) {
      log.error("unexpected error: no boss exists... userBoss=" + newBoss);
      return;
    }
    int assetId = aBoss.getAssetNumberWithinCity();
    NeutralCityElement bossCityElement = NeutralCityElementsRetrieveUtils
        .getNeutralCityElement(cityId, assetId);
    if (null == bossCityElement) {
      log.error("unexpected error: no neutral city element exists..." +
      		" aBoss=" + aBoss);
      return;
    }
    String bossName = null;
    
    if(MiscMethods.checkIfGoodSide(aUser.getType())) {
      bossName = bossCityElement.getGoodName();
    } else {
      bossName = bossCityElement.getBadName();
    }
    
    if (null != bossName) {
      resBuilder.setBossName(bossName);
    }
  }
  
  private void writeUserCityGems(int userId, int cityId, UserCityGem ucg) {
    if (null == ucg) {
      return;
    }
    int gemId = ucg.getGemId();
    int newQuantity = ucg.getQuantity();
    
    if (!UpdateUtils.get().updateUserCityGem(userId, cityId, gemId, newQuantity) ) {
      log.error("unexpected error: did not update the user's gems. "
          + "userCityGem=" + ucg);
    }
  }
  
  private boolean checkCityRankup(int cityId, List<Task> tasksInCity,
      Map<Integer, Integer> taskIdToNumTimesActedInRank) {
    
    if (tasksInCity == null) {
      return false;
    }
    
    //loop through all tasks for the city and see if user completed them
    for (Task task : tasksInCity) {
      int taskId = task.getId();
      int numActionsToCompleteTask = task.getNumForCompletion();
      
      //check if user did the task or finished it
      if (!taskIdToNumTimesActedInRank.containsKey(taskId) ||
          taskIdToNumTimesActedInRank.get(taskId) < numActionsToCompleteTask) {
        //user did not complete or start the task
        return false;
      }
    }
    
    return true;
  }
  
  private int coinBonusOnCityRankup(City aCity, int cityRank) {
    if (ControllerConstants.NOT_SET == cityRank) {
      return ControllerConstants.NOT_SET;
    }
    
    int maxMultiplier = 
        ControllerConstants.TASK_ACTION__MAX_CITY_RANK_UP_REWARD_MULTIPLIER;
    int multiplier = Math.min(cityRank + 1, maxMultiplier);
    int coinBonus = multiplier * aCity.getCoinsGainedBaseOnRankup();
    return coinBonus;
  }

  private int expBonusOnCityRankup(City aCity, int cityRank) {
    if (ControllerConstants.NOT_SET == cityRank) {
      return ControllerConstants.NOT_SET;
    }
    
    int maxMultiplier = 
        ControllerConstants.TASK_ACTION__MAX_CITY_RANK_UP_REWARD_MULTIPLIER;
    int multiplier = Math.min(cityRank + 1, maxMultiplier);
    int coinBonus = multiplier * aCity.getExpGainedBaseOnRankup();
    
    return coinBonus;
  }
  
  private void setBuilderStuff(Builder resBuilder, int lockBoxEventId,
      UserCityGem ucg, int coinBonus, int expBonus, boolean taskCompleted,
      boolean cityRankedUp) {
    if (lockBoxEventId != ControllerConstants.NOT_SET) {
      resBuilder.setEventIdOfLockBoxGained(lockBoxEventId);
    }
    if (null != ucg) {
      UserCityGemProto ucgp = 
          CreateInfoProtoUtils.createUserCityGemProto(ucg);
      resBuilder.setGem(ucgp);
    }
    if (ControllerConstants.NOT_SET != coinBonus) {
      resBuilder.setCoinBonusIfCityRankup(coinBonus);
    }
    if (ControllerConstants.NOT_SET != expBonus) {
      resBuilder.setExpBonusIfCityRankup(expBonus);
    }

    resBuilder.setTaskCompleted(taskCompleted);
    resBuilder.setCityRankedUp(cityRankedUp);
  }
  
  private int sumUpCoinsGained(boolean legitAction, int coinsGained,
      int coinBonus) {
    if (!legitAction) {
      return 0;
    }
    
    int totalCoinGain = 0;
    if (coinsGained != ControllerConstants.NOT_SET) {
      totalCoinGain += coinsGained;
    }
    if (coinBonus != ControllerConstants.NOT_SET) {
      totalCoinGain += coinBonus;
    }
    return totalCoinGain;
  }
  
  private int sumUpExpGained(boolean legitAction, Task task, int expBonus) {
    if (!legitAction) {
      return 0;
    }
    
    int totalExpGain = 0;
    if (task != null) {
      totalExpGain += task.getExpGained();
    }
    if (expBonus != ControllerConstants.NOT_SET) {
      totalExpGain += expBonus;
    }
    return totalExpGain;
  }
  
  private int chooseLootEquipId(Task task) {
    if (task.getPotentialLootEquipIds() == null
        || task.getPotentialLootEquipIds().size() <= 1) {
      return ControllerConstants.NOT_SET;
    }
    if (Math.random() < task.getChanceOfEquipFloat()) {
      int randIndex = (int) (Math.random() * task
          .getPotentialLootEquipIds().size());
      return task.getPotentialLootEquipIds().get(randIndex);
    }
    return ControllerConstants.NOT_SET;
  }

  private int calculateCoinsGained(Builder resBuilder, Task task) {
    //TODO: CALCULATE COINS GIVEN BACK
    int coinsGained = task.getMinCoinsGained()
        + (int) (Math.random() * (task.getMaxCoinsGained() + 1 - task
            .getMinCoinsGained()));
    resBuilder.setCoinsGained(coinsGained);
    return coinsGained;
  }
  
//  private int calculateExpGained(Builder resBuilder, Task task) {
//    
//    return 0;
//  }

  private boolean checkLegitAction(User user, Task task,
      Timestamp clientTime, Builder resBuilder, List<Integer> cityRankList) {
    if (task == null || clientTime == null) {
      resBuilder.setStatus(TaskActionStatus.OTHER_FAIL);
      log.error("parameter passed in is null. task=" + task
          + ", clientTime=" + clientTime);
      return false;
    }

    if (!MiscMethods.checkClientTimeAroundApproximateNow(clientTime)) {
      resBuilder
      .setStatus(TaskActionStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time="
          + clientTime + ", servertime~=" + new Date());
      return false;
    }

    int userId = user.getId();
    int cityId = task.getCityId();
    int cityRank = RetrieveUtils.userCityRetrieveUtils()
        .getCurrentCityRankForUser(userId, cityId);
    int cost = MiscMethods.taskEnergyCostForCityRank(task, cityRank);
    if (user.getEnergy() < cost) {
      resBuilder.setStatus(TaskActionStatus.USER_NOT_ENOUGH_ENERGY);
      log.error("user does not have enough energy. user's energy="
          + user.getEnergy() + ", task action requires"
          + cost);
      return false;
    }
    //return the city rank
    cityRankList.add(cityRank);

    int numReqEquipsWithoutQuantityReqFulfilled = getNumRequiredEquipmentsWithoutQuantityRequirementFulfilled(
        user, task);
    if (numReqEquipsWithoutQuantityReqFulfilled != 0) {
      resBuilder.setStatus(TaskActionStatus.USER_NOT_ALL_REQUIRED_ITEMS);
      log.error("user does not have all equips required for tasks. missing x types of equips, x="
          + numReqEquipsWithoutQuantityReqFulfilled);
      return false;
    }
    resBuilder.setStatus(TaskActionStatus.SUCCESS);
    return true;
  }

  private int getNumRequiredEquipmentsWithoutQuantityRequirementFulfilled(
      User user, Task task) {
    Map<Integer, Integer> equipIdsToQuantityReq = TaskEquipReqRetrieveUtils
        .getEquipmentIdsToQuantityForTaskId(task.getId());
    Map<Integer, List<UserEquip>> userEquips = RetrieveUtils
        .userEquipRetrieveUtils().getEquipIdsToUserEquipsForUser(
            user.getId());

    if (equipIdsToQuantityReq == null)
      return 0;

    int numReqEquipsWithoutQuantityReqFulfilled = equipIdsToQuantityReq
        .keySet().size();

    if (userEquips != null) {
      for (Integer userEquipId : userEquips.keySet()) {
        List<UserEquip> userEquipsForEquipId = userEquips
            .get(userEquipId);
        int numOfEquipUserHas = 0;
            for (UserEquip ue : userEquipsForEquipId) {
              numOfEquipUserHas += Math.pow(2, ue.getLevel()-1);
            }
            Integer quantityReq = equipIdsToQuantityReq.get(userEquipId);
            if (quantityReq != null && quantityReq <= numOfEquipUserHas) {
              numReqEquipsWithoutQuantityReqFulfilled--;
              if (numReqEquipsWithoutQuantityReqFulfilled == 0)
                break;
            } else {
              if (quantityReq != null && quantityReq > 0)
                log.error("user is missing equips of id " + userEquipId
                    + ". has " + numOfEquipUserHas + ", need "
                    + quantityReq);
            }
      }
    }
    return numReqEquipsWithoutQuantityReqFulfilled;
  }

  private int checkIfUserAcquiresLockBox(User user, Task task, Timestamp curTime) {
    Map<Integer, LockBoxEvent> events = LockBoxEventRetrieveUtils.getLockBoxEventIdsToLockBoxEvents();
    LockBoxEvent curEvent = null;
    for (LockBoxEvent event : events.values()) {
      if (curTime.getTime() > event.getStartDate().getTime() && curTime.getTime() < event.getEndDate().getTime()) {
        curEvent = event;
        break;
      }
    }

    if (curEvent != null) {
      float chanceToAttainBox = Math.min(ControllerConstants.LOCK_BOXES__CHANCE_TO_ACQUIRE_FROM_TASK_BASE*task.getEnergyCost(), ControllerConstants.LOCK_BOXES__CHANCE_TO_ACQUIRE_FROM_TASK_MAX);
      if (Math.random() < chanceToAttainBox) {
        return curEvent.getId();
      }
    }
    return ControllerConstants.NOT_SET;
  }
  
  //for now, give a user one gem until the player has all
  //five gems, then don't give any gems
  //starting users should get all non boss gems before the
  //city's first rank up
  private UserCityGem checkIfGemDropped(int userId, User user, int cityId,
      City c, int cityRank, Map<Integer, Integer> taskIdToNumTimesActedInRank) {
    Map<Integer, UserCityGem> gemIdsToUserCityGems = UserCityGemRetrieveUtils
        .getGemIdsToGemsForUserAndCity(userId, cityId);
    
    boolean hasPrestige = (user.getPrestigeLevel() > 0);
    boolean isKirinVillage =
        (c.getId() == ControllerConstants.TUTORIAL__FIRST_NEUTRAL_CITY_ID);
    boolean kirinVillageUnranked = (1 == cityRank);
    
    if (!hasPrestige && isKirinVillage && kirinVillageUnranked) {
      //starting users should get all non boss gems before the
      //city's first rank up
      return selectGemForNoob(userId, cityId, gemIdsToUserCityGems,
          taskIdToNumTimesActedInRank);
    } else {
      return selectGemRandomly(userId, cityId, gemIdsToUserCityGems);
    }
  }
  
  //taskIdToNumActionsBefore contains the tasks user did before
  //updating the user's tasks
  //taskIdToNumTimesActedInRank contains the tasks user did after
  //updating the user's tasks
  private UserCityGem selectGemForNoob(int userId, int cityId,
      Map<Integer, UserCityGem> gemIdsToUserCityGems,
      Map<Integer, Integer> taskIdToNumTimesActedInRank) {
    
    //if user has all non boss gems, exit
    List<Integer> nonbossGemIds = 
        CityGemRetrieveUtils.getNonbossGemsIds();
    if (gemIdsToUserCityGems.size() == nonbossGemIds.size()) {
      return null;
    }
    int absoluteNumTapsToRankupCity = 
        TaskRetrieveUtils.getNumTapsToRankupCity(cityId);
    
    //logic now is, every 3rd tap done, give a gem, user starts off
    //with 2 taps
    List<Task> tasksForCity =
        TaskRetrieveUtils.getAllTasksForCityId(cityId);
    CityGem newGem = gemForCurrentTap(tasksForCity, taskIdToNumTimesActedInRank,
        nonbossGemIds, absoluteNumTapsToRankupCity, gemIdsToUserCityGems);
    
    UserCityGem returnVal = null;
    //if user got a gem, increment the quantity user has by one
    if (null != newGem) {
      int gemId = newGem.getId();
      if (!gemIdsToUserCityGems.containsKey(gemId)) {
        UserCityGem ucg = new UserCityGem(userId, cityId, gemId, 0);
        gemIdsToUserCityGems.put(gemId, ucg);
      }
      returnVal = gemIdsToUserCityGems.get(gemId);
      int newQuantity = returnVal.getQuantity() + 1;
      returnVal.setQuantity(newQuantity);
    }
    return returnVal;
  }
  
  //nonbossGemIds is modified by this method
  private CityGem gemForCurrentTap(List<Task> tasksForCity,
      Map<Integer, Integer> taskIdToNumTimesActedInRank,
      List<Integer> nonbossGemIds, int absoluteNumTapsToRankupCity,
      Map<Integer, UserCityGem> gemIdsToUserCityGems) {
    
    //find out the average amount of taps to get all non boss gems
    //before ranking up a city, so if currentTapNum is a multiple of it
    //give a gem
    int avgTapsToProduceGem =
        absoluteNumTapsToRankupCity / nonbossGemIds.size(); 
    
    //calculate the current tap from the tasks user has done
    int currentTapNum = getCurrentTapNum(tasksForCity,
        taskIdToNumTimesActedInRank);
    
    //if user can get a gem, give him a gem he has not gotten already
    if (0 != currentTapNum % avgTapsToProduceGem) {
      //not time to get a gem
      return null;
    }
    
    //retain gems user has not gotten yet
    nonbossGemIds.removeAll(gemIdsToUserCityGems.keySet());
    //give first available gem
    CityGem returnVal = null;
    if (!nonbossGemIds.isEmpty()) {
      int gemId = nonbossGemIds.get(0);
      returnVal = CityGemRetrieveUtils.getCityGemForId(gemId);
    }
    return returnVal;
  }
  
  //calculate the current tap from the tasks user has done
  private int getCurrentTapNum(List<Task> tasksForCity,
      Map<Integer, Integer> taskIdToNumTimesActedInRank) {
    int currentTapNum = 0;
    for (Task aTask : tasksForCity) {
      int taskId = aTask.getId();
      if (taskIdToNumTimesActedInRank.containsKey(taskId)) {
        currentTapNum += taskIdToNumTimesActedInRank.get(taskId);
      }
    }
    return currentTapNum;
  }
  
  //initial maybe 25% hurdle to get a gem
  //if user gets a gem then gem dropping scheme is thus:
  //sum of drop rates for non boss gems = 1.
  //Generate randomNumber.
  //probabilityForNoGem = sum of drop rates for gems user has already
  //if randomNumber < probabilityForNoGem 
  //  then user does not get a gem
  //currentDropRate = probabilityForNoGem
  //if random number < currentDropRate + current gem drop rate,
  //  then this is the gem the user gets
  //
  private UserCityGem selectGemRandomly(int userId, int cityId,
      Map<Integer, UserCityGem> gemIdsToUserCityGems) {
    float dropProbability = ControllerConstants.TASK_ACTION__GEM_DROP_RATE;
    Random rand = new Random();
    float randFloat = rand.nextFloat();
    //check if a gem drops
    if (randFloat >= dropProbability) {
      //user does not get a gem
      return null;
    }
    
    Map<Integer, CityGem> gemIdsToActiveCityGems = 
        CityGemRetrieveUtils.getActiveCityGemIdsToCityGems();
    if (null == gemIdsToActiveCityGems || gemIdsToActiveCityGems.isEmpty()) {
      log.error("unexpected error: no city gems in the db!");
      return null;
    }
    
    boolean getBossGem = false;
    //select the next nonboss gem the user gets
    boolean allowDuplicates = false;
    CityGem cg = MiscMethods.selectCityGemOne(allowDuplicates, getBossGem,
        gemIdsToUserCityGems, gemIdsToActiveCityGems, rand);
//    boolean allowDuplicates = true;
//    CityGem cg = MiscMethods.selectCityGemTwo(allowDuplicates, getBossGem,
//        gemIdsToUserCityGems, gemIdsToActiveCityGems, rand);
    
    UserCityGem returnValue = null;
    if (null != cg) {
      //increment the quantity by 1
      int gemId = cg.getId();
      
      //base case, in case user does not have this gem
      if (!gemIdsToUserCityGems.containsKey(gemId)) {
        UserCityGem ucg = new UserCityGem(userId, cityId, gemId, 0);
        gemIdsToUserCityGems.put(gemId, ucg);
      }
      returnValue = gemIdsToUserCityGems.get(gemId);
      int newQuantity = returnValue.getQuantity() + 1;
      returnValue.setQuantity(newQuantity);
    }
    return returnValue;
  }

  //store in Map<Integer, Integer> taskIdToNumTimesActedInRank the 
  //updated amount of times the user did this task
  private int determineNumTimesTaskCompleted(int taskId,
      Map<Integer, Integer> taskIdToNumTimesActedInRank) {
    //base case (or when gained prestige and questions and stuff are reset) 
    int numTimesActedInRank = 0;
    
    if (taskIdToNumTimesActedInRank.get(taskId) != null) {
      numTimesActedInRank = taskIdToNumTimesActedInRank.get(taskId);
    }
    //user did the task once, increase by 1
    numTimesActedInRank++;
    taskIdToNumTimesActedInRank.put(taskId, numTimesActedInRank);
    
    return numTimesActedInRank;
  }
  

  private int giveEquip(Builder resBuilder, int userId, int lootEquipId) {
    if (lootEquipId != ControllerConstants.NOT_SET) {
      int userEquipId = InsertUtils.get().insertUserEquip(
          userId, lootEquipId,
          ControllerConstants.DEFAULT_USER_EQUIP_LEVEL,
          ControllerConstants.DEFAULT_USER_EQUIP_ENHANCEMENT_PERCENT);
      if (userEquipId < 0) {
        log.error("problem with giving 1 of equip " + lootEquipId + 
            " to task completer " + userId);
        lootEquipId = ControllerConstants.NOT_SET;
      } else {
        UserEquip newUE = new UserEquip(userEquipId, userId, lootEquipId,
            ControllerConstants.DEFAULT_USER_EQUIP_LEVEL, 0);
        FullUserEquipProto fuep = CreateInfoProtoUtils
            .createFullUserEquipProtoFromUserEquip(newUE);
        resBuilder.setLootUserEquip(fuep);
      }
    }
    
    return lootEquipId;
  }
  
  
  public void writeToUserCurrencyHistory(User aUser, int taskId, int coinChange, int previousSilver) {
    Timestamp date = new Timestamp((new Date()).getTime());

    Map<String, Integer> goldSilverChange = new HashMap<String, Integer>();
    Map<String, Integer> previousGoldSilver = new HashMap<String, Integer>();
    Map<String, String> reasonsForChanges = new HashMap<String, String>();
    String silver = MiscMethods.silver;
    String reasonForChange = ControllerConstants.UCHRFC__TASK_ACTION + taskId;
    
    goldSilverChange.put(silver, coinChange);
    previousGoldSilver.put(silver, previousSilver);
    reasonsForChanges.put(silver, reasonForChange);
    
    MiscMethods.writeToUserCurrencyOneUserGoldAndOrSilver(aUser, date, goldSilverChange,
        previousGoldSilver, reasonsForChanges);
  }
}
