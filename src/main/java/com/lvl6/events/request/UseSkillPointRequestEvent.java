package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.UseSkillPointRequestProto;

public class UseSkillPointRequestEvent extends RequestEvent {

  private UseSkillPointRequestProto useSkillPointRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      useSkillPointRequestProto = UseSkillPointRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = useSkillPointRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public UseSkillPointRequestProto getUseSkillPointRequestProto() {
    return useSkillPointRequestProto;
  }
}
