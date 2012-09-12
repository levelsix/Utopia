package com.lvl6.utils.utilmethods;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

public interface DeleteUtil {

	public abstract boolean deleteAvailableReferralCode(String referralCode);

	@CacheEvict(value = "questIdToUserTasksCompletedForQuestForUserCache", key = "#userId")
	public abstract boolean deleteUserQuestInfoInTaskProgressAndCompletedTasks(
			int userId, int questId, int numTasks);

	@CacheEvict(value = "questIdToUserDefeatTypeJobsCompletedForQuestForUserCache", key = "#userId")
	public abstract boolean deleteUserQuestInfoInDefeatTypeJobProgressAndCompletedDefeatTypeJobs(
			int userId, int questId, int numDefeatJobs);

	public abstract boolean deleteMarketplacePost(int mpId);

	@Caching(evict = {
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "specificUserStruct", key = "#userStructId") })
	public abstract boolean deleteUserStruct(int userStructId);
	
  public abstract boolean deleteUserEquip(int userEquipId);
  
  public abstract boolean deleteUserClanDataRelatedToClanId(int clanId, int numRowsToDelete);

  public abstract boolean deleteClanWithClanId(int clanId);
  
  public abstract boolean deleteBlacksmithAttempt(int blacksmithId);

}