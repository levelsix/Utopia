package com.lvl6.server.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ArmoryRequestEvent;
import com.lvl6.events.response.ArmoryResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.ArmoryRequestProto;
import com.lvl6.proto.EventProto.ArmoryRequestProto.ArmoryRequestType;
import com.lvl6.proto.EventProto.ArmoryResponseProto;
import com.lvl6.proto.EventProto.ArmoryResponseProto.ArmoryStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.SpecialQuestAction;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.QuestUtils;

@Component @DependsOn("gameServer") public class ArmoryController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public ArmoryController() {
    numAllocatedThreads = 4;
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
  protected void processRequestEvent(RequestEvent event) throws Exception {
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
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      List<UserEquip> userEquipsForEquipId = RetrieveUtils.userEquipRetrieveUtils().getUserEquipsWithEquipId(senderProto.getUserId(), equipId);;
      UserEquip userEquip = null;
      
      Equipment equipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment().get(equipId);

      if (quantity != 1 || equipment == null || !equipment.isBuyableInArmory()) {
        if (quantity > 1) {
          log.fatal("controller does not support selling quantity > 1");
          quantity = 1;
        }
        resBuilder.setStatus(ArmoryStatus.OTHER_FAIL);
      } else if (requestType != ArmoryRequestType.BUY) {
        log.fatal("controller does not support non buy");
        resBuilder.setStatus(ArmoryStatus.OTHER_FAIL);
      } else {
        if (requestType == ArmoryRequestType.BUY) {
          if (equipment.getCoinPrice() != Equipment.NOT_SET) {
            if (user.getCoins() >= equipment.getCoinPrice() * quantity) {
              legitBuy = true;
            } else {
              resBuilder.setStatus(ArmoryStatus.NOT_ENOUGH_CURRENCY_TO_BUY);
              log.error("player needs " + equipment.getCoinPrice() + " coins to buy equip with id "
                  + equipId + ", but only has " + user.getCoins());
            }
          }
          if (equipment.getDiamondPrice() != Equipment.NOT_SET) {
            if (user.getDiamonds() >= equipment.getDiamondPrice() * quantity) {
              legitBuy = true;
            } else {
              resBuilder.setStatus(ArmoryStatus.NOT_ENOUGH_CURRENCY_TO_BUY);
              log.error("player needs " + equipment.getDiamondPrice() + " diamonds to buy equip with id "
                  + equipId + ", but only has " + user.getDiamonds());
            }
          }
        } else if (requestType == ArmoryRequestType.SELL) {
          if (equipment.getDiamondPrice() != Equipment.NOT_SET) {
            resBuilder.setStatus(ArmoryStatus.CANNOT_SELL_DIAMOND_EQUIP);
            log.error("player tried to sell a diamond equip (equip with id " + equipId + ")");
          } else {
            if (userEquip != null) {
              legitSell = true;
            } else {
              resBuilder.setStatus(ArmoryStatus.NOT_ENOUGH_EQUIP_TO_SELL);
              log.error("player tried to sell " + quantity + " equips with id " + equipId + " but has none");
            }
          }
        }
      }
      if (legitBuy) {
        //        if (userEquip == null || userEquip.getQuantity() < 1) {
        //          if (MiscMethods.checkIfEquipIsEquippableOnUser(equipment, user) && !user.updateEquipped(equipment)) {
        //            log.error("problem with equipping " + equipment + " for user " + user);
        //          }
        //        }
        
        int userEquipId = InsertUtils.get().insertUserEquip(user.getId(), 
            equipId, ControllerConstants.DEFAULT_USER_EQUIP_LEVEL);
        if (userEquipId < 0) {
          resBuilder.setStatus(ArmoryStatus.OTHER_FAIL);
          log.error("problem with giving 1 of equip " + equipId + " to buyer " + user.getId());
          legitBuy = false;
        } else {
          resBuilder.setFullUserEquipOfBoughtItem(CreateInfoProtoUtils.createFullUserEquipProtoFromUserEquip(
              new UserEquip(userEquipId, user.getId(), equipId, ControllerConstants.DEFAULT_USER_EQUIP_LEVEL)));
        }
        
        if (equipment.getCoinPrice() != Equipment.NOT_SET) {
          if (!user.updateRelativeCoinsNaive(equipment.getCoinPrice() * -1)) {
            log.error("problem with taking away " + equipment.getCoinPrice() + " coins from user");
          }
        } else if (equipment.getDiamondPrice() != Equipment.NOT_SET)  {
          if (!user.updateRelativeDiamondsNaive(equipment.getDiamondPrice() * -1)) {
            log.error("problem with taking away " + equipment.getDiamondPrice() + " diamonds from user");
          }
        }
        resBuilder.setStatus(ArmoryStatus.SUCCESS);
      }
      if (legitSell) {
        if (userEquipsForEquipId.size() <= 1) {
          if (!MiscMethods.unequipUserEquipIfEquipped(user, userEquip)) {
            log.error("problem with unequipping userequip" + userEquip.getId());
          }
        }
        if (!DeleteUtils.get().deleteUserEquip(userEquip.getId())) {
          log.error("problem with delete from player userequip with id " + userEquip.getId());
        }
        if (!user.updateRelativeCoinsNaive((int)(ControllerConstants.ARMORY__SELL_RATIO * equipment.getCoinPrice()))) {
          log.error("problem with changing coin count by " + (int)(ControllerConstants.ARMORY__SELL_RATIO * equipment.getCoinPrice()));
        }
        resBuilder.setStatus(ArmoryStatus.SUCCESS);
      }

      ArmoryResponseProto resProto = resBuilder.build();
      ArmoryResponseEvent resEvent = new ArmoryResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setArmoryResponseProto(resProto);
      server.writeEvent(resEvent);

      if (legitBuy || legitSell) {
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }

      if (legitBuy) {
        QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, SpecialQuestAction.PURCHASE_FROM_ARMORY, false);
      }
      if (legitSell) {
        QuestUtils.checkAndSendQuestsCompleteBasic(server, user.getId(), senderProto, SpecialQuestAction.SELL_TO_ARMORY, true);
      }
    } catch (Exception e) {
      log.error("exception in ArmoryController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId()); 
    }
  }

}
