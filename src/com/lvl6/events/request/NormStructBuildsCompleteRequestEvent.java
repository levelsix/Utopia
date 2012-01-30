package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.NormStructBuildsCompleteRequestProto;

public class NormStructBuildsCompleteRequestEvent extends RequestEvent {

  private NormStructBuildsCompleteRequestProto normStructBuildsCompleteRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      normStructBuildsCompleteRequestProto = NormStructBuildsCompleteRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = normStructBuildsCompleteRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public NormStructBuildsCompleteRequestProto getNormStructBuildsCompleteRequestProto() {
    return normStructBuildsCompleteRequestProto;
  }
}
