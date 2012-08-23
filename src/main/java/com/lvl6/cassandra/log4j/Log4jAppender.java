package com.lvl6.cassandra.log4j;

import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
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
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import com.lvl6.cassandra.CassandraUtil;
import com.lvl6.cassandra.CassandraUtilImpl;
import com.lvl6.properties.MDCKeys;

public class Log4jAppender extends AppenderSkeleton {
	
	public static AtomicLong logcounter = new AtomicLong();
	
	protected ExecutorService executor = Executors.newFixedThreadPool(20);
	
	protected Log4JElasticSearchIndexer search;

	private String clusterName;

	public Log4jAppender(String clusterName, String hosts, String keyspace, String columnFamily, String elasticCluster, String elasticHosts) {
		super();
		this.clusterName = clusterName;
		this.hosts = hosts;
		this.keyspace = keyspace;
		this.columnFamily = columnFamily;
		this.elasticCluster = elasticCluster;
		this.elasticHosts = elasticHosts;
		startAppender();
	}

	private String hosts;
	private String instanceId;
	private String keyspace;
	private String columnFamily;
	private String elasticCluster;

	private String elasticHosts;
	private int replicationFactor = 1;

	private ThriftCluster cluster;
	private ColumnFamilyTemplate<String, String> client;
	
	protected CassandraHostConfigurator cassandraHostConfigurator;
	
	protected CassandraUtil cassandraUtil = new CassandraUtilImpl();

	private boolean startedUp = false;
	private boolean shutdown = false;

	private long lastPublishTime = -1;

	public Log4jAppender() {
		super();
		startAppender();
	}

	private void startAppender() {
		Runnable r = new Runnable() {
			public void run() {
				while (!shutdown) {
					try {
						Thread.sleep(3000);
						setup();
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
	
	private void setup() throws Exception {
		if(cluster == null || cassandraHostConfigurator == null || executor == null) {
			//setupExecutor();
			setupConnection();
		}
		if (client == null) {
			connect();
		}
		if(search == null) {
			search = new Log4JElasticSearchIndexer(elasticHosts, elasticCluster);
		}
	}
	
	

	@Override
	protected void append(final LoggingEvent event) {
		//long startTime = System.currentTimeMillis();
		//LogLog.warn("Recieved event: "+event.getMessage());
		if(executor != null) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				String message = event.getMessage() + "";
				if (startedUp && client != null) {
					try {
						UUID key = TimeUUIDUtils.getUniqueTimeUUIDinMillis();
						ColumnFamilyUpdater<String, String> updater = client.createUpdater(key.toString());
						updater.setLong(Log4JConstants.TIME, event.getTimeStamp());
						updater.setString(Log4JConstants.HOST, getHost());
						updater.setString(Log4JConstants.MESSAGE, message);
						updater.setString(Log4JConstants.LEVEL, event.getLevel() + "");
						updater.setString(Log4JConstants.NAME, event.getLoggerName());
						updater.setString(Log4JConstants.THREAD, event.getThreadName());
						addStackTrace(event, updater);
						addProperties(event, updater);
						addPlayerId(event, updater);
						addUdid(event, updater);
						client.update(updater);
						search.indexEvent(event, key, getHost());
						//logcounter.incrementAndGet();
						//LogLog.warn(message);
					} catch (Exception e) {
						client = null;
						LogLog.error("append failed, " + e.getMessage(), e);
					}
				} else {
					if (startedUp) {
						LogLog.warn("Log4jAppender, cluster not available, skipping logging, " + message);
					}
				}
			}
		});
		}
		//long endTime = System.currentTimeMillis();
		//lastPublishTime = endTime - startTime;
	}

	private void addUdid(LoggingEvent event,ColumnFamilyUpdater<String, String> updater) {
		String udId = (String) event.getMDC(MDCKeys.UDID);
		if (udId != null && !udId.equals("")) {
			updater.setString(Log4JConstants.UDID, udId.toString());
		}
	}

	private void addPlayerId(LoggingEvent event, ColumnFamilyUpdater<String, String> updater) {
		Object pid;
		try {
			pid =  event.getMDC(MDCKeys.PLAYER_ID);
			if (pid != null) {
				updater.setString(Log4JConstants.PLAYER_ID, pid.toString());
			}
		} catch (Exception e) {
			LogLog.error("Error setting playerId " + event.getMDC(MDCKeys.PLAYER_ID), e);
		}
	}

	private void addProperties(LoggingEvent event,ColumnFamilyUpdater<String, String> updater) {
		@SuppressWarnings("rawtypes")
		Map props = event.getProperties();
		for (Object pkey : props.keySet()) {
			updater.setString(pkey.toString(), props.get(pkey).toString());
		}
	}

	private void addStackTrace(LoggingEvent event,ColumnFamilyUpdater<String, String> updater) {
		if (event.getThrowableInformation() != null	&& event.getThrowableInformation().getThrowable() != null) {
			String stacktrace = ExceptionUtils.getFullStackTrace(event.getThrowableInformation().getThrowable());
			updater.setString(Log4JConstants.STACK_TRACE, stacktrace);
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
		if(!checkKeyspaceExists()){
			setupConnection();
		}
	}
	
	private void setupConnection() throws Exception {
		LogLog.warn("creating cassandra cluster connection: " + hosts);
		cassandraHostConfigurator = new CassandraHostConfigurator(hosts);
		cassandraHostConfigurator.setMaxActive(20);
		cassandraHostConfigurator.setCassandraThriftSocketTimeout(2500);
		cassandraHostConfigurator.setUseSocketKeepalive(true);
		cassandraHostConfigurator.setMaxWaitTimeWhenExhausted(500);
		cluster = new ThriftCluster(clusterName,cassandraHostConfigurator);
		// getOrCreateCluster(getClusterName(),
		// cassandraHostConfigurator);
		setupColumnFamilies(cluster);
	}

	private void setupColumnFamilies(Cluster c) throws Exception {
		KeyspaceDefinition keyspaceDef = c.describeKeyspace(keyspace);
		BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
		columnFamilyDefinition.setKeyspaceName(keyspace);
		columnFamilyDefinition.setName(columnFamily);
		columnFamilyDefinition.setComparatorType(ComparatorType.UTF8TYPE);
		//columnFamilyDefinition.setKeyValidationClass(ComparatorType.TIMEUUIDTYPE.getClassName());
		ColumnFamilyDefinition cfDef = new ThriftCfDef(columnFamilyDefinition);
		if(!checkKeyspaceExists()){
			createKeyspaceDef(c, cfDef);
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
		columnFamilyDefinition.addColumnDefinition(
			cassandraUtil.createBasicColumnDefinition(Log4JConstants.LEVEL, ComparatorType.UTF8TYPE, true));
		// time index
		columnFamilyDefinition.addColumnDefinition(
			cassandraUtil.createBasicColumnDefinition(Log4JConstants.TIME, ComparatorType.LONGTYPE, true));
		// host
		columnFamilyDefinition.addColumnDefinition(
				cassandraUtil.createBasicColumnDefinition(Log4JConstants.HOST, ComparatorType.UTF8TYPE, false));
		// message
		columnFamilyDefinition.addColumnDefinition(
				cassandraUtil.createBasicColumnDefinition(Log4JConstants.MESSAGE, ComparatorType.UTF8TYPE, false));
		// name
		columnFamilyDefinition.addColumnDefinition(
				cassandraUtil.createBasicColumnDefinition(Log4JConstants.NAME,ComparatorType.UTF8TYPE, false));
		// thread
		columnFamilyDefinition.addColumnDefinition(
				cassandraUtil.createBasicColumnDefinition(Log4JConstants.THREAD, ComparatorType.UTF8TYPE, false));
		// stacktrace
		columnFamilyDefinition.addColumnDefinition(
				cassandraUtil.createBasicColumnDefinition(Log4JConstants.STACK_TRACE, ComparatorType.UTF8TYPE, false));
		// playerId
		columnFamilyDefinition.addColumnDefinition(
				cassandraUtil.createBasicColumnDefinition(Log4JConstants.PLAYER_ID, ComparatorType.UTF8TYPE, true));
		// udId
		columnFamilyDefinition.addColumnDefinition(
				cassandraUtil.createBasicColumnDefinition(Log4JConstants.UDID, ComparatorType.UTF8TYPE, true));
		// ip
/*		columnFamilyDefinition.addColumnDefinition(
				cassandraUtil.createBasicColumnDefinition("ip", ComparatorType.UTF8TYPE, true));
*/
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

	private boolean checkKeyspaceExists() {
		KeyspaceDefinition keyspaceDef = cluster.describeKeyspace(keyspace);
		if (keyspaceDef == null) {
			return false;
		}
		return true;
	}

	private void createKeyspaceDef(Cluster c, ColumnFamilyDefinition cfDef) {
		LogLog.warn("Creating keyspace " + keyspace);
		KeyspaceDefinition newKeyspace = HFactory.createKeyspaceDefinition(
				keyspace, ThriftKsDef.DEF_STRATEGY_CLASS,
				replicationFactor, Arrays.asList(cfDef));
		c.addKeyspace(newKeyspace);
	}
	
	
	String host = "";
	
	private String getHost() {
		if (instanceId != null) {
			return instanceId;
		}
		try {
			if(host == null || host.equals("")) {
				host = InetAddress.getLocalHost().getHostAddress();
			}
		} catch (UnknownHostException e) {
		}
		return host;
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

	public String getElasticCluster() {
		return elasticCluster;
	}
	
	public void setElasticCluster(String elasticCluster) {
		this.elasticCluster = elasticCluster;
	}
	
	public String getElasticHosts() {
		return elasticHosts;
	}
	
	public void setElasticHosts(String elasticHosts) {
		this.elasticHosts = elasticHosts;
	}
}
