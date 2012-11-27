package com.lvl6.server.controller;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.InAppPurchaseRequestEvent;
import com.lvl6.events.response.InAppPurchaseResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.IAPValues;
import com.lvl6.proto.EventProto.InAppPurchaseRequestProto;
import com.lvl6.proto.EventProto.InAppPurchaseResponseProto;
import com.lvl6.proto.EventProto.InAppPurchaseResponseProto.InAppPurchaseStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.IAPHistoryRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtil;
import com.lvl6.utils.utilmethods.MiscMethods;

@Component @DependsOn("gameServer") public class InAppPurchaseController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final String SANDBOX_URL = "https://sandbox.itunes.apple.com/verifyReceipt";
  private static final String PRODUCTION_URL = "https://buy.itunes.apple.com/verifyReceipt";  

  @Autowired
  protected InsertUtil insertUtils;

  public void setInsertUtils(InsertUtil insertUtils) {
    this.insertUtils = insertUtils;
  }


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
  @SuppressWarnings("deprecation")
  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    InAppPurchaseRequestProto reqProto = ((InAppPurchaseRequestEvent)event).getInAppPurchaseRequestProto();

    MinimumUserProto senderProto = reqProto.getSender();
    String receipt = reqProto.getReceipt();

    InAppPurchaseResponseProto.Builder resBuilder = InAppPurchaseResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setReceipt(reqProto.getReceipt());

    // Lock this player's ID
    server.lockPlayer(senderProto.getUserId());
    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());

      JSONObject response;

      JSONObject jsonReceipt = new JSONObject();
      jsonReceipt.put(IAPValues.RECEIPT_DATA, receipt);
      log.info("Processing purchase: "+jsonReceipt.toString(4));
      // Send data
      URL url = new URL(PRODUCTION_URL);

      log.info("Sending purchase request to: "+url.toString());

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
      log.info("Response: "+responseString);

      response = new JSONObject(responseString);

      if (response.getInt(IAPValues.STATUS) == 21007 || response.getInt(IAPValues.STATUS) == 21008) {
        wr.close();
        rd.close();
        url = new URL(SANDBOX_URL);
        conn = url.openConnection();
        conn.setDoOutput(true);
        wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(jsonReceipt.toString());
        wr.flush();
        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        responseString = "";
        while ((line = rd.readLine()) != null) {
          responseString += line;
        }
        response = new JSONObject(responseString);
      }

      JSONObject receiptFromApple = null;
      if (response.getInt(IAPValues.STATUS) == 0) {
        receiptFromApple = response.getJSONObject(IAPValues.RECEIPT);
        if (!IAPHistoryRetrieveUtils.checkIfDuplicateTransaction(Long.parseLong(receiptFromApple.getString(IAPValues.TRANSACTION_ID)))) {
          try {
            int diamondChange = IAPValues.getDiamondsForPackageName(receiptFromApple.getString(IAPValues.PRODUCT_ID));
            double cashCost = IAPValues.getCashSpentForPackageName(receiptFromApple.getString(IAPValues.PRODUCT_ID));
            user.updateRelativeDiamondsNaive(diamondChange);
            if (!insertUtils.insertIAPHistoryElem(receiptFromApple, diamondChange, user, cashCost)) {
              log.error("problem with logging in-app purchase history for receipt:" + receiptFromApple.toString(4) + " and user " + user);
            }
            resBuilder.setStatus(InAppPurchaseStatus.SUCCESS);
            resBuilder.setPackageName(receiptFromApple.getString(IAPValues.PRODUCT_ID));
            resBuilder.setDiamondsGained(diamondChange);
            resBuilder.setPackagePrice(cashCost);
            log.info("successful in-app purchase from user " + user.getId() + " for package " + receiptFromApple.getString(IAPValues.PRODUCT_ID));
          } catch (Exception e) {
            log.error("problem with in app purchase flow", e);
          }
        } else {
          resBuilder.setStatus(InAppPurchaseStatus.DUPLICATE_RECEIPT);
          log.error("duplicate receipt from user " + user);
        }
      } else {
        log.error("problem with in-app purchase that client sent, with receipt " + receipt);
      }

      wr.close();
      rd.close();

      if (!resBuilder.hasStatus()) {
        resBuilder.setStatus(InAppPurchaseStatus.FAIL);
      }

      InAppPurchaseResponseProto resProto = resBuilder.build();

      InAppPurchaseResponseEvent resEvent = new InAppPurchaseResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setInAppPurchaseResponseProto(resProto);
      server.writeEvent(resEvent);


      if (receiptFromApple != null && resBuilder.getStatus() == InAppPurchaseStatus.SUCCESS) {
        Map<String, Object> logParams = new TreeMap<String, Object>();
        logParams.put("serverid", "1");
        logParams.put("localcents", reqProto.getLocalcents());
        logParams.put("localcurrency", reqProto.getLocalcurrency());
        logParams.put("igc", resBuilder.getDiamondsGained());
        logParams.put("igctype", "gold");
        logParams.put("transactionid", receiptFromApple.get(IAPValues.TRANSACTION_ID));
        logParams.put("platform", "itunes");
        logParams.put("locale", reqProto.getLocale());
        logParams.put("lang", "en");
        logParams.put("ipaddr", "192.168.1.1");
        JSONObject logJson = new JSONObject(logParams);

        SortedMap<String, Object> queryParams = new TreeMap<String, Object>();
        queryParams.put("gameid", "1089");
        queryParams.put("mobileid", user.getUdid());
        queryParams.put("userid", user.getKabamNaid());
        queryParams.put("receipt", receipt);
        queryParams.put("log", logJson.toString());
        
        long time = new Date().getTime()/1000;
        queryParams.put("timestamp", time);
        
        String str = "";
        for (String key : queryParams.keySet()) {
          Object val = queryParams.get(key);
          str += key + val;
        }
        str += "6592a1780e6d15e9135dc662c3c7a563";
        
        queryParams.put("sig", sha1(str));
        
        String newStr = "";
        List<String> newParams = new ArrayList<String>();
        for (String key : queryParams.keySet()) {
          Object val = queryParams.get(key);
          newParams.add(URLEncoder.encode(key, "UTF-8")+"="+URLEncoder.encode(val.toString(), "UTF-8"));
        }
        newStr = StringUtils.join(newParams, "&");
        log.info(newStr);
        
        url = new URL("http://payv2beta.kabam.com/api/paymentlog"); 
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setRequestMethod("POST");
        httpCon.setDoInput(true);
        httpCon.setDoOutput(true);
        httpCon.setUseCaches(false);
        httpCon.setRequestProperty("Content-Type", "text/html");
        httpCon.setRequestProperty("Content-Length", ""+ newStr.length());
        DataOutputStream out = new DataOutputStream(httpCon.getOutputStream()); 
        out.writeBytes(newStr);
        out.flush();
        BufferedReader in = new BufferedReader(new InputStreamReader(new DataInputStream(conn.getInputStream())));
        responseString = "";
        while ((line = in.readLine()) != null) {
          responseString += line;
        }
        log.info("Response: "+responseString);
        log.info("Status: "+httpCon.getResponseCode() + " Message: "+ httpCon.getResponseMessage());
        out.close();
        rd.close();
      }

      UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
      resEventUpdate.setTag(event.getTag());
      server.writeEvent(resEventUpdate);
    } catch (Exception e) {
      log.error("exception in InAppPurchaseController processEvent", e);
    } finally {
      // Unlock this player
      server.unlockPlayer(senderProto.getUserId());
    }
  }
  
  private static String sha1(String input) throws NoSuchAlgorithmException {
    MessageDigest mDigest = MessageDigest.getInstance("SHA1");
    byte[] result = mDigest.digest(input.getBytes());
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < result.length; i++) {
        sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
    }
     
    return sb.toString();
}
}
