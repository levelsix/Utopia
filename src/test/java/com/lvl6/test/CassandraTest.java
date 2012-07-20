package com.lvl6.test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;
import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftCluster;
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

	
	
	
	//@Test
	public void testEditColumnFamily() throws Exception {
		setupCase();
/*		try {
			BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
			columnFamilyDefinition.setKeyspaceName("DynKeyspace3");
			columnFamilyDefinition.setName("DynamicCF");

			ColumnFamilyDefinition cfDef = new ThriftCfDef(
					columnFamilyDefinition);

			KeyspaceDefinition keyspaceDefinition = HFactory
					.createKeyspaceDefinition("DynKeyspace3",
							"org.apache.cassandra.locator.SimpleStrategy", 1,
							Arrays.asList(cfDef));
			cassandraCluster.addKeyspace(keyspaceDefinition);
		} catch (Exception e) {

		}
		KeyspaceDefinition fromCluster = cassandraCluster
				.describeKeyspace("DynKeyspace3");
		ColumnFamilyDefinition cfDef = fromCluster.getCfDefs().get(0);

		BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition(
				cfDef);
		BasicColumnDefinition columnDefinition = new BasicColumnDefinition();
		columnDefinition.setName(StringSerializer.get().toByteBuffer(
				"birthdate"));
		columnDefinition.setIndexName("birthdate_idx");
		columnDefinition.setIndexType(ColumnIndexType.KEYS);
		columnDefinition.setValidationClass(ComparatorType.LONGTYPE
				.getClassName());
		columnFamilyDefinition.addColumnDefinition(columnDefinition);

		columnDefinition = new BasicColumnDefinition();
		columnDefinition.setName(StringSerializer.get().toByteBuffer(
				"nonindexed_field"));
		columnDefinition.setValidationClass(ComparatorType.LONGTYPE
				.getClassName());
		columnFamilyDefinition.addColumnDefinition(columnDefinition);

		cassandraCluster.updateColumnFamily(new ThriftCfDef(
				columnFamilyDefinition));

		fromCluster = cassandraCluster.describeKeyspace("DynKeyspace3");

		assertEquals(
				"birthdate",
				StringSerializer.get().fromByteBuffer(
						fromCluster.getCfDefs().get(0).getColumnMetadata()
								.get(0).getName()));
		assertEquals("birthdate_idx", fromCluster.getCfDefs().get(0)
				.getColumnMetadata().get(0).getIndexName());
		assertEquals(
				"nonindexed_field",
				StringSerializer.get().fromByteBuffer(
						fromCluster.getCfDefs().get(0).getColumnMetadata()
								.get(1).getName()));
								*/
	}
}
