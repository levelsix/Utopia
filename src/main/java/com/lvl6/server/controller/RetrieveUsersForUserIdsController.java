package com.lvl6.server.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveUsersForUserIdsRequestEvent;
import com.lvl6.events.response.RetrieveUsersForUserIdsResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.RetrieveUsersForUserIdsRequestProto;
import com.lvl6.proto.EventProto.RetrieveUsersForUserIdsResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;

 @Component public class RetrieveUsersForUserIdsController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveUsersForUserIdsController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveUsersForUserIdsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_USERS_FOR_USER_IDS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveUsersForUserIdsRequestProto reqProto = ((RetrieveUsersForUserIdsRequestEvent)event).getRetrieveUsersForUserIdsRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    List<Integer> requestedUserIds = reqProto.getRequestedUserIdsList();

    RetrieveUsersForUserIdsResponseProto.Builder resBuilder = RetrieveUsersForUserIdsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    Map<Integer, User> usersByIds = UserRetrieveUtils.getUsersByIds(requestedUserIds);
    if (usersByIds != null) {
      for (User user : usersByIds.values()) {
        resBuilder.addRequestedUsers(CreateInfoProtoUtils.createFullUserProtoFromUser(user));
      }
    } else {
      log.error("no users with the ids " + requestedUserIds);
    }
    RetrieveUsersForUserIdsResponseProto resProto = resBuilder.build();
    RetrieveUsersForUserIdsResponseEvent resEvent = new RetrieveUsersForUserIdsResponseEvent(senderProto.getUserId());
    resEvent.setTag(event.getTag());
    resEvent.setRetrieveUsersForUserIdsResponseProto(resProto);
    server.writeEvent(resEvent);
  }

}
