package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.GenerateAttackListRequestEvent;
import com.lvl6.events.response.GenerateAttackListResponseEvent;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.GenerateAttackListRequestProto;
import com.lvl6.proto.EventProto.GenerateAttackListResponseProto;
import com.lvl6.proto.EventProto.GenerateAttackListResponseProto.GenerateAttackListStatus;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;

  @Component @DependsOn("gameServer") public class GenerateAttackListController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public GenerateAttackListController() {
    numAllocatedThreads = 20;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new GenerateAttackListRequestEvent();
  }
  
  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_GENERATE_ATTACK_LIST_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    GenerateAttackListRequestProto reqProto = ((GenerateAttackListRequestEvent)event).getGenerateAttackListRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int numEnemies = reqProto.getNumEnemies();
    Double latLowerBound = (reqProto.hasLatLowerBound()) ? reqProto.getLatLowerBound() : null;
    Double latUpperBound = (reqProto.hasLatUpperBound()) ? reqProto.getLatUpperBound() : null;
    Double longLowerBound = (reqProto.hasLongLowerBound()) ? reqProto.getLongLowerBound() : null;
    Double longUpperBound = (reqProto.hasLongUpperBound()) ? reqProto.getLongUpperBound() : null;
    boolean showRealPlayers = reqProto.getShowRealPlayers();
    
    GenerateAttackListResponseProto.Builder resBuilder = GenerateAttackListResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(GenerateAttackListStatus.SUCCESS);
    resBuilder.setForMap(reqProto.getForMap());
    resBuilder.setShowRealPlayers(showRealPlayers);
    
    User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
    if (numEnemies > ControllerConstants.GENERATE_ATTACK_LIST__NUM_ENEMIES_TO_GENERATE_MAX || numEnemies < 1) {
      resBuilder.setStatus(GenerateAttackListStatus.INVALID_NUM_ENEMIES_COUNT);      
    } else if ((latLowerBound != null && (latLowerBound < ControllerConstants.LATITUDE_MIN || latLowerBound > ControllerConstants.LATITUDE_MAX)) || 
        (latUpperBound != null && (latUpperBound < ControllerConstants.LATITUDE_MIN || latUpperBound > ControllerConstants.LATITUDE_MAX)) || 
        (longLowerBound != null && (longLowerBound < ControllerConstants.LONGITUDE_MIN || longLowerBound > ControllerConstants.LONGITUDE_MAX)) ||
        (longUpperBound != null && (longUpperBound < ControllerConstants.LONGITUDE_MIN || longUpperBound > ControllerConstants.LONGITUDE_MAX))) {
      resBuilder.setStatus(GenerateAttackListStatus.INVALID_BOUND);
      log.error("invalid bounds passed in. lat lower bound=" + latLowerBound + ", lat upper bound=" + latUpperBound
          + ", long lower bound=" + longLowerBound + ", long upper bound=" + longUpperBound);
    } else if (user != null) {
      List<UserType> userTypes = new ArrayList<UserType>();
      if (MiscMethods.checkIfGoodSide(user.getType())) {
        userTypes.add(UserType.BAD_ARCHER);
        userTypes.add(UserType.BAD_MAGE);
        userTypes.add(UserType.BAD_WARRIOR);
      } else {
        userTypes.add(UserType.GOOD_ARCHER);
        userTypes.add(UserType.GOOD_MAGE);
        userTypes.add(UserType.GOOD_WARRIOR);
      }
      
      boolean realPlayersOnly = showRealPlayers;
      boolean fakePlayersOnly = !showRealPlayers;
      boolean offlinePlayersOnly = true; //does not include fake players
      boolean prestigePlayersOnly = false;
      boolean inactiveShield = true;
      
      if (user.getPrestigeLevel() > 0) {
        //show prestige people only
        prestigePlayersOnly = true;
      }
      
      //PROCURE THE VICTIMS! MWAHAHAHAH
      List<User> enemies = RetrieveUtils.userRetrieveUtils().getUsers(userTypes,
          numEnemies, user.getLevel(), user.getId(), false, latLowerBound,
          latUpperBound, longLowerBound, longUpperBound, true, realPlayersOnly,
          fakePlayersOnly, offlinePlayersOnly, prestigePlayersOnly,
          inactiveShield, null);
      if (enemies != null) {
        List<Integer> playerIds = new ArrayList<Integer>(); //ids that are added to builder
        Set<Integer> forbiddenIds = new HashSet<Integer>(); //ids that should not be added to builder
        addPlayersToBuilder(resBuilder, enemies, user, forbiddenIds, playerIds);
        
        if (enemies.size() < numEnemies) {
          //since not enough real offline players include the online players
          offlinePlayersOnly = false;
          enemies = RetrieveUtils.userRetrieveUtils().getUsers(userTypes, numEnemies,
              user.getLevel(), user.getId(), false, latLowerBound, latUpperBound,
              longLowerBound, longUpperBound, true, realPlayersOnly, fakePlayersOnly,
              offlinePlayersOnly, prestigePlayersOnly, inactiveShield, playerIds);
          
          forbiddenIds.addAll(playerIds);
          addPlayersToBuilder(resBuilder, enemies, user, forbiddenIds, null);
        }
      } else {
        resBuilder.setStatus(GenerateAttackListStatus.SOME_FAIL);
      }
    } else {
      resBuilder.setStatus(GenerateAttackListStatus.SOME_FAIL);
      log.error("no user in db with id " + senderProto.getUserId());
    }

    GenerateAttackListResponseProto resProto = resBuilder.build();

    GenerateAttackListResponseEvent resEvent = new GenerateAttackListResponseEvent(senderProto.getUserId());
    resEvent.setTag(event.getTag());
    resEvent.setGenerateAttackListResponseProto(resProto);

    server.writeEvent(resEvent);
  }

  private void addPlayersToBuilder (GenerateAttackListResponseProto.Builder resBuilder,
      List<User> enemies, User user, Set<Integer> forbiddenIds, List<Integer> playerIds) {
    for (User enemy : enemies) {
      int enemyId = enemy.getId();
      //add user if within level range and not forbidden
      if ((Math.abs(enemy.getLevel() - user.getLevel()) <= ControllerConstants.BATTLE__MAX_LEVEL_DIFFERENCE)
          && !forbiddenIds.contains(enemyId)) {
        FullUserProto fup = CreateInfoProtoUtils.createFullUserProtoFromUser(enemy);
        resBuilder.addEnemies(fup);
        if (null != playerIds) {
          playerIds.add(enemyId);
        }
      }
    }
  }
}
