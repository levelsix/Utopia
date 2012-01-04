package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.properties.EventProtocol;
import com.lvl6.proto.EventProto.ClericCostCalculateResponseProto;

public class ClericCostCalculateResponseEvent extends NonBroadcastResponseEvent {
  
  private ClericCostCalculateResponseProto clericCostCalcResponseProto;

  public ClericCostCalculateResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocol.S_CLERIC_COST_CALC_EVENT;
  }
  
  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = clericCostCalcResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setClericCostCalcResponseProto(
      ClericCostCalculateResponseProto clericCostCalcResponseProto) {
    this.clericCostCalcResponseProto = clericCostCalcResponseProto;
  }
  
}
