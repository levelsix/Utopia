package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.EarnFreeDiamondsRequestEvent;
import com.lvl6.events.response.EarnFreeDiamondsResponseEvent;
import com.lvl6.events.response.UpdateClientUserResponseEvent;
import com.lvl6.info.TwoLeggedOAuth;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.EarnFreeDiamondsRequestProto;
import com.lvl6.proto.EventProto.EarnFreeDiamondsRequestProto.AdColonyRewardType;
import com.lvl6.proto.EventProto.EarnFreeDiamondsResponseProto;
import com.lvl6.proto.EventProto.EarnFreeDiamondsResponseProto.Builder;
import com.lvl6.proto.EventProto.EarnFreeDiamondsResponseProto.EarnFreeDiamondsStatus;
import com.lvl6.proto.InfoProto.EarnFreeDiamondsType;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.AdColonyRecentHistoryRetrieveUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

@Component
@DependsOn("gameServer")
public class EarnFreeDiamondsController extends EventController {

  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static String LVL6_SHARED_SECRET = "mister8conrad3chan9is1a2very4great5man";
  private Mac hmacSHA1WithLVL6Secret = null;

  //  private static String ADCOLONY_V4VC_SECRET_KEY = "v4vc5ec0f36707ad4afaa5452e";

  private static String KIIP_CONSUMER_KEY = "d6c7530ce4dc64ecbff535e521a241e3";
  private static String KIIP_CONSUMER_SECRET = "da8d864f948ae2b4e83c1b6e6a8151ed";
  private static String KIIP_VERIFY_ENDPOINT = "https://api.kiip.me/1.0/transaction/verify";
  private static String KIIP_INVALIDATE_ENDPOINT = "https://api.kiip.me/1.0/transaction/invalidate";
  private static String KIIP_JSON_APP_KEY_KEY = "app_key";
  private static String KIIP_JSON_SUCCESS_KEY = "success";
  private static String KIIP_JSON_RECEIPT_KEY = "receipt";
  private static String KIIP_JSON_CONTENT_KEY = "content";
  private static String KIIP_JSON_SIGNATURE_KEY = "signature";
  private static String KIIP_JSON_TRANSACTION_ID_KEY = "transaction_id";
  private static String KIIP_JSON_QUANTITY_KEY = "quantity";

  private OAuthService oAuthService = null;

  public EarnFreeDiamondsController() {
    numAllocatedThreads = 1;
  }

  @Override
  public RequestEvent createRequestEvent() {
    return new EarnFreeDiamondsRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_EARN_FREE_DIAMONDS_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    EarnFreeDiamondsRequestProto reqProto = ((EarnFreeDiamondsRequestEvent)event).getEarnFreeDiamondsRequestProto();
    MinimumUserProto senderProto = reqProto.getSender();

    EarnFreeDiamondsType freeDiamondsType = reqProto.getFreeDiamondsType();
    Timestamp clientTime = new Timestamp(reqProto.getClientTime());

    String kiipReceiptString = (reqProto.hasKiipReceipt() && reqProto.getKiipReceipt().length() > 0) ? reqProto.getKiipReceipt() : null;

    String adColonyDigest = (reqProto.hasAdColonyDigest() && reqProto.getAdColonyDigest().length() > 0) ? reqProto.getAdColonyDigest() : null;
    int adColonyAmountEarned = reqProto.getAdColonyAmountEarned();
    AdColonyRewardType adColonyRewardType = reqProto.getAdColonyRewardType();

    ////    ////    //TODO:
    //    kiipReceiptString = "{\"signature\":\"a525d6cbb8ec18d5c4e47266d736162cf18a3ff7\",\"content\":\"reward_gold\",\"quantity\":\"2\",\"transaction_id\":\"4fe924dc4972e91ed6000147\"}";
    //    freeDiamondsType = EarnFreeDiamondsType.KIIP;

    EarnFreeDiamondsResponseProto.Builder resBuilder = EarnFreeDiamondsResponseProto.newBuilder();
    resBuilder.setSender(senderProto);

    server.lockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());

    try {
      User user = RetrieveUtils.userRetrieveUtils().getUserById(senderProto.getUserId());
      int previousGold = 0;

      boolean legitFreeDiamondsEarn = checkLegitFreeDiamondsEarnBasic(resBuilder, freeDiamondsType, clientTime, user, kiipReceiptString, adColonyDigest, adColonyAmountEarned, adColonyRewardType);

      JSONObject kiipConfirmationReceipt = null;

      if (legitFreeDiamondsEarn) {
        resBuilder.setFreeDiamondsType(freeDiamondsType);

        if (freeDiamondsType == EarnFreeDiamondsType.KIIP) {
          kiipConfirmationReceipt = getLegitKiipRewardReceipt(resBuilder, user, kiipReceiptString);
          if (kiipConfirmationReceipt == null) legitFreeDiamondsEarn = false;
          else {
            invalidateKiipTransaction(kiipConfirmationReceipt);
          }
        }
        if (freeDiamondsType == EarnFreeDiamondsType.ADCOLONY) {
          if (!signaturesAreEqual(resBuilder, user, adColonyDigest, adColonyAmountEarned, adColonyRewardType, clientTime)) {
            legitFreeDiamondsEarn = false;
          } else if (AdColonyRecentHistoryRetrieveUtils.checkIfDuplicateDigest(adColonyDigest)) {
            resBuilder.setStatus(EarnFreeDiamondsStatus.OTHER_FAIL);
            legitFreeDiamondsEarn = false;
          }
        }
      }

      EarnFreeDiamondsResponseEvent resEvent = new EarnFreeDiamondsResponseEvent(senderProto.getUserId());
      resEvent.setTag(event.getTag());
      resEvent.setEarnFreeDiamondsResponseProto(resBuilder.build());  
      server.writeEvent(resEvent);

      if (legitFreeDiamondsEarn) {
        previousGold = user.getDiamonds();
        
        Map<String, Integer> money = new HashMap<String, Integer>();
        List<String> keys = new ArrayList<String>();
        writeChangesToDB(user, freeDiamondsType, kiipConfirmationReceipt, adColonyAmountEarned, adColonyRewardType,
            money, keys);
        UpdateClientUserResponseEvent resEventUpdate = MiscMethods.createUpdateClientUserResponseEventAndUpdateLeaderboard(user);
        resEventUpdate.setTag(event.getTag());
        server.writeEvent(resEventUpdate);

        writeToDBHistory(user, freeDiamondsType, clientTime, kiipConfirmationReceipt, adColonyDigest, adColonyRewardType, adColonyAmountEarned);
        writeToUserCurrencyHistory(user, clientTime, money, keys, freeDiamondsType, previousGold);
      }
    } catch (Exception e) {
      log.error("exception in earn free gold processEvent", e);
    } finally {
      server.unlockPlayer(senderProto.getUserId(), this.getClass().getSimpleName());      
    }
  }

  private void invalidateKiipTransaction(JSONObject kiipConfirmationReceipt) {
    try {
      oAuthService = getOAuthService();   

      Token token = new Token("", "");            

      OAuthRequest request = new OAuthRequest(Verb.POST, KIIP_INVALIDATE_ENDPOINT);
      request.addBodyParameter(KIIP_JSON_APP_KEY_KEY, KIIP_CONSUMER_KEY);
      request.addBodyParameter(KIIP_JSON_RECEIPT_KEY, kiipConfirmationReceipt.toString());
      oAuthService.signRequest(token, request);  
      Response response = request.send();       
      if (response.getCode() == 200) {
        String responseJSONString = response.getBody();
        if (responseJSONString != null && responseJSONString.length() > 0) {
          JSONObject kiipResponse = new JSONObject(responseJSONString);
          if (!kiipResponse.getBoolean(KIIP_JSON_SUCCESS_KEY)) { 
            log.error("problem with invalidating kiip transaction with receipt " + kiipConfirmationReceipt);
          }
        } else {
          log.error("problem with invalidating kiip transaction with receipt " + kiipConfirmationReceipt);
        }
      }
    } catch (Exception e) {
      log.error("problem with invalidating kiip transaction with receipt " + kiipConfirmationReceipt, e);
    }

  }

  private boolean signaturesAreEqual(Builder resBuilder, User user, String adColonyDigest, int adColonyAmountEarned, 
      AdColonyRewardType adColonyRewardType, Timestamp clientTime) {
    String serverAdColonyDigest = null;
    String prepareString = user.getId() + user.getReferralCode() + adColonyAmountEarned + adColonyRewardType.getNumber() + clientTime.getTime();

    serverAdColonyDigest = getHMACSHA1DigestWithLVL6Secret(prepareString);

    if (serverAdColonyDigest == null || !serverAdColonyDigest.equals(adColonyDigest)) {
      resBuilder.setStatus(EarnFreeDiamondsStatus.OTHER_FAIL);
      log.error("failure in confirming adColony digest. server's digest is " + serverAdColonyDigest
          + ", client's is " + adColonyDigest);
      return false;
    }
    return true;
  }

  private JSONObject getLegitKiipRewardReceipt(Builder resBuilder, User user, String kiipReceipt) {

    try {
      oAuthService = getOAuthService();   

      Token token = new Token("", "");            

      OAuthRequest request = new OAuthRequest(Verb.POST, KIIP_VERIFY_ENDPOINT);
      request.addBodyParameter(KIIP_JSON_APP_KEY_KEY, KIIP_CONSUMER_KEY);
      request.addBodyParameter(KIIP_JSON_RECEIPT_KEY, kiipReceipt);
      oAuthService.signRequest(token, request);  
      Response response = request.send();       

      if (response.getCode() == 200) {
        String responseJSONString = response.getBody();
        if (responseJSONString != null && responseJSONString.length() > 0) {
          JSONObject kiipResponse = new JSONObject(responseJSONString);
          if (kiipResponse.getBoolean(KIIP_JSON_SUCCESS_KEY)) 
            return kiipResponse.getJSONObject(KIIP_JSON_RECEIPT_KEY);
        }
      }
    } catch (Exception e) {
      resBuilder.setStatus(EarnFreeDiamondsStatus.OTHER_FAIL);
      log.error("problem with checking kiip reward", e);
      return null;
    }
    resBuilder.setStatus(EarnFreeDiamondsStatus.OTHER_FAIL);
    log.error("problem with getting kiip Receipt, input kiipreceipt is=" + kiipReceipt);
    return null;
  }

  private void writeChangesToDB(User user, EarnFreeDiamondsType freeDiamondsType, JSONObject kiipReceipt, int adColonyAmountEarned, 
      AdColonyRewardType adColonyRewardType, Map<String, Integer> money, List<String> keys) throws JSONException {
    if (freeDiamondsType == EarnFreeDiamondsType.KIIP) {
      int diamondChange = kiipReceipt.getInt(KIIP_JSON_QUANTITY_KEY);
      if (!user.updateRelativeDiamondsForFree(diamondChange, freeDiamondsType)) {
        log.error("problem with updating diamonds. diamondChange=" + diamondChange
            + ", freeDiamondsType=" + freeDiamondsType);
      } else {
        String key = MiscMethods.gold;
        money.put(key, diamondChange);
        keys.add(key);
      }
    }
    if (freeDiamondsType == EarnFreeDiamondsType.ADCOLONY) {
      if (adColonyRewardType == AdColonyRewardType.DIAMONDS) {
        if (!user.updateRelativeDiamondsForFree(adColonyAmountEarned, freeDiamondsType)) {
          log.error("problem with updating diamonds. diamondChange=" + adColonyAmountEarned
              + ", freeDiamondsType=" + freeDiamondsType);
        } else {
          String key = MiscMethods.gold;
          money.put(key, adColonyAmountEarned);
          keys.add(key);
        }
      } else if (adColonyRewardType == AdColonyRewardType.COINS) {
        if (!user.updateRelativeCoinsAdcolonyvideoswatched(adColonyAmountEarned, 1)) {
          log.error("problem with updating coins. coin change=" + adColonyAmountEarned
              + ", Adcolonyvideoswatched=" + 1);
        } else {
          String key = MiscMethods.silver;
          money.put(key, adColonyAmountEarned);
          keys.add(key);
        }
      }
    }
    if (EarnFreeDiamondsType.FB_CONNECT == freeDiamondsType) {
      int diamondChange = ControllerConstants.EARN_FREE_DIAMONDS__FB_CONNECT_REWARD;
      if (!user.updateRelativeDiamondsForFree(diamondChange, freeDiamondsType)) {
        log.error("unexpected error: user was not awarded for connecting to facebook");
      } else {
        String key = MiscMethods.gold;
        money.put(key, diamondChange);
        keys.add(key);
      }
    }
  }

  private void writeToDBHistory(User user, EarnFreeDiamondsType freeDiamondsType, Timestamp clientTime, JSONObject kiipConfirmationReceipt, String adColonyDigest,
      AdColonyRewardType adColonyRewardType, int adColonyAmountEarned) {
    if (freeDiamondsType == EarnFreeDiamondsType.KIIP) {
      try {
        String content = kiipConfirmationReceipt.getString(KIIP_JSON_CONTENT_KEY);
        String signature = kiipConfirmationReceipt.getString(KIIP_JSON_SIGNATURE_KEY);
        int quantity = kiipConfirmationReceipt.getInt(KIIP_JSON_QUANTITY_KEY);
        String transactionId = kiipConfirmationReceipt.getString(KIIP_JSON_TRANSACTION_ID_KEY);

        if (!InsertUtils.get().insertKiipHistory(user.getId(), clientTime, content, signature, quantity, transactionId)) {
          log.error("problem with saving kiip reward into history. user=" + user + ", clientTime=" + clientTime
              + ", kiipConfirmationReceipt=" + kiipConfirmationReceipt);
        }
      } catch (Exception e) {
        log.error("problem with trying to save kiip reward in db. kiipConfirmationReceipt=" + kiipConfirmationReceipt);
      }
    }
    if (freeDiamondsType == EarnFreeDiamondsType.ADCOLONY) {
      if (!InsertUtils.get().insertAdcolonyRecentHistory(user.getId(), clientTime, adColonyAmountEarned, adColonyRewardType, adColonyDigest)) {
        log.error("problem with saving adcolony rewarding into recent history. user=" + user + ", clientTime=" + clientTime
            + ", amountEarned=" + adColonyAmountEarned + ", adColonyRewardType=" + adColonyRewardType + ", digest=" + adColonyDigest);
      }
    }
  }

  private boolean checkLegitFreeDiamondsEarnBasic(Builder resBuilder, EarnFreeDiamondsType freeDiamondsType, Timestamp clientTime, User user, 
      String kiipReceiptString, String adColonyDigest, int adColonyDiamondsEarned, AdColonyRewardType adColonyRewardType) {
    if (freeDiamondsType == null || clientTime == null || user == null) {
      resBuilder.setStatus(EarnFreeDiamondsStatus.OTHER_FAIL);
      log.error("parameter passed in is null. freeDiamondsType is " + freeDiamondsType + ", clientTime=" + clientTime + ", user=" + user);
      return false;
    }
    if (!MiscMethods.checkClientTimeAroundApproximateNow(clientTime)) {
      resBuilder.setStatus(EarnFreeDiamondsStatus.CLIENT_TOO_APART_FROM_SERVER_TIME);
      log.error("client time too apart of server time. client time=" + clientTime + ", servertime~="
          + new Date());
      return false;
    }
    if (freeDiamondsType == EarnFreeDiamondsType.KIIP) {
      if (!checkLegitKiipRedeem(resBuilder, kiipReceiptString)) {
        return false;
      }
    } else if (freeDiamondsType == EarnFreeDiamondsType.ADCOLONY) {
      if (!checkLegitAdColonyRedeem(resBuilder, adColonyDigest, adColonyDiamondsEarned, adColonyRewardType, user, clientTime)) {
        return false;
      }
      //    } else if (freeDiamondsType == EarnFreeDiamondsType.FB_INVITE) {
      //    } else if (freeDiamondsType == EarnFreeDiamondsType.TAPJOY) {
      //    } else if (freeDiamondsType == EarnFreeDiamondsType.FLURRY_VIDEO) {
      //    } else if (freeDiamondsType == EarnFreeDiamondsType.TWITTER) {
    } else if (EarnFreeDiamondsType.FB_CONNECT == freeDiamondsType) {
      if (user.isHasReceivedfbReward()) {
        log.error("user error: user already received fb connect diamonds");
        return false;
      }
    } else {
      resBuilder.setStatus(EarnFreeDiamondsStatus.METHOD_NOT_SUPPORTED);
      log.error("earn free gold type passed in not supported. type=" + freeDiamondsType);
      return false;
    }
    resBuilder.setStatus(EarnFreeDiamondsStatus.SUCCESS);
    return true;  
  }

  private boolean checkLegitAdColonyRedeem(Builder resBuilder, String adColonyDigest, int adColonyAmountEarned, AdColonyRewardType adColonyRewardType, User user, Timestamp clientTime) {
    if (adColonyDigest == null || (adColonyRewardType != AdColonyRewardType.DIAMONDS && adColonyRewardType != AdColonyRewardType.COINS)) {
      resBuilder.setStatus(EarnFreeDiamondsStatus.OTHER_FAIL);
      log.error("no digest given for AdColony");
      return false;
    }
    if (adColonyRewardType == AdColonyRewardType.DIAMONDS) {
      if ((user.getNumAdColonyVideosWatched()+1) % ControllerConstants.EARN_FREE_DIAMONDS__NUM_VIDEOS_FOR_DIAMOND_REWARD != 0) {
        resBuilder.setStatus(EarnFreeDiamondsStatus.OTHER_FAIL);
        log.error("not supposed to get diamonds yet, user before this try has only watched " + user.getNumAdColonyVideosWatched() + " videos");
        return false;
      }
    }
    if (adColonyAmountEarned <= 0) {
      resBuilder.setStatus(EarnFreeDiamondsStatus.OTHER_FAIL);
      log.error("<= 0 diamonds given from AdColony");
      return false;
    }
    return true;
  }

  private boolean checkLegitKiipRedeem(Builder resBuilder, String kiipReceiptString) {
    if (kiipReceiptString == null) {
      resBuilder.setStatus(EarnFreeDiamondsStatus.OTHER_FAIL);
      log.error("kiip receipt passed in is null");
      return false;
    }
    JSONObject kiipJSONReceipt;
    try {
      kiipJSONReceipt = new JSONObject(kiipReceiptString);
      if (kiipJSONReceipt.getInt(KIIP_JSON_QUANTITY_KEY) <= 0 || 
          kiipJSONReceipt.getString(KIIP_JSON_CONTENT_KEY).length() <= 0 ||
          kiipJSONReceipt.getString(KIIP_JSON_TRANSACTION_ID_KEY).length() <= 0 ||
          kiipJSONReceipt.getString(KIIP_JSON_SIGNATURE_KEY).length() <= 0) {
        resBuilder.setStatus(EarnFreeDiamondsStatus.OTHER_FAIL);
        log.error("kiip receipt passed in quantity may be <=0. kiipReceiptString=" + kiipReceiptString);
        return false;
      }
    } catch (JSONException e) {
      resBuilder.setStatus(EarnFreeDiamondsStatus.OTHER_FAIL);
      log.error("kiip receipt passed in has an error. kiipReceiptString=" + kiipReceiptString, e);
      return false;
    } catch (Exception e) {
      resBuilder.setStatus(EarnFreeDiamondsStatus.OTHER_FAIL);
      log.error("kiip receipt passed in has an error. kiipReceiptString=" + kiipReceiptString, e);
      return false;
    }
    return true;
  }


  private String getHMACSHA1DigestWithLVL6Secret(String prepareString) {
    try {
      Mac mac = getHMACSHA1WithLVL6Secret();
      if (mac == null) return null;

      byte[] text = prepareString.getBytes();

      return new String(Base64.encodeBase64(mac.doFinal(text))).trim();
    } catch (Exception e) {
      log.error("exception when trying to create hash for " + prepareString, e);
      return null;
    }
  }

  private Mac getHMACSHA1WithLVL6Secret() {
    if (hmacSHA1WithLVL6Secret == null) {
      SecretKey secretKey = null;

      byte[] keyBytes = LVL6_SHARED_SECRET.getBytes();
      secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");

      try {
        hmacSHA1WithLVL6Secret = Mac.getInstance("HmacSHA1");
        hmacSHA1WithLVL6Secret.init(secretKey);
      } catch (Exception e) {
        log.error("exception when trying to create mac with our secret", e);
        return null;
      }
    }
    return hmacSHA1WithLVL6Secret;
  }

  private OAuthService getOAuthService() {
    if (oAuthService == null) {
      oAuthService = new ServiceBuilder()
      .provider(TwoLeggedOAuth.class)
      .apiKey(KIIP_CONSUMER_KEY)
      .apiSecret(KIIP_CONSUMER_SECRET)
      .build();  
    }
    return oAuthService;
  }

  private void writeToUserCurrencyHistory(User aUser, Timestamp date, Map<String, Integer> money, List<String> keys,
      EarnFreeDiamondsType freeDiamondsType, int previousGold) {
    try {
      if(keys.isEmpty()) {
        return;
      }
      int userId = aUser.getId();
      int isSilver;
      String key = keys.get(0);
      int currencyChange = money.get(key);
      int currencyAfter;
      String reasonForChange = "earn free diamonds controller";
      
      if(key.equals(MiscMethods.silver)) {
        isSilver = 1;
        currencyAfter = aUser.getCoins();
      } else {
        isSilver = 0;
        currencyAfter = aUser.getDiamonds();
      }
      
      if (freeDiamondsType == EarnFreeDiamondsType.KIIP) {
        reasonForChange = ControllerConstants.UCHRFC__EARN_FREE_DIAMONDS_KIIP;
      } else if (freeDiamondsType == EarnFreeDiamondsType.ADCOLONY) {
        reasonForChange = ControllerConstants.UCHRFC__EARN_FREE_DIAMONDS_ADCOLONY;
      } else if (EarnFreeDiamondsType.FB_CONNECT == freeDiamondsType) {
        reasonForChange = ControllerConstants.UCHRFC__EARN_FREE_DIAMONDS_FB_CONNECT;
      }
      
      int inserted = InsertUtils.get().insertIntoUserCurrencyHistory(userId, date, isSilver,
          currencyChange, previousGold, currencyAfter, reasonForChange);

      log.info("Should be 1. Rows inserted into user_currency_history: " + inserted);
    } catch (Exception e) {
      log.error("Maybe table's not there or duplicate keys? ", e);
    }
  }
}
