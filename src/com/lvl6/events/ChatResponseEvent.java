package com.lvl6.events;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.proto.EventProto.ChatResponseProto;

public class ChatResponseEvent extends BroadcastResponseEvent {

  private ChatResponseProto chatResponseProto;

  
  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = chatResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

}
