package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.VaultRequestProto;

public class VaultRequestEvent extends RequestEvent {

  private VaultRequestProto vaultRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      vaultRequestProto = VaultRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = vaultRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public VaultRequestProto getVaultRequestProto() {
    return vaultRequestProto;
  }
}
