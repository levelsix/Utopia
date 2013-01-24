package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveClanTowerScoresRequestProto;

public class RetrieveClanTowerScoresRequestEvent extends RequestEvent {

  private RetrieveClanTowerScoresRequestProto retrieveClanTowerScoresRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      retrieveClanTowerScoresRequestProto = RetrieveClanTowerScoresRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveClanTowerScoresRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveClanTowerScoresRequestProto getRetrieveClanTowerScoresRequestProto() {
    return retrieveClanTowerScoresRequestProto;
  }
}
