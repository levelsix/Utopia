package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.DeleteClanRequestProto;

public class DeleteClanRequestEvent extends RequestEvent {

  private DeleteClanRequestProto deleteClanRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      deleteClanRequestProto = DeleteClanRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = deleteClanRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public DeleteClanRequestProto getDeleteClanRequestProto() {
    return deleteClanRequestProto;
  }
}
