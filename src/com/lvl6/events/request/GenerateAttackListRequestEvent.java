package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.GenerateAttackListRequestProto;

public class GenerateAttackListRequestEvent extends RequestEvent {

  private GenerateAttackListRequestProto generateAttackListRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      generateAttackListRequestProto = GenerateAttackListRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = generateAttackListRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public GenerateAttackListRequestProto getGenerateAttackListRequestProto() {
    return generateAttackListRequestProto;
  }
}
