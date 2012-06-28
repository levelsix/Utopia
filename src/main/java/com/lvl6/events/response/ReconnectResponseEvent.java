package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.ReconnectResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class ReconnectResponseEvent extends NormalResponseEvent {

  private ReconnectResponseProto reconnectResponseProto;
  
  public ReconnectResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_RECONNECT_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = reconnectResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setReconnectResponseProto(ReconnectResponseProto reconnectResponseProto) {
    this.reconnectResponseProto = reconnectResponseProto;
  }

}