package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class Mentorship implements Serializable {
  private static final long serialVersionUID = -5277597697514476946L;
  private int id;
  private int mentorId;
  private int menteeId;
  private Date startTime;
  private Date questOneCompleteTime;
  private Date questTwoCompleteTime;
  private Date questThreeCompleteTime;
  private Date questFourCompleteTime;
  private Date questFiveCompleteTime;
  private boolean isDropped;
  
  public Mentorship(int id, int mentorId, int menteeId, Date startTime,
      Date questOneCompleteTime, Date questTwoCompleteTime,
      Date questThreeCompleteTime, Date questFourCompleteTime,
      Date questFiveCompleteTime, boolean isDropped) {
    super();
    this.id = id;
    this.mentorId = mentorId;
    this.menteeId = menteeId;
    this.startTime = startTime;
    this.questOneCompleteTime = questOneCompleteTime;
    this.questTwoCompleteTime = questTwoCompleteTime;
    this.questThreeCompleteTime = questThreeCompleteTime;
    this.questFourCompleteTime = questFourCompleteTime;
    this.questFiveCompleteTime = questFiveCompleteTime;
    this.isDropped = isDropped;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getMentorId() {
    return mentorId;
  }

  public void setMentorId(int mentorId) {
    this.mentorId = mentorId;
  }

  public int getMenteeId() {
    return menteeId;
  }

  public void setMenteeId(int menteeId) {
    this.menteeId = menteeId;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getQuestOneCompleteTime() {
    return questOneCompleteTime;
  }

  public void setQuestOneCompleteTime(Date questOneCompleteTime) {
    this.questOneCompleteTime = questOneCompleteTime;
  }

  public Date getQuestTwoCompleteTime() {
    return questTwoCompleteTime;
  }

  public void setQuestTwoCompleteTime(Date questTwoCompleteTime) {
    this.questTwoCompleteTime = questTwoCompleteTime;
  }

  public Date getQuestThreeCompleteTime() {
    return questThreeCompleteTime;
  }

  public void setQuestThreeCompleteTime(Date questThreeCompleteTime) {
    this.questThreeCompleteTime = questThreeCompleteTime;
  }

  public Date getQuestFourCompleteTime() {
    return questFourCompleteTime;
  }

  public void setQuestFourCompleteTime(Date questFourCompleteTime) {
    this.questFourCompleteTime = questFourCompleteTime;
  }

  public Date getQuestFiveCompleteTime() {
    return questFiveCompleteTime;
  }

  public void setQuestFiveCompleteTime(Date questFiveCompleteTime) {
    this.questFiveCompleteTime = questFiveCompleteTime;
  }

  public boolean isDropped() {
    return isDropped;
  }

  public void setDropped(boolean isDropped) {
    this.isDropped = isDropped;
  }

  @Override
  public String toString() {
    return "Mentorship [id=" + id + ", mentorId=" + mentorId + ", menteeId="
        + menteeId + ", startTime=" + startTime + ", questOneCompleteTime="
        + questOneCompleteTime + ", questTwoCompleteTime="
        + questTwoCompleteTime + ", questThreeCompleteTime="
        + questThreeCompleteTime + ", questFourCompleteTime="
        + questFourCompleteTime + ", questFiveCompleteTime="
        + questFiveCompleteTime + ", isDropped=" + isDropped + "]";
  }
  
}