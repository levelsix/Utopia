package com.lvl6.server;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.events.GameEvent;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.events.response.BattleResponseEvent;
import com.lvl6.events.response.PostOnPlayerWallResponseEvent;
import com.lvl6.events.response.PurchaseFromMarketplaceResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.APNSProperties;
import com.lvl6.properties.Globals;
import com.lvl6.proto.EventProto.BattleResponseProto;
import com.lvl6.proto.EventProto.PostOnPlayerWallResponseProto;
import com.lvl6.proto.InfoProto.BattleResult;
import com.lvl6.proto.InfoProto.PlayerWallPostProto;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.Wrap;
import com.lvl6.utils.utilmethods.UpdateUtils;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.PayloadBuilder;

public class APNSWriter extends Wrap {
  //reference to game server
  private GameServer server;

  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());

  private static final int SOFT_MAX_NOTIFICATION_BADGES = 10;

  //3 hours
  private static final int MIN_MINUTES_BETWEEN_BATTLE_NOTIFICATIONS = 180;

  //1 week
  private static final int MINUTES_BEFORE_IGNORE_BADGE_CAP = 10080;

  //3 days
  private static final long MINUTES_BETWEEN_INACTIVE_DEVICE_TOKEN_FLUSH = 60*24*3;
  private static Date LAST_NULLIFY_INACTIVE_DEVICE_TOKEN_TIME = new Date();

  /** 
   * constructor.
   */
  public APNSWriter(GameServer server, int numWorkers) {
    this.server = server;
    initWrap(numWorkers);
  }

  /** 
   * note we override the Wrap's run method here
   * doing essentially the same thing, but 
   * first we allocate a ByteBuffer for this
   * thread to use
   */
  public void run() {
    ResponseEvent event;
    running = true;
    while (running) {
      try {
        if ((event = (NormalResponseEvent)eventQueue.deQueue()) != null) {
          processResponseEvent((NormalResponseEvent)event);
        }
      }
      catch(InterruptedException e) {
      }
    }
  }

  /** unused */
  protected void processEvent(GameEvent event) {}

  /** 
   * our own version of processEvent that takes 
   * the additional parameter of the writeBuffer 
   */
  protected void processResponseEvent(NormalResponseEvent event) {
    int playerId = event.getPlayerId();
    ConnectedPlayer connectedPlayer = server.getPlayerById(playerId);
    if (connectedPlayer != null) {
      log.info("wrote a response event to connected player with id " + playerId + " instead of APNS");
      server.writeEvent(event);
    } else {
      log.info("received APNS notification to send to player with id " + playerId);
      User user = UserRetrieveUtils.getUserById(playerId);
      if (user != null && user.getDeviceToken() != null && user.getDeviceToken().length() > 0) {
        ApnsServiceBuilder builder = APNS.newService().withCert(APNSProperties.PATH_TO_CERT, APNSProperties.CERT_PASSWORD);

        if (Globals.IS_SANDBOX) {
          builder.withSandboxDestination();
        }

        ApnsService service = builder.build();

        Date now = new Date();
        if (LAST_NULLIFY_INACTIVE_DEVICE_TOKEN_TIME.getTime() + 60000*MINUTES_BETWEEN_INACTIVE_DEVICE_TOKEN_FLUSH
            < now.getTime()) {
          LAST_NULLIFY_INACTIVE_DEVICE_TOKEN_TIME = now;
          Map<String, Date> inactiveDevices = service.getInactiveDevices();
          UpdateUtils.updateNullifyDeviceTokens(inactiveDevices.keySet());
        }

        if (BattleResponseEvent.class.isInstance(event)) {
          handleBattleNotification(service, (BattleResponseEvent)event, user, user.getDeviceToken());
        }

        if (PurchaseFromMarketplaceResponseEvent.class.isInstance(event)) {
          handlePurchaseFromMarketplaceNotification(service, (PurchaseFromMarketplaceResponseEvent)event, user, user.getDeviceToken());
        }

        if (PostOnPlayerWallResponseEvent.class.isInstance(event)) {
          handlePostOnPlayerWallNotification(service, (PostOnPlayerWallResponseEvent)event, user, user.getDeviceToken());
        }

        //        if (ReferralCodeUsedResponseEvent.class.isInstance(event)) {
        //          handleReferralCodeUsedNotification(service, (ReferralCodeUsedResponseEvent)event, user, user.getDeviceToken());
        //        }

        service.stop();
      } else {
        log.info("could not send push notification because user " + user + " has no device token");
      }
    }
  }


  private void handlePostOnPlayerWallNotification(ApnsService service, PostOnPlayerWallResponseEvent event, User user, String token) {
    PayloadBuilder pb = APNS.newPayload().actionKey("Use").badge(user.getNumBadges()+1);

    PostOnPlayerWallResponseProto resProto = event.getPostOnPlayerWallResponseProto();
    PlayerWallPostProto post = resProto.getPost();

    User poster = UserRetrieveUtils.getUserById(post.getPoster().getUserId());
    if (poster != null) {
      pb.alertBody(poster.getName() + " just posted on your wall! Check out the message.");

      if (!pb.isTooLong()) {
        service.push(token, pb.build());
        if (user.updateRelativeBadge(1)) {
          log.error("problem with pushing notification to user " + user);
        }
      }
    }
  }

  //  private void handleReferralCodeUsedNotification(ApnsService service, ReferralCodeUsedResponseEvent event, User user, String token) {
  //    PayloadBuilder pb = APNS.newPayload().actionKey("Use").badge(user.getNumBadges()+1);
  //
  //    ReferralCodeUsedResponseProto resProto = event.getReferralCodeUsedResponseProto();
  //    pb.alertBody("Congrats! You received free silver because " + resProto.getReferredPlayer().getName() + " used your referral code.");
  //
  //    if (!pb.isTooLong()) {
  //      service.push(token, pb.build());
  //      if (user.updateRelativeBadge(1)) {
  //        log.error("problem with pushing notification to user " + user);
  //      }
  //    }
  //  }

  private void handlePurchaseFromMarketplaceNotification(ApnsService service, PurchaseFromMarketplaceResponseEvent event, User user, String token) {
    PayloadBuilder pb = APNS.newPayload().actionKey("Redeem").badge(user.getNumBadges()+1).alertBody("Someone purchased your equipment in the marketplace. Redeem your earnings!");

    if (!pb.isTooLong()) {
      service.push(token, pb.build());
      if (user.updateRelativeBadge(1)) {
        log.error("problem with pushing notification to user " + user);
      }
    }
  }

  private void handleBattleNotification(ApnsService service, BattleResponseEvent event, User user, String token) {
    Date lastBattleNotificationTime = user.getLastBattleNotificationTime();
    Date now = new Date();
    if ((user.getNumBadges() < SOFT_MAX_NOTIFICATION_BADGES && 
        (lastBattleNotificationTime == null || now.getTime() - lastBattleNotificationTime.getTime() > 60000*MIN_MINUTES_BETWEEN_BATTLE_NOTIFICATIONS)) ||
        (lastBattleNotificationTime != null && lastBattleNotificationTime.getTime() + 60000*MINUTES_BEFORE_IGNORE_BADGE_CAP < now.getTime())) {

      PayloadBuilder pb = APNS.newPayload().actionKey("Retaliate").badge(user.getNumBadges()+1);

      BattleResponseProto battleResponseProto = event.getBattleResponseProto();

      boolean equipStolen = false;
      if (battleResponseProto.hasEquipGained() && battleResponseProto.getEquipGained().getEquipId() > 0) {
        equipStolen = true;
      }
      String attacker = (battleResponseProto.getAttacker().hasName()) ? battleResponseProto.getAttacker().getName() : "";
      BattleResult battleResult = battleResponseProto.getBattleResult();

      String alertBody = null;

      if (battleResult == BattleResult.ATTACKER_WIN) {
        alertBody = attacker + " has just humiliated you";
        if (equipStolen) {
          alertBody += " and stole equipment from you. Show justice to this thief!";        
        } else {
          alertBody += ". Fight back and defend your honor!";
        }
      } else if (battleResult == BattleResult.ATTACKER_FLEE){
        alertBody = attacker + " has just fled from you after initiating battle. Chase the coward down!";
      } else {
        alertBody = attacker + " attacked you, but you won the fight. Come back and train before your skills get rusty!";
      }

      pb.alertBody(alertBody);

      //TODO: trigger button to bring up something on application

      if (!pb.isTooLong()) {
        service.push(token, pb.build());
        if (!user.updateRelativeBadgeAbsoluteLastbattlenotificationtime(1, new Timestamp(now.getTime()))) {
          log.error("problem with pushing notification to user " + user);
        }
      }
    }
  }

}// APNSWriter