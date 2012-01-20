package com.lvl6.server.controller;

import java.util.List;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveEquipmentForArmoryRequestEvent;
import com.lvl6.events.response.RetrieveEquipmentForArmoryResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.proto.EventProto.RetrieveEquipmentForArmoryRequestProto;
import com.lvl6.proto.EventProto.RetrieveEquipmentForArmoryResponseProto;
import com.lvl6.proto.InfoProto.FullEquipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class RetrieveEquipmentForArmoryController extends EventController{

  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());        
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveEquipmentForArmoryRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_EQUIPS_FOR_ARMORY_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    RetrieveEquipmentForArmoryRequestProto reqProto = ((RetrieveEquipmentForArmoryRequestEvent)event).getRetrieveEquipmentForArmoryRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    RetrieveEquipmentForArmoryResponseProto.Builder resBuilder = RetrieveEquipmentForArmoryResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    List<Equipment> equips = EquipmentRetrieveUtils.getAllEquipmentForClassType(MiscMethods.getClassTypeFromUserType(senderProto.getUserType()));
    if (equips != null) {
      for (Equipment equip : equips) { 
        FullEquipProto fep = CreateInfoProtoUtils.createFullEquipProtoFromEquip(equip);
        resBuilder.addEquips(fep);
      }
    }
    RetrieveEquipmentForArmoryResponseProto resProto = resBuilder.build();

    RetrieveEquipmentForArmoryResponseEvent resEvent = new RetrieveEquipmentForArmoryResponseEvent(senderProto.getUserId());
    resEvent.setRetrieveEquipmentForArmoryResponseProto(resProto);

    server.writeEvent(resEvent);
  }

}
