package com.lvl6.cassandra;

import java.util.ArrayList;
import java.util.List;

import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ColumnSliceIterator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.SliceQuery;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class RollupUtilImpl implements RollupUtil, InitializingBean {

	
	public static String ROLLUPS_COLUMN_FAMILY = "rolos";
	
	
	@Autowired
	protected Keyspace keyspace;
	
	@Autowired
	protected Cluster cluster;
	
	
	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public Keyspace getKeyspace() {
		return keyspace;
	}

	public void setKeyspace(Keyspace keyspace) {
		this.keyspace = keyspace;
	}

	@Override
	public void addRollupEntry(RollupEntry entry) {
		Mutator<String> mutator = getMutator();
		mutator.addInsertion(entry.getKey(), ROLLUPS_COLUMN_FAMILY, HFactory.createColumn(entry.getColumn(), entry.getValue()));
		mutator.execute();
	}

	protected Mutator<String> getMutator() {
		return HFactory.createMutator(keyspace, StringSerializer.get());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		checkForRollupColumnFamily();
	}

	
	protected void checkForRollupColumnFamily() {
		boolean exists = false;
		KeyspaceDefinition kd = cluster.describeKeyspace(keyspace.getKeyspaceName());
		for(ColumnFamilyDefinition cfd : kd.getCfDefs()) {
			if(cfd.getName().equals(ROLLUPS_COLUMN_FAMILY)) {
				exists = true;
			}
		}
		if(!exists) {
			BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
			columnFamilyDefinition.setKeyspaceName(keyspace.getKeyspaceName());
			columnFamilyDefinition.setName(ROLLUPS_COLUMN_FAMILY);
			columnFamilyDefinition.setComparatorType(ComparatorType.UTF8TYPE);
		}
	}

	@Override
	public void addRollupEntries(List<RollupEntry> entries) {
		Mutator<String> mutator = getMutator();
		for(RollupEntry entry : entries) {
			mutator.addInsertion(entry.getKey(), ROLLUPS_COLUMN_FAMILY, HFactory.createColumn(entry.getColumn(), entry.getValue()));
		}
		mutator.execute();
	}

	@Override
	public List<RollupEntry> findEntries(String key, Long start, Long end) {
		SliceQuery<String, Long, Long> query = HFactory.createSliceQuery(keyspace, StringSerializer.get(),   LongSerializer.get(), LongSerializer.get());
		query.setColumnFamily(ROLLUPS_COLUMN_FAMILY);
		query.setKey(key);
		ColumnSliceIterator<String, Long, Long> iterator = new ColumnSliceIterator<String, Long, Long>( query, start, end, false);
		List<RollupEntry> entries =  new ArrayList<RollupEntry>();
		while(iterator.hasNext()) {
			HColumn<Long, Long> cl = iterator.next();
			RollupEntry ent = new RollupEntry(key, cl.getName(), cl.getValue());
			entries.add(ent);
		}
		return entries;
	}
	
	
	
	
	
}
