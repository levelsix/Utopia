package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.SellNormStructureRequestEvent;
import com.lvl6.events.response.SellNormStructureResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.SellNormStructureRequestProto;
import com.lvl6.proto.EventProto.SellNormStructureResponseProto;
import com.lvl6.proto.EventProto.SellNormStructureResponseProto.SellNormStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class SellNormStructureController extends EventController {

  @Override
  public RequestEvent createRequestEvent() {
    return new SellNormStructureRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_SELL_NORM_STRUCTURE_EVENT;
  }


  @Override
  protected void processRequestEvent(RequestEvent event) {
    SellNormStructureRequestProto reqProto = ((SellNormStructureRequestEvent)event).getSellNormStructureRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();

    SellNormStructureResponseProto.Builder resBuilder = SellNormStructureResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    UserStruct userStruct = UserStructRetrieveUtils.getSpecificUserStruct(userStructId);
    Structure struct = null;

    if (userStruct != null) {
      struct = StructureRetrieveUtils.getStructForStructId(userStruct.getStructId());
    }

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = null;
      if (userStruct != null) {
        user = UserRetrieveUtils.getUserById(senderProto.getUserId());
        if (user != null && struct != null && user.getId() == userStruct.getUserId() && userStruct.isComplete()) {
          int diamondChange = Math.max(0,  (int)Math.ceil(struct.getDiamondPrice()*ControllerConstants.SELL_NORM_STRUCTURE__PERCENT_RETURNED_TO_USER));
          int coinChange = Math.max(0,  (int)Math.ceil(struct.getCoinPrice()*ControllerConstants.SELL_NORM_STRUCTURE__PERCENT_RETURNED_TO_USER));
          
          if (!user.updateRelativeDiamondsCoinsExperienceNaive(diamondChange, coinChange, 0)) {
            log.error("problem with giving user stats back after selling struct");
            resBuilder.setStatus(SellNormStructureStatus.FAIL);
          } else {
            if (!DeleteUtils.deleteUserStruct(userStructId)) {
              log.error("problem with deleting user struct");
              resBuilder.setStatus(SellNormStructureStatus.FAIL);
            } else {
              resBuilder.setStatus(SellNormStructureStatus.SUCCESS);                      
            }
          }
        } else {
          resBuilder.setStatus(SellNormStructureStatus.FAIL);        
        }
      } else {
        resBuilder.setStatus(SellNormStructureStatus.FAIL);        
      }

      SellNormStructureResponseEvent resEvent = new SellNormStructureResponseEvent(senderProto.getUserId());
      resEvent.setSellNormStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (user != null) {
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in SellNormStructure processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }
}
