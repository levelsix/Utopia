package com.lvl6.server.controller;

import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveStaticDataForShopRequestEvent;
import com.lvl6.events.response.RetrieveStaticDataForShopResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.Structure;
import com.lvl6.proto.EventProto.RetrieveStaticDataForShopRequestProto;
import com.lvl6.proto.EventProto.RetrieveStaticDataForShopRequestProto.RetrieveForShopType;
import com.lvl6.proto.EventProto.RetrieveStaticDataForShopResponseProto;
import com.lvl6.proto.EventProto.RetrieveStaticDataForShopResponseProto.RetrieveStaticDataForShopStatus;
import com.lvl6.proto.InfoProto.FullEquipProto;
import com.lvl6.proto.InfoProto.FullStructureProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class RetrieveStaticDataForShopController extends EventController{

  public RetrieveStaticDataForShopController() {
    numAllocatedThreads = 3;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveStaticDataForShopRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_STATIC_DATA_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    RetrieveStaticDataForShopRequestProto reqProto = ((RetrieveStaticDataForShopRequestEvent)event).getRetrieveStaticDataForShopRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    RetrieveForShopType type = reqProto.getType();

    RetrieveStaticDataForShopResponseProto.Builder resBuilder = RetrieveStaticDataForShopResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(RetrieveStaticDataForShopStatus.SUCCESS);
    
    if (type == RetrieveForShopType.EQUIPMENT_FOR_ARMORY) {
      List<Equipment> equips = EquipmentRetrieveUtils.getAllArmoryEquipmentForClassType(MiscMethods.getClassTypeFromUserType(senderProto.getUserType()));
      if (equips != null) {
        for (Equipment equip : equips) { 
          FullEquipProto fep = CreateInfoProtoUtils.createFullEquipProtoFromEquip(equip);
          resBuilder.addEquips(fep);
        }
      } else {
        resBuilder.setStatus(RetrieveStaticDataForShopStatus.SOME_FAIL);
      }
    } else if (type == RetrieveForShopType.ALL_STRUCTURES) {
      Map<Integer, Structure> structIdsToStructs = StructureRetrieveUtils.getStructIdsToStructs();
      for (Structure struct : structIdsToStructs.values()) {
        if (struct != null) {
          FullStructureProto fsp = CreateInfoProtoUtils.createFullStructureProtoFromStructure(struct);
          resBuilder.addStructs(fsp);
        } else {
          resBuilder.setStatus(RetrieveStaticDataForShopStatus.SOME_FAIL);
        }
      }
    } else {
      resBuilder.setStatus(RetrieveStaticDataForShopStatus.SOME_FAIL);
    }
    
    RetrieveStaticDataForShopResponseProto resProto = resBuilder.build();

    RetrieveStaticDataForShopResponseEvent resEvent = new RetrieveStaticDataForShopResponseEvent(senderProto.getUserId());
    resEvent.setRetrieveStaticDataForShopResponseProto(resProto);

    server.writeEvent(resEvent);
  }

}
