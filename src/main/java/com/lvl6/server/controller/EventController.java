package com.lvl6.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.lvl6.events.GameEvent;
import com.lvl6.events.RequestEvent; import org.slf4j.*;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.Globals;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.server.GameServer;
import com.lvl6.utils.DBConnection;
import com.lvl6.utils.Wrap;

@Component
@DependsOn("gameServer")
public abstract class EventController extends Wrap {

	protected TransactionTemplate transactionTemplate;

	protected PlatformTransactionManager transactionManager;

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	@Autowired
	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		log.debug("Autowiring transactionManager into EventController");
		transactionTemplate = new TransactionTemplate(transactionManager);
		this.transactionManager = transactionManager;
	}

	@Autowired
	protected GameServer server;

	public GameServer getServer() {
		return server;
	}

	public void setServer(GameServer server) {
		this.server = server;
	}

	private static Logger log = LoggerFactory.getLogger(new Object() {
	}.getClass().getEnclosingClass());

	// we have the controllers call server.writeEvent manually already
	// /**
	// * utility method for sending events
	// */
	// protected void sendEvent(ResponseEvent e, ConnectedPlayer p) {
	// server.writeEvent(e);
	// }

	/**
	 * GameController subclasses should implement initController in order to do
	 * any initialization they require.
	 */
	protected void initController() {
	}

	/**
	 * factory method for fetching GameEvent objects
	 */
	public abstract RequestEvent createRequestEvent();

	/**
	 * subclasses must implement to do their processing
	 * 
	 * @throws Exception
	 */

	protected void processEvent(GameEvent event) throws Exception {
		final RequestEvent reqEvent = (RequestEvent) event;
		MiscMethods
				.setMDCProperties(
						null,
						reqEvent.getPlayerId(),
						MiscMethods.getIPOfPlayer(server,
								reqEvent.getPlayerId(), null));
		log.info("Received event: " + event.toString());

		final long startTime = System.nanoTime();
		final long endTime;
		try {
			Exception e = doInTransaction(reqEvent);
			if (e != null) {
				throw e;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			endTime = System.nanoTime();
			DBConnection.get().getConnection().close();
		}
		double numSeconds = (endTime - startTime) / 1000000;

		log.info("Finished processing event: " + event.toString() + ", took ~"
				+ numSeconds + "ms");

		if (numSeconds / 1000 > Globals.NUM_SECONDS_FOR_CONTROLLER_PROCESS_EVENT_LONGTIME_LOG_WARNING) {
			log.warn("event: "
					+ event.toString()
					+ " took over "
					+ Globals.NUM_SECONDS_FOR_CONTROLLER_PROCESS_EVENT_LONGTIME_LOG_WARNING
					+ " seconds");
		}

		MiscMethods.purgeMDCProperties();
	}

	protected Exception doInTransaction(final RequestEvent reqEvent) {
//		return transactionTemplate
//				.execute(new TransactionCallback<Exception>() {
//
//					@Override
//					public Exception doInTransaction(TransactionStatus arg0) {
						try {
							processRequestEvent(reqEvent);
							return null;
						} catch (Exception e) {
							return e;
						}
//					}
//				});
	}

	/**
	 * subclasses must implement to provide their Event type
	 */
	public abstract EventProtocolRequest getEventType();

	@Async
	protected abstract void processRequestEvent(RequestEvent event)
			throws Exception;

	protected int numAllocatedThreads = 0;

	public int getNumAllocatedThreads() {
		return numAllocatedThreads;
	}

}
