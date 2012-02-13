package com.lvl6.server.controller;

import java.util.List;

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

public class RetrieveUserEquipForUserController extends EventController {

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveUserEquipForUserRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_USER_EQUIP_FOR_USER_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    RetrieveUserEquipForUserRequestProto reqProto = ((RetrieveUserEquipForUserRequestEvent)event).getRetrieveUserEquipForUserRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int requstedPlayerId = reqProto.getPlayerIdOfRequested();

    RetrieveUserEquipForUserResponseProto.Builder resBuilder = RetrieveUserEquipForUserResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(requstedPlayerId);

    try {
      List<UserEquip> userEquipsForUser = UserEquipRetrieveUtils.getUserEquipsForUser(requstedPlayerId);
      if (userEquipsForUser != null) {
        for (UserEquip ue : userEquipsForUser) {
          resBuilder.addUserEquips(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(ue));
        }
      }
      RetrieveUserEquipForUserResponseEvent resEvent = new RetrieveUserEquipForUserResponseEvent(senderProto.getUserId());
      resEvent.setRetrieveUserEquipForUserResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
    } catch (Exception e) {
      log.error("exception in RetrieveUserEquipForUser processEvent", e);
    } finally {
      server.unlockPlayer(requstedPlayerId);      
    }
  }
}
