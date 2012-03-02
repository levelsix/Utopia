package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.GenerateAttackListRequestEvent;
import com.lvl6.events.response.GenerateAttackListResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.GenerateAttackListRequestProto;
import com.lvl6.proto.EventProto.GenerateAttackListResponseProto;
import com.lvl6.proto.EventProto.GenerateAttackListResponseProto.GenerateAttackListStatus;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class GenerateAttackListController extends EventController {

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
  protected void processRequestEvent(RequestEvent event) {
    GenerateAttackListRequestProto reqProto = ((GenerateAttackListRequestEvent)event).getGenerateAttackListRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int numEnemies = reqProto.getNumEnemies();
    Integer latLowerBound = (reqProto.hasLatLowerBound()) ? reqProto.getLatLowerBound() : null;
    Integer latUpperBound = (reqProto.hasLatUpperBound()) ? reqProto.getLatUpperBound() : null;
    Integer longLowerBound = (reqProto.hasLongLowerBound()) ? reqProto.getLongLowerBound() : null;
    Integer longUpperBound = (reqProto.hasLongUpperBound()) ? reqProto.getLongUpperBound() : null;
    
    GenerateAttackListResponseProto.Builder resBuilder = GenerateAttackListResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(GenerateAttackListStatus.SUCCESS);

    User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
    if (numEnemies > ControllerConstants.GENERATE_ATTACK_LIST__NUM_ENEMIES_TO_GENERATE_MAX || numEnemies < 1) {
      resBuilder.setStatus(GenerateAttackListStatus.INVALID_NUM_ENEMIES_COUNT);      
    } else if ((latLowerBound != null && (latLowerBound < ControllerConstants.LATITUDE_MIN || latLowerBound > ControllerConstants.LATITUDE_MAX)) || 
        (latUpperBound != null && (latUpperBound < ControllerConstants.LATITUDE_MIN || latUpperBound > ControllerConstants.LATITUDE_MAX)) || 
        (longLowerBound != null && (longLowerBound < ControllerConstants.LONGITUDE_MIN || longLowerBound > ControllerConstants.LONGITUDE_MAX)) ||
        (longUpperBound != null && (longUpperBound < ControllerConstants.LONGITUDE_MIN || longUpperBound > ControllerConstants.LONGITUDE_MAX))) {
      resBuilder.setStatus(GenerateAttackListStatus.INVALID_BOUND);
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

      List<User> enemies = UserRetrieveUtils.getUsers(userTypes, numEnemies, user.getLevel(), user.getId(), false, 
          latLowerBound, latUpperBound, longLowerBound, longUpperBound, true);
      if (enemies != null) {
        for (User enemy : enemies) {
          FullUserProto fup = CreateInfoProtoUtils.createFullUserProtoFromUser(enemy);
          resBuilder.addEnemies(fup);
        }
      } else {
        resBuilder.setStatus(GenerateAttackListStatus.SOME_FAIL);
      }
    } else {
      resBuilder.setStatus(GenerateAttackListStatus.SOME_FAIL);
    }

    GenerateAttackListResponseProto resProto = resBuilder.build();

    GenerateAttackListResponseEvent resEvent = new GenerateAttackListResponseEvent(senderProto.getUserId());
    resEvent.setTag(event.getTag());
    resEvent.setGenerateAttackListResponseProto(resProto);

    server.writeEvent(resEvent);
  }

}
