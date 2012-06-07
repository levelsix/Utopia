package com.lvl6.server.controller;


import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.ChangeUserLocationRequestEvent;
import com.lvl6.events.response.ChangeUserLocationResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Location;
import com.lvl6.info.User;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.ChangeUserLocationRequestProto;
import com.lvl6.proto.EventProto.ChangeUserLocationResponseProto;
import com.lvl6.proto.EventProto.ChangeUserLocationResponseProto.Builder;
import com.lvl6.proto.EventProto.ChangeUserLocationResponseProto.ChangeUserLocationStatus;
import com.lvl6.proto.InfoProto.LocationProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

  @Component @DependsOn("gameServer") public class ChangeUserLocationController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

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

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      
      boolean legitLocationChange = checkLegitLocationChange(resBuilder, user, location);

      ChangeUserLocationResponseEvent resEvent = new ChangeUserLocationResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setChangeUserLocationResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitLocationChange) {
        writeChangesToDB(user, location);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in ChangeUserLocation processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
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
