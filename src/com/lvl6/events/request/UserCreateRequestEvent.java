package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.PreDatabaseRequestEvent;
import com.lvl6.proto.EventProto.UserCreateRequestProto;

public class UserCreateRequestEvent extends PreDatabaseRequestEvent{

  private UserCreateRequestProto userCreateRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try { 
      userCreateRequestProto = UserCreateRequestProto.parseFrom(ByteString.copyFrom(buff));
      
      // Player id is -1 since it won't be initialized yet. 
      playerId = -1;
      
      udid = userCreateRequestProto.getUdid();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public UserCreateRequestProto getUserCreateRequestProto() {
    return userCreateRequestProto;
  }
}