package com.lvl6.server.controller;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.EnableAPNSRequestEvent;
import com.lvl6.events.response.EnableAPNSResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.EnableAPNSRequestProto;
import com.lvl6.proto.EventProto.EnableAPNSResponseProto;
import com.lvl6.proto.EventProto.EnableAPNSResponseProto.EnableAPNSStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;

  @Component @DependsOn("gameServer") public class EnableAPNSController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public EnableAPNSController() {
    numAllocatedThreads = 1;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new EnableAPNSRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_ENABLE_APNS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    EnableAPNSRequestProto reqProto = ((EnableAPNSRequestEvent)event).getEnableAPNSRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    String deviceToken = reqProto.getDeviceToken();

    EnableAPNSResponseProto.Builder resBuilder = EnableAPNSResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    server.lockPlayer(senderProto.getUserId());
    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

      boolean legitEnable = true;
      if (deviceToken != null && deviceToken.length() > 0 && user != null) { 
        resBuilder.setStatus(EnableAPNSStatus.SUCCESS);
      } else {
        legitEnable = false;
        resBuilder.setStatus(EnableAPNSStatus.NOT_ENABLED);
        log.error("problem with setting device token. user is " + user + ", device token is " + deviceToken);
      }
      EnableAPNSResponseProto resProto = resBuilder.build();
      EnableAPNSResponseEvent resEvent = new EnableAPNSResponseEvent(senderProto.getUserId());
      resEvent.setEnableAPNSResponseProto(resProto);
      server.writeEvent(resEvent);
      if (legitEnable) {
        if (user.getDeviceToken() == null || !deviceToken.equals(user.getDeviceToken())) {
          if (!user.updateSetdevicetoken(deviceToken)) {
            log.error("problem with setting user's device token to " + deviceToken);
          }
        }
      }
    } catch (Exception e) {
      log.error("exception in EnableAPNSController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId()); 
    }
  }

}
