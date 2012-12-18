package com.lvl6.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvl6.events.GameEvent;
import com.lvl6.events.NormalResponseEvent;
import com.lvl6.events.ResponseEvent;
import com.lvl6.events.response.BattleResponseEvent;
import com.lvl6.events.response.GeneralNotificationResponseEvent;
import com.lvl6.events.response.PostOnClanBulletinResponseEvent;
import com.lvl6.events.response.PostOnPlayerWallResponseEvent;
import com.lvl6.events.response.PurchaseFromMarketplaceResponseEvent;
import com.lvl6.info.User;
import com.lvl6.info.UserClan;
import com.lvl6.properties.APNSProperties;
import com.lvl6.properties.Globals;
import com.lvl6.proto.EventProto.BattleResponseProto;
import com.lvl6.proto.EventProto.GeneralNotificationResponseProto;
import com.lvl6.proto.EventProto.PostOnClanBulletinResponseProto;
import com.lvl6.proto.EventProto.PostOnPlayerWallResponseProto;
import com.lvl6.proto.EventProto.PurchaseFromMarketplaceResponseProto;
import com.lvl6.proto.InfoProto.BattleResult;
import com.lvl6.proto.InfoProto.ClanBulletinPostProto;
import com.lvl6.proto.InfoProto.MinimumClanProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.PlayerWallPostProto;
import com.lvl6.retrieveutils.UserClanRetrieveUtils;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.Wrap;
import com.lvl6.utils.utilmethods.UpdateUtils;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.PayloadBuilder;

public class APNSWriter extends Wrap {
	// reference to game server

	@Autowired
	UserClanRetrieveUtils userClanRetrieveUtil;
	
	public UserClanRetrieveUtils getUserClanRetrieveUtil() {
		return userClanRetrieveUtil;
	}
	
	public void setUserClanRetrieveUtil(UserClanRetrieveUtils userClanRetrieveUtil) {
		this.userClanRetrieveUtil = userClanRetrieveUtil;
	}
	
	public Map<Integer, ConnectedPlayer> getPlayersByPlayerId() {
		return playersByPlayerId;
	}
	
	public void setPlayersByPlayerId(Map<Integer, ConnectedPlayer> playersByPlayerId) {
		this.playersByPlayerId = playersByPlayerId;
	}

	@Resource(name="playersByPlayerId")
	protected Map<Integer, ConnectedPlayer> playersByPlayerId;

	
	@Autowired
	private GameServer server;

	private static Logger log = LoggerFactory.getLogger(APNSWriter.class);

	private static final int SOFT_MAX_NOTIFICATION_BADGES = 20;

	private static final int MIN_MINUTES_BETWEEN_BATTLE_NOTIFICATIONS = 180; // 3
																				// hours
	private static final int MIN_MINUTES_BETWEEN_MARKETPLACE_NOTIFICATIONS = 30;
	private static final int MIN_MINUTES_BETWEEN_WALL_POST_NOTIFICATIONS = 15;

	private static final int MAX_NUM_CHARACTERS_TO_SEND_FOR_WALL_POST = 120;

	// 3 days
	private static final long MINUTES_BETWEEN_INACTIVE_DEVICE_TOKEN_FLUSH = 60 * 24 * 3;
	private static Date LAST_NULLIFY_INACTIVE_DEVICE_TOKEN_TIME = new Date();

	@Autowired
	protected APNSProperties apnsProperties;

	public void setApnsProperties(APNSProperties apnsProperties) {
		this.apnsProperties = apnsProperties;
	}

	/**
	 * constructor.
	 */
	public APNSWriter() {

	}

	public void setServer(GameServer server) {
		this.server = server;
	}

	/**
	 * note we override the Wrap's run method here doing essentially the same
	 * thing, but first we allocate a ByteBuffer for this thread to use
	 */
	public void run() {

	}

	/** unused */
	protected void processEvent(GameEvent event) {
		if (event instanceof NormalResponseEvent)
			processResponseEvent((NormalResponseEvent) event);

	}

	/**
	 * our own version of processEvent that takes the additional parameter of
	 * the writeBuffer
	 */
	protected void processResponseEvent(NormalResponseEvent event) {
		int playerId = event.getPlayerId();
		ConnectedPlayer connectedPlayer = server.getPlayerById(playerId);
		if (connectedPlayer != null) {
			log.info("wrote a response event to connected player with id " + playerId
					+ " instead of sending APNS message");
			server.writeEvent(event);
		} else {
			log.info("received APNS notification to send to player with id " + playerId);
			User user = RetrieveUtils.userRetrieveUtils().getUserById(playerId);
			if (user != null && user.getDeviceToken() != null && user.getDeviceToken().length() > 0) {
				try {
					ApnsService service = getApnsService();
					if (service != null) {
						// service.start();
						Date now = new Date();
						if (LAST_NULLIFY_INACTIVE_DEVICE_TOKEN_TIME.getTime() + 60000
								* MINUTES_BETWEEN_INACTIVE_DEVICE_TOKEN_FLUSH < now.getTime()) {
							LAST_NULLIFY_INACTIVE_DEVICE_TOKEN_TIME = now;
							Map<String, Date> inactiveDevices = service.getInactiveDevices();
							UpdateUtils.get().updateNullifyDeviceTokens(inactiveDevices.keySet());
						}

						if (BattleResponseEvent.class.isInstance(event)) {
							handleBattleNotification(service, (BattleResponseEvent) event, user,
									user.getDeviceToken());
						}

						if (PurchaseFromMarketplaceResponseEvent.class.isInstance(event)) {
							handlePurchaseFromMarketplaceNotification(service,
									(PurchaseFromMarketplaceResponseEvent) event, user, user.getDeviceToken());
						}

						if (PostOnPlayerWallResponseEvent.class.isInstance(event)) {
							handlePostOnPlayerWallNotification(service,
									(PostOnPlayerWallResponseEvent) event, user, user.getDeviceToken());
						}
						
						if (GeneralNotificationResponseEvent.class.isInstance(event)) {
						  handleGeneralNotification(service,
						      (GeneralNotificationResponseEvent) event, user, user.getDeviceToken());
						}
						
						// if
						// (ReferralCodeUsedResponseEvent.class.isInstance(event))
						// {
						// handleReferralCodeUsedNotification(service,
						// (ReferralCodeUsedResponseEvent)event, user,
						// user.getDeviceToken());
						// }

						// service.stop();
					}else {
						log.warn("Apns service is null");
					}
					
				} catch (FileNotFoundException e) {
					log.error("File not found", e);
				}
			} else {
				log.warn("could not send push notification because user " + user + " has no device token");
			}
		}
	}

	protected ApnsService service;

	public ApnsService getApnsService() throws FileNotFoundException {
		if (service == null) {
			log.info("Apns Service null... building new");
			buildService();
		}
		try {
			log.info("Testing APNS connection");
			service.testConnection();
		} catch (Throwable e) {
			log.info("ApnsService connection test failed... building again");
		}
		return service;
	}

	protected void buildService() throws FileNotFoundException {
		log.info("Building ApnsService");
		File certFile = new File(apnsProperties.pathToCert);
		log.info(certFile.getAbsolutePath());
		try {
			if (certFile.exists() && certFile.canRead()) {
				ApnsServiceBuilder builder = APNS.newService()
						.withCert(apnsProperties.pathToCert, apnsProperties.certPassword).asNonBlocking();
				if (Globals.IS_SANDBOX()) {
					log.info("Building apns with sandbox=true");
					builder.withSandboxDestination();
				} else {
					builder.withProductionDestination();
				}
				service = builder.build();
				service.start();
			} else {
				log.error("Apns Certificate exists: {}  can read: {}", certFile.exists(), certFile.canRead());
			}
		} catch (Exception e) {
			log.error("Error getting apns cert.. Invalid SSL Config Exception", e);
		}
	}

	private void handleGeneralNotification(ApnsService service, GeneralNotificationResponseEvent event,
      User user, String token) {
	  if(user.getNumBadges() < SOFT_MAX_NOTIFICATION_BADGES) {
	    PayloadBuilder pb = APNS.newPayload().actionKey("View Now").badge(1);
	    
	    log.info("GeneralNotification for user: " + user.getId());
	    GeneralNotificationResponseProto resProto = event.getGeneralNotificationResponseProto();
	    String title = resProto.getTitle();
	    String subtitle = resProto.getSubtitle();
	    
	    String alertBody = title + " " + subtitle;
	    pb.alertBody(alertBody);
	    if (!pb.isTooLong()) {
	      log.info("sending apns for a general notification");
	      service.push(token, pb.build());
	    } else {
	      log.error("PlayloadBuilder isTooLong to send apns message for general notification");
	    }
	  }
	}
	
	/**
	 * send apns to the given playerId
	 */
	private void handlePostOnClanBulletinApnsNotification(ApnsService service, PostOnClanBulletinResponseEvent event,
			User user, String token) {
		if (user.getNumBadges() < SOFT_MAX_NOTIFICATION_BADGES) {
			//no time check since only leader can post and leader most likely won't spam messages
			PayloadBuilder pb = APNS.newPayload().actionKey("View").badge(1);
			
			log.info("PostOnClanBulletinNotification for user: " + user.getId());
			PostOnClanBulletinResponseProto resProto = event.getPostOnClanBulletinResponseProto();
			ClanBulletinPostProto post = resProto.getPost();

			String content = post.getContent();
			if (content.length() > MAX_NUM_CHARACTERS_TO_SEND_FOR_WALL_POST) {
				content = content.substring(0, MAX_NUM_CHARACTERS_TO_SEND_FOR_WALL_POST);
				int index = content.lastIndexOf(" ");
				if (index > 0) {
					content = content.substring(0, index);
					content += "...";
				}

			}

			MinimumUserProto poster = post.getPoster();
			MinimumClanProto clanOfPoster = poster.getClan();
			
			String clanName = clanOfPoster.getClanId() > 0 ? "[" + clanOfPoster.getTag() + "] " : "";
			pb.alertBody(clanName + poster.getName()
					+ " just posted on the clan bulletin: " + content);
			
			if (!pb.isTooLong()) {
				log.info("Pushing clan bulletin apns message");
				service.push(token, pb.build());

			} else {
				log.error("PlayloadBuilder isTooLong to send apns message");
			}
		}
	}

	private void determineEvent(ResponseEvent event, User user, String deviceToken) {
		try {
			ApnsService service = getApnsService();
			if (service != null) {
				
				if (PostOnClanBulletinResponseEvent.class.isInstance(event)) {
					handlePostOnClanBulletinApnsNotification(service, 
							(PostOnClanBulletinResponseEvent) event, user, deviceToken);
				}
			}else {
				log.warn("Apns service is null");
			}
			
		} catch (FileNotFoundException e) {
			log.error("File not found", e);
		}
	}

	/**
	 * sends to offline people
	 * @param event
	 * @param playerId - person to send event to
	 */
	protected void sendApnsNotificationToPlayer(ResponseEvent event, int playerId) {
		ConnectedPlayer player = playersByPlayerId.get(playerId);
		if(player == null){ 
			log.info("sending apns with type=" + event.getEventType()+ " to player with id " + playerId + ", event=" + event);
			
			User user = RetrieveUtils.userRetrieveUtils().getUserById(playerId);
			String deviceToken = user.getDeviceToken();
			if (user != null && deviceToken != null && deviceToken.length() > 0) {
				determineEvent(event, user, deviceToken);
			} else {
				log.warn("could not send push notification because user " + user + " has no device token");
			}
		}
	}
	
	// copied from EventWriter.processClanResponseEvent
	public void processClanResponseEvent(GameEvent event, int clanId) {
		log.debug("apnsWriter received clan event=" + event);
		ResponseEvent e = (ResponseEvent) event;
		List<UserClan> playersInClan = userClanRetrieveUtil.getUserClanMembersInClan(clanId);
		for (UserClan uc: playersInClan) {
			log.info("Sending apns to clan: {} member: {}", uc.getClanId(), uc.getUserId());
			sendApnsNotificationToPlayer(e, uc.getUserId());
		}
	}
	
	private void handlePostOnPlayerWallNotification(ApnsService service, PostOnPlayerWallResponseEvent event,
			User user, String token) {
		Date lastWallPostNotificationTime = user.getLastWallPostNotificationTime();
		Date now = new Date();
		if (user.getNumBadges() < SOFT_MAX_NOTIFICATION_BADGES
				&& (lastWallPostNotificationTime == null || now.getTime()
						- lastWallPostNotificationTime.getTime() > 60000 * MIN_MINUTES_BETWEEN_WALL_POST_NOTIFICATIONS)) {
			PayloadBuilder pb = APNS.newPayload().actionKey("View").badge(1);
			log.info("PostOnPlayerWallNotification for user: " + user.getId());
			PostOnPlayerWallResponseProto resProto = event.getPostOnPlayerWallResponseProto();
			PlayerWallPostProto post = resProto.getPost();

			String content = post.getContent();
			if (content.length() > MAX_NUM_CHARACTERS_TO_SEND_FOR_WALL_POST) {
				content = content.substring(0, MAX_NUM_CHARACTERS_TO_SEND_FOR_WALL_POST);
				int index = content.lastIndexOf(" ");
				if (index > 0) {
					content = content.substring(0, index);
					content += "...";
				}
			}
			String clan = post.getPoster().getClan().getClanId() > 0 ? "[" + post.getPoster().getClan().getTag() + "] " : "";
			pb.alertBody(clan + post.getPoster().getName()
					+ " just posted on your wall: " + content);

			if (!pb.isTooLong()) {
				log.info("Pushing apns message");
				service.push(token, pb.build());
				if (!user.updateRelativeBadgeAbsoluteLastWallPostNotificationTime(0,
						new Timestamp(now.getTime()))) {
					log.error("problem with updating wall post notification time for user " + user);
				}
			} else {
				log.error("PlayloadBuilder isTooLong to send apns message");
			}
		}
	}

	private void handlePurchaseFromMarketplaceNotification(ApnsService service,
			PurchaseFromMarketplaceResponseEvent event, User user, String token) {
		Date lastMarketplaceNotificationTime = user.getLastMarketplaceNotificationTime();
		Date now = new Date();
		if (user.getNumBadges() < SOFT_MAX_NOTIFICATION_BADGES
				&& (lastMarketplaceNotificationTime == null || now.getTime()
						- lastMarketplaceNotificationTime.getTime() > 60000 * MIN_MINUTES_BETWEEN_MARKETPLACE_NOTIFICATIONS)) {
			PurchaseFromMarketplaceResponseProto p = event.getPurchaseFromMarketplaceResponseProto();
            String clan = p.getPurchaser().getClan().getClanId() > 0 ? "[" + p.getPurchaser().getClan().getTag() + "] " : "";
			String text = clan + p.getPurchaser().getName()
					+ " has purchased your Level " + p.getMarketplacePost().getEquipLevel() + " "
					+ p.getMarketplacePost().getPostedEquip().getName()
					+ " in The Marketplace. Redeem your earnings!";
			PayloadBuilder pb = APNS.newPayload().actionKey("Redeem").badge(1).alertBody(text);
			log.info("PurchaseFromMarketplaceNotification for user: " + user.getId());
			if (!pb.isTooLong()) {
				log.info("Pushing apns message");
				service.push(token, pb.build());
				if (!user.updateRelativeBadgeAbsoluteLastMarketplaceNotificationTime(0,
						new Timestamp(now.getTime()))) {
					log.error("problem with updating marketplace notification time for user " + user);
				}
			} else {
				log.error("PlayloadBuilder isTooLong to send apns message");
			}
		}
	}

	private void handleBattleNotification(ApnsService service, BattleResponseEvent event, User user,
			String token) {
		Date lastBattleNotificationTime = user.getLastBattleNotificationTime();
		Date now = new Date();
		log.info("BattleNotification for user: " + user.getId());
		if (user.getNumBadges() < SOFT_MAX_NOTIFICATION_BADGES
				&& (lastBattleNotificationTime == null || now.getTime()
						- lastBattleNotificationTime.getTime() > 60000 * MIN_MINUTES_BETWEEN_BATTLE_NOTIFICATIONS)) {

			// PayloadBuilder pb =
			// APNS.newPayload().actionKey("Retaliate").badge(user.getNumBadges()+1);
			PayloadBuilder pb = APNS.newPayload().actionKey("Retaliate").badge(1);

			BattleResponseProto battleResponseProto = event.getBattleResponseProto();

			boolean equipStolen = false;
			if (battleResponseProto.hasUserEquipGained()
					&& battleResponseProto.getUserEquipGained().getEquipId() > 0) {
				equipStolen = true;
			}
			MinimumUserProto att = battleResponseProto.getAttacker();

            String clan = att.getClan().getClanId() > 0 ? "[" + att.getClan().getTag() + "] " : "";
			String attacker = (att.hasName()) ? clan + att.getName() : "";
			BattleResult battleResult = battleResponseProto.getBattleResult();

			String alertBody = null;

			if (battleResult == BattleResult.ATTACKER_WIN) {
				alertBody = attacker;
				if (equipStolen) {
					alertBody += " has stolen your Level "
							+ battleResponseProto.getUserEquipGained().getLevel() + " "
							+ battleResponseProto.getEquipGained().getName() + ". Fight back strong!";
				} else {
					alertBody += " just humiliated you in battle. Fight back and defend your honor!";
				}
			} else if (battleResult == BattleResult.ATTACKER_FLEE) {
				alertBody = attacker
						+ " has just fled from you after initiating battle. Chase the coward down!";
			} else {
				alertBody = attacker
						+ " attacked you, but you won the fight. Come back and train before your skills get rusty!";
			}

			pb.alertBody(alertBody);

			// TODO: trigger button to bring up something on application

			if (!pb.isTooLong()) {
				log.info("Pushing apns message");
				service.push(token, pb.build());
				// if
				// (!user.updateRelativeBadgeAbsoluteLastbattlenotificationtime(1,
				// new Timestamp(now.getTime()))) {
				if (!user.updateRelativeBadgeAbsoluteLastBattleNotificationTime(0,
						new Timestamp(now.getTime()))) {
					log.error("problem with updating battle notification time for user " + user);
				}
			} else {
				log.error("PayloadBuilder isTooLong to send apns message");
			}
		}
	}

}// APNSWriter