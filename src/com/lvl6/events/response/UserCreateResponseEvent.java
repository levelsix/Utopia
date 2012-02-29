package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.PreDatabaseResponseEvent;
import com.lvl6.proto.EventProto.UserCreateResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class UserCreateResponseEvent extends PreDatabaseResponseEvent{

  private UserCreateResponseProto userCreateResponseProto;
  
  public UserCreateResponseEvent(String udid) {
    super(udid);
    eventType = EventProtocolResponse.S_USER_CREATE_EVENT;
  }
  
  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = userCreateResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setUserCreateResponseProto(UserCreateResponseProto UserCreateResponseProto) {
    this.userCreateResponseProto = UserCreateResponseProto;
  }
  
}
