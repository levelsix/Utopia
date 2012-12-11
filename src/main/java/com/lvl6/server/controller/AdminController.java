package com.lvl6.server.controller;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.events.request.AdminChangeRequestEvent;
import com.lvl6.events.response.PurgeClientStaticDataResponseEvent;
import com.lvl6.misc.MiscMethods;
import com.lvl6.proto.AdminProto.AdminChangeRequestProto;
import com.lvl6.proto.EventProto.PurgeClientStaticDataResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.BuildStructJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.CityRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.DefeatTypeJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LevelsRequiredExperienceRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.NeutralCityElementsRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.PossessEquipJobRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.QuestRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskEquipReqRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.TaskRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.UpgradeStructJobRetrieveUtils;

  @Component @DependsOn("gameServer") public class AdminController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  public AdminController() {
    numAllocatedThreads = 1;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new AdminChangeRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.A_ADMIN_UPDATE;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    AdminChangeRequestProto reqProto = ((AdminChangeRequestEvent)event).getAdminChangeRequestProto();

    if (reqProto.hasStaticDataReloadType()) {
      switch (reqProto.getStaticDataReloadType()) {
      case ALL:
        MiscMethods.reloadAllRareChangeStaticData();
        break;
      case BUILD_STRUCT_JOBS:
        BuildStructJobRetrieveUtils.reload();
        break;
      case CITIES:
        CityRetrieveUtils.reload();
        break;
      case DEFEAT_TYPE_JOBS:
        DefeatTypeJobRetrieveUtils.reload();
        break;
      case EQUIPMENT:
        EquipmentRetrieveUtils.reload();
        break;
      case QUESTS:
        QuestRetrieveUtils.reload();
        break;
      case TASK_EQUIP_REQUIREMENTS:
        TaskEquipReqRetrieveUtils.reload();
        break;
      case TASKS:
        TaskRetrieveUtils.reload();
        break;
      case UPGRADE_STRUCT_JOBS:
        UpgradeStructJobRetrieveUtils.reload();
        break;
      case STRUCTURES:
        StructureRetrieveUtils.reload();
        break;
      case POSSESS_EQUIP_JOBS:
        PossessEquipJobRetrieveUtils.reload();
        break;
      case LEVELS_REQUIRED_EXPERIENCE:
        LevelsRequiredExperienceRetrieveUtils.reload();
        break;
      case NEUTRAL_CITY_ELEMS:
        NeutralCityElementsRetrieveUtils.reload();
        break;
      }
      log.info("reloaded all static rarechange data tables from db");
    }
    
    if (reqProto.hasPurgeStaticDataForConnectedClients() && 
        reqProto.getPurgeStaticDataForConnectedClients()) {
      PurgeClientStaticDataResponseProto.Builder builder = PurgeClientStaticDataResponseProto.newBuilder();
      Set<Integer> connectedPlayerIds = server.getConnectedPlayerIds();
      
      for(Integer playerId : connectedPlayerIds) {
        PurgeClientStaticDataResponseEvent resEvent = new PurgeClientStaticDataResponseEvent(playerId);
        resEvent.setPurgeClientStaticDataResponseProto(builder.setSenderId(playerId).build());
        server.writeEvent(resEvent);
      }
      log.info("finished telling all clients to purge their static data");
    }
    
    
  }
}
