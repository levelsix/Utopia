package com.lvl6.elasticsearch;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;

import me.prettyprint.cassandra.utils.TimeUUIDUtils;

import org.slf4j.MDC;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

public class LogBackElasticSearchAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private String elasticCluster;
	private String elasticHosts;
	protected LoggingElasticSearchIndexer search;
	
	
	
	@Override
	public void start() {
		super.start();
	}



	@Override
	protected void append(ILoggingEvent event) {
		if(search == null) {
			search = new LoggingElasticSearchIndexer(getElasticCluster(), getElasticHosts());
		}
		final Map<String, Object> mdccopy = MDC.getCopyOfContextMap();
		if(search != null) {
			UUID key = TimeUUIDUtils.getUniqueTimeUUIDinMillis();
			search.indexEvent(event, key, getHost(), mdccopy);
		}
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



	public LoggingElasticSearchIndexer getSearch() {
		return search;
	}



	public void setSearch(LoggingElasticSearchIndexer search) {
		this.search = search;
	}



}
