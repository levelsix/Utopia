package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.UserQuestDetailsRequestProto;

public class UserQuestDetailsRequestEvent extends RequestEvent {

  private UserQuestDetailsRequestProto userQuestDetailsRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      userQuestDetailsRequestProto = UserQuestDetailsRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = userQuestDetailsRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public UserQuestDetailsRequestProto getUserQuestDetailsRequestProto() {
    return userQuestDetailsRequestProto;
  }
}
