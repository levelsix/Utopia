package com.lvl6.server.controller;

import java.util.Map;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.events.request.EquipEquipmentRequestEvent;
import com.lvl6.events.response.EquipEquipmentResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Equipment;
import com.lvl6.info.User;
import com.lvl6.info.UserEquip;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.EquipEquipmentRequestProto;
import com.lvl6.proto.EventProto.EquipEquipmentResponseProto;
import com.lvl6.proto.EventProto.EquipEquipmentResponseProto.Builder;
import com.lvl6.proto.EventProto.EquipEquipmentResponseProto.EquipEquipmentStatus;
import com.lvl6.proto.InfoProto.EquipClassType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.FullEquipProto.EquipType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;

  @Component @DependsOn("gameServer") public class EquipEquipmentController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public EquipEquipmentController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new EquipEquipmentRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_EQUIP_EQUIPMENT_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    EquipEquipmentRequestProto reqProto = ((EquipEquipmentRequestEvent)event).getEquipEquipmentRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    UserEquip ue = RetrieveUtils.userEquipRetrieveUtils().getSpecificUserEquip(reqProto.getUserEquipId());
    boolean forPrestigeEquipSlot = reqProto.getForPrestigeEquipSlot();

    Map<Integer, Equipment> equipmentIdsToEquipment = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
    Equipment equip = (ue == null) ? null : equipmentIdsToEquipment.get(ue.getEquipId());

    EquipEquipmentResponseProto.Builder resBuilder = EquipEquipmentResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      boolean legitEquip = checkEquip(resBuilder, user, ue, forPrestigeEquipSlot, equip);

      EquipEquipmentResponseEvent resEvent = new EquipEquipmentResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setEquipEquipmentResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitEquip) {
        writeChangesToDB(user, ue, forPrestigeEquipSlot);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in EquipEquipment processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private void writeChangesToDB(User user, UserEquip ue, boolean forPrestigeEquipSlot) {
    if (!user.updateEquipped(ue, forPrestigeEquipSlot)) {
      log.error("problem with equipping " + ue);
    }
  }

  private boolean checkEquip(Builder resBuilder, User user, UserEquip ue,
      boolean forPrestigeEquipSlot, Equipment equip) {
    if (user == null) {
      resBuilder.setStatus(EquipEquipmentStatus.OTHER_FAIL);
      log.error("problem with equipping equip" + equip.getId() + ": user is null");
      return false;
    }
    if (equip == null) {
      resBuilder.setStatus(EquipEquipmentStatus.NOT_AN_EQUIP);
      log.error("problem with equipping equip- equipId passed in is not an equip, equip id was " + ue.getEquipId());
      return false;
    }
    if (ue == null) {
      resBuilder.setStatus(EquipEquipmentStatus.DOES_NOT_HAVE_THIS_EQUIP);
      log.error("problem with equipping equip" + equip.getId() + ": user doesnt have it");
      return false;      
    }
    if (equip.getMinLevel() > user.getLevel()) {
      resBuilder.setStatus(EquipEquipmentStatus.NOT_HIGH_ENOUGH_LEVEL);
      log.error("problem with equipping equip" + equip.getId() + ": user too low level to equip it. user level is "
          + user.getLevel() + ", equip minimum level is " + equip.getMinLevel());
      return false;
    }
    EquipClassType userClass = MiscMethods.getClassTypeFromUserType(user.getType());
    if (userClass != equip.getClassType() && equip.getClassType() != EquipClassType.ALL_AMULET) {
      resBuilder.setStatus(EquipEquipmentStatus.INCORRECT_CLASS_TYPE);
      log.error("problem with equipping equip" + equip.getId() + ": user not right class. user type is "
          + user.getType() + ", equip class is " + equip.getClassType());
      return false;      
    }
    boolean equippable = canEquip(user, ue, forPrestigeEquipSlot, equip);
    if (!equippable) {
      resBuilder.setStatus(EquipEquipmentStatus.ALREADY_AT_MAX_EQUIPPED_EQUIPS);
      return false;
    }
    resBuilder.setStatus(EquipEquipmentStatus.SUCCESS);
    return true;
  }
  
  private boolean canEquip(User u, UserEquip ue, boolean forPrestigeEquipSlot, Equipment e) {
    EquipType et = e.getType();
    int prestigeLevel = u.getPrestigeLevel();
    boolean isWeapon = (EquipType.WEAPON == et);
    boolean isArmor = (EquipType.ARMOR == et);
    boolean isAmulet = (EquipType.AMULET == et);
    
    if(!forPrestigeEquipSlot) {
      return true;
    }
    if (isWeapon) {
      return canEquipPrestigeWeapon(prestigeLevel);
    } else if (isArmor) {
      return canEquipPrestigeArmor(prestigeLevel);
    } else if (isAmulet) {
      return canEquipPrestigeAmulet(prestigeLevel);
    } else {
      log.error("unexpected error: equip with unknown equip type, equip=" + e);
      return false;
    }
  }
  
  private boolean canEquipPrestigeWeapon(int prestigeLevel) {
    if (prestigeLevel < ControllerConstants.PRESTIGE__LEVEL_TO_UNLOCK_EXTRA_WEAPON) {
      log.error("user error: user below prestige level to wear weapon. " +
      		"prestigeLevelRequired=" + prestigeLevel);
      return false;
    }
    
    return true;
  }
  
  private boolean canEquipPrestigeArmor(int prestigeLevel) {
    if (prestigeLevel < ControllerConstants.PRESTIGE__LEVEL_TO_UNLOCK_EXTRA_ARMOR) {
      log.error("user error: user below prestige level to wear armor. " +
          "prestigeLevelRequired=" + prestigeLevel);
      return false;
    }
    
    return true;
  }
  
  private boolean canEquipPrestigeAmulet(int prestigeLevel) {
    if (prestigeLevel < ControllerConstants.PRESTIGE__LEVEL_TO_UNLOCK_EXTRA_AMULET) {
      log.error("user error: user below prestige level to wear amulet. " +
          "prestigeLevelRequired=" + prestigeLevel);
      return false;
    }
    
    return true;
  }
}
