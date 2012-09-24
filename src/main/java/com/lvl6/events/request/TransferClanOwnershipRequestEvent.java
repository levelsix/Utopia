package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.TransferClanOwnershipRequestProto;

public class TransferClanOwnershipRequestEvent extends RequestEvent {

  private TransferClanOwnershipRequestProto transferClanOwnershipRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      transferClanOwnershipRequestProto = TransferClanOwnershipRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = transferClanOwnershipRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public TransferClanOwnershipRequestProto getTransferClanOwnershipRequestProto() {
    return transferClanOwnershipRequestProto;
  }
}
