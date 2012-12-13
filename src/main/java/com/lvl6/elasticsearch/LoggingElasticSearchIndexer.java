package com.lvl6.elasticsearch;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.support.replication.ReplicationType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;

import ch.qos.logback.classic.spi.ILoggingEvent;

import com.lvl6.properties.MDCKeys;

public class LoggingElasticSearchIndexer {

	public static String INDEX = "logging";
	public static String TYPE = "log4j";
	protected Lvl6ElasticSearch search;
	protected String elasticSearchCluster;
	protected String elasticSearchHosts;

	public LoggingElasticSearchIndexer(String elasticSearchCluster, String elasticSearchHosts) {
		super();
		this.elasticSearchCluster = elasticSearchCluster;
		this.elasticSearchHosts = elasticSearchHosts;
		search = new Lvl6ElasticSearch(elasticSearchHosts, elasticSearchCluster);
	}

	TimeValue timeout = new TimeValue(500);

	public void indexEvent(ILoggingEvent event, UUID key, String host, Map<String, Object> mdccopy) {
		Client client = search.getClient();
		try {
			XContentBuilder jsonBuilder = jsonBuilder();
			jsonBuilder.startObject()
				.field(LoggingConstants.TIME, event.getTimeStamp())
				.field(LoggingConstants.HOST, host)
				.field(LoggingConstants.MESSAGE, event.getFormattedMessage())
				.field(LoggingConstants.LEVEL, event.getLevel() + "")
				.field(LoggingConstants.NAME, event.getLoggerName())
				.field(LoggingConstants.THREAD, event.getThreadName());
			addStackTrace(event, jsonBuilder);
			addProperties(event, jsonBuilder);
			addPlayerId(mdccopy, jsonBuilder);
			addUdid(mdccopy, jsonBuilder);
			jsonBuilder.endObject();
			client.prepareIndex(INDEX, TYPE, key.toString())
				.setSource(jsonBuilder).setTimeout(timeout)
				.setReplicationType(ReplicationType.ASYNC)
				.execute().actionGet();
		} catch (ElasticSearchException e) {
			System.out.println("Error indexing log item: "+e.getMessage());
		} catch (IOException e) {
			System.out.println("Error indexing log item: "+e.getMessage());
		} finally {
			search.closeClient();
		}
	}
	

	
	protected BulkRequestBuilder bulkRequest;

	protected BulkRequestBuilder getBulkRequest() {
		if (bulkRequest == null) {
			Client client = search.getClient();
			bulkRequest = client.prepareBulk();
		}
		return bulkRequest;
	}

	private void addUdid(Map<String, Object> mdccopy, XContentBuilder jsonBuilder) throws IOException {
		if (mdccopy == null)
			return;
		String udId = (String) mdccopy.get(MDCKeys.UDID);
		if (udId != null && !udId.equals("")) {
			jsonBuilder.field(LoggingConstants.UDID, udId.toString());
		}
	}

	private void addPlayerId(Map<String, Object> mdccopy, XContentBuilder jsonBuilder) {
		if (mdccopy == null)
			return;
		Object pid;
		pid = mdccopy.get(MDCKeys.PLAYER_ID);
		try {
			if (pid != null) {
				jsonBuilder.field(LoggingConstants.PLAYER_ID, pid.toString());
			}
		} catch (Exception e) {
			//LogLog.error("Error setting playerId " + pid, e);
		}
	}

	private void addProperties(ILoggingEvent event, XContentBuilder jsonBuilder)	throws IOException {
		//@SuppressWarnings("rawtypes")
		Map<String, String> props = event.getMDCPropertyMap();
		for (String pkey : props.keySet()) {
			jsonBuilder.field(pkey.toString(), props.get(pkey).toString());
		}
	}

	private void addStackTrace(ILoggingEvent event, XContentBuilder jsonBuilder)	throws IOException {
		if (event.getThrowableProxy() != null) {
			String stacktrace = event.getThrowableProxy().getStackTraceElementProxyArray().toString(); 
					//ExceptionUtils.getFullStackTrace(event.getThrowableInformation().getThrowable());
			jsonBuilder.field(LoggingConstants.STACK_TRACE, stacktrace);
		}
	}
}
