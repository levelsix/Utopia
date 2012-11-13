package com.lvl6.server.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveStaticDataForShopRequestEvent;
import com.lvl6.events.response.RetrieveStaticDataForShopResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.Structure;
import com.lvl6.misc.MiscMethods;
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

  @Component @DependsOn("gameServer") public class RetrieveStaticDataForShopController extends EventController{

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public RetrieveStaticDataForShopController() {
    numAllocatedThreads = 3;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveStaticDataForShopRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_STATIC_DATA_FOR_SHOP_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    RetrieveStaticDataForShopRequestProto reqProto = ((RetrieveStaticDataForShopRequestEvent)event).getRetrieveStaticDataForShopRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    RetrieveForShopType type = reqProto.getType();

    RetrieveStaticDataForShopResponseProto.Builder resBuilder = RetrieveStaticDataForShopResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(RetrieveStaticDataForShopStatus.SUCCESS);
    
    if (type == RetrieveForShopType.EQUIPMENT_FOR_ARMORY) {
      List<Equipment> equips = EquipmentRetrieveUtils.getAllEquipmentForClassType(MiscMethods.getClassTypeFromUserType(senderProto.getUserType()));
      if (equips != null) {
        for (Equipment equip : equips) { 
          FullEquipProto fep = CreateInfoProtoUtils.createFullEquipProtoFromEquip(equip);
          resBuilder.addEquips(fep);
        }
      } else {
        resBuilder.setStatus(RetrieveStaticDataForShopStatus.SOME_FAIL);
        log.error("no equips available in the armory for classtype=" + MiscMethods.getClassTypeFromUserType(senderProto.getUserType()));
      }
    } else if (type == RetrieveForShopType.ALL_STRUCTURES) {
      Map<Integer, Structure> structIdsToStructs = StructureRetrieveUtils.getStructIdsToStructs();
      for (Structure struct : structIdsToStructs.values()) {
        if (struct != null) {
          FullStructureProto fsp = CreateInfoProtoUtils.createFullStructureProtoFromStructure(struct);
          resBuilder.addStructs(fsp);
        } else {
          resBuilder.setStatus(RetrieveStaticDataForShopStatus.SOME_FAIL);
          log.error("error with retrieving structs");
        }
      }
    } else {
      resBuilder.setStatus(RetrieveStaticDataForShopStatus.SOME_FAIL);
      log.error("unkown retrievestaticdataforshop type. type=" + type);
    }
    
    RetrieveStaticDataForShopResponseProto resProto = resBuilder.build();

    RetrieveStaticDataForShopResponseEvent resEvent = new RetrieveStaticDataForShopResponseEvent(senderProto.getUserId());
    resEvent.setTag(event.getTag());
    resEvent.setRetrieveStaticDataForShopResponseProto(resProto);

    server.writeEvent(resEvent);
  }

}
