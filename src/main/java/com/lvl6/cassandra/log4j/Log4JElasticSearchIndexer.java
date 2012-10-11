package com.lvl6.cassandra.log4j;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.support.replication.ReplicationType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.lvl6.elasticsearch.Lvl6ElasticSearch;
import com.lvl6.properties.MDCKeys;

public class Log4JElasticSearchIndexer {

	public static String INDEX = "logging";
	public static String TYPE = "log4j";
	protected Lvl6ElasticSearch search;
	protected String elasticSearchCluster;
	protected String elasticSearchHosts;

	public Log4JElasticSearchIndexer(String elasticSearchCluster,
			String elasticSearchHosts) {
		super();
		this.elasticSearchCluster = elasticSearchCluster;
		this.elasticSearchHosts = elasticSearchHosts;
		search = new Lvl6ElasticSearch(elasticSearchHosts, elasticSearchCluster);
	}

	TimeValue timeout = new TimeValue(500);

	public void indexEvent(LoggingEvent event, UUID key, String host,
			Map<String, Object> mdccopy) {
		Client client = search.getClient();
		try {
			XContentBuilder jsonBuilder = jsonBuilder();
			jsonBuilder.startObject()
					.field(Log4JConstants.TIME, event.getTimeStamp())
					.field(Log4JConstants.HOST, host)
					.field(Log4JConstants.MESSAGE, event.getMessage())
					.field(Log4JConstants.LEVEL, event.getLevel() + "")
					.field(Log4JConstants.NAME, event.getLoggerName())
					.field(Log4JConstants.THREAD, event.getThreadName());
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
			LogLog.error(e.getDetailedMessage());
			search.closeClient();
		} catch (IOException e) {
			LogLog.error(e.getMessage());
			search.closeClient();
		} finally {

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

	private void addUdid(Map<String, Object> mdccopy,
			XContentBuilder jsonBuilder) throws IOException {
		if (mdccopy == null)
			return;
		String udId = (String) mdccopy.get(MDCKeys.UDID);
		if (udId != null && !udId.equals("")) {
			jsonBuilder.field(Log4JConstants.UDID, udId.toString());
		}
	}

	private void addPlayerId(Map<String, Object> mdccopy,
			XContentBuilder jsonBuilder) {
		if (mdccopy == null)
			return;
		Object pid;
		pid = mdccopy.get(MDCKeys.PLAYER_ID);
		try {
			if (pid != null) {
				jsonBuilder.field(Log4JConstants.PLAYER_ID, pid.toString());
			}
		} catch (Exception e) {
			LogLog.error("Error setting playerId " + pid, e);
		}
	}

	private void addProperties(LoggingEvent event, XContentBuilder jsonBuilder)
			throws IOException {
		@SuppressWarnings("rawtypes")
		Map props = event.getProperties();
		for (Object pkey : props.keySet()) {
			jsonBuilder.field(pkey.toString(), props.get(pkey).toString());
		}
	}

	private void addStackTrace(LoggingEvent event, XContentBuilder jsonBuilder)
			throws IOException {
		if (event.getThrowableInformation() != null
				&& event.getThrowableInformation().getThrowable() != null) {
			String stacktrace = ExceptionUtils.getFullStackTrace(event
					.getThrowableInformation().getThrowable());
			jsonBuilder.field(Log4JConstants.STACK_TRACE, stacktrace);
		}
	}
}
