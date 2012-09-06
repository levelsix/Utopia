package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.DeleteClanResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class DeleteClanResponseEvent extends NormalResponseEvent {

  private DeleteClanResponseProto deleteClanResponseProto;
  
  public DeleteClanResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_DELETE_CLAN_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = deleteClanResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setDeleteClanResponseProto(DeleteClanResponseProto deleteClanResponseProto) {
    this.deleteClanResponseProto = deleteClanResponseProto;
  }

}
