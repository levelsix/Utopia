package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.RetrieveAllMentorsResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RetrieveAllMentorsResponseEvent extends NormalResponseEvent {
  
  private RetrieveAllMentorsResponseProto retrieveAllMentorsResponseProto;

  public RetrieveAllMentorsResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_RETRIEVE_ALL_MENTORS_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = retrieveAllMentorsResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }
  
  public void setRetrieveAllMentorsResponseProto(
      RetrieveAllMentorsResponseProto retrieveAllMentorsResponseProto) {
    this.retrieveAllMentorsResponseProto = retrieveAllMentorsResponseProto;
  }

  public RetrieveAllMentorsResponseProto getRetrieveAllMentorsResponseProto() {
    return retrieveAllMentorsResponseProto;
  }
  
}