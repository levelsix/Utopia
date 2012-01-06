package com.lvl6.events.request;

import java.nio.ByteBuffer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lvl6.events.RequestEvent;
import com.lvl6.proto.EventProto.TaskActionRequestProto;

public class TaskActionRequestEvent extends RequestEvent {

  private TaskActionRequestProto taskActionRequestProto;
  
  /**
   * read the event from the given ByteBuffer to populate this event
   */
  public void read(ByteBuffer buff) {
    try {
      taskActionRequestProto = TaskActionRequestProto.parseFrom(ByteString.copyFrom(buff));
      playerId = taskActionRequestProto.getSender().getUserId();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }

  public TaskActionRequestProto getTaskActionRequestProto() {
    return taskActionRequestProto;
  }
}
