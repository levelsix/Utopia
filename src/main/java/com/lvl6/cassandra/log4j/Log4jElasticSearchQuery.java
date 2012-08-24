package com.lvl6.cassandra.log4j;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.lvl6.elasticsearch.Lvl6ElasticSearch;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders.*;
import org.elasticsearch.index.query.QueryBuilders.*;
import org.elasticsearch.search.sort.SortBuilders.*;
import org.joda.time.DateTime;


public class Log4jElasticSearchQuery {
	
	@Autowired
	Lvl6ElasticSearch search;

	public Lvl6ElasticSearch getSearch() {
		return search;
	}

	public void setSearch(Lvl6ElasticSearch search) {
		this.search = search;
	}
	
	
	protected Date startDate = new DateTime().minusDays(1).toDate(); 
	protected Date endDate = new Date();
	protected int offset = 0;
	protected int limit = 100;
	protected String level = "All";
	protected String message = "";
	protected int playerId = -1;
	protected String thread = "";

		
	
	public SearchResponse search() {
		Client client = getSearch().getClient();
		return client.prepareSearch(Log4JElasticSearchIndexer.INDEX)
		.setTypes(Log4JElasticSearchIndexer.TYPE)
		.setQuery(buildQuery())
		.setFrom(offset)
		.setSize(limit)
		.execute()
		.actionGet();
	}
	
	protected QueryBuilder buildQuery() {
		RangeFilterBuilder dateFilter = FilterBuilders.rangeFilter(Log4JConstants.TIME);
		dateFilter.from(startDate.getTime());
		dateFilter.to(endDate.getTime());
		QueryBuilder query = QueryBuilders.filteredQuery(QueryBuilders.queryString(message), dateFilter);
		return query;
	}
	
	
	
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}
	
	
	
	
}
