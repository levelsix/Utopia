package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.EquipEquipmentRequestEvent;
import com.lvl6.events.response.EquipEquipmentResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.proto.EventProto.EquipEquipmentRequestProto;
import com.lvl6.proto.EventProto.EquipEquipmentResponseProto;
import com.lvl6.proto.EventProto.EquipEquipmentResponseProto.Builder;
import com.lvl6.proto.EventProto.EquipEquipmentResponseProto.EquipEquipmentStatus;
import com.lvl6.proto.InfoProto.FullEquipProto.ClassType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class EquipEquipmentController extends EventController {

  public EquipEquipmentController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new EquipEquipmentRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_EQUIP_EQUIPMENT_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    EquipEquipmentRequestProto reqProto = ((EquipEquipmentRequestEvent)event).getEquipEquipmentRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int equipId = reqProto.getEquipId();
    
    Equipment equip = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(equipId);
    EquipEquipmentResponseProto.Builder resBuilder = EquipEquipmentResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      UserEquip userEquip = (equip == null) ? null : UserEquipRetrieveUtils.getSpecificUserEquip(senderProto.getUserId(), equip.getId());
      
      boolean legitEquip = checkEquip(resBuilder, user, userEquip, equip);

      EquipEquipmentResponseEvent resEvent = new EquipEquipmentResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setEquipEquipmentResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitEquip) {
        writeChangesToDB(user, equip);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in EquipEquipment processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, Equipment equip) {
    if (user.updateEquipped(equip)) {
      log.error("problem with equipping " + equip + " for user " + user);
    }
  }

  private boolean checkEquip(Builder resBuilder, User user, UserEquip userEquip, Equipment equip) {
    if (user == null) {
      resBuilder.setStatus(EquipEquipmentStatus.OTHER_FAIL);
      return false;
    }
    if (equip == null) {
      resBuilder.setStatus(EquipEquipmentStatus.NOT_AN_EQUIP);
      return false;
    }
    if (userEquip == null || userEquip.getQuantity() < 1) {
      resBuilder.setStatus(EquipEquipmentStatus.DOES_NOT_HAVE_THIS_EQUIP);
      return false;      
    }
    if (equip.getMinLevel() > user.getLevel()) {
      resBuilder.setStatus(EquipEquipmentStatus.NOT_HIGH_ENOUGH_LEVEL);
      return false;
    }
    ClassType userClass = MiscMethods.getClassTypeFromUserType(user.getType());
    if (userClass != equip.getClassType() && equip.getClassType() != ClassType.ALL_AMULET) {
      resBuilder.setStatus(EquipEquipmentStatus.INCORRECT_CLASS_TYPE);
      return false;      
    }
    resBuilder.setStatus(EquipEquipmentStatus.SUCCESS);
    return true;
  }
}
