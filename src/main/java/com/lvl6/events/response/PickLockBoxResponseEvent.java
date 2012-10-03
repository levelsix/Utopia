package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.PickLockBoxResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class PickLockBoxResponseEvent extends NormalResponseEvent {

  private PickLockBoxResponseProto pickLockBoxResponseProto;
  
  public PickLockBoxResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_PICK_LOCK_BOX_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = pickLockBoxResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setPickLockBoxResponseProto(PickLockBoxResponseProto pickLockBoxResponseProto) {
    this.pickLockBoxResponseProto = pickLockBoxResponseProto;
  }

}
