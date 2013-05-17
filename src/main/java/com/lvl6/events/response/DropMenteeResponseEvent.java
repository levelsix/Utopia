package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.DropMenteeResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class DropMenteeResponseEvent extends NormalResponseEvent {
  
  private DropMenteeResponseProto dropMenteeResponseProto;

  public DropMenteeResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_RETRIEVE_ALL_MENTORS_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = dropMenteeResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }
  
  public void setDropMenteeResponseProto(
      DropMenteeResponseProto dropMenteeResponseProto) {
    this.dropMenteeResponseProto = dropMenteeResponseProto;
  }

  public DropMenteeResponseProto getDropMenteeResponseProto() {
    return dropMenteeResponseProto;
  }
  
}