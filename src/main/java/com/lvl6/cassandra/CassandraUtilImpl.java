package com.lvl6.cassandra;

import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import me.prettyprint.hector.api.ddl.ComparatorType;

public class CassandraUtilImpl implements CassandraUtil {
	/* (non-Javadoc)
	 * @see com.lvl6.cassandra.CassandraUtil#createBasicColumn(java.lang.String, me.prettyprint.hector.api.ddl.ComparatorType, boolean)
	 */
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
}
