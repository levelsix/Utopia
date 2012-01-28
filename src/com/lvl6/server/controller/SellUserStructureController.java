package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.SellUserStructureRequestEvent;
import com.lvl6.events.response.SellUserStructureResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.proto.EventProto.SellUserStructureRequestProto;
import com.lvl6.proto.EventProto.SellUserStructureResponseProto;
import com.lvl6.proto.EventProto.SellUserStructureResponseProto.SellUserStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class SellUserStructureController extends EventController {

  public static final double PERCENT_RETURNED_TO_USER = .5;
  
  @Override
  public RequestEvent createRequestEvent() {
    return new SellUserStructureRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_SELL_USER_STRUCTURE_EVENT;
  }

  
  @Override
  protected void processRequestEvent(RequestEvent event) {
    SellUserStructureRequestProto reqProto = ((SellUserStructureRequestEvent)event).getSellUserStructureRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();
    int structId = reqProto.getSoldStructId();
    Structure struct = StructureRetrieveUtils.getStructForStructId(structId);
    //UserStruct userStruct = UserStructRetrieveUtils.getSpecificUserStruct(userStructId);
    
    SellUserStructureResponseProto.Builder resBuilder = SellUserStructureResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    
    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      //if (user != null && struct != null && userStruct != null) {
      if (user != null && struct != null) {
        int diamondChange = Math.max(0,  (int)Math.floor(struct.getDiamondPrice()*PERCENT_RETURNED_TO_USER));
        int coinChange = Math.max(0,  (int)Math.floor(struct.getCoinPrice()*PERCENT_RETURNED_TO_USER));
        int woodChange = Math.max(0,  (int)Math.floor(struct.getWoodPrice()*PERCENT_RETURNED_TO_USER));

        if (!user.updateRelativeDiamondsCoinsWoodNaive(diamondChange, coinChange, woodChange)) {
          log.error("problem with giving user stats back after selling struct");
          resBuilder.setStatus(SellUserStructureStatus.FAIL);
        } else {
          if (!DeleteUtils.deleteUserStruct(userStructId)) {
            log.error("problem with deleting user struct");
            resBuilder.setStatus(SellUserStructureStatus.FAIL);
          } else {
            resBuilder.setStatus(SellUserStructureStatus.SUCCESS);                      
          }
        }
      } else {
        resBuilder.setStatus(SellUserStructureStatus.FAIL);        
      }
      
      SellUserStructureResponseEvent resEvent = new SellUserStructureResponseEvent(senderProto.getUserId());
      resEvent.setSellUserStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      
      if (user != null) {
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        server.writeEvent(resEventUpdate);
      }
        
    } catch (Exception e) {
      log.error("exception in SellUserStructure processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }
}
