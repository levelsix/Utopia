package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.NormStructBuildsCompleteResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class NormStructBuildsCompleteResponseEvent extends NonBroadcastResponseEvent {

  private NormStructBuildsCompleteResponseProto normStructBuildsCompleteResponseProto;
  
  public NormStructBuildsCompleteResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_NORM_STRUCT_BUILDS_COMPLETE;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = normStructBuildsCompleteResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setNormStructBuildsCompleteResponseProto(NormStructBuildsCompleteResponseProto normStructBuildsCompleteResponseProto) {
    this.normStructBuildsCompleteResponseProto = normStructBuildsCompleteResponseProto;
  }

}
