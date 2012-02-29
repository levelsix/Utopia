package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.ReferralCodeUsedResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class ReferralCodeUsedResponseEvent extends NormalResponseEvent{

  private ReferralCodeUsedResponseProto referralCodeUsedResponseProto;
  
  public ReferralCodeUsedResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_REFERRAL_CODE_USED_EVENT;
  }
  
  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = referralCodeUsedResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setReferralCodeUsedResponseProto(ReferralCodeUsedResponseProto referralCodeUsedResponseProto) {
    this.referralCodeUsedResponseProto = referralCodeUsedResponseProto;
  }

  public ReferralCodeUsedResponseProto getReferralCodeUsedResponseProto() {
    return referralCodeUsedResponseProto;
  }
  
}
