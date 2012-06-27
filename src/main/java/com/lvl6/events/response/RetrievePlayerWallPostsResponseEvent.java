package com.lvl6.events.response;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.proto.EventProto.RetrievePlayerWallPostsResponseProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolResponse;

public class RetrievePlayerWallPostsResponseEvent extends NormalResponseEvent {

  private RetrievePlayerWallPostsResponseProto retrievePlayerWallPostsResponseProto;
  
  public RetrievePlayerWallPostsResponseEvent(int playerId) {
    super(playerId);
    eventType = EventProtocolResponse.S_RETRIEVE_PLAYER_WALL_POSTS_EVENT;
  }

  /** 
   * write the event to the given ByteBuffer
   * 
   * note we are using 1.4 ByteBuffers for both client and server
   * depending on the deployment you may need to support older java
   * versions on the client and use old-style socket input/output streams
   */
  public int write(ByteBuffer buff) {
    ByteString b = retrievePlayerWallPostsResponseProto.toByteString();
    b.copyTo(buff);
    return b.size();
  }

  public void setRetrievePlayerWallPostsResponseProto(
      RetrievePlayerWallPostsResponseProto retrievePlayerWallPostsResponseProto) {
    this.retrievePlayerWallPostsResponseProto = retrievePlayerWallPostsResponseProto;
  }
  
}
