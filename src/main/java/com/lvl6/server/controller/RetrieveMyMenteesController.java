package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveMyMenteesRequestEvent;
import com.lvl6.events.response.RetrieveMyMenteesResponseEvent;
import com.lvl6.info.Mentorship;
import com.lvl6.proto.EventProto.RetrieveMyMenteesRequestProto;
import com.lvl6.proto.EventProto.RetrieveMyMenteesResponseProto;
import com.lvl6.proto.EventProto.RetrieveMyMenteesResponseProto.RetrieveMyMenteesStatus;
import com.lvl6.proto.InfoProto.MentorshipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MentorshipRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;

  @Component @DependsOn("gameServer") public class RetrieveMyMenteesController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveMyMenteesController() {
    numAllocatedThreads = 1;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveMyMenteesRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_MY_MENTEES_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveMyMenteesRequestProto reqProto = ((RetrieveMyMenteesRequestEvent)event).getRetrieveMyMenteesRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userId = senderProto.getUserId();
    
    RetrieveMyMenteesResponseProto.Builder resBuilder = RetrieveMyMenteesResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    
    RetrieveMyMenteesResponseEvent resEvent = new RetrieveMyMenteesResponseEvent(userId);
    try {
      Map<Integer, Mentorship> myMentees = MentorshipRetrieveUtils.getActiveMenteeIdToMentorshipsForMentor(userId);
      
      List<MentorshipProto> activeMentorshipProtos = new ArrayList<MentorshipProto>();
      for (Mentorship ms : myMentees.values()) {
        MentorshipProto mp = CreateInfoProtoUtils.createMentorshipProto(ms);
        activeMentorshipProtos.add(mp);
      }
      
      resBuilder.addAllMentorships(activeMentorshipProtos);
      resBuilder.setStatus(RetrieveMyMenteesStatus.SUCCESS);
      
      resEvent.setRetrieveMyMenteesResponseProto(resBuilder.build());
      
      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrieveMyMenteesController processEvent", e);
      try {
        resBuilder.setStatus(RetrieveMyMenteesStatus.FAIL_OTHER);
        resEvent.setRetrieveMyMenteesResponseProto(resBuilder.build());
        server.writeEvent(resEvent);
      } catch (Exception e2) {
        
      }
    }
    
  }

 
}
