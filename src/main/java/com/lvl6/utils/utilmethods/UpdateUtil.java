package com.lvl6.utils.utilmethods;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lvl6.info.ClanTower;
import com.lvl6.info.CoordinatePair;
import com.lvl6.info.Task;
import com.lvl6.info.UserStruct;
import com.lvl6.proto.EventProto.MenteeFinishedQuestResponseProto.MenteeQuestType;
import com.lvl6.proto.InfoProto.ExpansionDirection;
import com.lvl6.proto.InfoProto.StructOrientation;
import com.lvl6.proto.InfoProto.UserClanStatus;

public interface UpdateUtil {

  /*@Caching(evict = {
      //@CacheEvict(value = "unredeemedAndRedeemedUserQuestsForUser", key = "#userId"),
      //@CacheEvict(value = "incompleteUserQuestsForUser", key = "#userId"),
      //@CacheEvict(value = "unredeemedUserQuestsForUser", key = "#userId") })*/
  public abstract boolean updateUserQuestsCoinsretrievedforreq(int userId,
      List<Integer> questIds, int coinGain);

  public abstract void updateNullifyDeviceTokens(Set<String> deviceTokens);

  /*
   * used when an expansion is complete
   */
  public abstract boolean updateUserExpansionNumexpansionsIsexpanding(int userId,
      int farLeftExpansionsChange, int farRightExpansionsChange, int nearLeftExpansionsChange, int nearRightExpansionsChange, 
      boolean isExpanding);

  /*
   * used for purchasing a city expansion
   */
  public abstract boolean updateUserExpansionLastexpandtimeLastexpanddirectionIsexpanding(
      int userId, Timestamp lastExpandTime,
      ExpansionDirection lastExpansionDirection, boolean isExpanding);

  /*@Caching(evict = {
      //@CacheEvict(value = "unredeemedAndRedeemedUserQuestsForUser", key = "#userId"),
      //@CacheEvict(value = "incompleteUserQuestsForUser", key = "#userId"),
      //@CacheEvict(value = "unredeemedUserQuestsForUser", key = "#userId") })*/
  public abstract boolean updateUserQuestIscomplete(int userId, int questId);

  /*@Caching(evict = {
      //@CacheEvict(value = "unredeemedAndRedeemedUserQuestsForUser", key = "#userId"),
      //@CacheEvict(value = "incompleteUserQuestsForUser", key = "#userId"),
      //@CacheEvict(value = "unredeemedUserQuestsForUser", key = "#userId") })*/
  public abstract boolean updateRedeemUserQuest(int userId, int questId);

  /*
   * changin orientation
   */
  /*@Caching(evict = {
      ////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
      ////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
      //@CacheEvict(value = "specificUserStruct", key = "#userStructId") })*/
  public abstract boolean updateUserStructOrientation(int userStructId,
      StructOrientation orientation);

  /*@Caching(evict = {
      //@CacheEvict(value = "unredeemedAndRedeemedUserQuestsForUser", key = "#userId"),
      //@CacheEvict(value = "incompleteUserQuestsForUser", key = "#userId"),
      //@CacheEvict(value = "unredeemedUserQuestsForUser", key = "#userId") })*/
  public abstract boolean updateUserQuestsSetCompleted(int userId,
      int questId, boolean setTasksCompleteTrue,
      boolean setDefeatTypeJobsCompleteTrue);

  /*
   * used for updating is_complete=true and last_retrieved to upgrade_time+minutestogain for a userstruct
   */
  public abstract boolean updateUserStructsLastretrievedpostupgradeIscompleteLevelchange(
      List<UserStruct> userStructs, int levelChange);

  /*
   * used for updating last retrieved and/or last upgrade user struct time and is_complete
   */
  /*@Caching(evict = {
      ////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
      ////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
      //@CacheEvict(value = "specificUserStruct", key = "#userStructId") })*/
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
  /*@Caching(evict = {
      ////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
      ////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
      //@CacheEvict(value = "specificUserStruct", key = "#userStructId") })*/
  public abstract boolean updateUserStructLastretrievedLastupgradeIscomplete(
      int userStructId, Timestamp lastRetrievedTime,
      Timestamp lastUpgradeTime, boolean isComplete);

  /*
   * used for updating last retrieved user struct times
   */
  /*@Caching(evict = {
      ////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
      ////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
      //@CacheEvict(value = "specificUserStruct", key = "#userStructId") })*/
  public abstract boolean updateUserStructsLastretrieved(
      Map<Integer, Timestamp> userStructIdsToLastRetrievedTime,
      Map<Integer, UserStruct> structIdsToUserStructs);

  /*
   * used for upgrading user structs level
   */
  /*@Caching(evict = {
      ////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
      ////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
      //@CacheEvict(value = "specificUserStruct", key = "#userStructId") })*/
  public abstract boolean updateUserStructLevel(int userStructId,
      int levelChange);

  /*
   * used for moving user structs
   */
  /*@Caching(evict = {
      ////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
      ////@CacheEvict(value = "structIdsToUserStructsForUser", allEntries = true),
      //@CacheEvict(value = "specificUserStruct", key = "#userStructId") })*/
  public abstract boolean updateUserStructCoord(int userStructId,
      CoordinatePair coordinates);

  /*
   * used for tasks
   */
  /*@Caching(evict = {
      //@CacheEvict(value = "cityIdToUserCityRankCache", key = "#userId"),
      //@CacheEvict(value = "currentCityRankForUserCache", key = "#userId+':'+#cityId") })*/
  public abstract boolean incrementCityRankForUserCity(int userId,
      int cityId, int increment);

  public abstract boolean updateClanOwnerDescriptionForClan(int clanId, int ownerId, String description);

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

  public abstract boolean updateUserEquipOwner(int userEquipId, int newOwnerId, String reason);

  public abstract boolean updateAbsoluteBlacksmithAttemptcompleteTimeofspeedup(int blacksmithId, Date timeOfSpeedup, boolean attemptComplete);

  public abstract boolean updateUsersClanId(Integer clanId, List<Integer> userIds);

  public abstract boolean updateUserClanStatus(int userId, int clanId, UserClanStatus status);

  public abstract boolean incrementNumberOfLockBoxesForLockBoxEvent(int userId, int eventId,
      int increment);

  public abstract boolean incrementQuantityForLockBoxItem(int userId, int itemId, int increment);
  
  public abstract boolean decrementLockBoxItemsForUser(Map<Integer, Integer> itemIdsToQuantity, int userId, int decrement);
  
  public abstract boolean decrementNumLockBoxesIncrementNumTimesCompletedForUser(int eventId, int userId, int decrement, boolean completed, Timestamp curTime);

  //public abstract boolean updateRedeemLockBoxItems(int eventId, int userId, List<Integer> lockBoxItemIds, boolean redeem);
  public abstract boolean updateRedeemLockBoxEvent(int eventId, int userId, boolean redeem);
  
  public boolean replaceUserBoss(int userId, int bossId, Date startTime, int currentHealth, 
      int currentLevel, int gemlessStreak);
  
  public boolean incrementCurrentTierLevelForClan(int clanId);
  public abstract boolean updateClanTowerOwnerAndOrAttacker(int clanTowerId, int ownerId, Date ownedStartTime, int ownerBattleWins, 
		  int attackerId, Date attackStartTime, int attackerBattleWins, Date lastRewardGiven, int battleId);
  
  public abstract boolean updateClanTowerBattleWins(int clanTowerId, int ownerId, int attackerId, boolean ownerWon, int amountToIncrementBattleWinsBy, int battleId, int ownerUserId, 
      int attackerUserId);
  
  public abstract boolean resetClanTowerOwnerOrAttacker(List<Integer> clanTowerOwnerOrAttackerIds, boolean resetOwner);
  
  public abstract boolean updateTowerHistory(List<ClanTower> towers, String reasonForEntry, List<Integer> winnerIds);
  
  public boolean updateUsersAddDiamonds(List<Integer> userIds, int diamonds) ;
  
  public boolean updateLeaderboardEventSetRewardGivenOut(int eventId);

  public abstract boolean updateUserBoosterItemsForOneUser(int userId, Map<Integer, Integer> userBoosterItemIdsToQuantities);
  
  public abstract boolean updateClanJoinTypeForClan(int clanId, boolean requestToJoinRequired);
  
  public abstract boolean updateMentorshipTerminate(int mentorshipId);
  
  public abstract boolean updateMentorshipQuestCompleteTime(int mentorshipId, Date timeCompleted, MenteeQuestType type);
  
  public abstract boolean updateUserCityGems(int userId, int cityId,
      Map<Integer, Integer> gemIdsToQuantities);
  
  public abstract boolean updateUserCityGem(int userId, int cityId,
      int gemId, int newQuantity);
  
  public abstract boolean incrementUserCityNumTimesRedeemedGems(int userId,
      int cityId, int newQuantity);
  
}
