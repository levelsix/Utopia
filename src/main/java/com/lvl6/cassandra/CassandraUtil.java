package com.lvl6.cassandra;

import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ComparatorType;

public interface CassandraUtil {

	public abstract BasicColumnDefinition createBasicColumnDefinition(String name,
			ComparatorType type, boolean index);
	
	public abstract void insertCounterColumn(Keyspace keyspace, String key, String columnFamily, String column, Long initialValue);
	public abstract void insertCounterColumn(Keyspace keyspace, String key, String columnFamily, Long column, Long initialValue);
	
	public abstract void incrementCounter(Keyspace keyspace, String key, String columnFamily, String column, Long increment);
	public abstract void incrementCounter(Keyspace keyspace, String key, String columnFamily, Long column, Long increment);
	
	public abstract Long getCounterValue(Keyspace keyspace, String key, String columnFamily, String column, Long increment);
	public abstract Long getCounterValue(Keyspace keyspace, String key, String columnFamily, Long column, Long increment);

}