package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.LevelUpRequestEvent;
import com.lvl6.events.response.LevelUpResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.City;
import com.lvl6.info.Equipment;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.LevelUpRequestProto;
import com.lvl6.proto.EventProto.LevelUpResponseProto;
import com.lvl6.proto.EventProto.LevelUpResponseProto.Builder;
import com.lvl6.proto.EventProto.LevelUpResponseProto.LevelUpStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LevelsRequiredExperienceRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class LevelUpController extends EventController {

  public LevelUpController() {
    numAllocatedThreads = 4;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new LevelUpRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_LEVEL_UP_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    LevelUpRequestProto reqProto = ((LevelUpRequestEvent)event).getLevelUpRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    LevelUpResponseProto.Builder resBuilder = LevelUpResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      boolean legitLevelUp = checkLegitLevelUp(resBuilder, user);
      List<Integer> newlyUnlockedCityIds = null;
      if (legitLevelUp) {
        int newNextLevel = user.getLevel() + 2;
        int expRequiredForNewNextLevel = LevelsRequiredExperienceRetrieveUtils.getRequiredExperienceForLevel(newNextLevel);
        resBuilder.setNewNextLevel(newNextLevel);
        resBuilder.setExperienceRequiredForNewNextLevel(expRequiredForNewNextLevel);

        int newLevel = user.getLevel() + 1;
        resBuilder.setNewLevel(newLevel);

        if (newLevel == ControllerConstants.MIN_LEVEL_FOR_ARMORY) {
          resBuilder.setArmoryUnlocked(true);
        }
        if (newLevel == ControllerConstants.MIN_LEVEL_FOR_MARKETPLACE) {
          resBuilder.setMarketplaceUnlocked(true);
        }
        if (newLevel == ControllerConstants.MIN_LEVEL_FOR_VAULT) {
          resBuilder.setVaultUnlocked(true);
        }


        List<City> availCities = MiscMethods.getCitiesAvailableForUserLevel(newLevel);
        newlyUnlockedCityIds = new ArrayList<Integer>();
        for (City city : availCities) {
          if (city.getMinLevel() == newLevel) {
            newlyUnlockedCityIds.add(city.getId());
          }
          resBuilder.addCitiesNewlyAvailableToUser(CreateInfoProtoUtils.createFullCityProtoFromCity(city));
        }

        List<Equipment> availToUserInArmoryEquips = EquipmentRetrieveUtils.getAllArmoryEquipmentForClassType(MiscMethods.getClassTypeFromUserType(user.getType()));
        if (availToUserInArmoryEquips != null) {
          for (Equipment e : availToUserInArmoryEquips) {
            if (e != null && e.getMinLevel() == newLevel) {
              resBuilder.addNewlyEquippableAvailableInArmory(CreateInfoProtoUtils.createFullEquipProtoFromEquip(e));
            }
          }
        }

        Map<Integer, Structure> structIdsToStructs = StructureRetrieveUtils.getStructIdsToStructs();
        if (structIdsToStructs != null) {
          for (Structure struct : structIdsToStructs.values()) {
            if (struct != null && struct.getMinLevel() == newLevel) {
              resBuilder.addNewlyAvailableStructs(CreateInfoProtoUtils.createFullStructureProtoFromStructure(struct));
            } 
          }
        }
      }

      LevelUpResponseProto resProto = resBuilder.build();
      LevelUpResponseEvent resEvent = new LevelUpResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setLevelUpResponseProto(resProto);
      server.writeEvent(resEvent);

      if (legitLevelUp) {
        writeChangesToDB(user, newlyUnlockedCityIds);
      }

      UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
      resEventUpdate.setTag(event.getTag());
      server.writeEvent(resEventUpdate);
    } catch (Exception e) {
      log.error("exception in LevelUpController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId()); 
    }
  }

  private void writeChangesToDB(User user, List<Integer> newlyUnlockedCityIds) {
    if (!user.updateLevel(1)) {
      log.error("problem in changing the user's level");
    }
    if (newlyUnlockedCityIds != null && newlyUnlockedCityIds.size() > 0) {
      for (Integer cityId : newlyUnlockedCityIds) {
        if (!UpdateUtils.incrementCityRankForUserCity(user.getId(), cityId, 1)) {
          log.error("problem with unlocking city for user");
        }
      }
    }
    if (!user.updateAbsoluteRestoreEnergyStaminaRelativeUpdateSkillPoints(ControllerConstants.LEVEL_UP__SKILL_POINTS_GAINED, new Timestamp(new Date().getTime()))) {
      log.error("problem with restoring energy and stamina and awarding skill points");
    }
  }

  private boolean checkLegitLevelUp(Builder resBuilder, User user) {
    if (user == null) {
      resBuilder.setStatus(LevelUpStatus.OTHER_FAIL);
      return false;
    }
    if (user.getLevel() == ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER) {
      resBuilder.setStatus(LevelUpStatus.ALREADY_AT_MAX_LEVEL);
      return false;
    }
    Integer expRequiredForNextLevel = LevelsRequiredExperienceRetrieveUtils.getRequiredExperienceForLevel(user.getLevel() + 1);
    if (expRequiredForNextLevel == null) {
      resBuilder.setStatus(LevelUpStatus.OTHER_FAIL);
      return false;      
    }
    if (user.getExperience() < expRequiredForNextLevel) {
      resBuilder.setStatus(LevelUpStatus.NOT_ENOUGH_EXP_TO_NEXT_LEVEL);
      return false;            
    }
    resBuilder.setStatus(LevelUpStatus.SUCCESS);
    return true;
  }

}
