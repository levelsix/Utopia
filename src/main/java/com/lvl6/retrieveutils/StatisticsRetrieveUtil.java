package com.lvl6.retrieveutils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

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
	
	
	public Integer countPayingPlayers() {
		return jdbcTemplate.queryForInt("select count(*) from users where id in (select user_id from iap_history where premiumcur_purchased > 0)");
	}
	
	public Long countInAppPurchases() {
		return jdbcTemplate.queryForLong("select count(*) from iap_history where premiumcur_purchased > 0");
	}
	
	public Long sumOfInAppPurchases() {
		return jdbcTemplate.queryForLong("select sum(cash_spent) from iap_history");
	}
	
	public Long countNumberKiipRewardsRedeemed() {
		return jdbcTemplate.queryForLong("select count(*) from kiip_reward_history");
	}
	
	public Long countMarketplaceTransactions() {
		return jdbcTemplate.queryForLong("select count(*) from marketplace_transaction_history");
	}
	
	public Long countMarketplacePosts() {
		return jdbcTemplate.queryForLong("select count(*) from marketplace");
	}
	
	public Long sumOfSilverInWorld() {
		return jdbcTemplate.queryForLong("select sum(coins) + sum(vault_balance) from users where is_fake=0");
	}
	
	public Long sumOfDiamondsInWorld() {
		return jdbcTemplate.queryForLong("select sum(diamonds) from users where is_fake=0");
	}
	
	public List<Spender> getTopSpenders(Integer limit){
		List<Spender> spenders = this.jdbcTemplate.query(
		        "select user_id, sum(cash_spent) as amount_spent from iap_history group by user_id order by sum(cash_spent) desc limit "+limit,
		        new RowMapper<Spender>() {
		            public Spender mapRow(ResultSet rs, int rowNum) throws SQLException {
		            	Spender spender = new Spender();
		            	spender.setUserId(rs.getInt("user_id"));
		            	spender.setAmountSpent(rs.getDouble("amount_spent"));
		                return spender;
		            }
		        });
		return spenders;
	}
	
	
	public Collection<InAppPurchase> getTopInAppPurchases(Integer limit){
		List<InAppPurchase> inAppPurchases = this.jdbcTemplate.query(
		        "select user_id, cash_spent, purchase_date from iap_history order by purchase_date desc limit "+limit,
		        new RowMapper<InAppPurchase>() {
		            public InAppPurchase mapRow(ResultSet rs, int rowNum) throws SQLException {
		            	InAppPurchase inAppPurchase = new InAppPurchase();
		            	inAppPurchase.setUserId(rs.getInt("user_id"));
		            	inAppPurchase.setCashSpent(rs.getDouble("cash_spent"));
		            	inAppPurchase.setPurchasedDate(rs.getDate("purchase_date"));
		                return inAppPurchase;
		            }
		        });
		return inAppPurchases;
	}
	
}
