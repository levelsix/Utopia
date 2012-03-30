package com.lvl6.server.controller;

import java.sql.Timestamp;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.RetrieveCurrencyFromNormStructureRequestEvent;
import com.lvl6.events.response.RetrieveCurrencyFromNormStructureResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.Structure;
import com.lvl6.info.User;
import com.lvl6.info.UserStruct;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureRequestProto;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureResponseProto;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureResponseProto.Builder;
import com.lvl6.proto.EventProto.RetrieveCurrencyFromNormStructureResponseProto.RetrieveCurrencyFromNormStructureStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.retrieveutils.UserStructRetrieveUtils;
import com.lvl6.retrieveutils.rarechange.StructureRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;
import com.lvl6.utils.utilmethods.UpdateUtils;

public class RetrieveCurrencyFromNormStructureController extends EventController{

  public RetrieveCurrencyFromNormStructureController() {
    numAllocatedThreads = 14;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new RetrieveCurrencyFromNormStructureRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) {
    RetrieveCurrencyFromNormStructureRequestProto reqProto = ((RetrieveCurrencyFromNormStructureRequestEvent)event).getRetrieveCurrencyFromNormStructureRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    int userStructId = reqProto.getUserStructId();
    Timestamp timeOfRetrieval = new Timestamp(reqProto.getTimeOfRetrieval());

    RetrieveCurrencyFromNormStructureResponseProto.Builder resBuilder = RetrieveCurrencyFromNormStructureResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    UserStruct userStruct = UserStructRetrieveUtils.getSpecificUserStruct(userStructId);
    Structure struct = null;
    if (userStruct != null) {
      struct = StructureRetrieveUtils.getStructForStructId(userStruct.getStructId());
    }
    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
      
      boolean legitRetrieval = checkLegitRetrieval(resBuilder, user, userStruct, struct, timeOfRetrieval);
      
      if (legitRetrieval) {
        if (!user.updateRelativeCoinsNaive(MiscMethods.calculateIncomeGainedFromUserStruct(struct.getIncome(), userStruct.getLevel()))) {
          log.error("problem with updating user stats after retrieving currency");
          legitRetrieval = false;
        }
        if (!UpdateUtils.updateUserStructLastretrieved(userStructId, timeOfRetrieval)) {
          log.error("problem with updating user stats");
          legitRetrieval = false;
        }
      }

      RetrieveCurrencyFromNormStructureResponseEvent resEvent = new RetrieveCurrencyFromNormStructureResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setRetrieveCurrencyFromNormStructureResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);
      
      if (legitRetrieval) {
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
      }

    } catch (Exception e) {
      log.error("exception in RetrieveCurrencyFromNormStructureController processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private boolean checkLegitRetrieval(Builder resBuilder, User user, UserStruct userStruct, Structure struct, Timestamp timeOfRetrieval) {
    // TODO Auto-generated method stub
    if (user == null || userStruct == null || timeOfRetrieval == null || user.getId() != userStruct.getUserId() || !userStruct.isComplete() || userStruct.getLastRetrieved() == null) {
      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.OTHER_FAIL);
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(timeOfRetrieval)) {
      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.CLIENT_TOO_AHEAD_OF_SERVER_TIME);
      return false;
    }
    if ((timeOfRetrieval.getTime() - userStruct.getLastRetrieved().getTime())  < 60000*struct.getMinutesToGain()) {
      resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.NOT_LONG_ENOUGH);
      return false;
    }
    resBuilder.setStatus(RetrieveCurrencyFromNormStructureStatus.SUCCESS);
    return true;
  }

}
