package com.lvl6.server.controller;

import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.TaskActionRequestEvent;
import com.lvl6.events.response.TaskActionResponseEvent;
import com.lvl6.info.Task;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.proto.EventProto.TaskActionRequestProto;
import com.lvl6.proto.EventProto.TaskActionResponseProto;
import com.lvl6.proto.EventProto.TaskActionResponseProto.Builder;
import com.lvl6.proto.EventProto.TaskActionResponseProto.TaskActionStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskEquipReqRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;

public class TaskActionController extends EventController {
  
  private static final int NOT_SET = -1;
  
  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());    
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
  protected void processRequestEvent(RequestEvent event) {
    TaskActionRequestProto reqProto = ((TaskActionRequestEvent)event).getTaskActionRequestProto();
    MinimumUserProto senderProto = reqProto.getSender();
    int taskId = reqProto.getTaskId();

    User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
    TaskActionResponseProto.Builder resBuilder = TaskActionResponseProto.newBuilder();
    resBuilder.setSender(senderProto);    
    
    Task task = TaskRetrieveUtils.getTaskForTaskId(taskId);

    int coinsGained = NOT_SET;
    int lootEquipId = NOT_SET;
    boolean taskCompleted = false;
    boolean cityRankedUp = false;

    boolean legitAction = checkLegitAction(user, task, resBuilder);
    if (legitAction) {
      coinsGained = calculateCoinsGained(task);
      resBuilder.setCoinsGained(coinsGained);
      lootEquipId = chooseLootEquipId(task);
      if (lootEquipId != NOT_SET) {
        resBuilder.setLootEquipId(lootEquipId);
      }

      //TODO: check if task is completed. if so, check if city ranked up.
    }

    resBuilder.setTaskCompleted(taskCompleted);
    resBuilder.setCityRankedUp(cityRankedUp);

    TaskActionResponseProto resProto = resBuilder.build();
    TaskActionResponseEvent resEvent = new TaskActionResponseEvent(senderProto.getUserId());
    resEvent.setTaskActionResponseProto(resProto);
    server.writeEvent(resEvent);
    
    //TODO: write to db
  }


  private int chooseLootEquipId(Task task) {
    if (Math.random() < task.getChanceOfEquipFloat()) {
      int randIndex = (int)(Math.random() * task.getPotentialLootEquipIds().size());
      return task.getPotentialLootEquipIds().get(randIndex);
    }
    return NOT_SET;
  }

  private int calculateCoinsGained(Task task) {
    return task.getMinCoinsGained() + (int)Math.random()*(task.getMaxCoinsGained() + 1 - task.getMinCoinsGained());
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
      if (user.getArmySize() < task.getMinArmySize()) {
        resBuilder.setStatus(TaskActionStatus.USER_NOT_ENOUGH_ARMY);
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

    int numReqEquipsWithoutQuantityReqFulfilled = equipIdsToQuantityReq.keySet().size();
    
    for (UserEquip ue : userEquipIds) {
      Integer quantityReq = equipIdsToQuantityReq.get(ue.getEquipId());
      if (quantityReq != null && quantityReq < ue.getQuantity()) {
        numReqEquipsWithoutQuantityReqFulfilled--;
        if (numReqEquipsWithoutQuantityReqFulfilled == 0) break;
      }
    }
    
    return numReqEquipsWithoutQuantityReqFulfilled;
  }


}
