package com.lvl6.utils.utilmethods;

import java.util.List;


public interface DeleteUtil {

	public abstract boolean deleteAvailableReferralCode(String referralCode);

	////@CacheEvict(value = "questIdToUserTasksCompletedForQuestForUserCache", key = "#userId")
	public abstract boolean deleteUserQuestInfoInTaskProgressAndCompletedTasks(
			int userId, int questId, int numTasks);

	////@CacheEvict(value = "questIdToUserDefeatTypeJobsCompletedForQuestForUserCache", key = "#userId")
	public abstract boolean deleteUserQuestInfoInDefeatTypeJobProgressAndCompletedDefeatTypeJobs(
			int userId, int questId, int numDefeatJobs);

	public abstract boolean deleteMarketplacePost(int mpId);

	/*@Caching(evict = {
			////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			//@CacheEvict(value = "specificUserStruct", key = "#userStructId") })*/
	public abstract boolean deleteUserStruct(int userStructId);
	
  public abstract boolean deleteUserEquip(int userEquipId);
  
  public abstract boolean deleteUserEquips(List<Integer> userEquipIds);
  
  public abstract boolean deleteUserClanDataRelatedToClanId(int clanId, int numRowsToDelete);

  public abstract boolean deleteClanWithClanId(int clanId);
  
  public abstract boolean deleteBlacksmithAttempt(int blacksmithId);

  public abstract boolean deleteUserClan(int userId, int clanId);

  public void deleteUserClansForUserExceptSpecificClan(int userId, int clanId);
  
  public abstract boolean deleteEquipEnhancements(List<Integer> equipEnhancementIds);
  
  public abstract boolean deleteEquipEnhancementFeeders(List<Integer> equipEnhancementFeederIds);
  
  public abstract int deleteAllUserCitiesForUser(int userId);
  
  public abstract int deleteAllUserQuestsForUser(int userId);
  
  public abstract int deleteAllUserQuestsCompletedDefeatTypeJobsForUser(int userId);
  
  public abstract int deleteAllUserQuestsCompletedTasksForUser(int userId);
  
  public abstract int deleteAllUserQuestsDefeatTypeJobProgressForUser(int userId);
  
  public abstract int deleteAllUserQuestsTaskProgress(int userId);
  
  public abstract int deleteAllUserTasksForUser(int userId);
}