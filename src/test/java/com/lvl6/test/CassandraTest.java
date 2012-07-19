package com.lvl6.test;

import java.net.UnknownHostException;
import java.util.Arrays;

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
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvl6.ui.admin.pages.AdminPage;
import com.lvl6.ui.admin.pages.MainPage;

import junit.framework.TestCase;

public class CassandraTest extends TestCase {

	Logger log = LoggerFactory.getLogger(getClass());

	private ThriftCluster cassandraCluster;
	private CassandraHostConfigurator cassandraHostConfigurator;

	// @Before
	public void setupCase() throws TTransportException, TException,
			IllegalArgumentException, NotFoundException, UnknownHostException,
			Exception {
		cassandraHostConfigurator = new CassandraHostConfigurator(
				"lucidyang.dekayd.com");
		cassandraCluster = new ThriftCluster("Test Cluster",
				cassandraHostConfigurator);
	}

	@Test
	public void testWarning() {
		log.warn("This is a test warning for cassandra logging");
	}

	@Test
	public void testEditColumnFamily() throws Exception {
		setupCase();
		try {
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
	}
}
