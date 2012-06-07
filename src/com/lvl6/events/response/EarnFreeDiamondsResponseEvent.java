package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.EarnFreeDiamondsResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class EarnFreeDiamondsResponseEvent extends NormalResponseEvent {

  private EarnFreeDiamondsResponseProto earnFreeDiamondsResponseProto;
  
  public EarnFreeDiamondsResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_ENABLE_APNS_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = earnFreeDiamondsResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setEarnFreeDiamondsResponseProto(EarnFreeDiamondsResponseProto earnFreeDiamondsResponseProto) {
    this.earnFreeDiamondsResponseProto = earnFreeDiamondsResponseProto;
  }

}
