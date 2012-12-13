package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.events.request.LevelUpRequestEvent;
import com.lvl6.events.response.LevelUpResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.City;
import com.lvl6.info.Equipment;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.LevelUpRequestProto;
import com.lvl6.proto.EventProto.LevelUpResponseProto;
import com.lvl6.proto.EventProto.LevelUpResponseProto.Builder;
import com.lvl6.proto.EventProto.LevelUpResponseProto.LevelUpStatus;
import com.lvl6.proto.InfoProto.FullEquipProto.Rarity;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.rarechange.EquipmentRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LevelsRequiredExperienceRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

  @Component @DependsOn("gameServer") public class LevelUpController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

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
  protected void processRequestEvent(RequestEvent event) throws Exception {
    LevelUpRequestProto reqProto = ((LevelUpRequestEvent)event).getLevelUpRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    LevelUpResponseProto.Builder resBuilder = LevelUpResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      boolean legitLevelUp = checkLegitLevelUp(resBuilder, user);
      List<Integer> newlyUnlockedCityIds = null;
      if (legitLevelUp) {
        int newNextLevel = user.getLevel() + 2;
        int expRequiredForNewNextLevel = LevelsRequiredExperienceRetrieveUtils.getRequiredExperienceForLevel(newNextLevel);
        resBuilder.setNewNextLevel(newNextLevel);
        resBuilder.setExperienceRequiredForNewNextLevel(expRequiredForNewNextLevel);

        int newLevel = user.getLevel() + 1;
        resBuilder.setNewLevel(newLevel);

        List<City> availCities = MiscMethods.getCitiesAvailableForUserLevel(newLevel);
        for (City city : availCities) {
          if (city.getMinLevel() == newLevel) {
            resBuilder.addCitiesNewlyAvailableToUser(CreateInfoProtoUtils.createFullCityProtoFromCity(city));
            if (newlyUnlockedCityIds == null) newlyUnlockedCityIds = new ArrayList<Integer>();
            newlyUnlockedCityIds.add(city.getId());
          }
        }

        Map<Integer, Equipment> equipIdToEquips = EquipmentRetrieveUtils.getEquipmentIdsToEquipment();
        if (equipIdToEquips != null) {
          for (Equipment e : equipIdToEquips.values()) {
            if (e != null && e.getMinLevel() == newLevel && 
                e.getClassType() == MiscMethods.getClassTypeFromUserType(user.getType()) &&
                (e.getRarity() == Rarity.EPIC || e.getRarity() == Rarity.LEGENDARY)) {
              resBuilder.addNewlyEquippableEpicsAndLegendaries(CreateInfoProtoUtils.createFullEquipProtoFromEquip(e));
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
      
      UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
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
        if (!UpdateUtils.get().incrementCityRankForUserCity(user.getId(), cityId, 1)) {
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
      log.error("user is null");
      return false;
    }
    if (user.getLevel() == ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER) {
      resBuilder.setStatus(LevelUpStatus.ALREADY_AT_MAX_LEVEL);
      log.error("user is already at server's allowed max level: " + ControllerConstants.LEVEL_UP__MAX_LEVEL_FOR_USER);
      return false;
    }
    Integer expRequiredForNextLevel = LevelsRequiredExperienceRetrieveUtils.getRequiredExperienceForLevel(user.getLevel() + 1);
    if (expRequiredForNextLevel == null) {
      resBuilder.setStatus(LevelUpStatus.OTHER_FAIL);
      log.error("no experience required inputted for level: " + user.getLevel() + 1);
      return false;      
    }
    if (user.getExperience() < expRequiredForNextLevel) {
      resBuilder.setStatus(LevelUpStatus.NOT_ENOUGH_EXP_TO_NEXT_LEVEL);
      log.error("user only has " + user.getExperience() + " and needs " + expRequiredForNextLevel + " for next level");
      return false;            
    }
    resBuilder.setStatus(LevelUpStatus.SUCCESS);
    return true;
  }

}
