package com.lvl6.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.DropMenteeRequestEvent;
import com.lvl6.events.response.DropMenteeResponseEvent;
import com.lvl6.info.Mentorship;
import com.lvl6.proto.EventProto.DropMenteeRequestProto;
import com.lvl6.proto.EventProto.DropMenteeResponseProto;
import com.lvl6.proto.EventProto.DropMenteeResponseProto.Builder;
import com.lvl6.proto.EventProto.DropMenteeResponseProto.DropMenteeStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MentorshipRetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

@Component @DependsOn("gameServer") public class DropMenteeController extends EventController{

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public DropMenteeController() {
    numAllocatedThreads = 3;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new DropMenteeRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_DROP_MENTEE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    DropMenteeRequestProto reqProto = 
        ((DropMenteeRequestEvent)event).getDropMenteeRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userId = senderProto.getUserId();
    int menteeToDrop = reqProto.getMenteeToDrop();

    DropMenteeResponseProto.Builder resBuilder = DropMenteeResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    DropMenteeResponseEvent resEvent = new DropMenteeResponseEvent(userId);
    //don't think lock required
    try {
      Mentorship mentorshipForMentee = 
          MentorshipRetrieveUtils.getActiveMentorshipForMentee(menteeToDrop);

      boolean legitDrop = checkLegitDrop(resBuilder, userId, menteeToDrop,
          mentorshipForMentee);

      boolean saveSuccessful = false;
      if (legitDrop) {
        saveSuccessful = writeChangesToDb(mentorshipForMentee);
      }
      
      if (saveSuccessful) {
        resBuilder.setStatus(DropMenteeStatus.SUCCESS);
      }

      resEvent.setDropMenteeResponseProto(resBuilder.build());

      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in DropMenteeController processEvent", e);
      try {
        resBuilder.setStatus(DropMenteeStatus.FAIL_OTHER);
        resEvent.setDropMenteeResponseProto(resBuilder.build());
        server.writeEvent(resEvent);
      } catch (Exception e2) {

      }
    }
  }

  private boolean checkLegitDrop(Builder resBuilder, int userId, int menteeToDrop,
      Mentorship mentorshipForMentee) {

    if (0 > userId || null == mentorshipForMentee) {
      resBuilder.setStatus(DropMenteeStatus.FAIL_OTHER);
      log.error("unexpected error: userId invalid or mentee has no mentor. " +
          "userId=" + userId + "; menteeToDropId=" + menteeToDrop +
          "; mentorship=" + mentorshipForMentee);
      return false;
    }

    int mentorId = mentorshipForMentee.getMentorId();
    if (userId != mentorId) {
      resBuilder.setStatus(DropMenteeStatus.FAIL_NOT_MENTEE);
      log.error("unexpected error: user is not mentee's mentor. userId=" +
          userId + "; mentorshipForMentee=" + mentorshipForMentee);
      return false;
    }

    return true;
  }

  private boolean writeChangesToDb(Mentorship mentorshipForMentee) {
    int mentorshipId = mentorshipForMentee.getId();

    return UpdateUtils.get().updateMentorshipTerminate(mentorshipId);
  }

}
