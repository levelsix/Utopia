package com.lvl6.cassandra.log4j;

import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.cassandra.utils.TimeUUIDUtils;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnDefinition;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import com.lvl6.properties.MDCKeys;

public class Log4jAppender extends AppenderSkeleton {

	private static final StringSerializer se = new StringSerializer();
	private static final LongSerializer le = new LongSerializer();

	private String clusterName;

	private String hosts;
	private String instanceId;
	private String keyspace;
	private String columnFamily;
	private int replicationFactor = 1;

	private ThriftCluster cluster;
	private ColumnFamilyTemplate<String, String> client;
	
	protected CassandraHostConfigurator cassandraHostConfigurator;
	

	private boolean startedUp = false;
	private boolean shutdown = false;

	private long lastPublishTime = -1;

	public Log4jAppender() {
		super();
		Runnable r = new Runnable() {

			public void run() {
				while (!shutdown) {
					try {
						Thread.sleep(1000);
						if(cluster == null || cassandraHostConfigurator == null) {
							setupConnection();
						}
						if (client == null) {
							connect();
						}
						if (lastPublishTime > 50) {
							// TODO: some metrics to deactivate logging to this
							// appender if its getting too time consuming, or
							// queue
						}
					} catch (Exception e) {
						LogLog.error("Log4jAppender", e);
					} finally {
						startedUp = true;
					}
				}
			}
		};
		Thread alive = new Thread(r, "Cassandra-Log4J-Alive");
		alive.setDaemon(true);
		alive.start();
	}

	@Override
	protected void append(LoggingEvent event) {
		long startTime = System.currentTimeMillis();
		String message = event.getMessage() + "";
		if (startedUp && client != null) {
			try {
				UUID key = TimeUUIDUtils.getUniqueTimeUUIDinMillis();
				ColumnFamilyUpdater<String, String> updater = client
						.createUpdater(key.toString());
				updater.setLong("time", event.getTimeStamp());
				updater.setString("host", getHost());
				updater.setString("message", message);
				updater.setString("level", event.getLevel() + "");
				updater.setString("name", event.getLoggerName());
				updater.setString("thread", event.getThreadName());
				addStackTrace(event, updater);
				addProperties(event, updater);
				addPlayerId(event, updater);
				addUdid(event, updater);
				client.update(updater);
			} catch (Exception e) {
				client = null;
				LogLog.error("append failed, " + e.getMessage(), e);
			}
		} else {
			if (startedUp) {
				LogLog.warn("Log4jAppender, "
						+ "cluster not available, skipping logging, " + message);
			}
		}
		long endTime = System.currentTimeMillis();
		lastPublishTime = endTime - startTime;
	}

	private void addUdid(LoggingEvent event,
			ColumnFamilyUpdater<String, String> updater) {
		String udId = (String) event.getMDC(MDCKeys.UDID);
		if (udId != null && !udId.equals(""))
			updater.setString("udid", udId.toString());
	}

	private void addPlayerId(LoggingEvent event, ColumnFamilyUpdater<String, String> updater) {
		Long playerId = -1l;
		try {
			Integer pid = (Integer) event.getMDC(MDCKeys.PLAYER_ID);
			if (pid != null)
				updater.setString("playerIdString", playerId.toString());
			/*	playerId = Long.parseLong(pid.toString());
			if (playerId != null) {
				if (playerId != null && playerId > 0)
					LogLog.warn("Saving playerId: " + playerId);
					
			}*/
		} catch (Exception e) {
			LogLog.error("Error setting playerId " + playerId, e);
		}
	}

	private void addProperties(LoggingEvent event,
			ColumnFamilyUpdater<String, String> updater) {
		Map props = event.getProperties();
		for (Object pkey : props.keySet()) {
			updater.setString(pkey.toString(), props.get(pkey).toString());
		}
	}

	private void addStackTrace(LoggingEvent event,
			ColumnFamilyUpdater<String, String> updater) {
		if (event.getThrowableInformation() != null
				&& event.getThrowableInformation().getThrowable() != null) {
			String stacktrace = ExceptionUtils.getFullStackTrace(event
					.getThrowableInformation().getThrowable());
			updater.setString("stacktrace", stacktrace);
		}
	}

	public void close() {
		LogLog.warn("Closing log4cassandra appender");
		shutdown = true;
		cluster.getConnectionManager().shutdown();
	}

	private void connect() throws Exception {
		LogLog.warn("connect() ");
		Keyspace ksp = createKeyspace(keyspace, cluster);
		client = new ThriftColumnFamilyTemplate<String, String>(ksp,
				columnFamily, StringSerializer.get(), StringSerializer.get());
		
	}
	
	private void setupConnection() throws Exception {
		LogLog.warn("creating cassandra cluster connection: " + hosts);
		cassandraHostConfigurator = new CassandraHostConfigurator(
				hosts);
		cassandraHostConfigurator.setMaxActive(20);
		cassandraHostConfigurator.setCassandraThriftSocketTimeout(500);
		cassandraHostConfigurator.setMaxWaitTimeWhenExhausted(500);
		cluster = new ThriftCluster(clusterName,
				cassandraHostConfigurator);// getOrCreateCluster(getClusterName(),
											// cassandraHostConfigurator);
		setupColumnFamilies(cluster);
	}

	private void setupColumnFamilies(Cluster c) throws Exception {
		KeyspaceDefinition keyspaceDef = c.describeKeyspace(keyspace);
		BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
		columnFamilyDefinition.setKeyspaceName(keyspace);
		columnFamilyDefinition.setName(columnFamily);
		columnFamilyDefinition.setComparatorType(ComparatorType.UTF8TYPE);
		// columnFamilyDefinition.setKeyValidationClass(ComparatorType.BYTESTYPE.getClassName());
		ColumnFamilyDefinition cfDef = new ThriftCfDef(columnFamilyDefinition);
		if (keyspaceDef == null) {
			LogLog.warn("Creating keyspace " + keyspace);
			KeyspaceDefinition newKeyspace = HFactory.createKeyspaceDefinition(
					keyspace, ThriftKsDef.DEF_STRATEGY_CLASS,
					replicationFactor, Arrays.asList(cfDef));
			c.addKeyspace(newKeyspace);
		} else {
			LogLog.warn(keyspaceDef.getName() + " exists");
		}
		keyspaceDef = null;
		cfDef = null;
		KeyspaceDefinition fromCluster = c.describeKeyspace(keyspace);
		for (ColumnFamilyDefinition cfd : fromCluster.getCfDefs()) {
			if (cfd.getName().equals(columnFamily)) {
				cfDef = cfd;
			}
		}
		columnFamilyDefinition = new BasicColumnFamilyDefinition(cfDef);

		// level index
		BasicColumnDefinition bcdf = new BasicColumnDefinition();
		bcdf.setName(StringSerializer.get().toByteBuffer("level"));
		bcdf.setIndexName("level_index");
		bcdf.setIndexType(ColumnIndexType.KEYS);
		bcdf.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
		columnFamilyDefinition.addColumnDefinition(bcdf);

		// time index
		BasicColumnDefinition bcdf2 = new BasicColumnDefinition();
		bcdf2.setName(StringSerializer.get().toByteBuffer("time"));
		bcdf2.setIndexName("time_index");
		bcdf2.setIndexType(ColumnIndexType.KEYS);
		bcdf2.setValidationClass(ComparatorType.LONGTYPE.getClassName());
		columnFamilyDefinition.addColumnDefinition(bcdf2);

		// host
		BasicColumnDefinition bcdf3 = new BasicColumnDefinition();
		bcdf3.setName(StringSerializer.get().toByteBuffer("host"));
		bcdf3.setIndexType(ColumnIndexType.KEYS);
		bcdf3.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
		columnFamilyDefinition.addColumnDefinition(bcdf3);

		// message
		BasicColumnDefinition bcdf4 = new BasicColumnDefinition();
		bcdf4.setName(StringSerializer.get().toByteBuffer("message"));
		bcdf4.setIndexType(ColumnIndexType.KEYS);
		bcdf4.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
		columnFamilyDefinition.addColumnDefinition(bcdf4);

		// name
		BasicColumnDefinition bcdf5 = new BasicColumnDefinition();
		bcdf5.setName(StringSerializer.get().toByteBuffer("name"));
		bcdf5.setIndexType(ColumnIndexType.KEYS);
		bcdf5.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
		columnFamilyDefinition.addColumnDefinition(bcdf5);

		// thread
		BasicColumnDefinition bcdf6 = new BasicColumnDefinition();
		bcdf6.setName(StringSerializer.get().toByteBuffer("thread"));
		bcdf6.setIndexType(ColumnIndexType.KEYS);
		bcdf6.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
		columnFamilyDefinition.addColumnDefinition(bcdf6);

		// stacktrace
		BasicColumnDefinition bcdf7 = new BasicColumnDefinition();
		bcdf7.setName(StringSerializer.get().toByteBuffer("stacktrace"));
		bcdf7.setIndexType(ColumnIndexType.KEYS);
		bcdf7.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
		columnFamilyDefinition.addColumnDefinition(bcdf7);

		// playerId
		BasicColumnDefinition bcdf8 = new BasicColumnDefinition();
		bcdf8.setName(StringSerializer.get().toByteBuffer("playerId"));
		bcdf8.setIndexName("playerId_index");
		bcdf8.setIndexType(ColumnIndexType.KEYS);
		bcdf8.setValidationClass(ComparatorType.LONGTYPE.getClassName());
		columnFamilyDefinition.addColumnDefinition(bcdf8);
		
		// playerIdString
		BasicColumnDefinition bcdf11 = new BasicColumnDefinition();
		bcdf11.setName(StringSerializer.get().toByteBuffer("playerIdString"));
		bcdf11.setIndexName("playerIdString_index");
		bcdf11.setIndexType(ColumnIndexType.KEYS);
		bcdf11.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
		columnFamilyDefinition.addColumnDefinition(bcdf11);

		// udId
		BasicColumnDefinition bcdf9 = new BasicColumnDefinition();
		bcdf9.setName(StringSerializer.get().toByteBuffer("udid"));
		bcdf9.setIndexName("udid_index");
		bcdf9.setIndexType(ColumnIndexType.KEYS);
		bcdf9.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
		columnFamilyDefinition.addColumnDefinition(bcdf9);

		// ip
		BasicColumnDefinition bcdf10 = new BasicColumnDefinition();
		bcdf10.setName(StringSerializer.get().toByteBuffer("ip"));
		bcdf10.setIndexType(ColumnIndexType.KEYS);
		bcdf10.setIndexName("ip_index");
		bcdf10.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
		columnFamilyDefinition.addColumnDefinition(bcdf10);

		cfDef = new ThriftCfDef(columnFamilyDefinition);
		c.updateColumnFamily(cfDef);
		keyspaceDef = c.describeKeyspace(keyspace);
		for (ColumnFamilyDefinition cfd : keyspaceDef.getCfDefs()) {
			LogLog.warn("ColumnFamilyDefinition: " + cfd.getName() + ":"
					+ cfd.getColumnType().getValue());
			for (ColumnDefinition cd : cfd.getColumnMetadata()) {
				LogLog.warn("Name: "
						+ StringSerializer.get().fromByteBuffer(cd.getName())
						+ ", IndexType: " + cd.getIndexType());
			}
		}
	}

	private String getHost() {
		if (instanceId != null) {
			return instanceId;
		}
		try {
			String host = InetAddress.getLocalHost().getHostAddress();
			return host;
		} catch (UnknownHostException e) {
		}
		return "";
	}

	public boolean requiresLayout() {
		return false;
	}

	public void setColumnFamily(String columnFamily) {
		this.columnFamily = columnFamily;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	public void setReplicationFactor(int replicationFactor) {
		this.replicationFactor = replicationFactor;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

}
