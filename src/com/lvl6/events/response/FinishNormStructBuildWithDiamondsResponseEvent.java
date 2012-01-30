package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.FinishNormStructBuildWithDiamondsResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class FinishNormStructBuildWithDiamondsResponseEvent extends NonBroadcastResponseEvent{

  private FinishNormStructBuildWithDiamondsResponseProto finishNormStructBuildWithDiamondsResponseProto;
  
  public FinishNormStructBuildWithDiamondsResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_FINISH_NORM_STRUCT_BUILD_WITH_DIAMONDS_EVENT;
  }
  
  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = finishNormStructBuildWithDiamondsResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setFinishNormStructBuildWithDiamondsResponseProto(
      FinishNormStructBuildWithDiamondsResponseProto finishNormStructBuildWithDiamondsResponseProto) {
    this.finishNormStructBuildWithDiamondsResponseProto = finishNormStructBuildWithDiamondsResponseProto;
  }
  
  
  
}
