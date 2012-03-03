package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.LoadNeutralCityResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class LoadNeutralCityResponseEvent extends NormalResponseEvent{

  private LoadNeutralCityResponseProto loadNeutralCityResponseProto;
  
  public LoadNeutralCityResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_LOAD_NEUTRAL_CITY_EVENT;
  }
  
  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = loadNeutralCityResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setLoadNeutralCityResponseProto(
      LoadNeutralCityResponseProto loadNeutralCityResponseProto) {
    this.loadNeutralCityResponseProto = loadNeutralCityResponseProto;
  }
  
  
  
}
