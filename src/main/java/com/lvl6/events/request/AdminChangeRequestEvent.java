package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.AdminProto.AdminChangeRequestProto;

public class AdminChangeRequestEvent extends RequestEvent {
  
  private static final int ADMIN_ID = -10;

  private AdminChangeRequestProto armoryRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      armoryRequestProto = AdminChangeRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = ADMIN_ID;
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public AdminChangeRequestProto getAdminChangeRequestProto() {
    return armoryRequestProto;
  }
}
