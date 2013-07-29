package com.lvl6.server.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ChangeUserLocationRequestEvent;
import com.lvl6.events.response.ChangeUserLocationResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Location;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.ChangeUserLocationRequestProto;
import com.lvl6.proto.EventProto.ChangeUserLocationResponseProto;
import com.lvl6.proto.EventProto.ChangeUserLocationResponseProto.Builder;
import com.lvl6.proto.EventProto.ChangeUserLocationResponseProto.ChangeUserLocationStatus;
import com.lvl6.proto.InfoProto.LocationProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.RetrieveUtils;

  @Component @DependsOn("gameServer") public class ChangeUserLocationController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  public ChangeUserLocationController() {
    numAllocatedThreads = 3;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new ChangeUserLocationRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_CHANGE_USER_LOCATION_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    ChangeUserLocationRequestProto reqProto = ((ChangeUserLocationRequestEvent)event).getChangeUserLocationRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    LocationProto location = reqProto.getUserLocation();
    
    ChangeUserLocationResponseProto.Builder resBuilder = ChangeUserLocationResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      
      boolean legitLocationChange = checkLegitLocationChange(resBuilder, user, location);

      ChangeUserLocationResponseEvent resEvent = new ChangeUserLocationResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setChangeUserLocationResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitLocationChange) {
        writeChangesToDB(user, location);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in ChangeUserLocation processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private boolean checkLegitLocationChange(Builder resBuilder, User user, LocationProto location) {
    if (user == null || location == null) {
      resBuilder.setStatus(ChangeUserLocationStatus.OTHER_FAIL);
      log.error("user or location is null. user="+user+", location="+location);
      return false;
    }
    if (location.getLatitude() < ControllerConstants.LATITUDE_MIN || location.getLatitude() > ControllerConstants.LATITUDE_MAX || 
        location.getLongitude() < ControllerConstants.LONGITUDE_MIN || location.getLongitude() > ControllerConstants.LONGITUDE_MAX) {
      resBuilder.setStatus(ChangeUserLocationStatus.INVALID_BOUNDS);
      log.error("location is out of bounds. location="+location);
      return false;
    }
    resBuilder.setStatus(ChangeUserLocationStatus.SUCCESS);
    return true;
  }

  private void writeChangesToDB(User user, LocationProto location) {
    if (!user.updateAbsoluteUserLocation(new Location(location.getLatitude(), location.getLongitude()))) {
      log.error("problem with updating user location for " + user + ", location " + location);
    }
  }
}
