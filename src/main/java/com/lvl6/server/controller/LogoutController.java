package com.lvl6.server.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.LogoutRequestEvent;
import com.lvl6.info.User;
import com.lvl6.proto.EventProto.LogoutRequestProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.utils.ConnectedPlayer;
import com.lvl6.utils.RetrieveUtils;

@Component
@DependsOn("gameServer")
public class LogoutController extends EventController {

	private static Logger log = Logger.getLogger(new Object() {
	}.getClass().getEnclosingClass());

	public LogoutController() {
		numAllocatedThreads = 4;
	}

	@Resource(name = "playersByPlayerId")
	protected Map<Integer, ConnectedPlayer> playersByPlayerId;

	public Map<Integer, ConnectedPlayer> getPlayersByPlayerId() {
		return playersByPlayerId;
	}

	public void setPlayersByPlayerId(Map<Integer, ConnectedPlayer> playersByPlayerId) {
		this.playersByPlayerId = playersByPlayerId;
	}

	@Override
	public RequestEvent createRequestEvent() {
		return new LogoutRequestEvent();
	}

	@Override
	public EventProtocolRequest getEventType() {
		return EventProtocolRequest.C_LOGOUT_EVENT;
	}

	@Override
	protected void processRequestEvent(RequestEvent event) throws Exception {
		LogoutRequestProto reqProto = ((LogoutRequestEvent) event)
				.getLogoutRequestProto();

		MinimumUserProto sender = reqProto.getSender();
		int playerId = sender.getUserId();

		if (playerId > 0) {
			server.lockPlayer(playerId);
			try {
				User user = RetrieveUtils.userRetrieveUtils().getUserById(playerId);
				if (user != null) {
					if (!user.updateLastlogout(new Timestamp(new Date().getTime()))) {
						log.error("problem with updating user's last logout time for user "	+ playerId);
					}
				}
				log.info("Player logged out: "+playerId);
				playersByPlayerId.remove(playerId);
			} catch (Exception e) {
				log.error("exception in updating user logout", e);
			} finally {
				server.unlockPlayer(playerId);
			}
		} else {
			log.error("cannot update last logout because playerid of sender:"+sender.getName()+" is <= 0, it's "	+ playerId);
		}

		// TODO: clear cache

	}

}
