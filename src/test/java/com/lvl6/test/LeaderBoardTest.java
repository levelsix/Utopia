package com.lvl6.test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import junit.framework.TestCase;
import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.model.CqlQuery;
import me.prettyprint.cassandra.model.CqlRows;
import me.prettyprint.cassandra.serializers.DateSerializer;
import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.TimeUUIDSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.cassandra.utils.TimeUUIDUtils;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;

import org.apache.cassandra.thrift.NotFoundException;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.Tuple;

import com.lvl6.cassandra.CassandraUtil;
import com.lvl6.cassandra.CassandraUtilImpl;
import com.lvl6.cassandra.log4j.Log4jAppender;
import com.lvl6.leaderboards.LeaderBoardUtil;
import com.lvl6.properties.MDCKeys;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class LeaderBoardTest extends TestCase {

	Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	protected LeaderBoardUtil lb;
	
	public LeaderBoardUtil getLb() {
		return lb;
	}

	public void setLb(LeaderBoardUtil lb) {
		this.lb = lb;
	}

	
	
	
	@Test
	public void testLeadboardSet() {
		int rank = 0;
		Set<Tuple> ex = lb.getExperienceTopN(rank, rank+100);
		String leaderBoard = "experience";
		printRanks(rank, ex, leaderBoard);
		rank = 0;
		ex = lb.getBattlesWonTopN(rank, rank+100);
		leaderBoard = "battles won";
		printRanks(rank, ex, leaderBoard);
		rank = 0;
		ex = lb.getTotalCoinValueForTopN(rank, rank+100);
		leaderBoard = "total coins";
		printRanks(rank, ex, leaderBoard);
		rank = 0;
		ex = lb.getBattlesWonOverTotalBattlesRatioTopN(rank, rank+100);
		leaderBoard = "best kdr";
		printRanks(rank, ex, leaderBoard);
	}
	
	

	private void printRanks(int rank, Set<Tuple> ex, String leaderBoard) {
		/*log.info("Ranks for "+leaderBoard);
		for(Tuple t : ex) {
			log.info("Rank: "+rank+" User: "+t.getElement()+" Score: "+t.getScore());
			rank++;
		}*/
	}
}
