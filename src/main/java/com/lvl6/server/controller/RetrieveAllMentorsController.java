package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveAllMentorsRequestEvent;
import com.lvl6.events.response.RetrieveAllMentorsResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.RetrieveAllMentorsRequestProto;
import com.lvl6.proto.EventProto.RetrieveAllMentorsResponseProto;
import com.lvl6.proto.EventProto.RetrieveAllMentorsResponseProto.RetrieveAllMentorsStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;

  @Component @DependsOn("gameServer") public class RetrieveAllMentorsController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveAllMentorsController() {
    numAllocatedThreads = 1;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveAllMentorsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_ALL_MENTORS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveAllMentorsRequestProto reqProto = ((RetrieveAllMentorsRequestEvent)event).getRetrieveAllMentorsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userId = senderProto.getUserId();
    
    RetrieveAllMentorsResponseProto.Builder resBuilder = RetrieveAllMentorsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    
    RetrieveAllMentorsResponseEvent resEvent = new RetrieveAllMentorsResponseEvent(userId);
    try {
      List<User> allMentors = RetrieveUtils.userRetrieveUtils().getAllMentors();
      
      List<MinimumUserProto> allMentorMups = new ArrayList<MinimumUserProto>();
      for (User mentor : allMentors) {
        MinimumUserProto mup = CreateInfoProtoUtils.createMinimumUserProtoFromUser(mentor);
        allMentorMups.add(mup);
      }
      
      resBuilder.addAllMentors(allMentorMups);
      resBuilder.setStatus(RetrieveAllMentorsStatus.SUCCESS);
      
      resEvent.setRetrieveAllMentorsResponseProto(resBuilder.build());
      
      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrieveAllMentorsController processEvent", e);
      try {
        resBuilder.setStatus(RetrieveAllMentorsStatus.FAIL_OTHER);
        resEvent.setRetrieveAllMentorsResponseProto(resBuilder.build());
        server.writeEvent(resEvent);
      } catch (Exception e2) {
        
      }
    }
    
  }

 
}
