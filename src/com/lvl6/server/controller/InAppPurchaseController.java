package com.lvl6.server.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.InAppPurchaseRequestEvent;
import com.lvl6.events.response.InAppPurchaseResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.Globals;
import com.lvl6.properties.IAPValues;
import com.lvl6.proto.EventProto.InAppPurchaseRequestProto;
import com.lvl6.proto.EventProto.InAppPurchaseResponseProto;
import com.lvl6.proto.EventProto.InAppPurchaseResponseProto.InAppPurchaseStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.IAPHistoryRetrieveUtils;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class InAppPurchaseController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String SANDBOX_URL = "https://sandbox.itunes.apple.com/verifyReceipt";
  private static final String PRODUCTION_URL = "https://buy.itunes.apple.com/verifyReceipt";  

  public InAppPurchaseController() {
    numAllocatedThreads = 2;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new InAppPurchaseRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_IN_APP_PURCHASE_EVENT;
  }

  /*
   * db stuff done before sending event to eventwriter/client because the client's not waiting 
   * on it immediately anyways
   */
  @Override
  protected void processRequestEvent(RequestEvent event) {
    InAppPurchaseRequestProto reqProto = ((InAppPurchaseRequestEvent)event).getInAppPurchaseRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    String receipt = reqProto.getReceipt();

    InAppPurchaseResponseProto.Builder resBuilder = InAppPurchaseResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    // Lock this player's ID
    server.lockPlayer(senderProto.getUserId());
    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());
  
      JSONObject response;
      try {
        JSONObject jsonReceipt = new JSONObject();
        jsonReceipt.put(IAPValues.RECEIPT_DATA, receipt);
  
        // Send data
        URL url;
        if (Globals.IS_SANDBOX) {
          url = new URL(SANDBOX_URL);
        } else {
          url = new URL(PRODUCTION_URL);
        }
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(jsonReceipt.toString());
        wr.flush();
  
        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
  
        String responseString = "";
        String line;
        while ((line = rd.readLine()) != null) {
            responseString += line;
        }
  
        response = new JSONObject(responseString);
        if (response.getInt(IAPValues.STATUS) == 0) {
          JSONObject receiptFromApple = response.getJSONObject(IAPValues.RECEIPT);
          if (!IAPHistoryRetrieveUtils.checkIfDuplicateTransaction(Long.parseLong(receiptFromApple.getString(IAPValues.TRANSACTION_ID)))) {
            int diamondChange = IAPValues.getDiamondsForPackageName(receiptFromApple.getString(IAPValues.PRODUCT_ID));
            double cashCost = IAPValues.getCashSpentForPackageName(receiptFromApple.getString(IAPValues.PRODUCT_ID));
            user.updateRelativeDiamondsNaive(diamondChange);
            if (!InsertUtils.insertIAPHistoryElem(receiptFromApple, diamondChange, user, cashCost)) {
              log.error("problem with logging in-app purchase history for receipt:" + receiptFromApple + " and user " + user);
            }
            resBuilder.setStatus(InAppPurchaseStatus.SUCCESS);
          } else {
            log.error("duplicate receipt");
          }
        }
        wr.close();
        rd.close();
      } catch (Exception e) { e.printStackTrace();}
      
      if (!resBuilder.hasStatus()) {
        resBuilder.setStatus(InAppPurchaseStatus.FAIL);
      }
  
      InAppPurchaseResponseProto resProto = resBuilder.build();
  
      InAppPurchaseResponseEvent resEvent = new InAppPurchaseResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setInAppPurchaseResponseProto(resProto);
      server.writeEvent(resEvent);
  
      UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
      resEventUpdate.setTag(event.getTag());
      server.writeEvent(resEventUpdate);
    } catch (Exception e) {
      log.error("exception in InAppPurchaseController processEvent", e);
    } finally {
      // Unlock this player
      server.unlockPlayer(senderProto.getUserId());
    }
  }

}
