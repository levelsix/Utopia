package com.lvl6.cassandra.log4j;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.lvl6.elasticsearch.Lvl6ElasticSearch;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.lucene.search.TermFilter;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders.*;
import org.elasticsearch.index.query.QueryBuilders.*;
import org.elasticsearch.search.sort.SortBuilders.*;
import org.joda.time.DateTime;


public class Log4jElasticSearchQuery {
	
	private static Logger log = LoggerFactory.getLogger(Log4jElasticSearchQuery.class);
	
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
		SearchRequestBuilder request = client.prepareSearch(Log4JElasticSearchIndexer.INDEX)
		.setTypes(Log4JElasticSearchIndexer.TYPE)
		.setQuery(buildQuery())
		.setFrom(offset)
		.setSize(limit);
		log.info("Performing search: \n{}", request.toString());
		return request.execute().actionGet();
	}
	
	protected QueryBuilder buildQuery() {
		RangeFilterBuilder dateFilter = getDateFilter();
		QueryBuilder matchQuery = getSearchQuery();
		BoolQueryBuilder andQuery = QueryBuilders.boolQuery();
		if(message!= null && !message.equals("")) {
			andQuery.must(matchQuery);
		}
		if(level != null && !level.equals("")){
			andQuery.must(getLevelQuery());
		}
		if(playerId != -1) {
			andQuery.must(getPlayerIdQuery());
		}
		QueryBuilder query = QueryBuilders.filteredQuery(andQuery,dateFilter);
		return query;
	}

	private RangeFilterBuilder getDateFilter() {
		RangeFilterBuilder dateFilter = FilterBuilders.rangeFilter(Log4JConstants.TIME);
		if(startDate != null) {
			dateFilter.from(startDate.getTime());
		}
		if(endDate != null) {
			dateFilter.to(endDate.getTime());
		}
		return dateFilter;
	}

	protected QueryBuilder getLevelQuery() {
		return QueryBuilders.fieldQuery(Log4JConstants.LEVEL, level);
	}
	
	protected QueryBuilder getPlayerIdQuery() {
		return QueryBuilders.fieldQuery(Log4JConstants.PLAYER_ID, playerId);
	}
	
	
	protected QueryBuilder getSearchQuery() {
		QueryBuilder matchQuery = QueryBuilders.matchQuery("_all", message);
		return matchQuery;
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
