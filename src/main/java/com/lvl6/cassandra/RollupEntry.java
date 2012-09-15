package com.lvl6.cassandra;

public class RollupEntry {
	@Override
	public String toString() {
		return "RollupEntry [key=" + key + ", column=" + column + ", value="
				+ value + "]";
	}
	public RollupEntry(String key, Long column, Long value) {
		super();
		this.key = key;
		this.column = column;
		this.value = value;
	}

	
	String key;
	Long column;
	Long value;

	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Long getColumn() {
		return column;
	}
	public void setColumn(Long column) {
		this.column = column;
	}
	public Long getValue() {
		return value;
	}
	public void setValue(Long value) {
		this.value = value;
	}
}
