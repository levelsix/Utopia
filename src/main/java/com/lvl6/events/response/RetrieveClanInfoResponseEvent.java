package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.RetrieveClanInfoResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RetrieveClanInfoResponseEvent extends NormalResponseEvent {

  private RetrieveClanInfoResponseProto retrieveClanInfoResponseProto;
  
  public RetrieveClanInfoResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_RETRIEVE_CLAN_INFO_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = retrieveClanInfoResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setRetrieveClanInfoResponseProto(RetrieveClanInfoResponseProto retrieveClanInfoResponseProto) {
    this.retrieveClanInfoResponseProto = retrieveClanInfoResponseProto;
  }

}
