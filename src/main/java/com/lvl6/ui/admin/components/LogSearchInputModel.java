package com.lvl6.ui.admin.components;

import java.util.Date;

import org.apache.wicket.IClusterable;

public class LogSearchInputModel implements IClusterable {

	private static final long serialVersionUID = 1L;
	
	protected Date start;
	protected Date end;
	protected String level;
	protected String search;

	
	
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		this.search = search;
	}
	
	
}
