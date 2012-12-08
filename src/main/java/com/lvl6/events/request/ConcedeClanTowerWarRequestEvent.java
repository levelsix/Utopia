package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.BeginClanTowerWarRequestProto;
import com.lvl6.proto.EventProto.ConcedeClanTowerWarRequestProto;

public class ConcedeClanTowerWarRequestEvent extends RequestEvent {

  private ConcedeClanTowerWarRequestProto concedeClanTowerWarRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      concedeClanTowerWarRequestProto = ConcedeClanTowerWarRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = concedeClanTowerWarRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ConcedeClanTowerWarRequestProto getConcedeClanTowerWarRequestProto() {
    return concedeClanTowerWarRequestProto;
  }
}
