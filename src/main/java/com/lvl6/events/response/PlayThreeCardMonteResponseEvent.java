package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PlayThreeCardMonteResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PlayThreeCardMonteResponseEvent extends NormalResponseEvent{

  private PlayThreeCardMonteResponseProto playThreeCardMonteResponseProto;
  
  public PlayThreeCardMonteResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_PLAY_THREE_CARD_MONTE_EVENT;
  }
  
  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = playThreeCardMonteResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setPlayThreeCardMonteResponseProto(PlayThreeCardMonteResponseProto playThreeCardMonteResponseProto) {
    this.playThreeCardMonteResponseProto = playThreeCardMonteResponseProto;
  }
  
}
