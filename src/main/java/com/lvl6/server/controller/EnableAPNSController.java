package com.lvl6.server.controller;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.events.request.EnableAPNSRequestEvent;
import com.lvl6.events.response.EnableAPNSResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.EnableAPNSRequestProto;
import com.lvl6.proto.EventProto.EnableAPNSResponseProto;
import com.lvl6.proto.EventProto.EnableAPNSResponseProto.EnableAPNSStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;

  @Component @DependsOn("gameServer") public class EnableAPNSController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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
    if (deviceToken != null && deviceToken.length() == 0) deviceToken = null;

    EnableAPNSResponseProto.Builder resBuilder = EnableAPNSResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      if (deviceToken != null && user != null) { 
        resBuilder.setStatus(EnableAPNSStatus.SUCCESS);
      } else {
        resBuilder.setStatus(EnableAPNSStatus.NOT_ENABLED);
      }

      EnableAPNSResponseProto resProto = resBuilder.build();
      EnableAPNSResponseEvent resEvent = new EnableAPNSResponseEvent(senderProto.getUserId());
      resEvent.setEnableAPNSResponseProto(resProto);
      server.writeEvent(resEvent);

      boolean isDifferent = checkIfNewTokenDifferent(user.getDeviceToken(), deviceToken);

      if (isDifferent) {
        if (!user.updateSetdevicetoken(deviceToken)) {
          log.error("problem with setting user's device token to " + deviceToken);
        }
      }
    } catch (Exception e) {
      log.error("exception in EnableAPNSController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName()); 
    }
  }

  private boolean checkIfNewTokenDifferent(String oldToken, String newToken) {
    boolean oldTokenIsNothing = oldToken == null || oldToken.length() == 0;
    boolean newTokenIsNothing = newToken == null || newToken.length() == 0;
    
    if (oldTokenIsNothing && newTokenIsNothing) {
      return false;
    }
    
    if (!oldTokenIsNothing && !newTokenIsNothing) {
      return !oldToken.equals(newToken);
    }
    
    return true;
  }

}
