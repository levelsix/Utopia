package com.lvl6.server.controller;

import java.util.List;
import java.util.Map;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.LoadPlayerCityRequestEvent;
import com.lvl6.events.response.LoadPlayerCityResponseEvent;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.User;
import com.lvl6.info.UserCritstruct;
import com.lvl6.info.UserStruct;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.LoadPlayerCityRequestProto;
import com.lvl6.proto.EventProto.LoadPlayerCityResponseProto;
import com.lvl6.proto.EventProto.LoadPlayerCityResponseProto.Builder;
import com.lvl6.proto.EventProto.LoadPlayerCityResponseProto.LoadPlayerCityStatus;
import com.lvl6.proto.InfoProto.CoordinateProto;
import com.lvl6.proto.InfoProto.CritStructType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserCritstructRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class LoadPlayerCityController extends EventController {

  @Override
  public RequestEvent createRequestEvent() {
    return new LoadPlayerCityRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_LOAD_PLAYER_CITY_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    LoadPlayerCityRequestProto reqProto = ((LoadPlayerCityRequestEvent)event).getLoadPlayerCityRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    MinimumUserProto cityOwnerProto = reqProto.getCityOwner();

    LoadPlayerCityResponseProto.Builder resBuilder = LoadPlayerCityResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    resBuilder.setStatus(LoadPlayerCityStatus.SUCCESS);
    server.lockPlayer(senderProto.getUserId());

    try {
      User owner = UserRetrieveUtils.getUserById(cityOwnerProto.getUserId());
      Map<CritStructType, UserCritstruct> userCritStructs = UserCritstructRetrieveUtils.getUserCritstructsForUser(cityOwnerProto.getUserId());
      setResponseCritstructs(resBuilder, userCritStructs);
      List<UserStruct> userStructs = UserStructRetrieveUtils.getUserStructsForUser(cityOwnerProto.getUserId());
      setResponseUserStructs(resBuilder, userStructs);
      
      boolean ownerIsGood = MiscMethods.checkIfGoodSide(cityOwnerProto.getUserType());
      List<User> ownerAllies = UserRetrieveUtils.getUsersForSide(ownerIsGood, ControllerConstants.LOAD_PLAYER_CITY__APPROX_NUM_ALLIES_IN_CITY, owner.getLevel(), owner.getId());
      setResponseOwnerAllies(resBuilder, ownerAllies);
      
      LoadPlayerCityResponseEvent resEvent = new LoadPlayerCityResponseEvent(senderProto.getUserId());
      resEvent.setLoadPlayerCityResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

    } catch (Exception e) {
      log.error("exception in LoadPlayerCity processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }


  }

  private void setResponseOwnerAllies(Builder resBuilder, List<User> ownerAllies) {
    if (ownerAllies != null) {
      for (User ownerAlly : ownerAllies) {
        resBuilder.addOwnerAllies(CreateInfoProtoUtils.createFullUserProtoFromUser(ownerAlly));
      }
    } else {
      resBuilder.setStatus(LoadPlayerCityStatus.OTHER_FAIL);
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
    }
  }

  private void setResponseCritstructs(Builder resBuilder, Map<CritStructType, UserCritstruct> userCritStructs) {
    if (userCritStructs != null) {
      for (CritStructType cst : userCritStructs.keySet()) {
        UserCritstruct uc = userCritStructs.get(cst);
        if (uc != null) {
          if (cst == CritStructType.ARMORY) {
            resBuilder.setArmory(CreateInfoProtoUtils.createFullUserCritstructProtoFromUserCritstruct(uc));
          }
          if (cst == CritStructType.AVIARY) {
            resBuilder.setAviary(CreateInfoProtoUtils.createFullUserCritstructProtoFromUserCritstruct(uc));
          }
          if (cst == CritStructType.CARPENTER) {
            resBuilder.setCarpenter(CreateInfoProtoUtils.createFullUserCritstructProtoFromUserCritstruct(uc));
          }
          if (cst == CritStructType.LUMBERMILL) {
            resBuilder.setLumbermill(CreateInfoProtoUtils.createFullUserCritstructProtoFromUserCritstruct(uc));
          }
          if (cst == CritStructType.MARKETPLACE) {
            resBuilder.setMarketplace(CreateInfoProtoUtils.createFullUserCritstructProtoFromUserCritstruct(uc));
          }
          if (cst == CritStructType.VAULT) {
            resBuilder.setVault(CreateInfoProtoUtils.createFullUserCritstructProtoFromUserCritstruct(uc));
          }
        } else {
          resBuilder.setStatus(LoadPlayerCityStatus.OTHER_FAIL);
        }
      }
    } else {
      resBuilder.setStatus(LoadPlayerCityStatus.OTHER_FAIL);
    }
  }

}
