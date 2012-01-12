package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.InAppPurchaseRequestEvent;
import com.lvl6.events.request.VaultRequestEvent;
import com.lvl6.events.response.InAppPurchaseResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.VaultResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.InAppPurchaseRequestProto;
import com.lvl6.proto.EventProto.InAppPurchaseResponseProto;
import com.lvl6.proto.EventProto.VaultRequestProto;
import com.lvl6.proto.EventProto.VaultRequestProto.VaultRequestType;
import com.lvl6.proto.EventProto.VaultResponseProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class InAppPurchaseController extends EventController {
  
  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());    
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new InAppPurchaseRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_IN_APP_PURCHASE_EVENT;
  }
  
  /*
   * db stuff done before sending event to eventwriter/client because the client's not waiting 
   * on it immediately anyways
   */
  @Override
  protected void processRequestEvent(RequestEvent event) {
    InAppPurchaseRequestProto reqProto = ((InAppPurchaseRequestEvent)event).getInAppPurchaseRequestProto();
    
    MinimumUserProto senderProto = reqProto.getSender();
    String receipt = reqProto.getReceipt();
        
    InAppPurchaseResponseProto.Builder resBuilder = InAppPurchaseResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    
    // Lock this player's ID
    server.lockPlayer(senderProto.getUserId());
    User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

    
    /*
     * do stuff in here
     * if apple verifies it call     user.updateRelativeDiamonds(diamondsPurchased);
     * 
     */
    
    
    InAppPurchaseResponseProto resProto = resBuilder.build();
    
    InAppPurchaseResponseEvent resEvent = new InAppPurchaseResponseEvent(senderProto.getUserId());
    resEvent.setInAppPurchaseResponseProto(resProto);
    
    server.writeEvent(resEvent);
    
    
    UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
    server.writeEvent(resEventUpdate);

    // Unlock this player
    server.unlockPlayer(senderProto.getUserId());
  }

}
