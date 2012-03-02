package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.EquipEquipmentResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class EquipEquipmentResponseEvent extends NormalResponseEvent {

  private EquipEquipmentResponseProto equipEquipmentResponseProto;
  
  public EquipEquipmentResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_EQUIP_EQUIPMENT_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = equipEquipmentResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setEquipEquipmentResponseProto(EquipEquipmentResponseProto equipEquipmentResponseProto) {
    this.equipEquipmentResponseProto = equipEquipmentResponseProto;
  }

}
