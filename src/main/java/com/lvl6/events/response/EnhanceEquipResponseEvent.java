package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.EnhanceEquipResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class EnhanceEquipResponseEvent extends NormalResponseEvent {

  private EnhanceEquipResponseProto enhanceEquipResponseProto;
  
  public EnhanceEquipResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_ENHANCE_EQUIP_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = enhanceEquipResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setEnhanceEquipResponseProto(EnhanceEquipResponseProto enhanceEquipResponseProto) {
    this.enhanceEquipResponseProto = enhanceEquipResponseProto;
  }

}
