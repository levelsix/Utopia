package com.lvl6.ui.admin.components;

import java.util.Date;

import org.apache.wicket.IClusterable;

public class LogSearchInputModel implements IClusterable {

	private static final long serialVersionUID = 1L;
	
	protected Date start;
	protected Date end;
	protected String level;
	protected String playerId;
	protected String searchInput;
	protected Integer offset = 0;
	protected Integer show = 300;

	
	
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	
	
	public String getSearchInput() {
		return searchInput;
	}
	public void setSearchInput(String searchInput) {
		this.searchInput = searchInput;
	}
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
	
	
	
}
