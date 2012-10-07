package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.CollectFromGoldmineResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class CollectFromGoldmineResponseEvent extends NormalResponseEvent {

  private CollectFromGoldmineResponseProto collectFromGoldmineResponseProto;
  
  public CollectFromGoldmineResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_COLLECT_FROM_GOLDMINE_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = collectFromGoldmineResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setCollectFromGoldmineResponseProto(CollectFromGoldmineResponseProto collectFromGoldmineResponseProto) {
    this.collectFromGoldmineResponseProto = collectFromGoldmineResponseProto;
  }

  public CollectFromGoldmineResponseProto getCollectFromGoldmineResponseProto() {   //because APNS required
    return collectFromGoldmineResponseProto;
  }
  
}
