package com.lvl6.test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import junit.framework.TestCase;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.model.CqlQuery;
import me.prettyprint.cassandra.serializers.DateSerializer;
import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.cassandra.utils.TimeUUIDUtils;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.cassandra.thrift.NotFoundException;
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


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-spring-application-context.xml")
public class CassandraTest extends TestCase {

	Logger log = LoggerFactory.getLogger(getClass());
	org.apache.log4j.Logger alog = org.apache.log4j.Logger.getLogger(getClass());

	
	@Autowired
	private ThriftCluster cassandraCluster;
	@Autowired
	private CassandraHostConfigurator cassandraHostConfigurator;

/*	@Autowired
	private Log4jAppender appender;
	
	
	public Log4jAppender getAppender() {
		return appender;
	}

	public void setAppender(Log4jAppender appender) {
		this.appender = appender;
	}*/

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
		
		long start = new Date().getTime();
		int testLoopCount = 1000000;
		for(int i=0; i < testLoopCount; i++){
			log.debug("slf4j test debug for cassandra logging: {}", i);
			//log.info("slf4j test info for cassandra logging: {}", i);
			alog.debug("log4j test debug for cassandra logging: "+i);
			//alog.info("log4j test info for cassandra logging: "+i);
			
			/*log.warn("This is a test warning for cassandra logging {}", i);
			log.error("This is a test error for cassandra logging {}", i);*/
		}
		while(Log4jAppender.logcounter.get() < 2*testLoopCount-1) {
			//log.info("Count: {}", Log4jAppender.logcounter.get());
			long now = new Date().getTime();
			if(Log4jAppender.logcounter.get() % 100 == 0) {
				//log.info("elapsed time: {}ms, total sent: {}", now-start, Log4jAppender.logcounter.get());
			}
		}
		long end = new Date().getTime();
		log.info("Testing time: {}ms, total sent: {}", end-start, Log4jAppender.logcounter.get());
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
		addRow();
	}

	private void addRow() {
		Keyspace ksp = HFactory.createKeyspace(KEYSPACE, cassandraCluster);
		ColumnFamilyTemplate<String, String> client = new ThriftColumnFamilyTemplate<String, String>(ksp,
				CF, StringSerializer.get(), StringSerializer.get());
		UUID key = TimeUUIDUtils.getUniqueTimeUUIDinMillis();
		ColumnFamilyUpdater<String, String> updater = client
		.createUpdater(key.toString());
		updater.setString("testString", "Test String: "+Math.random());
		updater.setInteger("testInteger", 99);
		updater.setLong("testLong", 420l);
		updater.setLong("testDate", new Date().getTime());
		updater.setDouble("testDouble", 2013d);
		client.update(updater);
	}
	
	
	private void createTestTable() {
		String cql = 
		"drop table "+KEYSPACE+"."+CF+";"+
		"CREATE TABLE "+KEYSPACE+"."+CF+"("+
		  "KEY timeuuid PRIMARY KEY,"+
		  "testString text,"+
		  "testLong bigint,"+
		  "testDouble double,"+
		  "testDate timestamp,"+
		  "testInteger bigint"+
		") WITH"+
		  "comment='This is a junit test table for datatypes' AND"+
		  "comparator=text AND"+
		  "read_repair_chance=0.000000 AND"+
		  "gc_grace_seconds=0 AND"+
		  "default_validation=text AND"+
		  "min_compaction_threshold=4 AND"+
		  "max_compaction_threshold=32 AND"+
		  "replicate_on_write='false' AND"+
		  "compaction_strategy_class='SizeTieredCompactionStrategy' AND"+
		  "compression_parameters:sstable_compression='SnappyCompressor';"+
		
		"CREATE INDEX testString_index ON BasicCassandraTest (testString);"+
		
		"CREATE INDEX testLong_index ON BasicCassandraTest (testLong);"+
		
		"CREATE INDEX testDouble_index ON BasicCassandraTest (testDouble);"+
		
		"CREATE INDEX testDate_index ON BasicCassandraTest (testDate);"+
		
		"CREATE INDEX testInteger_index ON BasicCassandraTest (testInteger);";
		Keyspace ksp = HFactory.createKeyspace(KEYSPACE, cassandraCluster);
		CqlQuery<String,String,Long> cqlQuery = new CqlQuery<String,String,Long>(ksp, StringSerializer.get(), StringSerializer.get(), LongSerializer.get());
		cqlQuery.setQuery(cql);
		cqlQuery.execute();
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
			columnFamilyDefinition.setComparatorType(ComparatorType.UTF8TYPE);
			columnFamilyDefinition.setDefaultValidationClass(ComparatorType.UTF8TYPE.getClassName());
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
