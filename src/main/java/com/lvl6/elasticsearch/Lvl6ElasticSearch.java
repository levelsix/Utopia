package com.lvl6.elasticsearch;

import org.apache.log4j.helpers.LogLog;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class Lvl6ElasticSearch implements InitializingBean {
	protected static Logger log = LoggerFactory.getLogger(Lvl6ElasticSearch.class);
	
	public Lvl6ElasticSearch(String hosts, String clusterName) {
		super();
		this.hosts = hosts;
		this.clusterName = clusterName;
		setup();
	}

	
	
	
	public Lvl6ElasticSearch() {
		super();
	}




	protected String hosts = "";
	protected String clusterName = "";
	protected TransportClient elasticSearchClient;
	
	public TransportClient getClient() {
		if(elasticSearchClient == null) {
			setup();
		}
		return elasticSearchClient;
	}
	
	public void closeClient() {
		if(elasticSearchClient != null) {
			elasticSearchClient.close();
		}
		elasticSearchClient = null;
	}
	
	
	public String getHosts() {
		return hosts;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		setup();
	}

	public void setup() {
		if(!getHosts().equals("")) {
			LogLog.warn("Setting up elastic search with hosts: "+ hosts);
			Settings.Builder settings = ImmutableSettings.settingsBuilder().put("cluster.name", getClusterName());
			elasticSearchClient = new TransportClient(settings);
			String[] hostz = hosts.split(",");
			for(int i = 0; i<hostz.length; i++) {
				log.info("Adding elasticsearch host: {}", hostz[i]);
				try {
					elasticSearchClient.addTransportAddress(new InetSocketTransportAddress(hostz[i], 9300));
				}catch(Exception e) {
					LogLog.error("Error adding host "+hostz[i]+" to elastic search client: "+e.getMessage());
				}
			}
		}else {
			log.error("Elastic search hosts property was null or ''");
		}
	}
}
