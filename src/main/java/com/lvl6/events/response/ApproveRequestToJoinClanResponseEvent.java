package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.ApproveRequestToJoinClanResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class ApproveRequestToJoinClanResponseEvent extends NormalResponseEvent {

  private ApproveRequestToJoinClanResponseProto approveRequestToJoinClanResponseProto;
  
  public ApproveRequestToJoinClanResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_APPROVE_REQUEST_TO_JOIN_CLAN_EVENT;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = approveRequestToJoinClanResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setApproveRequestToJoinClanResponseProto(ApproveRequestToJoinClanResponseProto approveRequestToJoinClanResponseProto) {
    this.approveRequestToJoinClanResponseProto = approveRequestToJoinClanResponseProto;
  }

}
