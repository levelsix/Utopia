package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.RetrieveUserEquipForUserRequestProto;

public class RetrieveUserEquipForUserRequestEvent extends RequestEvent{
  private RetrieveUserEquipForUserRequestProto retrieveUserEquipForUserRequestProto;
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  @Override
  public void read(ByteBuffer buff) {
    try {
      retrieveUserEquipForUserRequestProto = RetrieveUserEquipForUserRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = retrieveUserEquipForUserRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public RetrieveUserEquipForUserRequestProto getRetrieveUserEquipForUserRequestProto() {
    return retrieveUserEquipForUserRequestProto;
  }
  
}//RetrieveUserEquipForUserRequestProto