package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.SubmitEquipEnhancementResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class SubmitEquipEnhancementResponseEvent extends NormalResponseEvent {

  private SubmitEquipEnhancementResponseProto submitEquipEnhanceResponseProto;
  
  public SubmitEquipEnhancementResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_SUBMIT_EQUIP_ENHANCEMENT_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = submitEquipEnhanceResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setSubmitEquipEnhancementResponseProto(SubmitEquipEnhancementResponseProto submitEquipEnhanceResponseProto) {
    this.submitEquipEnhanceResponseProto = submitEquipEnhanceResponseProto;
  }

}
