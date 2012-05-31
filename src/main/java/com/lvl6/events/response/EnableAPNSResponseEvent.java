package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.EnableAPNSResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class EnableAPNSResponseEvent extends NormalResponseEvent {

  private EnableAPNSResponseProto enableAPNSResponseProto;
  
  public EnableAPNSResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_ENABLE_APNS_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = enableAPNSResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setEnableAPNSResponseProto(EnableAPNSResponseProto enableAPNSResponseProto) {
    this.enableAPNSResponseProto = enableAPNSResponseProto;
  }

}
