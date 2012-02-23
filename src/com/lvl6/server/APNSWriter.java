package com.lvl6.server;

import java.sql.Timestamp;
import java.util.Date;

import com.lvl6.events.GameEvent;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.events.response.BattleResponseEvent;
import com.lvl6.events.response.PurchaseFromMarketplaceResponseEvent;
import com.lvl6.info.User;
import com.lvl6.properties.APNSProperties;
import com.lvl6.properties.Globals;
import com.lvl6.proto.EventProto.BattleResponseProto;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.Wrap;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.PayloadBuilder;

public class APNSWriter extends Wrap {
  //reference to game server
  private GameServer server;

  private static final int SOFT_MAX_NOTIFICATION_BADGES = 10;
  private static final int MIN_MINUTES_BETWEEN_BATTLE_NOTIFICATIONS = 180;
  private static final int MINUTES_BEFORE_IGNORE_BADGE_CAP = 10080;


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
      server.writeEvent(event);
    } else {
      User user = UserRetrieveUtils.getUserById(playerId);
      if (user != null && user.getDeviceToken() != null && user.getDeviceToken().length() > 0) {
        if (BattleResponseEvent.class.isInstance(event)) {
          handleBattleNotification((BattleResponseEvent)event, user);
        }
        if (PurchaseFromMarketplaceResponseEvent.class.isInstance(event)) {
          handlePurchaseFromMarketplaceNotification((PurchaseFromMarketplaceResponseEvent)event, user);
        }
      }
    }
  }

  private void handlePurchaseFromMarketplaceNotification(PurchaseFromMarketplaceResponseEvent event, User user) {
    //               send, increment badge
  }

  private void handleBattleNotification(BattleResponseEvent event, User user) {
    Date lastBattleNotificationTime = user.getLastBattleNotificationTime();
    Date now = new Date();
    if ((user.getNumBadges() < SOFT_MAX_NOTIFICATION_BADGES && 
        (lastBattleNotificationTime == null || now.getTime() - lastBattleNotificationTime.getTime() > 60000*MIN_MINUTES_BETWEEN_BATTLE_NOTIFICATIONS)) ||
        (lastBattleNotificationTime != null && lastBattleNotificationTime.getTime() + 60000*MINUTES_BEFORE_IGNORE_BADGE_CAP < now.getTime())) {

      ApnsServiceBuilder builder = APNS.newService().withCert(APNSProperties.PATH_TO_CERT, APNSProperties.CERT_PASSWORD);
      if (Globals.IS_SANDBOX) {
        builder.withSandboxDestination();
      }
      ApnsService service = builder.build();
      String token = user.getDeviceToken();
      PayloadBuilder pb = APNS.newPayload().actionKey("Retaliate").badge(user.getNumBadges()+1);

      BattleResponseProto battleResponseProto = event.getBattleResponseProto();

      boolean equipStolen = false;
      if (battleResponseProto.hasEquipGained() && battleResponseProto.getEquipGained().getEquipId() > 0) {
        equipStolen = true;
      }
      String attacker = (battleResponseProto.getAttacker().hasName()) ? battleResponseProto.getAttacker().getName() : "";
      if (attacker == "") {
        attacker = "An enemy";
      }
      String alertBody = attacker + " has just humiliated you ";
      if (equipStolen) {
        alertBody += "and stole equipment from you. Show justice to this thief!";        
      } else {
        alertBody += ". Fight back and defend your honor!";
      }

      pb.alertBody(alertBody);

      //TODO: trigger button to bring up something on application

      if (pb.isTooLong()) {
        service.push(token, pb.build());
        if (!user.updateRelativeBadgeAbsoluteLastbattlenotificationtime(1, new Timestamp(now.getTime()))) {
          log.error("problem with pushing notification to user " + user);
        }
      }
    }
  }

}// APNSWriter