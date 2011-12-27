package com.lvl6.info;

import com.lvl6.proto.InfoProto.MinimumUserProto.UserType;

public class User {

  private int id;
  private String name;
  private UserType type;
  private String udid;
  
  public User(int id) {
    this.id = id;
  }

  public User(int id, String name, UserType type, String udid) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.udid = udid;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public UserType getType() {
    return type;
  }

  public void setType(UserType type) {
    this.type = type;
  }

  public String getUdid() {
    return udid;
  }

  public void setUdid(String udid) {
    this.udid = udid;
  }

  @Override
  public String toString() {
    return "User [id=" + id + ", name=" + name + ", type=" + type + ", udid="
        + udid + "]";
  }

}
