package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;
import com.lvl6.stats.InAppPurchase;
import com.lvl6.stats.Spender;

@Component 
@DependsOn("gameServer") 
public class StatisticsRetrieveUtil {
	private static Logger log = LoggerFactory.getLogger(StatisticsRetrieveUtil.class);
	
	private JdbcTemplate jdbcTemplate;
	protected String iap = DBConstants.TABLE_IAP_HISTORY;

	@Resource
    public void setDataSource(DataSource dataSource) {
		log.info("Setting datasource and creating jdbcTemplate");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
	
	public int countLoginsToday() {
		String yesterday = formatDateToString(new DateTime().minusDays(1).toDate());
		return countLoginsSince(yesterday);
	}
	
	public int countLoginsThisWeek() {
		String yesterday = formatDateToString(new DateTime().minusDays(7).toDate());
		return countLoginsSince(yesterday);
	}

	////
	public int countLoginsSince(String formattedDate) {
		String queryString = "select count(*) from "+DBConstants.TABLE_USER+" where "+DBConstants.USER__LAST_LOGIN+" > '"+formattedDate+"'";
		log.info("Executing: {}", queryString);
		return jdbcTemplate.queryForInt(queryString);
	}

	//Date twenty4ago = new DateTime().minusDays(1).toDate();
	protected String formatDateToString(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		String formatted = format.format(date);
		return formatted;
	}
	
	//
	public Integer countPayingPlayers() {
		return jdbcTemplate.queryForInt("select count(*) from " + DBConstants.TABLE_USER + " where " + DBConstants.USER__ID +
		    " in (select " + DBConstants.IAP_HISTORY__USER_ID + " from " + DBConstants.TABLE_IAP_HISTORY + " where " + 
		    DBConstants.IAP_HISTORY__PREMIUMCUR_PURCHASED + " > 0)");
	}
	
	//
	public Long countInAppPurchases() {
		return jdbcTemplate.queryForLong("select count(*) from " + DBConstants.TABLE_IAP_HISTORY + " where " + DBConstants.IAP_HISTORY__PREMIUMCUR_PURCHASED + " > 0");
	}
	
	//
	public Long sumOfInAppPurchases() {
		return jdbcTemplate.queryForLong("select sum(" + DBConstants.IAP_HISTORY__CASH_SPENT + ") from " + DBConstants.TABLE_IAP_HISTORY);
	}
	
	//
	public Long countNumberKiipRewardsRedeemed() {
		return jdbcTemplate.queryForLong("select count(*) from " + DBConstants.TABLE_KIIP_REWARD_HISTORY);
	}
	
	//
	public Long countMarketplaceTransactions() {
		return jdbcTemplate.queryForLong("select count(*) from " + DBConstants.TABLE_MARKETPLACE_TRANSACTION_HISTORY);
	}
	
	//
	public Long countMarketplacePosts() {
		return jdbcTemplate.queryForLong("select count(*) from " + DBConstants.TABLE_MARKETPLACE);
	}
	
	//
	public Long sumOfSilverInWorld() {
		return jdbcTemplate.queryForLong("select sum(" + DBConstants.USER__COINS +") + sum(" + DBConstants.USER__VAULT_BALANCE + ") from " + DBConstants.TABLE_USER 
		    + " where " + DBConstants.USER__IS_FAKE + "=0");
	}
	
	
	//
	public Long sumOfDiamondsInWorld() {
		return jdbcTemplate.queryForLong("select sum(" + DBConstants.USER__DIAMONDS + ") from " + DBConstants.TABLE_USER + " where " + DBConstants.USER__IS_FAKE + "=0");
	}
	
	//
	public List<Spender> getTopSpenders(Integer limit){
	  final String amountSpent = "amount_spent";
	  final String userId = DBConstants.TABLE_USER+"."+DBConstants.USER__ID;
		List<Spender> spenders = this.jdbcTemplate.query(
		        "select " + DBConstants.IAP_HISTORY__USER_ID + ", sum(" + DBConstants.IAP_HISTORY__CASH_SPENT + ") as " + amountSpent + ", "+DBConstants.USER__NAME+" from " +
		            DBConstants.TABLE_IAP_HISTORY + ", "+DBConstants.TABLE_USER+" where "+ userId +"="+DBConstants.IAP_HISTORY__USER_ID+
		            " group by " + DBConstants.IAP_HISTORY__USER_ID + " order by sum(" + DBConstants.IAP_HISTORY__CASH_SPENT + ") desc limit "+limit,

		        new RowMapper<Spender>() {
		            public Spender mapRow(ResultSet rs, int rowNum) throws SQLException {
		            	Spender spender = new Spender();
		            	spender.setUserId(rs.getInt(DBConstants.IAP_HISTORY__USER_ID));
                  spender.setName(rs.getString(DBConstants.USER__NAME));
		            	spender.setAmountSpent(Math.ceil(rs.getDouble(amountSpent)));
		                return spender;
		            }
		        });
		return spenders;
	}
	
	//
	public List<InAppPurchase> getTopInAppPurchases(Integer limit){
		List<InAppPurchase> inAppPurchases = this.jdbcTemplate.query(
		        "select " + DBConstants.IAP_HISTORY__USER_ID + ", " + DBConstants.IAP_HISTORY__CASH_SPENT + ", " + DBConstants.IAP_HISTORY__PURCHASE_DATE + ", "+DBConstants.USER__NAME+
		        " from " + DBConstants.TABLE_IAP_HISTORY + ", "+DBConstants.TABLE_USER+" where "+DBConstants.TABLE_USER+"."+DBConstants.USER__ID+"="+DBConstants.IAP_HISTORY__USER_ID +
		        " order by " + DBConstants.IAP_HISTORY__PURCHASE_DATE + " desc limit "+limit,
		        new RowMapper<InAppPurchase>() {
		            public InAppPurchase mapRow(ResultSet rs, int rowNum) throws SQLException {
		            	InAppPurchase inAppPurchase = new InAppPurchase();
		            	inAppPurchase.setUserId(rs.getInt(DBConstants.IAP_HISTORY__USER_ID));
		            	inAppPurchase.setCashSpent(Math.ceil(rs.getDouble(DBConstants.IAP_HISTORY__CASH_SPENT)));
		            	inAppPurchase.setName(rs.getString(DBConstants.USER__NAME));
		            	inAppPurchase.setPurchasedDate(new Date(rs.getTimestamp(DBConstants.IAP_HISTORY__PURCHASE_DATE).getTime()));
		                return inAppPurchase;
		            }
		        });
		return inAppPurchases;
	}
	
}
