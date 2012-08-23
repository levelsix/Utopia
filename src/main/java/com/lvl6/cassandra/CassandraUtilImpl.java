package com.lvl6.cassandra;

import static me.prettyprint.hector.api.factory.HFactory.createCounterColumn;
import static me.prettyprint.hector.api.factory.HFactory.createMutator;
import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.thrift.ThriftCounterColumnQuery;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.CounterQuery;

public class CassandraUtilImpl implements CassandraUtil {
	/* (non-Javadoc)
	 * @see com.lvl6.cassandra.CassandraUtil#createBasicColumn(java.lang.String, me.prettyprint.hector.api.ddl.ComparatorType, boolean)
	 */
	protected StringSerializer ss = StringSerializer.get();
	protected LongSerializer ls = LongSerializer.get();
	
	@Override
	public BasicColumnDefinition createBasicColumnDefinition(String name, ComparatorType type, boolean index){
		BasicColumnDefinition bcdf = new BasicColumnDefinition();
		bcdf.setName(StringSerializer.get().toByteBuffer(name));
		if(index) {
			bcdf.setIndexName(name+"_index");
			bcdf.setIndexType(ColumnIndexType.KEYS);
		}
		bcdf.setValidationClass(type.getClassName());
		return bcdf;
	}

	@Override
	public void insertCounterColumn(Keyspace keyspace, String key,
			String columnFamily, String column, Long initialValue) {
		Mutator<String> m = createMutator(keyspace, ss);
		//MutationResult mr = 
		m.insertCounter(key, columnFamily, createCounterColumn(column, initialValue));
	}

	@Override
	public void insertCounterColumn(Keyspace keyspace, String key,
			String columnFamily, Long column, Long initialValue) {
		Mutator<String> m = createMutator(keyspace, ss);
		//MutationResult mr = 
		m.insertCounter(key, columnFamily, createCounterColumn(column, initialValue, LongSerializer.get()));		
	}

	@Override
	public void incrementCounter(Keyspace keyspace, String key,
			String columnFamily, String column, Long increment) {
		Mutator<String> m = createMutator(keyspace, ss);
		m.incrementCounter(key, columnFamily, column, increment);
	}

	@Override
	public void incrementCounter(Keyspace keyspace, String key,
			String columnFamily, Long column, Long increment) {
		Mutator<String> m = createMutator(keyspace, ss);
		m.incrementCounter(key, columnFamily, column, increment);
		
	}

	@Override
	public Long getCounterValue(Keyspace keyspace, String key,
			String columnFamily, String column, Long increment) {
		CounterQuery<String, String> counter = new ThriftCounterColumnQuery<String,String>(keyspace, ss, ss);
		counter.setColumnFamily(columnFamily).setKey(key).setName(column);
		return counter.execute().get().getValue();
	}

	@Override
	public Long getCounterValue(Keyspace keyspace, String key,
			String columnFamily, Long column, Long increment) {
		CounterQuery<String, Long> counter = new ThriftCounterColumnQuery<String,Long>(keyspace, ss, ls);
		counter.setColumnFamily(columnFamily).setKey(key).setName(column);
		return counter.execute().get().getValue();
	}
	
	
	
/*	
*/
	
	
}
