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
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;

public class RetrieveTasksForCityController extends EventController{

  public RetrieveTasksForCityController() {
    numAllocatedThreads = 8;
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

    for (Task task : tasks) {
      FullTaskProto ftp = CreateInfoProtoUtils.createFullTaskProtoFromTask(senderProto.getUserType(), task);
      resBuilder.addTasks(ftp);
    }
    RetrieveTasksForCityResponseProto resProto = resBuilder.build();
    
    RetrieveTasksForCityResponseEvent resEvent = new RetrieveTasksForCityResponseEvent(senderProto.getUserId());
    resEvent.setTag(event.getTag());
    resEvent.setRetrieveTasksForCityResponseProto(resProto);
    
    server.writeEvent(resEvent);
  }

}
