package com.lvl6.server.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.InAppPurchaseRequestEvent;
import com.lvl6.events.response.InAppPurchaseResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.InAppPurchaseRequestProto;
import com.lvl6.proto.EventProto.InAppPurchaseResponseProto;
import com.lvl6.proto.EventProto.InAppPurchaseResponseProto.InAppPurchaseStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class InAppPurchaseController extends EventController {

  @Override
  protected void initController() {
    log.info("initController for " + this.getClass().toString());    
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
    System.out.println("in here");
    InAppPurchaseRequestProto reqProto = ((InAppPurchaseRequestEvent)event).getInAppPurchaseRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    String receipt = reqProto.getReceipt();

    InAppPurchaseResponseProto.Builder resBuilder = InAppPurchaseResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    // Lock this player's ID
    server.lockPlayer(senderProto.getUserId());
    User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

    JSONObject response;
    try {
      JSONObject jsonReceipt = new JSONObject();
      jsonReceipt.put("receipt-data", receipt);

      // Send data
      URL url = new URL("https://sandbox.itunes.apple.com/verifyReceipt");
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

      System.out.println(responseString);
      response = new JSONObject(responseString);
//      System.out.println(response.getInt("status"));
      if (response.getInt("status") == 0) {
        resBuilder.setStatus(InAppPurchaseStatus.SUCCESS);
      }
      wr.close();
      rd.close();
    } catch (Exception e) { e.printStackTrace();}
    
    if (!resBuilder.hasStatus()) {
      resBuilder.setStatus(InAppPurchaseStatus.FAIL);
    }

    InAppPurchaseResponseProto resProto = resBuilder.build();

    InAppPurchaseResponseEvent resEvent = new InAppPurchaseResponseEvent(senderProto.getUserId());
    resEvent.setInAppPurchaseResponseProto(resProto);
    server.writeEvent(resEvent);

    UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
    server.writeEvent(resEventUpdate);

    // Unlock this player
    server.unlockPlayer(senderProto.getUserId());
  }

}
