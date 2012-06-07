package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.EarnFreeGoldResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class EarnFreeGoldResponseEvent extends NormalResponseEvent {

  private EarnFreeGoldResponseProto earnFreeGoldResponseProto;
  
  public EarnFreeGoldResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_ENABLE_APNS_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = earnFreeGoldResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setEarnFreeGoldResponseProto(EarnFreeGoldResponseProto earnFreeGoldResponseProto) {
    this.earnFreeGoldResponseProto = earnFreeGoldResponseProto;
  }

}
