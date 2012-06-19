package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.LoadPlayerCityRequestEvent;
import com.lvl6.events.response.LoadPlayerCityResponseEvent;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.LoadPlayerCityRequestProto;
import com.lvl6.proto.EventProto.LoadPlayerCityResponseProto;
import com.lvl6.proto.EventProto.LoadPlayerCityResponseProto.Builder;
import com.lvl6.proto.EventProto.LoadPlayerCityResponseProto.LoadPlayerCityStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

  @Component @DependsOn("gameServer") public class LoadPlayerCityController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  public LoadPlayerCityController() {
    numAllocatedThreads = 10;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new LoadPlayerCityRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_LOAD_PLAYER_CITY_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    LoadPlayerCityRequestProto reqProto = ((LoadPlayerCityRequestEvent)event).getLoadPlayerCityRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int cityOwnerId = reqProto.getCityOwnerId();

    LoadPlayerCityResponseProto.Builder resBuilder = LoadPlayerCityResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    resBuilder.setStatus(LoadPlayerCityStatus.SUCCESS);
    server.lockPlayer(senderProto.getUserId());

    try {
      User owner = RetrieveUtils.userRetrieveUtils().getUserById(cityOwnerId);

      List<UserStruct> userStructs = RetrieveUtils.userStructRetrieveUtils().getUserStructsForUser(cityOwnerId);
      setResponseUserStructs(resBuilder, userStructs);

//      Map<CritStructType, UserCritstruct> userCritStructs = UserCritstructRetrieveUtils.getUserCritstructsForUser(cityOwnerProto.getUserId());
//      setResponseCritstructs(resBuilder, userCritStructs);
      
//      UserCityExpansionData userCityExpansionData = UserCityExpansionRetrieveUtils.getUserCityExpansionDataForUser(cityOwnerProto.getUserId());
//      if (userCityExpansionData != null) {
//        resBuilder.setUserCityExpansionData(CreateInfoProtoUtils.createFullUserCityExpansionDataProtoFromUserCityExpansionData(userCityExpansionData));
//      }

      resBuilder.setCityOwner(CreateInfoProtoUtils.createMinimumUserProtoFromUser(owner));
      
      boolean ownerIsGood = MiscMethods.checkIfGoodSide(owner.getType());
      boolean senderIsGood = MiscMethods.checkIfGoodSide(senderProto.getUserType());

      List<UserType> userTypes = new ArrayList<UserType>();
      if (senderIsGood) {
        userTypes.add(UserType.BAD_ARCHER);
        userTypes.add(UserType.BAD_MAGE);
        userTypes.add(UserType.BAD_WARRIOR);
      } else {
        userTypes.add(UserType.GOOD_ARCHER);
        userTypes.add(UserType.GOOD_MAGE);
        userTypes.add(UserType.GOOD_WARRIOR);
      }

      if (ownerIsGood != senderIsGood) {    //loading enemy city, load some of owners allies (more enemies from your POV)
        List<User> ownerAllies = RetrieveUtils.userRetrieveUtils().getUsers(userTypes, ControllerConstants.LOAD_PLAYER_CITY__APPROX_NUM_USERS_IN_CITY, owner.getLevel(), owner.getId(), false, 
            null, null, null, null, false);
        setResponseOwnerAlliesOrEnemies(resBuilder, ownerAllies, true);
      } else {                              //loading ally city or your city, creating some of owners enemies
        List<User> ownerEnemies = RetrieveUtils.userRetrieveUtils().getUsers(userTypes, ControllerConstants.LOAD_PLAYER_CITY__APPROX_NUM_USERS_IN_CITY, owner.getLevel(), owner.getId(), false, 
            null, null, null, null, false);
        setResponseOwnerAlliesOrEnemies(resBuilder, ownerEnemies, false);
      }

      LoadPlayerCityResponseEvent resEvent = new LoadPlayerCityResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setLoadPlayerCityResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

    } catch (Exception e) {
      log.error("exception in LoadPlayerCity processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private void setResponseOwnerAlliesOrEnemies(Builder resBuilder, List<User> users, boolean ownerAllies) {
    if (users != null) {
      for (User user : users) {
        if (ownerAllies) {
          resBuilder.addOwnerAllies(CreateInfoProtoUtils.createFullUserProtoFromUser(user));
        } else {
          resBuilder.addOwnerEnemies(CreateInfoProtoUtils.createFullUserProtoFromUser(user));          
        }
      }
    } else {
      resBuilder.setStatus(LoadPlayerCityStatus.OTHER_FAIL);
      log.error("no users found when loading city owner's allies/enemies, ownerAllies="+ownerAllies);
    }    
  }

  private void setResponseUserStructs(Builder resBuilder,
      List<UserStruct> userStructs) {
    if (userStructs != null) {
      for (UserStruct userStruct : userStructs) {
        resBuilder.addOwnerNormStructs(CreateInfoProtoUtils.createFullUserStructureProtoFromUserstruct(userStruct));
      }
    } else {
      resBuilder.setStatus(LoadPlayerCityStatus.OTHER_FAIL);
      log.error("user structs found for user is null");
    }
  }

//  private void setResponseCritstructs(Builder resBuilder, Map<CritStructType, UserCritstruct> userCritStructs) {
//    if (userCritStructs != null) {
//      for (CritStructType cst : userCritStructs.keySet()) {
//        UserCritstruct uc = userCritStructs.get(cst);
//        if (uc != null) {
//          if (cst == CritStructType.ARMORY) {
//            resBuilder.setArmory(CreateInfoProtoUtils.createFullUserCritstructProtoFromUserCritstruct(uc));
//          }
//          if (cst == CritStructType.AVIARY) {
//            resBuilder.setAviary(CreateInfoProtoUtils.createFullUserCritstructProtoFromUserCritstruct(uc));
//          }
//          if (cst == CritStructType.CARPENTER) {
//            resBuilder.setCarpenter(CreateInfoProtoUtils.createFullUserCritstructProtoFromUserCritstruct(uc));
//          }
//          if (cst == CritStructType.MARKETPLACE) {
//            resBuilder.setMarketplace(CreateInfoProtoUtils.createFullUserCritstructProtoFromUserCritstruct(uc));
//          }
//          if (cst == CritStructType.VAULT) {
//            resBuilder.setVault(CreateInfoProtoUtils.createFullUserCritstructProtoFromUserCritstruct(uc));
//          }
//        } else {
//          resBuilder.setStatus(LoadPlayerCityStatus.OTHER_FAIL);
//        }
//      }
//    } else {
//      resBuilder.setStatus(LoadPlayerCityStatus.OTHER_FAIL);
//    }
//  }

}
