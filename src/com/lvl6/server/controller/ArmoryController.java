package com.lvl6.server.controller;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ArmoryRequestEvent;
import com.lvl6.events.response.ArmoryResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.events.response.VaultResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.proto.EventProto.ArmoryRequestProto;
import com.lvl6.proto.EventProto.ArmoryResponseProto;
import com.lvl6.proto.EventProto.ArmoryResponseProto.ArmoryStatus;
import com.lvl6.proto.EventProto.VaultResponseProto;
import com.lvl6.proto.EventProto.ArmoryRequestProto.ArmoryRequestType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserEquipRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class ArmoryController extends EventController {

  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new ArmoryRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_ARMORY_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    ArmoryRequestProto reqProto = ((ArmoryRequestEvent)event).getArmoryRequestProto();
    
    MinimumUserProto senderProto = reqProto.getSender();
    ArmoryRequestType requestType = reqProto.getRequestType();
    int equipId = reqProto.getEquipId();
    int quantity = reqProto.getQuantity();
    
    ArmoryResponseProto.Builder resBuilder = ArmoryResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    
    boolean legitBuy = false;
    boolean legitSell = false;
    
    server.lockPlayer(senderProto.getUserId());
    User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

    if (quantity < 1) {
      resBuilder.setStatus(ArmoryStatus.OTHER_FAIL);
    } else {
      if (requestType == ArmoryRequestType.BUY) {
        Equipment equipment = EquipmentRetrieveUtils.getAllEquipmentIdsToEquipment().get(equipId);
        if (equipment == null) {
          resBuilder.setStatus(ArmoryStatus.OTHER_FAIL);
        } else {
          if (equipment.getCoinPrice() != Equipment.NOT_SET) {
            if (user.getCoins() >= equipment.getCoinPrice() * quantity) {
              legitBuy = true;
            } else {
              resBuilder.setStatus(ArmoryStatus.NOT_ENOUGH_CURRENCY_TO_BUY);
            }
          }
          if (equipment.getDiamondPrice() != Equipment.NOT_SET) {
            if (user.getDiamonds() >= equipment.getDiamondPrice() * quantity) {
              legitBuy = true;
            } else {
              resBuilder.setStatus(ArmoryStatus.NOT_ENOUGH_CURRENCY_TO_BUY);
            }
          }
        }
      } else if (requestType == ArmoryRequestType.SELL) {
        UserEquip userEquip = UserEquipRetrieveUtils.getSpecificUserEquip(senderProto.getUserId(), equipId);
        if (userEquip.getQuantity() >= quantity) {
          legitSell = true;
        } else {
          resBuilder.setStatus(ArmoryStatus.NOT_ENOUGH_EQUIP_TO_SELL);
        }
      }
    }
    if (legitBuy) {
      //TODO: impl
      resBuilder.setStatus(ArmoryStatus.SUCCESS);
    }
    if (legitSell) {
      //TODO: impl
      resBuilder.setStatus(ArmoryStatus.SUCCESS);
    }
    
    ArmoryResponseProto resProto = resBuilder.build();
    ArmoryResponseEvent resEvent = new ArmoryResponseEvent(senderProto.getUserId());
    resEvent.setArmoryResponseProto(resProto);
    server.writeEvent(resEvent);
    UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
    server.writeEvent(resEventUpdate);
    server.unlockPlayer(senderProto.getUserId());
  }

}
