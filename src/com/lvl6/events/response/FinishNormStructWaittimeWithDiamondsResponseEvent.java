package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.FinishNormStructWaittimeWithDiamondsResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class FinishNormStructWaittimeWithDiamondsResponseEvent extends NonBroadcastResponseEvent{

  private FinishNormStructWaittimeWithDiamondsResponseProto finishNormStructWaittimeWithDiamondsResponseProto;
  
  public FinishNormStructWaittimeWithDiamondsResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS_EVENT;
  }
  
  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = finishNormStructWaittimeWithDiamondsResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setFinishNormStructWaittimeWithDiamondsResponseProto(
      FinishNormStructWaittimeWithDiamondsResponseProto finishNormStructWaittimeWithDiamondsResponseProto) {
    this.finishNormStructWaittimeWithDiamondsResponseProto = finishNormStructWaittimeWithDiamondsResponseProto;
  }
  
  
  
}
