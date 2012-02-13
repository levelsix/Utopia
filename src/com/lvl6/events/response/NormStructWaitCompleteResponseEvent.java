package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.NormStructWaitCompleteResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class NormStructWaitCompleteResponseEvent extends NonBroadcastResponseEvent {

  private NormStructWaitCompleteResponseProto normStructWaitCompleteResponseProto;
  
  public NormStructWaitCompleteResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_NORM_STRUCT_WAIT_COMPLETE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = normStructWaitCompleteResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setNormStructWaitCompleteResponseProto(NormStructWaitCompleteResponseProto normStructWaitCompleteResponseProto) {
    this.normStructWaitCompleteResponseProto = normStructWaitCompleteResponseProto;
  }

}
