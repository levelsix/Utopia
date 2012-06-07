package com.lvl6.server.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveUserEquipForUserRequestEvent;
import com.lvl6.events.response.RetrieveUserEquipForUserResponseEvent;
import com.lvl6.info.UserEquip;
import com.lvl6.proto.EventProto.RetrieveUserEquipForUserRequestProto;
import com.lvl6.proto.EventProto.RetrieveUserEquipForUserResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;

  @Component @DependsOn("gameServer") public class RetrieveUserEquipForUserController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveUserEquipForUserController() {
    numAllocatedThreads = 8;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveUserEquipForUserRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_USER_EQUIP_FOR_USER;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveUserEquipForUserRequestProto reqProto = ((RetrieveUserEquipForUserRequestEvent)event).getRetrieveUserEquipForUserRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int relevantUserId = reqProto.getRelevantUserId();

    RetrieveUserEquipForUserResponseProto.Builder resBuilder = RetrieveUserEquipForUserResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setRelevantUserId(relevantUserId);

    server.lockPlayer(relevantUserId);
    try {
      List<UserEquip> userEquips = UserEquipRetrieveUtils.getUserEquipsForUser(relevantUserId);
      if (userEquips != null) {
        for (UserEquip ue : userEquips) {
          if (ue != null) {
            resBuilder.addUserEquips(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(ue));
          }
        }
      }
      RetrieveUserEquipForUserResponseProto resProto = resBuilder.build();
      RetrieveUserEquipForUserResponseEvent resEvent = new RetrieveUserEquipForUserResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveUserEquipForUserResponseProto(resProto);
      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrieveUserEquipForUserController processEvent", e);
    } finally {
      server.unlockPlayer(relevantUserId); 
    }
  }

}
