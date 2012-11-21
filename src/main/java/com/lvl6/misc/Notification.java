package com.lvl6.misc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.lvl6.events.response.GeneralNotificationResponseEvent;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.GeneralNotificationResponseProto;
import com.lvl6.proto.InfoProto.NotificationTitleColorProto;
import com.lvl6.server.GameServer;
import com.lvl6.utils.ConnectedPlayer;

public class Notification implements Runnable {

	  private static Logger log = Logger.getLogger(new Object() { }.getClass().getEnclosingClass());
	  
	  private GameServer server;
	  private Collection<ConnectedPlayer> allOnlinePlayers;
	  private Map<String, Object> keysAndValues;
	  
	  public static final String ATTACKER_CONCEDED = "attacker conceded";
	  public static final String ATTACKER_NOT_ENOUGH_MEMBERS = "attacker does not have enough members";
	  public static final String CLAN_TOWER_WAR_ENDED = "battle ended";
	  public static final String FOUND_AN_EPIC = "found an epic";
	  public static final String OWNER_CONCEDED = "owner conceded";
	  public static final String OWNER_NOT_ENOUGH_MEMBERS = "owner does not have enough members";
	  
	  //TODO: Determine the amount of time in between periodic notifications
	  public static final long milliseconds_between_periodic_notifications = 
			  (long)(1.0 / 5.0 * ControllerConstants.NUM_HOURS_FOR_CLAN_TOWER_WAR * 60 * 60 * 1000);
	  /*
	   * This class exists so as to not keep writing 
	   * new Runnable() {
					@Override
					public void run() {
						...
					}
				}
	   * and passing it into executor.execute whenever a message wants to be sent to players online.
	   * Example would be in SendGroupChatController.java regarding sending to all players online.
	   */
	  public Notification (GameServer server, 
			  Collection<ConnectedPlayer> allOnlinePlayers) {
		  this.server = server;
		  this.allOnlinePlayers = allOnlinePlayers;
		  this.keysAndValues = new HashMap<String, Object>();
	  }
	  
	  public void run () {
		  final GeneralNotificationResponseProto.Builder notificationProto = 
				  generateNotificationBuilder();
		  for (ConnectedPlayer player : allOnlinePlayers) {
				log.info("Sending general notification message to player: " + player.getPlayerId());
				GeneralNotificationResponseEvent aNotification = new GeneralNotificationResponseEvent(
						player.getPlayerId());
				aNotification.setGeneralNotificationResponseProto(notificationProto.build());
				try {
					server.writeEvent(aNotification);
				} catch (Exception e) {
					log.error(e);
				}
			}
	  }
	  
	  private GeneralNotificationResponseProto.Builder generateNotificationBuilder() {
		  final GeneralNotificationResponseProto.Builder notificationProto = 
				  GeneralNotificationResponseProto.newBuilder();
		  notificationProto.setTitle((String)keysAndValues.get("title"));
		  notificationProto.setSubtitle((String)keysAndValues.get("subtitle"));
		  notificationProto.setRgb((NotificationTitleColorProto)keysAndValues.get("rgb"));
		  
		  return notificationProto;
	  }
	  
	  public void setNotificationAsAttackerConceded () {
		  
	  }
	  
	  public void setNotificationAsClanTowerStatus(){
		  
	  }
	  
	  public void setNotificationAsClanTowerWarStarted (String clanTowerOwnerName, 
			  String clanTowerAttackerName, String towerName) {
	  
		  //TODO: write logic for this function, store to keysAndValues
		  //ex: keysAndValues.put("title", "clan tower war started");
		  //    keysAndValues.put("subtitle", "whatever"); and so on
		  
	  }
	  
	  public void setNotificationAsClanTowerWarEnded (String clanTowerOwnerName, 
			  String clanTowerAttackerName, String towerName, boolean attackerWon) {
		//TODO: write logic for this function
	  }
	  
	  public void setNotificationAsClanCreated (String clanName) {
		//TODO: write logic for this function
	  }
	  
	  public void setNotificationAsEpicWeaponDropped (
			  String userName, String equipName, String townName) {
		//TODO: write logic for this function
	  }
	  
	  public void setNotificationAsOwnerConceded () {
		  
	  }
}
