package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveAvailableMenteesRequestEvent;
import com.lvl6.events.response.RetrieveAvailableMenteesResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.RetrieveAvailableMenteesRequestProto;
import com.lvl6.proto.EventProto.RetrieveAvailableMenteesResponseProto;
import com.lvl6.proto.EventProto.RetrieveAvailableMenteesResponseProto.RetrieveAvailableMenteesStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.MinimumUserProtoWithCreateTime;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MentorshipRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;

  @Component @DependsOn("gameServer") public class RetrieveAvailableMenteesController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveAvailableMenteesController() {
    numAllocatedThreads = 2;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveAvailableMenteesRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_AVAILABLE_MENTEES_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveAvailableMenteesRequestProto reqProto = ((RetrieveAvailableMenteesRequestEvent)event)
        .getRetrieveAvailableMenteesRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userId = senderProto.getUserId();
    
    RetrieveAvailableMenteesResponseProto.Builder resBuilder = RetrieveAvailableMenteesResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    
    RetrieveAvailableMenteesResponseEvent resEvent = new RetrieveAvailableMenteesResponseEvent(userId);
    //no lock since new users change so rapidly(?)
    try {
      long subtrahend = 60 * 1000 * 
          ControllerConstants.MENTORSHIPS__SUBTRAHEND_IN_MINUTES_TO_NOW_TO_FIND_MENTEE;
      Date lastLoginAfterNow = new Date((new Date()).getTime() - subtrahend);
      int limit = ControllerConstants.MENTORSHIPS__MAX_MENTEES_TO_RETRIEVE;
      //get all the mentees
      List<Integer> idsOfTakenMentees = MentorshipRetrieveUtils.getAllMenteeIds();
      List<User> availableMentees = RetrieveUtils.userRetrieveUtils().getMentees(idsOfTakenMentees,
          lastLoginAfterNow, limit);
      
      //convert to protos
      List<MinimumUserProtoWithCreateTime> allMenteeMups = new ArrayList<MinimumUserProtoWithCreateTime>();
      for (User mentee : availableMentees) {
        MinimumUserProtoWithCreateTime mupwct = CreateInfoProtoUtils.createMinimumUserProtoWithCreateTime(mentee);
        allMenteeMups.add(mupwct);
      }
      
      //send to client
      resBuilder.addAllMentees(allMenteeMups);
      resBuilder.setStatus(RetrieveAvailableMenteesStatus.SUCCESS);
      
      resEvent.setRetrieveAvailableMenteesResponseProto(resBuilder.build());
      
      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrieveAvailableMenteesController processEvent", e);
      try {
        resBuilder.setStatus(RetrieveAvailableMenteesStatus.FAIL_OTHER);
        resEvent.setRetrieveAvailableMenteesResponseProto(resBuilder.build());
        server.writeEvent(resEvent);
      } catch (Exception e2) {
        
      }
    }
    
  }

 
}
