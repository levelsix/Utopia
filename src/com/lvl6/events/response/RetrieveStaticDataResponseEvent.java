package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NonBroadcastResponseEvent;
import com.lvl6.proto.EventProto.RetrieveStaticDataResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RetrieveStaticDataResponseEvent extends NonBroadcastResponseEvent {

  private RetrieveStaticDataResponseProto retrieveStaticDataResponseProto;
  
  public RetrieveStaticDataResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_RETRIEVE_STATIC_DATA_EVENT;
  }

  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = retrieveStaticDataResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setRetrieveStaticDataResponseProto(
      RetrieveStaticDataResponseProto retrieveStaticDataResponseProto) {
    this.retrieveStaticDataResponseProto = retrieveStaticDataResponseProto;
  }
  
}
