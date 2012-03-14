package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PurgeClientStaticDataResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PurgeClientStaticDataResponseEvent extends NormalResponseEvent {

  private PurgeClientStaticDataResponseProto purgeClientStaticDataResponseProto;
  
  public PurgeClientStaticDataResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_PURGE_STATIC_DATA_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = purgeClientStaticDataResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPurgeClientStaticDataResponseProto(PurgeClientStaticDataResponseProto purgeClientStaticDataResponseProto) {
    this.purgeClientStaticDataResponseProto = purgeClientStaticDataResponseProto;
  }

}
