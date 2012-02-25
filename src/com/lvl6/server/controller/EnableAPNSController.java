package com.lvl6.server.controller;

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

public class EnableAPNSController extends EventController {

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
  protected void processRequestEvent(RequestEvent event) {
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
      }
      EnableAPNSResponseProto resProto = resBuilder.build();
      EnableAPNSResponseEvent resEvent = new EnableAPNSResponseEvent(senderProto.getUserId());
      resEvent.setEnableAPNSResponseProto(resProto);
      server.writeEvent(resEvent);
      if (legitEnable) {
        if (user.getDeviceToken() == null || !deviceToken.equals(user.getDeviceToken())) {
          if (!user.updateSetdevicetoken(deviceToken)) {
            log.error("problem with setting user's device token");
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
