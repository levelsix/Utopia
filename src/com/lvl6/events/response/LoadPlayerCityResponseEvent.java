package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.LoadPlayerCityResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class LoadPlayerCityResponseEvent extends NonBroadcastResponseEvent{

  private LoadPlayerCityResponseProto loadPlayerCityResponseProto;
  
  public LoadPlayerCityResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_LOAD_PLAYER_CITY_EVENT;
  }
  
  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = loadPlayerCityResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setLoadPlayerCityResponseProto(
      LoadPlayerCityResponseProto loadPlayerCityResponseProto) {
    this.loadPlayerCityResponseProto = loadPlayerCityResponseProto;
  }
  
  
  
}
