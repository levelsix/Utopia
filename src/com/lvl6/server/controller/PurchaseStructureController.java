package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.PurchaseStructureRequestEvent;
import com.lvl6.events.response.PurchaseStructureResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.PurchaseStructureRequestProto;
import com.lvl6.proto.EventProto.PurchaseStructureResponseProto;
import com.lvl6.proto.EventProto.PurchaseStructureResponseProto.Builder;
import com.lvl6.proto.EventProto.PurchaseStructureResponseProto.PurchaseStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class PurchaseStructureController extends EventController {

  @Override
  public RequestEvent createRequestEvent() {
    return new PurchaseStructureRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_PURCHASE_STRUCTURE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    PurchaseStructureRequestProto reqProto = ((PurchaseStructureRequestEvent)event).getPurchaseStructureRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int structId = reqProto.getStructId();
    CoordinatePair cp = new CoordinatePair(reqProto.getStructCoordinates().getX(), reqProto.getStructCoordinates().getY());
    
    PurchaseStructureResponseProto.Builder resBuilder = PurchaseStructureResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      Structure struct = StructureRetrieveUtils.getStructForStructId(structId);

      boolean legitPurchase = checkLegitPurchase(resBuilder, struct, user);

      if (legitPurchase) {
        int userStructId = InsertUtils.insertUserStruct(user.getId(), struct.getId(), cp);
        if (userStructId <= 0) {
          legitPurchase = false;
          resBuilder.setStatus(PurchaseStructureStatus.OTHER_FAIL);
        } else {
          resBuilder.setUserStructId(userStructId);
        }
      }

      PurchaseStructureResponseEvent resEvent = new PurchaseStructureResponseEvent(senderProto.getUserId());
      resEvent.setPurchaseStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitPurchase) {
        writeChangesToDB(user, struct);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in PurchaseStructure processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void writeChangesToDB(User user, Structure struct) {
    int diamondChange = Math.max(0, struct.getDiamondPrice());
    int coinChange = Math.max(0, struct.getCoinPrice());
    int woodChange = Math.max(0, struct.getWoodPrice());

    if (!user.updateRelativeDiamondsCoinsWoodNaive(diamondChange, coinChange, woodChange)) {
      log.error("problem with changing user stats after purchasing a structure");
    }
  }

  private boolean checkLegitPurchase(Builder resBuilder, Structure struct,
      User user) {
    if (user == null || struct == null) {
      resBuilder.setStatus(PurchaseStructureStatus.OTHER_FAIL);
      return false;
    }
    if (user.getLevel() < struct.getMinLevel()) {
      resBuilder.setStatus(PurchaseStructureStatus.LEVEL_TOO_LOW);
      return false;
    }
    if (user.getCoins() < struct.getCoinPrice()) {
      resBuilder.setStatus(PurchaseStructureStatus.NOT_ENOUGH_COINS);
      return false;
    }
    if (user.getDiamonds() < struct.getDiamondPrice()) {
      resBuilder.setStatus(PurchaseStructureStatus.NOT_ENOUGH_DIAMONDS);
      return false;
    }
    if (user.getWood() < struct.getWoodPrice()) {
      resBuilder.setStatus(PurchaseStructureStatus.NOT_ENOUGH_WOOD);
      return false;
    }
    resBuilder.setStatus(PurchaseStructureStatus.SUCCESS);
    return true;
  }
}
