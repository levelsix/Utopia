package com.lvl6.elasticsearch;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import me.prettyprint.cassandra.utils.TimeUUIDUtils;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.MDC;

public class Log4jElasticSearchAppender extends AppenderSkeleton {
	
	public static AtomicLong logcounter = new AtomicLong();
	
	protected ExecutorService executor = Executors.newFixedThreadPool(20);
	
	protected Log4JElasticSearchIndexer search;

	private String clusterName;

	public Log4jElasticSearchAppender(String elasticCluster, String elasticHosts) {
		super();
		this.elasticCluster = elasticCluster;
		this.elasticHosts = elasticHosts;
		startAppender();
	}

	private String elasticCluster;
	private String elasticHosts;

	private boolean startedUp = false;
	private boolean shutdown = false;

	private long lastPublishTime = -1;


	private void startAppender() {
		Runnable r = new Runnable() {
			public void run() {
				while (!shutdown) {
					try {
						Thread.sleep(3000);
						setup();
						if (lastPublishTime > 50) {
							// TODO: some metrics to deactivate logging to this
							// appender if its getting too time consuming, or
							// queue
						}
					} catch (Exception e) {
						LogLog.error("Log4jElasticSearchAppender", e);
					} finally {
						startedUp = true;
					}
				}
			}

			
		};
		Thread alive = new Thread(r, "ElasticSearch-Log4J");
		alive.setDaemon(true);
		alive.start();
	}
	
	private void setup() throws Exception {
		if(search == null) {
			search = new Log4JElasticSearchIndexer(elasticCluster, elasticHosts);
		}
	}
	
	

	@Override
	protected void append(final LoggingEvent event) {
		@SuppressWarnings("unchecked")
		final Map<String, Object> mdccopy = MDC.getCopyOfContextMap();
		if(executor != null) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				String message = event.getMessage() + "";
				if (startedUp ) {
					try {
						UUID key = TimeUUIDUtils.getUniqueTimeUUIDinMillis();
						search.indexEvent(event, key, getHost(), mdccopy);
					} catch (Exception e) {
						LogLog.error("append failed, " + e.getMessage(), e);
					}
				} else {
					if (startedUp) {
						LogLog.warn("Log4jElasticSearchAppender, cluster not available, skipping logging, " + message);
					}
				}
			}
		});
		}
		//long endTime = System.currentTimeMillis();
		//lastPublishTime = endTime - startTime;
	}



	public void close() {
		LogLog.warn("Closing log4jElasticSearch appender");
		shutdown = true;
	}



		
	String host = "";
	private String getHost() {
		try {
			if(host == null || host.equals("")) {
				host = InetAddress.getLocalHost().getHostAddress();
			}
		} catch (UnknownHostException e) {
		}
		return host;
	}

	public boolean requiresLayout() {
		return false;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getElasticCluster() {
		return elasticCluster;
	}
	
	public void setElasticCluster(String elasticCluster) {
		this.elasticCluster = elasticCluster;
	}
	
	public String getElasticHosts() {
		return elasticHosts;
	}
	
	public void setElasticHosts(String elasticHosts) {
		this.elasticHosts = elasticHosts;
	}
}
