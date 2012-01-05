package com.lvl6.server.controller;

import java.util.List;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveTasksForCityRequestEvent;
import com.lvl6.events.response.RetrieveTasksForCityResponseEvent;
import com.lvl6.info.Task;
import com.lvl6.proto.EventProto.RetrieveTasksForCityRequestProto;
import com.lvl6.proto.EventProto.RetrieveTasksForCityResponseProto;
import com.lvl6.proto.InfoProto.FullTaskProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.MinimumUserProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.TaskRetrieveUtils;

public class RetrieveTasksForCityController extends EventController{

  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());    
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveTasksForCityRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_TASKS_FOR_CITY_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    RetrieveTasksForCityRequestProto reqProto = ((RetrieveTasksForCityRequestEvent)event).getRetrieveTasksForCityRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int cityId = reqProto.getCityId();
    List<Task> tasks = TaskRetrieveUtils.getAllTasksForCityId(cityId);
    
    RetrieveTasksForCityResponseProto.Builder resBuilder = RetrieveTasksForCityResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    boolean goodSide;
    if (senderProto.getUserType() == UserType.GOOD_ARCHER || senderProto.getUserType() == UserType.GOOD_MAGE || 
        senderProto.getUserType() == UserType.GOOD_WARRIOR) goodSide = true;
    else goodSide = false;

    String name = null;
    String description = null;
    for (Task task : tasks) {
      if (goodSide) {
        name = task.getGoodName();
        description = task.getGoodDesc();
      } else {
        name = task.getBadName();
        description = task.getBadDesc();        
      }
      FullTaskProto ftp = FullTaskProto.newBuilder().setId(task.getId()).setName(name).setDescription(description)
          .setCityId(task.getCityId()).setEnergyCost(task.getEnergyCost()).setMinCoinsGained(task.getMinCoinsGained())
          .setMaxCoinsGained(task.getMaxCoinsGained()).setChanceOfEquipLoot(task.getChanceOfEquipFloat())
          .setExpGained(task.getExpGained()).setMinArmySize(task.getMinArmySize())
          .setAssetNumWithinCity(task.getAssetNumberWithinCity()).setNumRequiredForCompletion(task.getNumForCompletion())
          .addAllEquipIds(task.getPotentialLootEquipIds()).build();
      resBuilder.addTasks(ftp);
    }
    RetrieveTasksForCityResponseProto resProto = resBuilder.build();
    
    RetrieveTasksForCityResponseEvent resEvent = new RetrieveTasksForCityResponseEvent(senderProto.getUserId());
    resEvent.setRetrieveTasksForCityResponseProto(resProto);
    
    server.writeEvent(resEvent);
    
  }

}
