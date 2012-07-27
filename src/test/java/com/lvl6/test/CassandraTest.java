package com.lvl6.test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import junit.framework.TestCase;
import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
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

import org.apache.cassandra.thrift.NotFoundException;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lvl6.cassandra.CassandraUtil;
import com.lvl6.cassandra.CassandraUtilImpl;
import com.lvl6.cassandra.log4j.Log4jAppender;
import com.lvl6.properties.MDCKeys;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class CassandraTest extends TestCase {

	Logger log = LoggerFactory.getLogger(getClass());

	
	@Autowired
	private ThriftCluster cassandraCluster;
	@Autowired
	private CassandraHostConfigurator cassandraHostConfigurator;

	@Autowired
	private Log4jAppender appender;
	
	
	public Log4jAppender getAppender() {
		return appender;
	}

	public void setAppender(Log4jAppender appender) {
		this.appender = appender;
	}

	public ThriftCluster getCassandraCluster() {
		return cassandraCluster;
	}

	public void setCassandraCluster(ThriftCluster cassandraCluster) {
		this.cassandraCluster = cassandraCluster;
	}

	public CassandraHostConfigurator getCassandraHostConfigurator() {
		return cassandraHostConfigurator;
	}

	public void setCassandraHostConfigurator(
			CassandraHostConfigurator cassandraHostConfigurator) {
		this.cassandraHostConfigurator = cassandraHostConfigurator;
	}

	
	

	// @Before
	public void setupCase() throws TTransportException, TException,
			IllegalArgumentException, NotFoundException, UnknownHostException,
			Exception {
		
	}

	@Test
	public void testWarning() {
		log.info("This is a test info for cassandra logging");
		log.warn("This is a test warning for cassandra logging");
		log.error("This is a test error for cassandra logging");
	}

	
	//@Test
	public void testAppender() {
		
	}

	
	protected static String KEYSPACE = "Junit";
	protected static String CF = "BasicCassandraTest";
	
	
	@Test
	public void testEditColumnFamily() throws Exception {
		setupCase();
		CassandraUtil util = new CassandraUtilImpl();
		createTestColumnFamily();
		KeyspaceDefinition fromCluster = cassandraCluster.describeKeyspace(KEYSPACE);
		createColumnsForTestColumnFamily(util, fromCluster);
		Keyspace ksp = HFactory.createKeyspace(KEYSPACE, cassandraCluster);
		fromCluster = cassandraCluster.describeKeyspace(KEYSPACE);
		ColumnFamilyTemplate<String, String> client = new ThriftColumnFamilyTemplate<String, String>(ksp,
				CF, StringSerializer.get(), StringSerializer.get());
		UUID key = TimeUUIDUtils.getUniqueTimeUUIDinMillis();
		ColumnFamilyUpdater<String, String> updater = client
		.createUpdater(key.toString());
		updater.setString("testString", "Test String: "+Math.random());
		updater.setInteger("testInteger", 99);
		updater.setLong("testLong", 420l);
		updater.setDate("testDate", new Date());
		updater.setDouble("testDouble", 2013d);
	}

	private void createColumnsForTestColumnFamily(CassandraUtil util,
			KeyspaceDefinition fromCluster) {
		ColumnFamilyDefinition cfDef = fromCluster.getCfDefs().get(0);
		BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition(cfDef);
		columnFamilyDefinition.addColumnDefinition(util.createBasicColumnDefinition("testString", StringSerializer.get().getComparatorType(), true));
		columnFamilyDefinition.addColumnDefinition(util.createBasicColumnDefinition("testInteger", IntegerSerializer.get().getComparatorType(), true));
		columnFamilyDefinition.addColumnDefinition(util.createBasicColumnDefinition("testLong", LongSerializer.get().getComparatorType(), true));
		columnFamilyDefinition.addColumnDefinition(util.createBasicColumnDefinition("testDate", DateSerializer.get().getComparatorType(), true));
		columnFamilyDefinition.addColumnDefinition(util.createBasicColumnDefinition("testDouble", DoubleSerializer.get().getComparatorType(), true));
		cassandraCluster.updateColumnFamily(new ThriftCfDef(columnFamilyDefinition));
	}

	private void createTestColumnFamily() {
		try {
			BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
			columnFamilyDefinition.setKeyspaceName(KEYSPACE);
			columnFamilyDefinition.setName(CF);
			ColumnFamilyDefinition cfDef = new ThriftCfDef(columnFamilyDefinition);
			KeyspaceDefinition keyspaceDefinition = HFactory
					.createKeyspaceDefinition(KEYSPACE,
						"org.apache.cassandra.locator.SimpleStrategy", 
						1,
						Arrays.asList(cfDef));
			cassandraCluster.addKeyspace(keyspaceDefinition);
		} catch (Exception e) {

		}
	}
}
