package com.lvl6.server.controller;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.lvl6.proto.EventProto.RefillStatWaitCompleteResponseProto.RefillStatWaitCompleteStatus;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.utilmethods.MiscMethods;

public class EarnFreeGoldController extends EventController {

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static String LVL6_SHARED_SECRET = "mister8conrad3chan9is1a2very4great5man";
  //  private static String ADCOLONY_V4VC_SECRET_KEY = "v4vc5ec0f36707ad4afaa5452e";

  private static String KIIP_CONSUMER_KEY = "d6c7530ce4dc64ecbff535e521a241e3";
  private static String KIIP_CONSUMER_SECRET = "da8d864f948ae2b4e83c1b6e6a8151ed";
  private static String KIIP_VERIFY_ENDPOINT = "https://api.kiip.me/1.0/transaction/verify";
  private static String KIIP_INVALIDATE_ENDPOINT = "https://api.kiip.me/1.0/transaction/invalidate";
  private static String KIIP_JSON_SUCCESS_KEY = "success";
  private static String KIIP_JSON_RECEIPT_KEY = "receipt";
  private static String KIIP_JSON_CONTENT_KEY = "content";
  private static String KIIP_JSON_SIGNATURE_KEY = "receipt";
  private static String KIIP_JSON_TRANSACTION_ID_KEY = "receipt";
  private static String KIIP_JSON_QUANTITY_KEY = "receipt";

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

    String kiipReceiptString = (reqProto.hasKiipReceipt() && reqProto.getKiipReceipt().length() > 0) ? reqProto.getKiipReceipt() : null;

    String adColonyDigest = (reqProto.hasAdColonyDigest() && reqProto.getAdColonyDigest().length() > 0) ? reqProto.getAdColonyDigest() : null;
    int adColonyGoldEarned = reqProto.getAdColonyGoldEarned();



    EarnFreeGoldResponseProto.Builder resBuilder = EarnFreeGoldResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId());

    try {
      User user = UserRetrieveUtils.getUserById(senderProto.getUserId());

      boolean legitFreeGoldEarn = checkLegitFreeGoldEarnBasic(resBuilder, freeGoldType, clientTime, user, kiipReceiptString, adColonyDigest, adColonyGoldEarned);

      JSONObject kiipConfirmationReceipt = null;

      //      kiipReceipt = new JSONObject("{\"content\": \"abc\", \"signature\": \"def74f51b2ab87c3fd4c169fad18b40fab8924fd\", \"transaction_id\": \"4f1e2755cc693441c330044a8\", \"quantity\": 500}");

      if (legitFreeGoldEarn) {
        if (freeGoldType == EarnFreeGoldType.KIIP) {
          kiipConfirmationReceipt = getLegitKiipRewardReceipt(resBuilder, user, kiipReceiptString);
          if (kiipConfirmationReceipt == null) legitFreeGoldEarn = false;
        }
        if (freeGoldType == EarnFreeGoldType.ADCOLONY) {
          if (!signaturesAreEqual(resBuilder, user, adColonyDigest, adColonyGoldEarned, clientTime)) legitFreeGoldEarn = false;
        }
      }

      EarnFreeGoldResponseEvent resEvent = new EarnFreeGoldResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setEarnFreeGoldResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitFreeGoldEarn) {
        writeChangesToDB(user, freeGoldType, kiipConfirmationReceipt, adColonyGoldEarned);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEvent(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);
        
        writeToDBHistory(user, freeGoldType, kiipConfirmationReceipt, adColonyDigest, adColonyGoldEarned);
      }
    } catch (Exception e) {
      log.error("exception in earn free gold processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId());      
    }
  }

  private boolean signaturesAreEqual(Builder resBuilder, User user, String adColonyDigest, int adColonyGoldEarned, Timestamp clientTime) {
    String serverAdColonyDigest = null;
    String prepareString = user.getId() + user.getReferralCode() + adColonyGoldEarned + clientTime.getTime();
    serverAdColonyDigest = getHMACSHA1Digest(prepareString, LVL6_SHARED_SECRET);

    if (serverAdColonyDigest == null || !serverAdColonyDigest.equals(adColonyDigest)) {
      resBuilder.setStatus(EarnFreeGoldStatus.OTHER_FAIL);
      log.error("failure in confirming adColony digest. server's digest is " + serverAdColonyDigest
          + ", client's is " + adColonyDigest);
      return false;
    }
    return true;
  }

  private JSONObject getLegitKiipRewardReceipt(Builder resBuilder, User user, String kiipReceipt) {
    OAuthConsumer consumer = new DefaultOAuthConsumer(KIIP_CONSUMER_KEY, KIIP_CONSUMER_SECRET);
    URL url;
    String responseMessage = null;
    try {
      url = new URL(KIIP_VERIFY_ENDPOINT);
      HttpURLConnection request = (HttpURLConnection) url.openConnection();
      consumer.sign(request);
      request.connect();

      if (request.getResponseCode() == 200) {
        responseMessage = request.getResponseMessage();

        if (responseMessage != null) {
          JSONObject kiipResponse = new JSONObject(responseMessage);
          boolean success = kiipResponse.getBoolean(KIIP_JSON_SUCCESS_KEY);
          if (success) {
            String kiipResponseReceiptString = kiipResponse.getString(KIIP_JSON_RECEIPT_KEY);
            if (kiipResponseReceiptString != null && kiipResponseReceiptString.length() > 0) {
              return new JSONObject(kiipResponseReceiptString);
            }
          }
        }
      }
    } catch (MalformedURLException e) {
      resBuilder.setStatus(EarnFreeGoldStatus.OTHER_FAIL);
      log.error("problem with kiip endpoint URL", e);
      return null;
    } catch (Exception e) {
      resBuilder.setStatus(EarnFreeGoldStatus.OTHER_FAIL);
      log.error("problem with checking kiip reward", e);
      return null;
    }
    return null;
  }

  private void writeChangesToDB(User user, EarnFreeGoldType freeGoldType, JSONObject kiipReceipt, int adColonyGoldEarned) throws JSONException {
    if (freeGoldType == EarnFreeGoldType.KIIP) {
      if (!user.updateRelativeDiamondsForFree(kiipReceipt.getInt(KIIP_JSON_QUANTITY_KEY), freeGoldType)) {
        log.error("problem with updating diamonds. diamondChange=" + kiipReceipt.getInt(KIIP_JSON_QUANTITY_KEY)
            + ", freeGoldType=" + freeGoldType);
      }
    }
    if (freeGoldType == EarnFreeGoldType.ADCOLONY) {
      if (!user.updateRelativeDiamondsForFree(adColonyGoldEarned, freeGoldType)) {
        log.error("problem with updating diamonds. diamondChange=" + adColonyGoldEarned
            + ", freeGoldType=" + freeGoldType);
      }
    }
  }
  
  private void writeToDBHistory(User user, EarnFreeGoldType freeGoldType, JSONObject kiipConfirmationReceipt, String adColonyDigest,
      int adColonyGoldEarned) {
    if (freeGoldType == EarnFreeGoldType.KIIP) {
      //TODO:
    }
    if (freeGoldType == EarnFreeGoldType.ADCOLONY) {
      //TODO:
    }
  }

  private boolean checkLegitFreeGoldEarnBasic(Builder resBuilder, EarnFreeGoldType freeGoldType, Timestamp clientTime, User user, 
      String kiipReceiptString, String adColonyDigest, int adColonyGoldEarned) {
    if (freeGoldType == null || clientTime == null || user == null) {
      resBuilder.setStatus(EarnFreeGoldStatus.OTHER_FAIL);
      log.error("parameter passed in is null. freeGoldType is " + freeGoldType + ", clientTime=" + clientTime + ", user=" + user);
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(clientTime)) {
      resBuilder.setStatus(EarnFreeGoldStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + clientTime + ", servertime~="
          + new Date());
      return false;
    }
    if (freeGoldType == EarnFreeGoldType.KIIP) {
      if (!checkLegitKiipRedeem(resBuilder, kiipReceiptString)) {
        return false;
      }
    } else if (freeGoldType == EarnFreeGoldType.ADCOLONY) {
      if (!checkLegitAdColonyRedeem(resBuilder, adColonyDigest, adColonyGoldEarned, user, clientTime)) {
        return false;
      }
      //    } else if (freeGoldType == EarnFreeGoldType.FB_INVITE) {
      //    } else if (freeGoldType == EarnFreeGoldType.TAPJOY) {
      //    } else if (freeGoldType == EarnFreeGoldType.FLURRY_VIDEO) {
      //    } else if (freeGoldType == EarnFreeGoldType.TWITTER) {
    } else {
      resBuilder.setStatus(EarnFreeGoldStatus.METHOD_NOT_SUPPORTED);
      log.error("earn free gold type passed in not supported. type=" + freeGoldType);
      return false;
    }
    resBuilder.setStatus(EarnFreeGoldStatus.SUCCESS);
    return true;  
  }

  private boolean checkLegitAdColonyRedeem(Builder resBuilder, String adColonyDigest, int adColonyGoldEarned, User user, Timestamp clientTime) {
    if (adColonyDigest == null) {
      resBuilder.setStatus(EarnFreeGoldStatus.OTHER_FAIL);
      log.error("no digest given for AdColony");
      return false;
    }
    if (adColonyGoldEarned <= 0) {
      resBuilder.setStatus(EarnFreeGoldStatus.OTHER_FAIL);
      log.error("<= 0 gold given from AdColony");
      return false;
    }
    return true;
  }

  private boolean checkLegitKiipRedeem(Builder resBuilder, String kiipReceiptString) {
    if (kiipReceiptString == null) {
      resBuilder.setStatus(EarnFreeGoldStatus.OTHER_FAIL);
      log.error("kiip receipt passed in is null");
      return false;
    }
    JSONObject kiipJSONReceipt;
    try {
      kiipJSONReceipt = new JSONObject(kiipReceiptString);
      if (kiipJSONReceipt.getInt(KIIP_JSON_QUANTITY_KEY) <= 0) {
        resBuilder.setStatus(EarnFreeGoldStatus.OTHER_FAIL);
        log.error("kiip receipt passed in quantity may be <=0. kiipReceiptString=" + kiipReceiptString);
        return false;
      }
    } catch (JSONException e) {
      resBuilder.setStatus(EarnFreeGoldStatus.OTHER_FAIL);
      log.error("kiip receipt passed in has an error. kiipReceiptString=" + kiipReceiptString);
      return false;
    }
    return true;
  }

  private String getHMACSHA1Digest(String prepareString, String secretString) {
    try {
      Mac mac = Mac.getInstance("HmacSHA1");
      SecretKeySpec secret = new SecretKeySpec(secretString.getBytes(),"HmacSHA1");
      mac.init(secret);
      byte[] digest = mac.doFinal(prepareString.getBytes());
      String enc = new String(digest);
      return enc;
    } catch (Exception e) {
      log.error("exception when trying to create hash for " + prepareString);
      return null;
    }
  }


}
