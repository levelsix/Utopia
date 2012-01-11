package com.lvl6.utils;

import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.UpdateClientUserResponseProto;

public class MiscMethods {

  public static UpdateClientUserResponseEvent createUpdateClientUserResponseEvent(User user) {
    UpdateClientUserResponseEvent resEvent = new UpdateClientUserResponseEvent(user.getId());
    UpdateClientUserResponseProto resProtoAttacker = UpdateClientUserResponseProto.newBuilder()
        .setSender(CreateInfoProtoUtils.createFullUserProtoFromUser(user)).build();
    resEvent.setUpdateClientUserResponseProto(resProtoAttacker);
    return resEvent;
  }
  
}
