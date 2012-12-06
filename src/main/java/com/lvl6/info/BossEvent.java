package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class BossEvent implements Serializable {
  private static final long serialVersionUID = 828184315386406610L;
  private int id;
  private int cityId;
  private Date startDate;
  private Date endDate;
  private String eventName;
  private String headerImage;
  private int leftEquipId;
  private String leftTag;
  private int middleEquipId;
  private String middleTag;
  private int rightEquipId;
  private String rightTag;
  private String infoDescription;
  
  public BossEvent(int id, int cityId, Date startDate, Date endDate,
      String eventName, String headerImage, int leftEquipId, String leftTag,
      int middleEquipId, String middleTag, int rightEquipId, String rightTag,
      String infoDescription) {
    super();
    this.id = id;
    this.cityId = cityId;
    this.startDate = startDate;
    this.endDate = endDate;
    this.eventName = eventName;
    this.headerImage = headerImage;
    this.leftEquipId = leftEquipId;
    this.leftTag = leftTag;
    this.middleEquipId = middleEquipId;
    this.middleTag = middleTag;
    this.rightEquipId = rightEquipId;
    this.rightTag = rightTag;
    this.infoDescription = infoDescription;
  }
  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
  public int getCityId() {
    return cityId;
  }
  public void setCityId(int cityId) {
    this.cityId = cityId;
  }
  public Date getStartDate() {
    return startDate;
  }
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }
  public Date getEndDate() {
    return endDate;
  }
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }
  public String getEventName() {
    return eventName;
  }
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  public String getHeaderImage() {
    return headerImage;
  }
  public void setHeaderImage(String headerImage) {
    this.headerImage = headerImage;
  }
  public int getLeftEquipId() {
    return leftEquipId;
  }
  public void setLeftEquipId(int leftEquipId) {
    this.leftEquipId = leftEquipId;
  }
  public String getLeftTag() {
    return leftTag;
  }
  public void setLeftTag(String leftTag) {
    this.leftTag = leftTag;
  }
  public int getMiddleEquipId() {
    return middleEquipId;
  }
  public void setMiddleEquipId(int middleEquipId) {
    this.middleEquipId = middleEquipId;
  }
  public String getMiddleTag() {
    return middleTag;
  }
  public void setMiddleTag(String middleTag) {
    this.middleTag = middleTag;
  }
  public int getRightEquipId() {
    return rightEquipId;
  }
  public void setRightEquipId(int rightEquipId) {
    this.rightEquipId = rightEquipId;
  }
  public String getRightTag() {
    return rightTag;
  }
  public void setRightTag(String rightTag) {
    this.rightTag = rightTag;
  }
  public String getInfoDescription() {
    return infoDescription;
  }
  public void setInfoDescription(String infoDescription) {
    this.infoDescription = infoDescription;
  }
  @Override
  public String toString() {
    return "BossEvent [id=" + id + ", cityId=" + cityId + ", startDate="
        + startDate + ", endDate=" + endDate + ", eventName=" + eventName
        + ", headerImage=" + headerImage + ", leftEquipId=" + leftEquipId
        + ", leftTag=" + leftTag + ", middleEquipId=" + middleEquipId
        + ", middleTag=" + middleTag + ", rightEquipId=" + rightEquipId
        + ", rightTag=" + rightTag + ", infoDescription=" + infoDescription 
        + "]";
  }
}