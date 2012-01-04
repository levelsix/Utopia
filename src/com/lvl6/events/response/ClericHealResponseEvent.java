package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.properties.EventProtocol;
import com.lvl6.proto.EventProto.ClericHealResponseProto;

public class ClericHealResponseEvent extends NonBroadcastResponseEvent{

  private ClericHealResponseProto clericHealResponseProto;
  
  public ClericHealResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocol.S_CLERIC_HEAL_EVENT;
  }
  
  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = clericHealResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setClericHealResponseProto(
      ClericHealResponseProto clericHealResponseProto) {
    this.clericHealResponseProto = clericHealResponseProto;
  }
  
  
  
}
