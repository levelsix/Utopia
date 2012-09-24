package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.ApproveOrRejectRequestToJoinClanRequestProto;

public class ApproveOrRejectRequestToJoinClanRequestEvent extends RequestEvent {

  private ApproveOrRejectRequestToJoinClanRequestProto approveOrRejectRequestToJoinClanRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      approveOrRejectRequestToJoinClanRequestProto = ApproveOrRejectRequestToJoinClanRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = approveOrRejectRequestToJoinClanRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public ApproveOrRejectRequestToJoinClanRequestProto getApproveOrRejectRequestToJoinClanRequestProto() {
    return approveOrRejectRequestToJoinClanRequestProto;
  }
}
