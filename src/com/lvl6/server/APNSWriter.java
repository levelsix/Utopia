package com.lvl6.server;

import java.util.Date;

import com.lvl6.events.GameEvent;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.events.response.BattleResponseEvent;
import com.lvl6.events.response.PurchaseFromMarketplaceResponseEvent;
import com.lvl6.info.User;
import com.lvl6.retrieveutils.UserRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.Wrap;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

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
          handleBattleNotification(event, user);
        }
        if (PurchaseFromMarketplaceResponseEvent.class.isInstance(event)) {
          handlePurchaseFromMarketplaceNotification(event, user);
        }
      }
    }
  }

  private void handlePurchaseFromMarketplaceNotification(NormalResponseEvent event, User user) {
    //               send, increment badge
  }

  private void handleBattleNotification(NormalResponseEvent event, User user) {
    Date lastBattleNotificationTime = user.getLastBattleNotificationTime();
    Date now = new Date();
    if ((user.getNumBadges() < SOFT_MAX_NOTIFICATION_BADGES && 
        (lastBattleNotificationTime == null || now.getTime() - lastBattleNotificationTime.getTime() > 60000*MIN_MINUTES_BETWEEN_BATTLE_NOTIFICATIONS)) ||
        (lastBattleNotificationTime != null && lastBattleNotificationTime.getTime() + 60000*MINUTES_BEFORE_IGNORE_BADGE_CAP < now.getTime())) {
      
      ApnsService service = APNS.newService().withCert("/path/to/certificate.p12", "MyCertPassword").withSandboxDestination().build();
      
      
      //send, increment badge, change last battle notification time
    }
  }

}// APNSWriter