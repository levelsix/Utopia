package com.lvl6.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import com.lvl6.info.AdminChatPost;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.properties.DBConstants;

public class AdminChatUtil {
	
	private static Logger log = LoggerFactory.getLogger(AdminChatUtil.class);
	
	private JdbcTemplate jdbcTemplate;
	protected String iap = DBConstants.TABLE_IAP_HISTORY;

	@Resource
    public void setDataSource(DataSource dataSource) {
		log.info("Setting datasource and creating jdbcTemplate");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
	
	public List<AdminChatPost> getMessagesToAndFromAdmin(int offset, int limit){
		String query = "SELECT "
				+"chat."+DBConstants.PRIVATE_CHAT_POSTS__ID+"," 
				+"chat."+DBConstants.PRIVATE_CHAT_POSTS__POSTER_ID+","
				+"chat."+DBConstants.PRIVATE_CHAT_POSTS__RECIPIENT_ID+","
				+"chat."+DBConstants.PRIVATE_CHAT_POSTS__TIME_OF_POST+","
				+"chat."+DBConstants.PRIVATE_CHAT_POSTS__CONTENT+","
				+" FROM " 
				+DBConstants.TABLE_PRIVATE_CHAT_POSTS
				+" as chat "
				+" where (chat."+DBConstants.PRIVATE_CHAT_POSTS__RECIPIENT_ID
				+"="+ControllerConstants.STARTUP__ADMIN_CHAT_USER_ID
				+" or chat."+DBConstants.PRIVATE_CHAT_POSTS__POSTER_ID
				+"="+ControllerConstants.STARTUP__ADMIN_CHAT_USER_ID
				+") order by chat."+DBConstants.PRIVATE_CHAT_POSTS__TIME_OF_POST
				+" DESC" +
				" OFFSET="+offset+
				" LIMIT="+limit;
		List<AdminChatPost> msgs = jdbcTemplate.query(query, new RowMapper<AdminChatPost>() {
			@Override
			public AdminChatPost mapRow(ResultSet rs, int index) throws SQLException {
				return new AdminChatPost(rs.getInt(DBConstants.PRIVATE_CHAT_POSTS__ID),
						rs.getInt(DBConstants.PRIVATE_CHAT_POSTS__POSTER_ID),
						rs.getInt(DBConstants.PRIVATE_CHAT_POSTS__RECIPIENT_ID),
						rs.getDate(DBConstants.PRIVATE_CHAT_POSTS__TIME_OF_POST),
						rs.getString(DBConstants.PRIVATE_CHAT_POSTS__CONTENT));
			}
		});
		return addUsernames(msgs);
	}
	
	
	protected List<AdminChatPost> addUsernames(List<AdminChatPost> msgs){
		List<Integer> users = new ArrayList<Integer>();
		for(AdminChatPost msg : msgs) {
			if(msg.getPosterId() != ControllerConstants.STARTUP__ADMIN_CHAT_USER_ID) {
				users.add(msg.getPosterId());
			}else {
				users.add(msg.getRecipientId());
			}
		}
		String query = "select "+DBConstants.USER__ID+", "+DBConstants.USER__NAME+
				" from "+DBConstants.TABLE_USER+
				" where "+DBConstants.USER__ID+
				" in (?)";
		Map<Integer, String> usernames = (Map<Integer, String> ) jdbcTemplate.query(query, new Object[] {users}, new ResultSetExtractor<Map<Integer, String>>(){
			@Override
			public Map<Integer, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
				Map<Integer, String> usernames = new LinkedHashMap<Integer, String>();
				while(rs.next()) {
					usernames.put(rs.getInt(DBConstants.USER__ID), rs.getString(DBConstants.USER__NAME));
				}
				return usernames;
			}
		});
		for(AdminChatPost msg : msgs) {
			if(msg.getPosterId() != ControllerConstants.STARTUP__ADMIN_CHAT_USER_ID) {
				msg.setUsername(usernames.get(msg.getPosterId()));
			}else {
				msg.setUsername(usernames.get(msg.getRecipientId()));
			}
		}
		return msgs;
	}
	
	
	
	
}
