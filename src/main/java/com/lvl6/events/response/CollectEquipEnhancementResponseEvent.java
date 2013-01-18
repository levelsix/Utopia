package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.CollectEquipEnhancementResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class CollectEquipEnhancementResponseEvent extends NormalResponseEvent {

  private CollectEquipEnhancementResponseProto collectEquipEnhanceResponseProto;
  
  public CollectEquipEnhancementResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_COLLECT_EQUIP_ENHANCEMENT_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = collectEquipEnhanceResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setCollectEquipEnhancementResponseProto(CollectEquipEnhancementResponseProto collectEquipEnhanceResponseProto) {
    this.collectEquipEnhanceResponseProto = collectEquipEnhanceResponseProto;
  }

}
