package com.lvl6.cassandra;

import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;

public interface CassandraUtil {

	public abstract BasicColumnDefinition createBasicColumnDefinition(String name,
			ComparatorType type, boolean index);

}