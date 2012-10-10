package com.lvl6.info;

import java.io.Serializable;
import java.util.Date;

public class GoldSale implements Serializable {
  private static final long serialVersionUID = 7996839582751539192L;
  private int id;
  private Date startDate;
  private Date endDate;
  private String package1SaleIdentifier;
  private String package2SaleIdentifier;
  private String package3SaleIdentifier;
  private String package4SaleIdentifier;
  private String package5SaleIdentifier;
  
  public GoldSale(int id, Date startDate, Date endDate,
      String package1SaleIdentifier, String package2SaleIdentifier,
      String package3SaleIdentifier, String package4SaleIdentifier,
      String package5SaleIdentifier) {
    super();
    this.id = id;
    this.startDate = startDate;
    this.endDate = endDate;
    this.package1SaleIdentifier = package1SaleIdentifier;
    this.package2SaleIdentifier = package2SaleIdentifier;
    this.package3SaleIdentifier = package3SaleIdentifier;
    this.package4SaleIdentifier = package4SaleIdentifier;
    this.package5SaleIdentifier = package5SaleIdentifier;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public String getPackage1SaleIdentifier() {
    return package1SaleIdentifier;
  }

  public void setPackage1SaleIdentifier(String package1SaleIdentifier) {
    this.package1SaleIdentifier = package1SaleIdentifier;
  }

  public String getPackage2SaleIdentifier() {
    return package2SaleIdentifier;
  }

  public void setPackage2SaleIdentifier(String package2SaleIdentifier) {
    this.package2SaleIdentifier = package2SaleIdentifier;
  }

  public String getPackage3SaleIdentifier() {
    return package3SaleIdentifier;
  }

  public void setPackage3SaleIdentifier(String package3SaleIdentifier) {
    this.package3SaleIdentifier = package3SaleIdentifier;
  }

  public String getPackage4SaleIdentifier() {
    return package4SaleIdentifier;
  }

  public void setPackage4SaleIdentifier(String package4SaleIdentifier) {
    this.package4SaleIdentifier = package4SaleIdentifier;
  }

  public String getPackage5SaleIdentifier() {
    return package5SaleIdentifier;
  }

  public void setPackage5SaleIdentifier(String package5SaleIdentifier) {
    this.package5SaleIdentifier = package5SaleIdentifier;
  }

  @Override
  public String toString() {
    return "GoldSale [id=" + id + ", startDate=" + startDate + ", endDate="
        + endDate + ", package1SaleIdentifier=" + package1SaleIdentifier
        + ", package2SaleIdentifier=" + package2SaleIdentifier
        + ", package3SaleIdentifier=" + package3SaleIdentifier
        + ", package4SaleIdentifier=" + package4SaleIdentifier
        + ", package5SaleIdentifier=" + package5SaleIdentifier + "]";
  }
}