package com.lvl6.retrieveutils;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.lvl6.properties.DBConstants;

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
	
	//TODO top 20 spenders
	//TODO last 10 purchases
	
}
