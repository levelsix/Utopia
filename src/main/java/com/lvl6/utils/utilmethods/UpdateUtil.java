package com.lvl6.utils.utilmethods;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Task;
import com.lvl6.info.UserStruct;
import com.lvl6.proto.InfoProto.CritStructType;
import com.lvl6.proto.InfoProto.ExpansionDirection;
import com.lvl6.proto.InfoProto.StructOrientation;

public interface UpdateUtil {

	@Caching(evict = {
			@CacheEvict(value = "unredeemedAndRedeemedUserQuestsForUser", key = "#userId"),
			@CacheEvict(value = "incompleteUserQuestsForUser", key = "#userId"),
			@CacheEvict(value = "unredeemedUserQuestsForUser", key = "#userId") })
	public abstract boolean updateUserQuestsCoinsretrievedforreq(int userId,
			List<Integer> questIds, int coinGain);

	public abstract void updateNullifyDeviceTokens(Set<String> deviceTokens);

	/*
	 * used when an expansion is complete
	 */
	public abstract boolean updateUserExpansionNumexpansionsIsexpanding(
			int userId, int farLeftExpansionsChange,
			int farRightExpansionsChange, boolean isExpanding);

	/*
	 * used for purchasing a city expansion
	 */
	public abstract boolean updateUserExpansionLastexpandtimeLastexpanddirectionIsexpanding(
			int userId, Timestamp lastExpandTime,
			ExpansionDirection lastExpansionDirection, boolean isExpanding);

	@Caching(evict = {
			@CacheEvict(value = "unredeemedAndRedeemedUserQuestsForUser", key = "#userId"),
			@CacheEvict(value = "incompleteUserQuestsForUser", key = "#userId"),
			@CacheEvict(value = "unredeemedUserQuestsForUser", key = "#userId") })
	public abstract boolean updateUserQuestIscomplete(int userId, int questId);

	@Caching(evict = {
			@CacheEvict(value = "unredeemedAndRedeemedUserQuestsForUser", key = "#userId"),
			@CacheEvict(value = "incompleteUserQuestsForUser", key = "#userId"),
			@CacheEvict(value = "unredeemedUserQuestsForUser", key = "#userId") })
	public abstract boolean updateRedeemUserQuest(int userId, int questId);

	/*
	 * changin orientation
	 */
	@Caching(evict = {
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "specificUserStruct", key = "#userStructId") })
	public abstract boolean updateUserStructOrientation(int userStructId,
			StructOrientation orientation);

	@Caching(evict = {
			@CacheEvict(value = "unredeemedAndRedeemedUserQuestsForUser", key = "#userId"),
			@CacheEvict(value = "incompleteUserQuestsForUser", key = "#userId"),
			@CacheEvict(value = "unredeemedUserQuestsForUser", key = "#userId") })
	public abstract boolean updateUserQuestsSetCompleted(int userId,
			int questId, boolean setTasksCompleteTrue,
			boolean setDefeatTypeJobsCompleteTrue);

	public abstract boolean updateUserCritstructOrientation(int userId,
			StructOrientation orientation, CritStructType critStructType);

	/*
	 * used for moving user structs
	 */
	public abstract boolean updateUserCritstructCoord(int userId,
			CoordinatePair coordinates, CritStructType critStructType);

	/*
	 * used for updating is_complete=true and last_retrieved to upgrade_time+minutestogain for a userstruct
	 */
	public abstract boolean updateUserStructsLastretrievedpostupgradeIscompleteLevelchange(
			List<UserStruct> userStructs, int levelChange);

	/*
	 * used for updating last retrieved and/or last upgrade user struct time and is_complete
	 */
	@Caching(evict = {
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "specificUserStruct", key = "#userStructId") })
	public abstract boolean updateUserStructLastretrievedIscompleteLevelchange(
			int userStructId, Timestamp lastRetrievedTime, boolean isComplete,
			int levelChange);

	/*
	 * used for updating is_complete=true and last_retrieved to purchased_time+minutestogain for a userstruct
	 */
	public abstract boolean updateUserStructsLastretrievedpostbuildIscomplete(
			List<UserStruct> userStructs);

	/*
	 * used for updating last retrieved and/or last upgrade user struct time and is_complete
	 */
	@Caching(evict = {
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "specificUserStruct", key = "#userStructId") })
	public abstract boolean updateUserStructLastretrievedLastupgradeIscomplete(
			int userStructId, Timestamp lastRetrievedTime,
			Timestamp lastUpgradeTime, boolean isComplete);

	/*
	 * used for updating last retrieved user struct time
	 */
	@Caching(evict = {
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "specificUserStruct", key = "#userStructId") })
	public abstract boolean updateUserStructLastretrieved(int userStructId,
			Timestamp lastRetrievedTime);

	/*
	 * used for upgrading user structs level
	 */
	@Caching(evict = {
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "specificUserStruct", key = "#userStructId") })
	public abstract boolean updateUserStructLevel(int userStructId,
			int levelChange);

	/*
	 * used for moving user structs
	 */
	@Caching(evict = {
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
			@CacheEvict(value = "specificUserStruct", key = "#userStructId") })
	public abstract boolean updateUserStructCoord(int userStructId,
			CoordinatePair coordinates);
	
	/*
   * used for tasks
   */
  @Caching(evict = {
      @CacheEvict(value = "cityIdToUserCityRankCache", key = "#userId"),
      @CacheEvict(value = "currentCityRankForUserCache", key = "#userId+':'+#cityId") })
  public abstract boolean incrementCityRankForUserCity(int userId,
      int cityId, int increment);

	/*
	 * used for tasks
	 */
	public abstract boolean incrementTimesCompletedInRankForUserTask(
			int userId, int taskId, int increment);

	public abstract boolean incrementUserQuestDefeatTypeJobProgress(int userId,
			int questId, int defeatTypeJobId, int increment);

	public abstract boolean incrementUserQuestTaskProgress(int userId,
			int questId, int taskId, int increment);

	public abstract boolean resetTimesCompletedInRankForUserTasksInCity(
			int userId, List<Task> tasksInCity);
	
	public abstract boolean updateUserEquipOwner(int userEquipId, int newOwnerId);

  public abstract boolean updateAbsoluteBlacksmithAttemptcompleteTimeofspeedup(int blacksmithId, Date timeOfSpeedup, boolean attemptComplete);
  
  public abstract boolean updateUsersClanId(Integer clanId, List<Integer> userIds);

}