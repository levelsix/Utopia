package com.lvl6.server.controller;

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.EarnFreeGoldRequestEvent;
import com.lvl6.events.response.EarnFreeGoldResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.EarnFreeGoldRequestProto;
import com.lvl6.proto.EventProto.EarnFreeGoldRequestProto.EarnFreeGoldType;
import com.lvl6.proto.EventProto.EarnFreeGoldResponseProto;
import com.lvl6.proto.EventProto.EarnFreeGoldResponseProto.Builder;
import com.lvl6.proto.EventProto.EarnFreeGoldResponseProto.EarnFreeGoldStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class EarnFreeGoldController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
  
  private static String ADCOLONY_V4VC_SECRET_KEY = "v4vc5ec0f36707ad4afaa5452e";
  
  private static String KIIP_KEY = "d6c7530ce4dc64ecbff535e521a241e3";
  private static String KIIP_SECRET = "da8d864f948ae2b4e83c1b6e6a8151ed";
  private static String KIIP_ENDPOINT = "https://api.kiip.me/1.0/transaction/invalidate";

  public EarnFreeGoldController() {
    numAllocatedThreads = 1;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new EarnFreeGoldRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_EARN_FREE_GOLD;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    EarnFreeGoldRequestProto reqProto = ((EarnFreeGoldRequestEvent)event).getEarnFreeGoldRequestProto();
    MinimumUserProto senderProto = reqProto.getSender();

    EarnFreeGoldType freeGoldType = reqProto.getFreeGoldType();
    Timestamp clientTime = new Timestamp(reqProto.getClientTime());

    EarnFreeGoldResponseProto.Builder resBuilder = EarnFreeGoldResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    
    server.lockPlayer(senderProto.getUserId());
    
    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      
      boolean legitFreeGoldEarn = checkLegitFreeGoldEarn(resBuilder, freeGoldType, clientTime, user);

      if (legitFreeGoldEarn) {
        
      }
      
      EarnFreeGoldResponseEvent resEvent = new EarnFreeGoldResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setEarnFreeGoldResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitFreeGoldEarn) {
        writeChangesToDB(freeGoldType);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }
    } catch (Exception e) {
      log.error("exception in EquipEquipment processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }


  private void writeChangesToDB(EarnFreeGoldType freeGoldType) {
    
//    URL url = new URL()
    
    // TODO Auto-generated method stub
    
  }

  private boolean checkLegitFreeGoldEarn(Builder resBuilder, EarnFreeGoldType freeGoldType, Timestamp clientTime, User user) {
    if (freeGoldType == null || clientTime == null || user == null) {
      resBuilder.setStatus(EarnFreeGoldStatus.OTHER_FAIL);
      log.error("parameter passed in is null. freeGoldType is " + freeGoldType + ", clientTime=" + clientTime + ", user=" + user);
      return false;
    }

    if (freeGoldType == EarnFreeGoldType.KIIP) {
    } else if (freeGoldType == EarnFreeGoldType.ADCOLONY) {
    } else if (freeGoldType == EarnFreeGoldType.FB_INVITE) {
    } else if (freeGoldType == EarnFreeGoldType.TAPJOY) {
    } else if (freeGoldType == EarnFreeGoldType.FLURRY_VIDEO) {
    } else if (freeGoldType == EarnFreeGoldType.TWITTER) {
    } else {
      resBuilder.setStatus(EarnFreeGoldStatus.METHOD_NOT_SUPPORTED);
      log.error("earn free gold type passed in not supported. type=" + freeGoldType);
      return false;
    }
    resBuilder.setStatus(EarnFreeGoldStatus.SUCCESS);
    return true;  
  }


}
