package com.lvl6.server.controller;

import java.util.List;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.GenerateAttackListRequestEvent;
import com.lvl6.events.response.GenerateAttackListResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.GenerateAttackListRequestProto;
import com.lvl6.proto.EventProto.GenerateAttackListResponseProto;
import com.lvl6.proto.InfoProto.FullUserProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class GenerateAttackListController extends EventController {

  private static final int NUM_ENEMIES_TO_GENERATE = 25;

  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());        
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new GenerateAttackListRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_GENERATE_ATTACK_LIST_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    GenerateAttackListRequestProto reqProto = ((GenerateAttackListRequestEvent)event).getGenerateAttackListRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();

    GenerateAttackListResponseProto.Builder resBuilder = GenerateAttackListResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
    if (user != null) {
      boolean generateListOfGoodSide = true;
      if (MiscMethods.checkIfGoodSide(user.getType())) {
        generateListOfGoodSide = false; 
      }
            
      List<User> enemies = UserRetrieveUtils.getUsersForSide(generateListOfGoodSide, NUM_ENEMIES_TO_GENERATE, user.getLevel(), user.getId());
      if (enemies != null) {
        for (User enemy : enemies) {
          FullUserProto fup = CreateInfoProtoUtils.createFullUserProtoFromUser(enemy);
          resBuilder.addEnemies(fup);
        }
      }
    }

    GenerateAttackListResponseProto resProto = resBuilder.build();

    GenerateAttackListResponseEvent resEvent = new GenerateAttackListResponseEvent(senderProto.getUserId());
    resEvent.setGenerateAttackListResponseProto(resProto);

    server.writeEvent(resEvent);
  }

}
