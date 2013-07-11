package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.CreateClanColeaderResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class CreateClanColeaderResponseEvent extends NormalResponseEvent {

  private CreateClanColeaderResponseProto createClanColeaderResponseProto;
  
  public CreateClanColeaderResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_APPROVE_OR_REJECT_REQUEST_TO_JOIN_CLAN_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = createClanColeaderResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setCreateClanColeaderResponseProto(CreateClanColeaderResponseProto createClanColeaderResponseProto) {
    this.createClanColeaderResponseProto = createClanColeaderResponseProto;
  }

}
