package com.lvl6.server.controller;

import java.util.List;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.LevelUpRequestEvent;
import com.lvl6.events.response.LevelUpResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.City;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.LevelUpRequestProto;
import com.lvl6.proto.EventProto.LevelUpResponseProto;
import com.lvl6.proto.EventProto.LevelUpResponseProto.Builder;
import com.lvl6.proto.EventProto.LevelUpResponseProto.LevelUpStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.LevelsRequiredExperienceRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class LevelUpController extends EventController {

  @Override
  public RequestEvent createRequestEvent() {
    return new LevelUpRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_ARMORY_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    LevelUpRequestProto reqProto = ((LevelUpRequestEvent)event).getLevelUpRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    LevelUpResponseProto.Builder resBuilder = LevelUpResponseProto.newBuilder();

    server.lockPlayer(senderProto.getUserId());
    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      boolean legitLevelUp = checkLegitLevelUp(resBuilder, user);
      if (legitLevelUp) {
        int newNextLevel = user.getLevel() + 2;
        int expRequiredForNewNextLevel = LevelsRequiredExperienceRetrieveUtils.getRequiredExperienceForLevel(user.getLevel() + 2);
        resBuilder.setNewNextLevel(newNextLevel);
        resBuilder.setExperienceRequiredForNewNextLevel(expRequiredForNewNextLevel);

        List<City> availCities = MiscMethods.getCitiesAvailableForUserLevel(user.getLevel() + 1);
        for (City city : availCities) {
          resBuilder.addCitiesAvailableToUser(CreateInfoProtoUtils.createFullCityProtoFromCity(city));
        }
      }
      
      
      LevelUpResponseProto resProto = resBuilder.build();
      LevelUpResponseEvent resEvent = new LevelUpResponseEvent(senderProto.getUserId());
      resEvent.setLevelUpResponseProto(resProto);
      server.writeEvent(resEvent);

      writeChangesToDB(user);
      
      UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
      server.writeEvent(resEventUpdate);
    } catch (Exception e) {
      log.error("exception in LevelUpController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId()); 
    }
  }

  private void writeChangesToDB(User user) {
    if (!user.updateLevel(1)) {
      log.error("problem in changing the user's level");
    }
  }

  private boolean checkLegitLevelUp(Builder resBuilder, User user) {
    if (user == null) {
      resBuilder.setStatus(LevelUpStatus.OTHER_FAIL);
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
