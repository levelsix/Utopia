package com.lvl6.server.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.lvl6.events.RequestEvent;
import com.lvl6.events.request.BeginMentoringUserRequestEvent;
import com.lvl6.events.response.BeginMentoringUserResponseEvent;
import com.lvl6.info.Mentorship;
import com.lvl6.info.PrivateChatPost;
import com.lvl6.info.User;
import com.lvl6.misc.MiscMethods;
import com.lvl6.properties.ControllerConstants;
import com.lvl6.proto.EventProto.BeginMentoringUserRequestProto;
import com.lvl6.proto.EventProto.BeginMentoringUserResponseProto;
import com.lvl6.proto.EventProto.BeginMentoringUserResponseProto.BeginMentoringUserStatus;
import com.lvl6.proto.EventProto.BeginMentoringUserResponseProto.Builder;
import com.lvl6.proto.EventProto.MenteeFinishedQuestResponseProto.MenteeQuestType;
import com.lvl6.proto.InfoProto.MentorshipProto;
import com.lvl6.proto.InfoProto.MinimumUserProto;
import com.lvl6.proto.InfoProto.PrivateChatPostProto;
import com.lvl6.proto.ProtocolsProto.EventProtocolRequest;
import com.lvl6.retrieveutils.MentorshipRetrieveUtils;
import com.lvl6.server.EventWriter;
import com.lvl6.utils.CreateInfoProtoUtils;
import com.lvl6.utils.RetrieveUtils;
import com.lvl6.utils.utilmethods.InsertUtils;

@Component @DependsOn("gameServer")public class BeginMentoringUserController extends EventController {
  private static Logger log = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());

  @Resource
  protected EventWriter eventWriter;

  public EventWriter getEventWriter() {
    return eventWriter;
  }

  public void setEventWriter(EventWriter eventWriter) {
    this.eventWriter = eventWriter;
  }
  
  public BeginMentoringUserController() {
    numAllocatedThreads = 1;
  }
  
  @Override
  public RequestEvent createRequestEvent() {
    return new BeginMentoringUserRequestEvent();
  }

  @Override
  public EventProtocolRequest getEventType() {
    return EventProtocolRequest.C_BEGIN_MENTORING_USER_EVENT;
  }

  @Override
  protected void processRequestEvent(RequestEvent event) throws Exception {
    BeginMentoringUserRequestProto reqProto = 
        ((BeginMentoringUserRequestEvent) event).getBeginMentoringUserRequestProto();
    
    //stuff client sent
    MinimumUserProto senderProto = reqProto.getSender();
    int menteeId = reqProto.getMenteeId();
    Date clientTime = new Date(reqProto.getClientTime());
    
    //response to send back to client
    BeginMentoringUserResponseProto.Builder resBuilder = BeginMentoringUserResponseProto.newBuilder();
    resBuilder.setSender(senderProto);
    resBuilder.setStatus(BeginMentoringUserStatus.FAIL_OTHER);
    
    //lock on new user? to prevent multiple mentors from getting the guy?
    server.lockPlayer(menteeId, this.getClass().getSimpleName());
    try {
      int mentorId = senderProto.getUserId();
      List<Integer> userIds = new ArrayList<Integer>();
      userIds.add(mentorId);
      userIds.add(menteeId);
      
      Map<Integer, User> idsToUsers = RetrieveUtils.userRetrieveUtils().getUsersByIds(userIds); 
      
      boolean legit = checkLegit(resBuilder, mentorId, menteeId, idsToUsers, clientTime);
      
      boolean saveSuccessful = false;
      List<Integer> mentorIds = new ArrayList<Integer>();
      if (legit) {
        saveSuccessful = writeChangesToDb(mentorId, menteeId, idsToUsers, clientTime, mentorIds);
      }
      
      if (saveSuccessful) {
        int mentorshipId = mentorIds.get(0);
        resBuilder.setStatus(BeginMentoringUserStatus.SUCCESS);
        //write some private chats
        setInitialMessages(resBuilder, mentorId, menteeId, idsToUsers, clientTime);
        
        //set the mentorship object
        Mentorship ms = MentorshipRetrieveUtils.getActiveMentorshipForMentee(mentorshipId);
        MentorshipProto msp = CreateInfoProtoUtils.createMentorshipProto(ms);
        resBuilder.setMentorship(msp);
        
        //set if this is alliance or legion
        User mentor = idsToUsers.get(mentorId);
        resBuilder.setIsGood(MiscMethods.checkIfGoodSide(mentor.getType()));
      }
      
      //write to mentor
      BeginMentoringUserResponseProto bmurp = resBuilder.build();
      BeginMentoringUserResponseEvent bmure = new BeginMentoringUserResponseEvent(mentorId);
      bmure.setTag(event.getTag());
      bmure.setBeginMentoringUserResponseProto(bmurp);
      eventWriter.processGlobalChatResponseEvent(bmure);
      //write something to mentee?
      
    } catch (Exception e) {
      try {
        resBuilder.setStatus(BeginMentoringUserStatus.FAIL_OTHER);
        BeginMentoringUserResponseProto bmurp = resBuilder.build();
        BeginMentoringUserResponseEvent bmure =
            new BeginMentoringUserResponseEvent(senderProto.getUserId());
        bmure.setTag(event.getTag());
        bmure.setBeginMentoringUserResponseProto(bmurp);
        server.writeAPNSNotificationOrEvent(bmure);
      } catch (Exception e2) {
        
      }
      
    } finally {
      server.unlockPlayer(menteeId, this.getClass().getSimpleName());
    }
  }
  
  private boolean checkLegit(Builder resBuilder, int mentorId, int menteeId,
      Map<Integer, User> idsToUsers, Date clientTime) {
    if (null == idsToUsers || idsToUsers.isEmpty() || idsToUsers.size() != 2) {
      resBuilder.setStatus(BeginMentoringUserStatus.FAIL_OTHER);
      log.error("unexpected error: no users exist with ids. mentorId=" + mentorId +
          "; menteeId=" + menteeId);
      return false;
    }
    User mentor = idsToUsers.get(mentorId);
    User mentee = idsToUsers.get(menteeId);
    if (MiscMethods.checkIfGoodSide(mentor.getType()) == MiscMethods.checkIfGoodSide(mentee.getType())) {
      resBuilder.setStatus(BeginMentoringUserStatus.FAIL_WRONG_SIDE);
      log.error("unexpected error. mentor, mentee wrong sides. mentor=" + mentor + ", mentee=" + mentee);
      return false;
    }
    
    if (!mentor.isMentor()) {
      resBuilder.setStatus(BeginMentoringUserStatus.FAIL_NOT_MENTOR);
      log.error("unexpected error: nonMentor=" + mentor + ", mentee=" + mentee);
      return false;
    }
    
    //check potential mentee has no mentor 
    Map<Integer, List<Mentorship>> menteeIdsToMentorships = MentorshipRetrieveUtils
        .getActiveMenteeIdToMentorshipsForMentorOrMentee(mentorId, menteeId);
    
    if (menteeIdsToMentorships.containsKey(menteeId)) {
      resBuilder.setStatus(BeginMentoringUserStatus.FAIL_ALREADY_BEING_MENTORED);
      log.error("unexpected error: user already has a mentor. mentee=" + mentee);
      return false;
    }
    
    //check that the mentor is under the max number of mentees allowed
    int numMentees = menteeIdsToMentorships.size();
    int limit = ControllerConstants.MENTORSHIPS__MAX_MENTEE_LIMIT;
    if (numMentees >= limit) {
      resBuilder.setStatus(BeginMentoringUserStatus.FAIL_ALREADY_AT_MAX_NUM_MENTEES);
      log.error("user error: user already has max mentees allowed. limit=" + limit +
          "numMmentees=" + numMentees);
      return false;
    }
    
    //check mentor isn't spamming to acquire mentees
    Date lastMentorshipStartTime = getLastMentorshipStartTime(menteeIdsToMentorships);
    long timeMentorCanAcquireAnother = lastMentorshipStartTime.getTime() +
        (60 * 1000 * ControllerConstants.MENTORSHIPS__MINUTES_UNTIL_NEXT_MENTORSHIP);
    if (clientTime.getTime() < timeMentorCanAcquireAnother) {
      resBuilder.setStatus(BeginMentoringUserStatus.FAIL_TOO_EARLY_TO_ACQUIRE_MENTEE);
      log.error("user error: mentor is being too greedy tryung to acqire another" +
      		" mentee. mentor=" + mentor + "; lastMentorshipTime=" + lastMentorshipStartTime +
      		"; clientTime=" + clientTime);
      return false;
    }
    
    return true;
  }
  
  private Date getLastMentorshipStartTime(Map<Integer, List<Mentorship>> idsToMentorships) {
    Date latest = null;
    //loop through, ideally, one element lists and select element w/
    //most recent start time
    for (List<Mentorship> ms : idsToMentorships.values()) {
      if (ms.size() > 1) {
        log.error("unexpected error: too many mentors for user. mentorships=" +
            MiscMethods.shallowListToString(ms));
      }
      Mentorship m = ms.get(0);
      Date startTime = m.getStartTime();
      if (null == latest) {
        latest = startTime;
        continue;
      }
      if (startTime.getTime() > latest.getTime()) {
        latest = startTime;
      }
    }
    return latest;
  }
  
  private boolean writeChangesToDb(int mentorId, int menteeId,
      Map<Integer, User> idsToUsers, Date clientTime,
      List<Integer> mentorshipIds) {
    
    User mentee = idsToUsers.get(menteeId);
    //CHECK TO SEE IF THE USER FINISHED ANY OTHER MENTOR QUESTS
    List<MenteeQuestType> typeList = new ArrayList<MenteeQuestType>();
    if (ControllerConstants.NOT_SET != mentee.getClanId()) {
      typeList.add(MenteeQuestType.JOINED_A_CLAN);
    }
    if (mentee.getLevel() >=
        ControllerConstants.MENTORSHIPS__MENTEE_LEVEL_FOR_QUEST) {
      typeList.add(MenteeQuestType.LEVELED_UP_TO_LEVEL_N);
    }
    //write to Mentorships table
    int mentorshipId = InsertUtils.get().insertIntoMentorships(mentorId, menteeId,
        clientTime, typeList);
    
    mentorshipIds.add(mentorshipId);
    
    if (ControllerConstants.NOT_SET == mentorshipId) {
      return false;
    } else {
      return true;
    }
  }
  
  private void setInitialMessages(Builder resBuilder, int mentorId, int menteeId,
      Map<Integer, User> idsToUsers, Date clientTime) {
    //setting the initial posts mentee receives from mentor
    List<PrivateChatPost> posts = createPosts(mentorId, menteeId, clientTime);
    
    if (posts.isEmpty()) {
      log.error("unexpected error: could not create initial private chats between" +
      		" mentor and mentee." + MiscMethods.shallowMapToString(idsToUsers));
      return;
    }
    User mentor = idsToUsers.get(mentorId);
    User mentee = idsToUsers.get(menteeId);
    Map<Integer, MinimumUserProto> idsToMups = 
        new HashMap<Integer, MinimumUserProto>(); 
    MinimumUserProto mentorMup = CreateInfoProtoUtils
        .createMinimumUserProtoFromUser(mentor);
    MinimumUserProto menteeMup = CreateInfoProtoUtils
        .createMinimumUserProtoFromUser(mentee);
    
    idsToMups.put(mentorId, mentorMup);
    idsToMups.put(menteeId, menteeMup);
    List<PrivateChatPostProto> pcpPosts = CreateInfoProtoUtils.
        createPrivateChatPostProtoFromPrivateChatPostsAndProtos(posts, idsToMups);
    
    resBuilder.addAllInitialMessages(pcpPosts);
  }
  
  private List<PrivateChatPost> createPosts(int mentorId, int menteeId, Date clientTime) {
    List<PrivateChatPost> returnValue = new ArrayList<PrivateChatPost>();
    
    int numCopies = 2;
    List<Integer> posterIds = new ArrayList<Integer>(
        Collections.nCopies(numCopies, mentorId));
    List<Integer> recipientIds = new ArrayList<Integer>(
        Collections.nCopies(numCopies, menteeId));
    
    String content1 = ControllerConstants.MENTORSHIPS_INITIAL_MESSAGE_ONE;
    String content2 = ControllerConstants.MENTORSHIPS_INITIAL_MESSAGE_TWO;
    List<String> contents = new ArrayList<String>();
    contents.add(content1);
    contents.add(content2);
    
    List<Date> timeOfPosts = new ArrayList<Date>();
    timeOfPosts.add(clientTime);
    //space out posts
    Date clientTime2 = new Date(clientTime.getTime() + 2000);
    timeOfPosts.add(clientTime2);
    
    List<Integer> ids = InsertUtils.get().insertIntoPrivateChatPosts(posterIds,
        recipientIds, contents, timeOfPosts);
    
    if (ids.size() != 2) {
      log.error("unexpected error: could not create default mentor messages");
      return returnValue;
    }
    int id1 = ids.get(0); 
    int id2 = ids.get(1);
    
    PrivateChatPost p1 = new PrivateChatPost(id1, mentorId, menteeId, clientTime, content1);
    PrivateChatPost p2 = new PrivateChatPost(id2, mentorId, menteeId, clientTime2, content2);
    
    returnValue.add(p1);
    returnValue.add(p2);
    return returnValue;
  }
}