package com.lvl6.server.controller;

import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.TaskActionRequestEvent;
import com.lvl6.events.response.TaskActionResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.City;
import com.lvl6.info.Quest;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.info.UserQuest;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.TaskActionRequestProto;
import com.lvl6.proto.EventProto.TaskActionResponseProto;
import com.lvl6.proto.EventProto.TaskActionResponseProto.Builder;
import com.lvl6.proto.EventProto.TaskActionResponseProto.TaskActionStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserCityRetrieveUtils;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestRetrieveUtils;
import com.lvl6.retrieveutils.UserQuestsCompletedTasksRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserTaskRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskEquipReqRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class TaskActionController extends EventController {

  @Override
  public RequestEvent createRequestEvent() {
    return new TaskActionRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_TASK_ACTION_EVENT;
  }

  /*
   * db stuff done before sending event to eventwriter/client because the client's not waiting 
   * on it immediately anyways
   */
  @Override
  protected void processRequestEvent(RequestEvent event) {
    TaskActionRequestProto reqProto = ((TaskActionRequestEvent)event).getTaskActionRequestProto();
    MinimumUserProto senderProto = reqProto.getSender();
    int taskId = reqProto.getTaskId();

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      TaskActionResponseProto.Builder resBuilder = TaskActionResponseProto.newBuilder();
      resBuilder.setSender(senderProto);    

      Task task = TaskRetrieveUtils.getTaskForTaskId(taskId);
      //TODO: check the level of this task's city, use that as multiplier for expGained, energyCost, etc.

      int coinsGained = ControllerConstants.TASK_ACTION__NOT_SET;
      int lootEquipId = ControllerConstants.TASK_ACTION__NOT_SET;
      int coinBonus  = ControllerConstants.TASK_ACTION__NOT_SET;
      int expBonus = ControllerConstants.TASK_ACTION__NOT_SET;
      boolean taskCompleted = false;
      boolean cityRankedUp = false;
      boolean changeNumTimesUserActedInDB = true;
      List<Task> tasksInCity = null;

      boolean legitAction = checkLegitAction(user, task, resBuilder);
      if (legitAction) {
        coinsGained = calculateCoinsGained(task);
        resBuilder.setCoinsGained(coinsGained);
        lootEquipId = chooseLootEquipId(task);
        if (lootEquipId != ControllerConstants.TASK_ACTION__NOT_SET) {
          resBuilder.setLootEquipId(lootEquipId);
        }
        Map<Integer, Integer> taskIdToNumTimesActedInRank = UserTaskRetrieveUtils.getTaskIdToNumTimesActedInRankForUser(senderProto.getUserId());
        int numTimesActedInRank = 0;
        if (taskIdToNumTimesActedInRank != null && taskIdToNumTimesActedInRank.get(task.getId()) != null) {
          numTimesActedInRank = taskIdToNumTimesActedInRank.get(task.getId());
        }
        numTimesActedInRank++;

        taskIdToNumTimesActedInRank.put(task.getId(), numTimesActedInRank);

        if (numTimesActedInRank > task.getNumForCompletion()) {
          changeNumTimesUserActedInDB = false;
        }
        if (numTimesActedInRank == task.getNumForCompletion()) {
          taskCompleted = true;
          tasksInCity = TaskRetrieveUtils.getAllTasksForCityId(task.getCityId());
          cityRankedUp = checkCityRankup(taskIdToNumTimesActedInRank, task.getCityId(), tasksInCity);
          if (cityRankedUp) {
            int cityRank = UserCityRetrieveUtils.getCurrentCityRankForUser(user.getId(), task.getCityId());
            if (cityRank != ControllerConstants.TASK_ACTION__NOT_SET) {
              if (cityRank == ControllerConstants.TASK_ACTION__MAX_CITY_RANK) {
                cityRankedUp = false;
              }
              cityRank++;
              City city = CityRetrieveUtils.getCityForCityId(task.getCityId());
              int multiplier = cityRank;
              coinBonus = multiplier * city.getCoinsGainedBaseOnRankup();
              resBuilder.setCoinBonusIfCityRankup(coinBonus);
              expBonus = multiplier * city.getExpGainedBaseOnRankup();
              resBuilder.setExpBonusIfCityRankup(expBonus);
            }
          }

        }
      }

      resBuilder.setTaskCompleted(taskCompleted);
      resBuilder.setCityRankedUp(cityRankedUp);

      TaskActionResponseProto resProto = resBuilder.build();
      TaskActionResponseEvent resEvent = new TaskActionResponseEvent(senderProto.getUserId());
      resEvent.setTaskActionResponseProto(resProto);
      server.writeEvent(resEvent);

      int totalCoinGain = 0;
      int totalExpGain = 0;
      if (legitAction) {
        if (coinsGained != ControllerConstants.TASK_ACTION__NOT_SET) totalCoinGain += coinsGained;
        if (coinBonus != ControllerConstants.TASK_ACTION__NOT_SET) totalCoinGain += coinBonus;
        if (task != null) totalExpGain += task.getExpGained();
        if (expBonus != ControllerConstants.TASK_ACTION__NOT_SET) totalExpGain += expBonus;
      }

      writeChangesToDB(legitAction, user, task, cityRankedUp, changeNumTimesUserActedInDB, lootEquipId, 
          totalCoinGain, totalExpGain, tasksInCity);
      //TODO: should these send new response? or package inside battles?
      //TODO: AchievementCheck.checkBattle(); 
      //TODO: LevelCheck.checkUser();

      if (legitAction) {
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        server.writeEvent(resEventUpdate);
        if (taskCompleted) {
          checkQuestsPostTaskAction(user, task, senderProto);
        }
      }
    } catch (Exception e) {
      log.error("exception in TaskActionController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private void checkQuestsPostTaskAction(User user, Task task, MinimumUserProto senderProto) {
    List<UserQuest> inProgressUserQuests = UserQuestRetrieveUtils.getInProgressUserQuestsForUser(user.getId());
    if (inProgressUserQuests != null) {
      for (UserQuest userQuest : inProgressUserQuests) {
        if (!userQuest.isTasksComplete()) {
          Quest quest = QuestRetrieveUtils.getQuestForQuestId(userQuest.getQuestId());
          if (quest != null) {
            if (quest.getTasksRequired() != null && quest.getTasksRequired().contains(task.getId())) {
              List<Integer> userCompletedTasksForQuest = UserQuestsCompletedTasksRetrieveUtils.
                  getUserTasksCompletedForQuest(user.getId(), quest.getId());
              if (!userCompletedTasksForQuest.contains(task.getId())) {
                if (InsertUtils.insertCompletedTaskIdForUserQuest(user.getId(), task.getId(), quest.getId())) {
                  userCompletedTasksForQuest.add(task.getId());
                  if (userCompletedTasksForQuest.containsAll(quest.getTasksRequired())) {
                    if (UpdateUtils.updateUserQuestsSetCompleted(user.getId(), quest.getId(), true, false, false)) {
                      userQuest.setTasksComplete(true);
                      QuestUtils.checkAndSendQuestComplete(server, quest, userQuest, senderProto);
                    } else {
                      log.error("problem with marking tasks completed for a user quest");
                    }
                  }
                } else {
                  log.error("problem with adding task to user's completed tasks for quest");
                }
              }
            }
          }
        }
      }
    }
  }


  private void writeChangesToDB(boolean legitAction, User user, Task task, boolean cityRankedUp, boolean changeNumTimesUserActedInDB, 
      int lootEquipId, int totalCoinGain, int totalExpGain, List<Task> tasksInCity) {

    if (legitAction) {
      if (cityRankedUp) {
        if (!UpdateUtils.incrementCityRankForUserCity(user.getId(), task.getCityId(), 1)) {
          log.error("problem with updating user city rank post-task");
        }
        if (tasksInCity != null) {
          if (!UpdateUtils.resetTimesCompletedInRankForUserTasksInCity(user.getId(), tasksInCity)) {
            log.error("problem with resetting user times completed in rank post-task");
          }
        }
      } else {
        if (changeNumTimesUserActedInDB) {
          if (!UpdateUtils.incrementTimesCompletedInRankForUserTask(user.getId(), task.getId(), 1)) {
            log.error("problem with incrementing user times completed in rank post-task");
          }
        }
      }

      if (lootEquipId != ControllerConstants.TASK_ACTION__NOT_SET) {
        if (!UpdateUtils.incrementUserEquip(user.getId(), lootEquipId, 1)) {
          log.error("problem with incrementing user equip post-task");
        }
      }
      if (!user.updateRelativeCoinsExpTaskscompletedEnergy(totalCoinGain, totalExpGain, 1, task.getEnergyCost()*-1)) {
        log.error("problem with updating user stats post-task");
      }
    }
  }

  private boolean checkCityRankup(
      Map<Integer, Integer> taskIdToNumTimesActedInRank, int cityId, List<Task> tasksInCity) {
    tasksInCity = TaskRetrieveUtils.getAllTasksForCityId(cityId);

    if (tasksInCity == null) {
      return false;
    }

    for (Task task : tasksInCity) {
      if (!taskIdToNumTimesActedInRank.containsKey(task.getId()) ||
          taskIdToNumTimesActedInRank.get(task.getId()) < task.getNumForCompletion()) {
        return false;
      }
    }    
    return true;
  }

  private int chooseLootEquipId(Task task) {
    if (Math.random() < task.getChanceOfEquipFloat()) {
      int randIndex = (int)(Math.random() * task.getPotentialLootEquipIds().size());
      return task.getPotentialLootEquipIds().get(randIndex);
    }
    return ControllerConstants.TASK_ACTION__NOT_SET;
  }

  private int calculateCoinsGained(Task task) {
    return task.getMinCoinsGained() + (int)(Math.random()*(task.getMaxCoinsGained() + 1 - task.getMinCoinsGained()));
  }

  private boolean checkLegitAction(User user, Task task, Builder resBuilder) {
    boolean actionIsLegit = true;
    if (task == null) {
      resBuilder.setStatus(TaskActionStatus.INVALID_TASK_ID);
      actionIsLegit = false;
    } else {
      if (user.getEnergy() < task.getEnergyCost()) {
        resBuilder.setStatus(TaskActionStatus.USER_NOT_ENOUGH_ENERGY);
        actionIsLegit = false;
      }

      int numReqEquipsWithoutQuantityReqFulfilled = getNumRequiredEquipmentsWithoutQuantityRequirementFulfilled(user, task);
      if (numReqEquipsWithoutQuantityReqFulfilled != 0) {
        resBuilder.setStatus(TaskActionStatus.USER_NOT_ALL_REQUIRED_ITEMS);
        actionIsLegit = false;
      }

    }
    if (actionIsLegit) {
      resBuilder.setStatus(TaskActionStatus.SUCCESS);
    }
    return actionIsLegit;
  }

  private int getNumRequiredEquipmentsWithoutQuantityRequirementFulfilled(User user, Task task) {
    Map<Integer, Integer> equipIdsToQuantityReq = TaskEquipReqRetrieveUtils.getEquipmentIdsToQuantityForTaskId(task.getId());
    List<UserEquip> userEquipIds = UserEquipRetrieveUtils.getUserEquipsForUser(user.getId());

    if (equipIdsToQuantityReq == null)
      return 0;

    int numReqEquipsWithoutQuantityReqFulfilled = equipIdsToQuantityReq.keySet().size();

    if (userEquipIds != null) {
      for (UserEquip ue : userEquipIds) {
        Integer quantityReq = equipIdsToQuantityReq.get(ue.getEquipId());
        if (quantityReq != null && quantityReq <= ue.getQuantity()) {
          numReqEquipsWithoutQuantityReqFulfilled--;
          if (numReqEquipsWithoutQuantityReqFulfilled == 0) break;
        }
      }
    }

    return numReqEquipsWithoutQuantityReqFulfilled;
  }


}
