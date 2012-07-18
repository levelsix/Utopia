package com.lvl6.utils.utilmethods;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import com.lvl6.info.BlacksmithAttempt;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Location;
import com.lvl6.info.MarketplacePost;
import com.lvl6.info.User;
import com.lvl6.proto.InfoProto.BattleResult;
import com.lvl6.proto.InfoProto.MarketplacePostType;
import com.lvl6.proto.InfoProto.UserType;

public interface InsertUtil {

//	public abstract CacheManager getCache();
//
//	public abstract void setCache(CacheManager cache);


	@Caching(evict = {
			@CacheEvict(value = "userEquipsForUser", key = "#userId"),
			@CacheEvict(value = "equipsToUserEquipsForUser", key = "#userId"),
			@CacheEvict(value = "userEquipsWithEquipId", key = "#userId+':'+#equipId") })
	public abstract int insertUserEquip(int userId, int equipId, int level);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.lvl6.utils.utilmethods.InsertUtil#insertAviaryAndCarpenterCoords(int,
	 * com.lvl6.info.CoordinatePair, com.lvl6.info.CoordinatePair)
	 */
	public abstract boolean insertAviaryAndCarpenterCoords(int userId,
			CoordinatePair aviary, CoordinatePair carpenter);

	public abstract boolean insertAdcolonyRecentHistory(int userId,
			Timestamp timeOfReward, int diamondsEarned, String digest);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.lvl6.utils.utilmethods.InsertUtil#insertBattleHistory(int, int,
	 * com.lvl6.proto.InfoProto.BattleResult, java.util.Date, int, int, int)
	 */
	public abstract boolean insertBattleHistory(int attackerId, int defenderId,
			BattleResult result, Date battleCompleteTime, int coinsStolen,
			int stolenEquipId, int expGained, int stolenEquipLevel);

	public abstract boolean insertUnredeemedUserQuest(int userId, int questId,
			boolean hasNoRequiredTasks, boolean hasNoRequiredDefeatTypeJobs);

	/* used for quest defeat type jobs */
	public abstract boolean insertCompletedDefeatTypeJobIdForUserQuest(
			int userId, int dtjId, int questId);

	/* used for quest tasks */
	public abstract boolean insertCompletedTaskIdForUserQuest(int userId,
			int taskId, int questId);

	public abstract boolean insertUserStructJustBuilt(int userId, int structId,
			Timestamp timeOfStructPurchase, Timestamp timeOfStructBuild,
			CoordinatePair structCoords);

	/*
	 * returns the id of the userstruct, -1 if none
	 */
	public abstract int insertUserStruct(int userId, int structId,
			CoordinatePair coordinates, Timestamp timeOfPurchase);

	public abstract boolean insertIAPHistoryElem(JSONObject appleReceipt,
			int diamondChange, User user, double cashCost);

	public abstract boolean insertMarketplaceItem(int posterId,
      MarketplacePostType postType, int postedEquipId, int diamondCost,
      int coinCost, Timestamp timeOfPost, int equipLevel);

	public abstract boolean insertMarketplaceItemIntoHistory(
			MarketplacePost mp, int buyerId);

	public abstract boolean insertReferral(int referrerId, int referredId,
			int coinsGivenToReferrer);

	// returns -1 if error
	public abstract int insertUser(String udid, String name, UserType type,
			Location location, String deviceToken, String newReferCode,
			int level, int attack, int defense, int energy, int health,
			int stamina, int experience, int coins, int diamonds,
			Integer weaponEquipped, Integer armorEquipped,
			Integer amuletEquipped, boolean isFake);

	/*
	 * returns the id of the post, -1 if none
	 */
	public abstract int insertPlayerWallPost(int posterId, int wallOwnerId,
			String content, Timestamp timeOfPost);

	public abstract boolean insertKiipHistory(int userId, Timestamp clientTime,
			String content, String signature, int quantity, String transactionId);

  public abstract int insertForgeAttemptIntoBlacksmith(int userId, int equipId,
      int goalLevel, boolean paidToGuarantee, Timestamp startTime,
      int diamondCostForGuarantee, Timestamp timeOfSpeedup, boolean attemptComplete);

  public abstract boolean insertForgeAttemptIntoBlacksmithHistory(BlacksmithAttempt ba, boolean successfulForge);
}