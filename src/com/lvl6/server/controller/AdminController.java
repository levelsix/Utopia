package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.AdminChangeRequestEvent;
import com.lvl6.proto.AdminProto.AdminChangeRequestProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.*;

public class AdminController extends EventController {
  
  @Override
  public RequestEvent createRequestEvent() {
    return new AdminChangeRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.A_ADMIN_UPDATE;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    AdminChangeRequestProto reqProto = ((AdminChangeRequestEvent)event).getAdminChangeRequestProto();
    
    if (reqProto.hasStaticDataReloadType()) {
      switch (reqProto.getStaticDataReloadType()) {
      case ALL:
        BuildStructJobRetrieveUtils.reload();
        CityRetrieveUtils.reload();
        DefeatTypeJobRetrieveUtils.reload();
        EquipmentRetrieveUtils.reload();
        MarketplaceJobRetrieveUtils.reload();
        QuestRetrieveUtils.reload();
        TaskEquipReqRetrieveUtils.reload();
        TaskRetrieveUtils.reload();
        UpgradeStructJobRetrieveUtils.reload();
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
      case MARKETPLACE_JOBS:
        MarketplaceJobRetrieveUtils.reload();
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
      }
    }
  }
}
