package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

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
import com.lvl6.retrieveutils.UserQuestsTaskProgressRetrieveUtils;
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

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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
   * db stuff done before sending event to eventwriter/client because the client's not waiting 
   * on it immediately anyways
   */
  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    TaskActionRequestProto reqProto = ((TaskActionRequestEvent)event).getTaskActionRequestProto();
    MinimumUserProto senderProto = reqProto.getSender();
    int taskId = reqProto.getTaskId();
    Timestamp clientTime = new Timestamp(reqProto.getCurTime());

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      TaskActionResponseProto.Builder resBuilder = TaskActionResponseProto.newBuilder();
      resBuilder.setSender(senderProto);    

      Task task = TaskRetrieveUtils.getTaskForTaskId(taskId);
      //TODO: check the level of this task's city, use that as multiplier for expGained, energyCost, etc.

      int coinsGained = ControllerConstants.NOT_SET;
      int lootEquipId = ControllerConstants.NOT_SET;
      int coinBonus  = ControllerConstants.NOT_SET;
      int expBonus = ControllerConstants.NOT_SET;
      boolean taskCompleted = false;
      boolean cityRankedUp = false;
      boolean changeNumTimesUserActedInDB = true;
      List<Task> tasksInCity = null;

      boolean legitAction = checkLegitAction(user, task, clientTime, resBuilder);
      if (legitAction) {
        resBuilder.setCityId(task.getCityId());
        coinsGained = calculateCoinsGained(task);
        resBuilder.setCoinsGained(coinsGained);
        lootEquipId = chooseLootEquipId(task);
        if (lootEquipId != ControllerConstants.NOT_SET) {
          resBuilder.setLootEquipId(lootEquipId);
        }

        int cityRank = UserCityRetrieveUtils.getCurrentCityRankForUser(user.getId(), task.getCityId());
        if (cityRank < ControllerConstants.TASK_ACTION__MAX_CITY_RANK) {
          Map<Integer, Integer> taskIdToNumTimesActedInRank = UserTaskRetrieveUtils.getTaskIdToNumTimesActedInRankForUser(senderProto.getUserId());
          int numTimesActedInRank = 0;

          //this should never happen
          if (taskIdToNumTimesActedInRank == null) taskIdToNumTimesActedInRank = new HashMap<Integer, Integer>();
          
          if (taskIdToNumTimesActedInRank.get(task.getId()) != null) {
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
              if (cityRank != ControllerConstants.NOT_SET) {
                City city = CityRetrieveUtils.getCityForCityId(task.getCityId());
                int multiplier = cityRank+1;
                coinBonus = multiplier * city.getCoinsGainedBaseOnRankup();
                resBuilder.setCoinBonusIfCityRankup(coinBonus);
                expBonus = multiplier * city.getExpGainedBaseOnRankup();
                resBuilder.setExpBonusIfCityRankup(expBonus);
              }
            }
          }
        } else {
          changeNumTimesUserActedInDB = false;
        }
      }

      resBuilder.setTaskCompleted(taskCompleted);
      resBuilder.setCityRankedUp(cityRankedUp);

      TaskActionResponseProto resProto = resBuilder.build();
      TaskActionResponseEvent resEvent = new TaskActionResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setTaskActionResponseProto(resProto);
      server.writeEvent(resEvent);

      int totalCoinGain = 0;
      int totalExpGain = 0;
      if (legitAction) {
        if (coinsGained != ControllerConstants.NOT_SET) totalCoinGain += coinsGained;
        if (coinBonus != ControllerConstants.NOT_SET) totalCoinGain += coinBonus;
        if (task != null) totalExpGain += task.getExpGained();
        if (expBonus != ControllerConstants.NOT_SET) totalExpGain += expBonus;
      }

      writeChangesToDB(legitAction, user, task, cityRankedUp, changeNumTimesUserActedInDB, lootEquipId, 
          totalCoinGain, totalExpGain, tasksInCity, clientTime);

      if (legitAction) {
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        checkQuestsPostTaskAction(user, task, senderProto, lootEquipId);
      }
    } catch (Exception e) {
      log.error("exception in TaskActionController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());
    }
  }

  private void checkQuestsPostTaskAction(User user, Task task, MinimumUserProto senderProto, int lootEquipId) {
    List<UserQuest> inProgressUserQuests = UserQuestRetrieveUtils.getIncompleteUserQuestsForUser(user.getId());

    if (inProgressUserQuests != null) {
      Map<Integer, List<Integer>> questIdToUserTasksCompletedForQuestForUser = null;
      Map<Integer, Map<Integer, Integer>> questIdToTaskIdsToNumTimesActedInQuest = null;

      for (UserQuest userQuest : inProgressUserQuests) {
        boolean questCompletedAndSent = false;
        if (!userQuest.isTasksComplete()) {
          Quest quest = QuestRetrieveUtils.getQuestForQuestId(userQuest.getQuestId());
          if (quest != null) {
            List<Integer> tasksRequired = quest.getTasksRequired();
            if (tasksRequired != null) {
              if (questIdToUserTasksCompletedForQuestForUser == null) {
                questIdToUserTasksCompletedForQuestForUser = UserQuestsCompletedTasksRetrieveUtils.getQuestIdToUserTasksCompletedForQuestForUser(user.getId());
              }
              List<Integer> userCompletedTasksForQuest = questIdToUserTasksCompletedForQuestForUser.get(quest.getId());
              if (userCompletedTasksForQuest == null) userCompletedTasksForQuest = new ArrayList<Integer>();
              List<Integer> tasksRemaining = new ArrayList<Integer>(tasksRequired);
              tasksRemaining.removeAll(userCompletedTasksForQuest);

              Map<Integer, Task> remainingTaskMap = TaskRetrieveUtils.getTasksForTaskIds(tasksRemaining);
              if (remainingTaskMap != null && remainingTaskMap.size() > 0) {
                for (Task remainingTask : remainingTaskMap.values()) {
                  if (remainingTask.getId() == task.getId()) {
                    if (questIdToTaskIdsToNumTimesActedInQuest == null) {
                      questIdToTaskIdsToNumTimesActedInQuest = UserQuestsTaskProgressRetrieveUtils.getQuestIdToTaskIdsToNumTimesActedInQuest(userQuest.getUserId());
                    }
                    Map<Integer, Integer> taskIdToNumTimesActed = questIdToTaskIdsToNumTimesActedInQuest.get(userQuest.getQuestId()); 

                    if (taskIdToNumTimesActed == null) taskIdToNumTimesActed = new HashMap<Integer, Integer>();
                    if (taskIdToNumTimesActed.get(remainingTask.getId()) != null && 
                        taskIdToNumTimesActed.get(remainingTask.getId()) + 1 == remainingTask.getNumForCompletion()) {
                      //TODO: note: not SUPER necessary to delete/update them, but they do capture wrong data if complete (the one that completes is not factored in)
                      if (InsertUtils.insertCompletedTaskIdForUserQuest(user.getId(), remainingTask.getId(), quest.getId())) {
                        userCompletedTasksForQuest.add(remainingTask.getId());
                        if (userCompletedTasksForQuest.containsAll(tasksRequired)) {
                          if (UpdateUtils.updateUserQuestsSetCompleted(user.getId(), quest.getId(), true, false)) {
                            userQuest.setTasksComplete(true);
                            questCompletedAndSent = QuestUtils.checkQuestCompleteAndMaybeSendIfJustCompleted(server, quest, userQuest, senderProto, true, null);
                          } else {
                            log.error("problem with marking tasks completed for a user quest, questId=" + quest.getId());
                          }
                        }
                      } else {
                        log.error("problem with adding tasks to user's completed tasks for quest. taskId="
                            + remainingTask.getId() + ", questId=" + quest.getId());
                      }
                    } else {
                      if (!UpdateUtils.incrementUserQuestTaskProgress(user.getId(), quest.getId(), remainingTask.getId(), 1)) {
                        log.error("problem with incrementing user quest task progress by 1 for quest id " + quest.getId()
                            + ", task id=" + remainingTask.getId());
                      }
                    }
                  }
                }
              }
            }
            if (lootEquipId > ControllerConstants.NOT_SET && !questCompletedAndSent) {
              QuestUtils.checkQuestCompleteAndMaybeSendIfJustCompleted(server, quest, userQuest, senderProto, true, null);
            }
          }
        }
      }
    }
  }


  private void writeChangesToDB(boolean legitAction, User user, Task task, boolean cityRankedUp, boolean changeNumTimesUserActedInDB, 
      int lootEquipId, int totalCoinGain, int totalExpGain, List<Task> tasksInCity, Timestamp clientTime) {

    if (legitAction) {
      if (cityRankedUp) {
        if (!UpdateUtils.incrementCityRankForUserCity(user.getId(), task.getCityId(), 1)) {
          log.error("problem with incrementing user city rank post-task by " + task.getCityId());
        }
        if (tasksInCity != null) {
          if (!UpdateUtils.resetTimesCompletedInRankForUserTasksInCity(user.getId(), tasksInCity)) {
            log.error("problem with resetting user times completed in rank post-task");
          }
        }
      } else {
        if (changeNumTimesUserActedInDB) {
          if (!UpdateUtils.incrementTimesCompletedInRankForUserTask(user.getId(), task.getId(), 1)) {
            log.error("problem with incrementing user times completed in rank post-task for task " + task.getId());
          }
        }
      }

      if (lootEquipId != ControllerConstants.NOT_SET) {
        if (!UpdateUtils.incrementUserEquip(user.getId(), lootEquipId, 1)) {
          log.error("problem with giving user 1 of equip " + lootEquipId);
        }
      }

      boolean simulateEnergyRefill = (user.getEnergy() == user.getEnergyMax());
      if (!user.updateRelativeCoinsExpTaskscompletedEnergySimulateenergyrefill(totalCoinGain, totalExpGain, 1, task.getEnergyCost()*-1, simulateEnergyRefill, clientTime)) {
        log.error("problem with updating user stats post-task. coinChange=" + totalCoinGain + ", expGain="
            + totalExpGain + ", increased tasks completed by 1, energyChange=" + task.getEnergyCost()*-1 + 
            ", clientTime=" + clientTime + ", simulateEnergyRefill=" + simulateEnergyRefill + ", user=" + user);
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
    if (task.getPotentialLootEquipIds() == null || task.getPotentialLootEquipIds().size() <= 1) {
      return ControllerConstants.NOT_SET;
    }
    if (Math.random() < task.getChanceOfEquipFloat()) {
      int randIndex = (int)(Math.random() * task.getPotentialLootEquipIds().size());
      return task.getPotentialLootEquipIds().get(randIndex);
    }
    return ControllerConstants.NOT_SET;
  }

  private int calculateCoinsGained(Task task) {
    return task.getMinCoinsGained() + (int)(Math.random()*(task.getMaxCoinsGained() + 1 - task.getMinCoinsGained()));
  }

  private boolean checkLegitAction(User user, Task task, Timestamp clientTime, Builder resBuilder) {
    if (task == null || clientTime == null) {
      resBuilder.setStatus(TaskActionStatus.OTHER_FAIL);
      log.error("parameter passed in is null. task=" + task + ", clientTime=" + clientTime);
      return false;
    } 

    if (!MiscMethods.checkClientTimeAroundApproximateNow(clientTime)) {
      resBuilder.setStatus(TaskActionStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + clientTime + ", servertime~="
          + new Date());
      return false;
    }

    if (user.getEnergy() < task.getEnergyCost()) {
      resBuilder.setStatus(TaskActionStatus.USER_NOT_ENOUGH_ENERGY);
      log.error("user does not have enough energy. user's energy=" + user.getEnergy() + ", task action requires"
          + task.getEnergyCost());
      return false;
    }

    int numReqEquipsWithoutQuantityReqFulfilled = getNumRequiredEquipmentsWithoutQuantityRequirementFulfilled(user, task);
    if (numReqEquipsWithoutQuantityReqFulfilled != 0) {
      resBuilder.setStatus(TaskActionStatus.USER_NOT_ALL_REQUIRED_ITEMS);
      log.error("user does not have all equips required for tasks. missing x types of equips, x=" + numReqEquipsWithoutQuantityReqFulfilled);
      return false;
    }
    resBuilder.setStatus(TaskActionStatus.SUCCESS);
    return true;
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
        } else {
          if (quantityReq != null && quantityReq > 0)
            log.error("user is missing equips of id " + ue.getEquipId() + ". has " + ue.getQuantity() + ", need " + quantityReq);
        }
      }
    }
    return numReqEquipsWithoutQuantityReqFulfilled;
  }


}