package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.ApproveRequestToJoinClanRequestProto;

public class ApproveRequestToJoinClanRequestEvent extends RequestEvent {

  private ApproveRequestToJoinClanRequestProto approveRequestToJoinClanRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      approveRequestToJoinClanRequestProto = ApproveRequestToJoinClanRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = approveRequestToJoinClanRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ApproveRequestToJoinClanRequestProto getApproveRequestToJoinClanRequestProto() {
    return approveRequestToJoinClanRequestProto;
  }
}
