
package com.lvl6.server.controller;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.UserCreateRequestEvent;
import com.lvl6.events.response.ReferralCodeUsedResponseEvent;
import com.lvl6.events.response.UserCreateResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.Globals;
import com.lvl6.proto.EventProto.ReferralCodeUsedResponseProto;
import com.lvl6.proto.EventProto.UserCreateRequestProto;
import com.lvl6.proto.EventProto.UserCreateResponseProto;
import com.lvl6.proto.EventProto.UserCreateResponseProto.Builder;
import com.lvl6.proto.EventProto.UserCreateResponseProto.UserCreateStatus;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.FullUserStructureProto;
import com.lvl6.proto.InfoProto.LocationProto;
import com.lvl6.proto.InfoProto.UserType;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.AvailableReferralCodeRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.NIOUtils;
import com.lvl6.utils.utilmethods.DeleteUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class UserCreateController extends EventController {

  public UserCreateController() {
    numAllocatedThreads = 3;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new UserCreateRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_USER_CREATE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    UserCreateRequestProto reqProto = ((UserCreateRequestEvent)event).getUserCreateRequestProto();
    String udid = reqProto.getUdid();
    String name = reqProto.getName();
    UserType type = reqProto.getType();
    List<FullUserStructureProto> fullUserStructs = reqProto.getStructuresList();
    LocationProto location = reqProto.getUserLocation();

    String referrerCode = (reqProto.hasReferrerCode()) ? reqProto.getReferrerCode() : null;
    String deviceToken = (reqProto.hasDeviceToken()) ? reqProto.getDeviceToken() : null;

    UserCreateResponseProto.Builder resBuilder = UserCreateResponseProto.newBuilder();

    boolean legitUserCreate = checkLegitUserCreate(resBuilder, udid, name, fullUserStructs, 
        location);

    User referrer = null;
    User user = null;
    if (legitUserCreate) {
      referrer = (referrerCode != null && referrerCode.length() > 0) ? UserRetrieveUtils.getUserByReferralCode(referrerCode) : null;

      String newReferCode = grabNewReferCode();
      
      int userId = InsertUtils.insertUser(udid, name, type, location, referrer != null, deviceToken, newReferCode);
      if (userId > 0) {
        server.lockPlayer(userId);
        try {
          user = UserRetrieveUtils.getUserById(userId);
          FullUserProto userProto = CreateInfoProtoUtils.createFullUserProtoFromUser(user);
          resBuilder.setSender(userProto);
        } catch (Exception e) {
          log.error("exception in UserCreateController processEvent", e);
        } finally {
          server.unlockPlayer(userId); 
        }
      } else {
        resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
      }
    }

    UserCreateResponseProto resProto = resBuilder.build();
    UserCreateResponseEvent resEvent = new UserCreateResponseEvent(udid);
    resEvent.setTag(event.getTag());
    resEvent.setUserCreateResponseProto(resProto);

    log.info("Writing event: " + resEvent);
    // Write event directly since EventWriter cannot handle without userId.
    ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);
    NIOUtils.prepBuffer(resEvent, writeBuffer);

    SocketChannel sc = server.removePreDbPlayer(udid);

    if (user != null) {
      ConnectedPlayer p = new ConnectedPlayer();
      p.setPlayerId(user.getId());
      p.setChannel(sc);
      server.addPlayer(p);
      log.debug("delegate event, new player created and channel set, player:" + 
          p.getPlayerId() + ", channel: " + sc);
    }

    NIOUtils.channelWrite(sc, writeBuffer);

    if (legitUserCreate) {
      writeUserStructs(fullUserStructs);
      writeUserCritstructs();
      if (!UpdateUtils.incrementCityRankForUserCity(user.getId(), 1, 1)) {
        log.error("problem with giving user access to first city");
      }
      
      if (referrer != null && user != null) {
        rewardReferrer(referrer, user);        
      }
    }    
  }

  private String grabNewReferCode() {
    String newReferCode = AvailableReferralCodeRetrieveUtils.getAvailableReferralCode();
    if (newReferCode != null && newReferCode.length() > 0) {
      while (!DeleteUtils.deleteAvailableReferralCode(newReferCode)) {
        newReferCode = AvailableReferralCodeRetrieveUtils.getAvailableReferralCode();
      }
    }
    return newReferCode;
  }

  private void rewardReferrer(User referrer, User user) {
    server.lockPlayer(referrer.getId());
    try {
      if (!referrer.updateRelativeDiamondsNumreferrals(ControllerConstants.USER_CREATE__DIAMOND_REWARD_FOR_REFERRER, 1)) {
        log.error("problem with rewarding the referrer");
      } else {
        InsertUtils.insertReferral(referrer.getId(), user.getId());

        ReferralCodeUsedResponseEvent resEvent = new ReferralCodeUsedResponseEvent(referrer.getId());
        ReferralCodeUsedResponseProto resProto = ReferralCodeUsedResponseProto.newBuilder()
            .setSender(CreateInfoProtoUtils.createMinimumUserProtoFromUser(referrer))
            .setReferredPlayer(CreateInfoProtoUtils.createMinimumUserProtoFromUser(user)).build();
        resEvent.setReferralCodeUsedResponseProto(resProto);
        server.writeAPNSNotificationOrEvent(resEvent);
      }
    } catch (Exception e) {
      log.error("exception in UserCreateController processEvent", e);
    } finally {
      server.unlockPlayer(referrer.getId()); 
    }
  }

  private boolean checkLegitUserCreate(Builder resBuilder, String udid,
      String name, List<FullUserStructureProto> fullUserStructs,
      LocationProto location) {
    if (udid == null || name == null || fullUserStructs == null || fullUserStructs.size() == 0 || location == null) {
      resBuilder.setStatus(UserCreateStatus.OTHER_FAIL);
      return false;
    }
    if (UserRetrieveUtils.getUserByUDID(udid) != null) {
      resBuilder.setStatus(UserCreateStatus.USER_WITH_UDID_ALREADY_EXISTS);
      return false;
    }
    if (location.getLatitude() < ControllerConstants.LATITUDE_MIN || location.getLatitude() > ControllerConstants.LATITUDE_MAX || 
        location.getLongitude() < ControllerConstants.LONGITUDE_MIN || location.getLongitude() > ControllerConstants.LONGITUDE_MAX) {
      resBuilder.setStatus(UserCreateStatus.INVALID_LOCATION);
      return false;
    }
    if (name.length() < ControllerConstants.USER_CREATE__MIN_NAME_LENGTH) {
      resBuilder.setStatus(UserCreateStatus.INVALID_NAME);
      return false;
    }
    resBuilder.setStatus(UserCreateStatus.SUCCESS);
    return true;
  }

}
