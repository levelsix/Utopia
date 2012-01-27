package com.lvl6.server.controller;


import java.nio.ByteBuffer;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.StartupRequestEvent;
import com.lvl6.events.response.StartupResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.Globals;
import com.lvl6.proto.EventProto.StartupRequestProto;
import com.lvl6.proto.EventProto.StartupResponseProto;
import com.lvl6.proto.EventProto.StartupResponseProto.StartupStatus;
import com.lvl6.proto.EventProto.StartupResponseProto.UpdateStatus;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.server.GameServer;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.NIOUtils;

public class StartupController extends EventController {
  
  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());    
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new StartupRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_STARTUP_EVENT;
  }
  
  /*
   * 1. Retrieve full user from db
   */
  @Override
  protected void processRequestEvent(RequestEvent event) {
    StartupRequestProto reqProto = ((StartupRequestEvent)event).getStartupRequestProto();
    UpdateStatus updateStatus;
    String udid = reqProto.getUdid();
    
    StartupResponseProto.Builder resBuilder = StartupResponseProto.newBuilder();
    
    // Check version number
    if ((int)reqProto.getVersionNum() < (int)GameServer.clientVersionNumber) {
      updateStatus = UpdateStatus.MAJOR_UPDATE;
    } else if (reqProto.getVersionNum() < GameServer.clientVersionNumber) {
      updateStatus = UpdateStatus.MINOR_UPDATE;
    } else {
      updateStatus = UpdateStatus.NO_UPDATE;
    }
    
    resBuilder.setUpdateStatus(updateStatus);
    
    // Don't fill in other fields if it is a major update
    StartupStatus startupStatus = StartupStatus.USER_NOT_IN_DB;
    if (updateStatus != UpdateStatus.MAJOR_UPDATE) {
      User user = UserRetrieveUtils.getUserByUDID(udid);
      if (user != null) {
        startupStatus = StartupStatus.USER_IN_DB;
        
        FullUserProto fup = CreateInfoProtoUtils.createFullUserProtoFromUser(user);
        resBuilder.setSender(fup);
      }
    }
    resBuilder.setStartupStatus(startupStatus);
    
    StartupResponseProto resProto = resBuilder.build();
    StartupResponseEvent resEvent = new StartupResponseEvent(udid);
    resEvent.setStartupResponseProto(resProto);
    
    log.info("Writing event: " + resEvent);
    // Write event directly since EventWriter cannot handle without userId.
    ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Globals.MAX_EVENT_SIZE);
    NIOUtils.prepBuffer(resEvent, writeBuffer);
    NIOUtils.channelWrite(server.removePreDbPlayer(udid), writeBuffer);
  }
}
