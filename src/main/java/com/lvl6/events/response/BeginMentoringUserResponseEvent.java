package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.BeginMentoringUserResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class BeginMentoringUserResponseEvent extends NormalResponseEvent {
  
  private BeginMentoringUserResponseProto beginMentoringUserResponseProto;

  public BeginMentoringUserResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_BEGIN_MENTORING_USER_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = beginMentoringUserResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }
  
  public void setBeginMentoringUserResponseProto(
      BeginMentoringUserResponseProto beginMentoringUserResponseProto) {
    this.beginMentoringUserResponseProto = beginMentoringUserResponseProto;
  }

  public BeginMentoringUserResponseProto getBeginMentoringUserResponseProto() {
    return beginMentoringUserResponseProto;
  }
  
}