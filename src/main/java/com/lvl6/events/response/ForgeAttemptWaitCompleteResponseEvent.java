package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.ForgeAttemptWaitCompleteResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class ForgeAttemptWaitCompleteResponseEvent extends NormalResponseEvent {

  private ForgeAttemptWaitCompleteResponseProto forgeAttemptWaitCompleteResponseProto;
  
  public ForgeAttemptWaitCompleteResponseEvent(int playerId){
    super(playerId);
    eventType = EventProtocolResponse.S_FORGE_ATTEMPT_WAIT_COMPLETE;
  }
  
  @Override
  public int write(ByteBuffer bb) {
    ByteString b = forgeAttemptWaitCompleteResponseProto.toByteString();
    b.copyTo(bb);
    return b.size();
  }

  public void setForgeAttemptWaitCompleteResponseProto(ForgeAttemptWaitCompleteResponseProto forgeAttemptWaitCompleteResponseProto) {
    this.forgeAttemptWaitCompleteResponseProto = forgeAttemptWaitCompleteResponseProto;
  }

}
