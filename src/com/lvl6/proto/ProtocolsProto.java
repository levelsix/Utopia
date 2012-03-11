// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Protocols.proto

package com.lvl6.proto;

public final class ProtocolsProto {
  private ProtocolsProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public enum EventProtocolRequest
      implements com.google.protobuf.ProtocolMessageEnum {
    C_CHAT_EVENT(0, 0),
    C_BATTLE_EVENT(1, 1),
    C_VAULT_EVENT(2, 2),
    C_TASK_ACTION_EVENT(3, 3),
    C_RETRIEVE_USER_EQUIP_FOR_USER(4, 4),
    C_STARTUP_EVENT(5, 5),
    C_RETRIEVE_STATIC_DATA_FOR_SHOP_EVENT(6, 8),
    C_ARMORY_EVENT(7, 9),
    C_IN_APP_PURCHASE_EVENT(8, 10),
    C_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT(9, 11),
    C_POST_TO_MARKETPLACE_EVENT(10, 12),
    C_RETRACT_POST_FROM_MARKETPLACE_EVENT(11, 13),
    C_PURCHASE_FROM_MARKETPLACE_EVENT(12, 14),
    C_USE_SKILL_POINT_EVENT(13, 15),
    C_GENERATE_ATTACK_LIST_EVENT(14, 16),
    C_PURCHASE_NORM_STRUCTURE_EVENT(15, 17),
    C_MOVE_OR_ROTATE_NORM_STRUCTURE_EVENT(16, 18),
    C_SELL_NORM_STRUCTURE_EVENT(17, 19),
    C_UPGRADE_NORM_STRUCTURE_EVENT(18, 20),
    C_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT(19, 21),
    C_REFILL_STAT_WITH_DIAMONDS_EVENT(20, 22),
    C_FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS_EVENT(21, 23),
    C_NORM_STRUCT_WAIT_COMPLETE_EVENT(22, 24),
    C_REDEEM_MARKETPLACE_EARNINGS_EVENT(23, 25),
    C_CRIT_STRUCTURE_ACTION_EVENT(24, 26),
    C_LOAD_PLAYER_CITY_EVENT(25, 27),
    C_RETRIEVE_STATIC_DATA_EVENT(26, 28),
    C_QUEST_ACCEPT_EVENT(27, 29),
    C_USER_QUEST_DETAILS_EVENT(28, 30),
    C_QUEST_REDEEM_EVENT(29, 31),
    C_PURCHASE_CITY_EXPANSION_EVENT(30, 32),
    C_EXPANSION_WAIT_COMPLETE_EVENT(31, 33),
    C_REFILL_STAT_WAIT_COMPLETE_EVENT(32, 34),
    C_LEVEL_UP_EVENT(33, 35),
    C_ENABLE_APNS_EVENT(34, 36),
    C_PURCHASE_MARKETPLACE_LICENSE_EVENT(35, 37),
    C_USER_CREATE_EVENT(36, 38),
    C_EQUIP_EQUIPMENT_EVENT(37, 39),
    C_CHANGE_USER_LOCATION_EVENT(38, 40),
    C_LOAD_NEUTRAL_CITY_EVENT(39, 41),
    A_ADMIN_UPDATE(40, 300),
    ;
    
    public static final int C_CHAT_EVENT_VALUE = 0;
    public static final int C_BATTLE_EVENT_VALUE = 1;
    public static final int C_VAULT_EVENT_VALUE = 2;
    public static final int C_TASK_ACTION_EVENT_VALUE = 3;
    public static final int C_RETRIEVE_USER_EQUIP_FOR_USER_VALUE = 4;
    public static final int C_STARTUP_EVENT_VALUE = 5;
    public static final int C_RETRIEVE_STATIC_DATA_FOR_SHOP_EVENT_VALUE = 8;
    public static final int C_ARMORY_EVENT_VALUE = 9;
    public static final int C_IN_APP_PURCHASE_EVENT_VALUE = 10;
    public static final int C_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT_VALUE = 11;
    public static final int C_POST_TO_MARKETPLACE_EVENT_VALUE = 12;
    public static final int C_RETRACT_POST_FROM_MARKETPLACE_EVENT_VALUE = 13;
    public static final int C_PURCHASE_FROM_MARKETPLACE_EVENT_VALUE = 14;
    public static final int C_USE_SKILL_POINT_EVENT_VALUE = 15;
    public static final int C_GENERATE_ATTACK_LIST_EVENT_VALUE = 16;
    public static final int C_PURCHASE_NORM_STRUCTURE_EVENT_VALUE = 17;
    public static final int C_MOVE_OR_ROTATE_NORM_STRUCTURE_EVENT_VALUE = 18;
    public static final int C_SELL_NORM_STRUCTURE_EVENT_VALUE = 19;
    public static final int C_UPGRADE_NORM_STRUCTURE_EVENT_VALUE = 20;
    public static final int C_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT_VALUE = 21;
    public static final int C_REFILL_STAT_WITH_DIAMONDS_EVENT_VALUE = 22;
    public static final int C_FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS_EVENT_VALUE = 23;
    public static final int C_NORM_STRUCT_WAIT_COMPLETE_EVENT_VALUE = 24;
    public static final int C_REDEEM_MARKETPLACE_EARNINGS_EVENT_VALUE = 25;
    public static final int C_CRIT_STRUCTURE_ACTION_EVENT_VALUE = 26;
    public static final int C_LOAD_PLAYER_CITY_EVENT_VALUE = 27;
    public static final int C_RETRIEVE_STATIC_DATA_EVENT_VALUE = 28;
    public static final int C_QUEST_ACCEPT_EVENT_VALUE = 29;
    public static final int C_USER_QUEST_DETAILS_EVENT_VALUE = 30;
    public static final int C_QUEST_REDEEM_EVENT_VALUE = 31;
    public static final int C_PURCHASE_CITY_EXPANSION_EVENT_VALUE = 32;
    public static final int C_EXPANSION_WAIT_COMPLETE_EVENT_VALUE = 33;
    public static final int C_REFILL_STAT_WAIT_COMPLETE_EVENT_VALUE = 34;
    public static final int C_LEVEL_UP_EVENT_VALUE = 35;
    public static final int C_ENABLE_APNS_EVENT_VALUE = 36;
    public static final int C_PURCHASE_MARKETPLACE_LICENSE_EVENT_VALUE = 37;
    public static final int C_USER_CREATE_EVENT_VALUE = 38;
    public static final int C_EQUIP_EQUIPMENT_EVENT_VALUE = 39;
    public static final int C_CHANGE_USER_LOCATION_EVENT_VALUE = 40;
    public static final int C_LOAD_NEUTRAL_CITY_EVENT_VALUE = 41;
    public static final int A_ADMIN_UPDATE_VALUE = 300;
    
    
    public final int getNumber() { return value; }
    
    public static EventProtocolRequest valueOf(int value) {
      switch (value) {
        case 0: return C_CHAT_EVENT;
        case 1: return C_BATTLE_EVENT;
        case 2: return C_VAULT_EVENT;
        case 3: return C_TASK_ACTION_EVENT;
        case 4: return C_RETRIEVE_USER_EQUIP_FOR_USER;
        case 5: return C_STARTUP_EVENT;
        case 8: return C_RETRIEVE_STATIC_DATA_FOR_SHOP_EVENT;
        case 9: return C_ARMORY_EVENT;
        case 10: return C_IN_APP_PURCHASE_EVENT;
        case 11: return C_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT;
        case 12: return C_POST_TO_MARKETPLACE_EVENT;
        case 13: return C_RETRACT_POST_FROM_MARKETPLACE_EVENT;
        case 14: return C_PURCHASE_FROM_MARKETPLACE_EVENT;
        case 15: return C_USE_SKILL_POINT_EVENT;
        case 16: return C_GENERATE_ATTACK_LIST_EVENT;
        case 17: return C_PURCHASE_NORM_STRUCTURE_EVENT;
        case 18: return C_MOVE_OR_ROTATE_NORM_STRUCTURE_EVENT;
        case 19: return C_SELL_NORM_STRUCTURE_EVENT;
        case 20: return C_UPGRADE_NORM_STRUCTURE_EVENT;
        case 21: return C_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT;
        case 22: return C_REFILL_STAT_WITH_DIAMONDS_EVENT;
        case 23: return C_FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS_EVENT;
        case 24: return C_NORM_STRUCT_WAIT_COMPLETE_EVENT;
        case 25: return C_REDEEM_MARKETPLACE_EARNINGS_EVENT;
        case 26: return C_CRIT_STRUCTURE_ACTION_EVENT;
        case 27: return C_LOAD_PLAYER_CITY_EVENT;
        case 28: return C_RETRIEVE_STATIC_DATA_EVENT;
        case 29: return C_QUEST_ACCEPT_EVENT;
        case 30: return C_USER_QUEST_DETAILS_EVENT;
        case 31: return C_QUEST_REDEEM_EVENT;
        case 32: return C_PURCHASE_CITY_EXPANSION_EVENT;
        case 33: return C_EXPANSION_WAIT_COMPLETE_EVENT;
        case 34: return C_REFILL_STAT_WAIT_COMPLETE_EVENT;
        case 35: return C_LEVEL_UP_EVENT;
        case 36: return C_ENABLE_APNS_EVENT;
        case 37: return C_PURCHASE_MARKETPLACE_LICENSE_EVENT;
        case 38: return C_USER_CREATE_EVENT;
        case 39: return C_EQUIP_EQUIPMENT_EVENT;
        case 40: return C_CHANGE_USER_LOCATION_EVENT;
        case 41: return C_LOAD_NEUTRAL_CITY_EVENT;
        case 300: return A_ADMIN_UPDATE;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<EventProtocolRequest>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<EventProtocolRequest>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<EventProtocolRequest>() {
            public EventProtocolRequest findValueByNumber(int number) {
              return EventProtocolRequest.valueOf(number);
            }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.lvl6.proto.ProtocolsProto.getDescriptor().getEnumTypes().get(0);
    }
    
    private static final EventProtocolRequest[] VALUES = {
      C_CHAT_EVENT, C_BATTLE_EVENT, C_VAULT_EVENT, C_TASK_ACTION_EVENT, C_RETRIEVE_USER_EQUIP_FOR_USER, C_STARTUP_EVENT, C_RETRIEVE_STATIC_DATA_FOR_SHOP_EVENT, C_ARMORY_EVENT, C_IN_APP_PURCHASE_EVENT, C_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT, C_POST_TO_MARKETPLACE_EVENT, C_RETRACT_POST_FROM_MARKETPLACE_EVENT, C_PURCHASE_FROM_MARKETPLACE_EVENT, C_USE_SKILL_POINT_EVENT, C_GENERATE_ATTACK_LIST_EVENT, C_PURCHASE_NORM_STRUCTURE_EVENT, C_MOVE_OR_ROTATE_NORM_STRUCTURE_EVENT, C_SELL_NORM_STRUCTURE_EVENT, C_UPGRADE_NORM_STRUCTURE_EVENT, C_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT, C_REFILL_STAT_WITH_DIAMONDS_EVENT, C_FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS_EVENT, C_NORM_STRUCT_WAIT_COMPLETE_EVENT, C_REDEEM_MARKETPLACE_EARNINGS_EVENT, C_CRIT_STRUCTURE_ACTION_EVENT, C_LOAD_PLAYER_CITY_EVENT, C_RETRIEVE_STATIC_DATA_EVENT, C_QUEST_ACCEPT_EVENT, C_USER_QUEST_DETAILS_EVENT, C_QUEST_REDEEM_EVENT, C_PURCHASE_CITY_EXPANSION_EVENT, C_EXPANSION_WAIT_COMPLETE_EVENT, C_REFILL_STAT_WAIT_COMPLETE_EVENT, C_LEVEL_UP_EVENT, C_ENABLE_APNS_EVENT, C_PURCHASE_MARKETPLACE_LICENSE_EVENT, C_USER_CREATE_EVENT, C_EQUIP_EQUIPMENT_EVENT, C_CHANGE_USER_LOCATION_EVENT, C_LOAD_NEUTRAL_CITY_EVENT, A_ADMIN_UPDATE, 
    };
    
    public static EventProtocolRequest valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    
    private final int index;
    private final int value;
    
    private EventProtocolRequest(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    // @@protoc_insertion_point(enum_scope:com.lvl6.proto.EventProtocolRequest)
  }
  
  public enum EventProtocolResponse
      implements com.google.protobuf.ProtocolMessageEnum {
    S_CHAT_EVENT(0, 0),
    S_BATTLE_EVENT(1, 1),
    S_VAULT_EVENT(2, 2),
    S_TASK_ACTION_EVENT(3, 3),
    S_RETRIEVE_USER_EQUIP_FOR_USER(4, 4),
    S_STARTUP_EVENT(5, 5),
    S_RETRIEVE_STATIC_DATA_FOR_SHOP_EVENT(6, 8),
    S_ARMORY_EVENT(7, 9),
    S_IN_APP_PURCHASE_EVENT(8, 10),
    S_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT(9, 11),
    S_POST_TO_MARKETPLACE_EVENT(10, 12),
    S_RETRACT_POST_FROM_MARKETPLACE_EVENT(11, 13),
    S_PURCHASE_FROM_MARKETPLACE_EVENT(12, 14),
    S_USE_SKILL_POINT_EVENT(13, 15),
    S_GENERATE_ATTACK_LIST_EVENT(14, 16),
    S_PURCHASE_NORM_STRUCTURE_EVENT(15, 17),
    S_MOVE_OR_ROTATE_NORM_STRUCTURE_EVENT(16, 18),
    S_SELL_NORM_STRUCTURE_EVENT(17, 19),
    S_UPGRADE_NORM_STRUCTURE_EVENT(18, 20),
    S_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT(19, 21),
    S_REFILL_STAT_WITH_DIAMONDS_EVENT(20, 22),
    S_FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS_EVENT(21, 23),
    S_NORM_STRUCT_WAIT_COMPLETE_EVENT(22, 24),
    S_REDEEM_MARKETPLACE_EARNINGS_EVENT(23, 25),
    S_CRIT_STRUCTURE_ACTION_EVENT(24, 26),
    S_LOAD_PLAYER_CITY_EVENT(25, 27),
    S_RETRIEVE_STATIC_DATA_EVENT(26, 28),
    S_QUEST_ACCEPT_EVENT(27, 29),
    S_USER_QUEST_DETAILS_EVENT(28, 30),
    S_QUEST_REDEEM_EVENT(29, 31),
    S_PURCHASE_CITY_EXPANSION_EVENT(30, 32),
    S_EXPANSION_WAIT_COMPLETE_EVENT(31, 33),
    S_REFILL_STAT_WAIT_COMPLETE_EVENT(32, 34),
    S_LEVEL_UP_EVENT(33, 35),
    S_ENABLE_APNS_EVENT(34, 36),
    S_PURCHASE_MARKETPLACE_LICENSE_EVENT(35, 37),
    S_USER_CREATE_EVENT(36, 38),
    S_EQUIP_EQUIPMENT_EVENT(37, 39),
    S_CHANGE_USER_LOCATION_EVENT(38, 40),
    S_LOAD_NEUTRAL_CITY_EVENT(39, 41),
    S_UPDATE_CLIENT_USER_EVENT(40, 51),
    S_QUEST_COMPLETE_EVENT(41, 52),
    S_REFERRAL_CODE_USED_EVENT(42, 53),
    ;
    
    public static final int S_CHAT_EVENT_VALUE = 0;
    public static final int S_BATTLE_EVENT_VALUE = 1;
    public static final int S_VAULT_EVENT_VALUE = 2;
    public static final int S_TASK_ACTION_EVENT_VALUE = 3;
    public static final int S_RETRIEVE_USER_EQUIP_FOR_USER_VALUE = 4;
    public static final int S_STARTUP_EVENT_VALUE = 5;
    public static final int S_RETRIEVE_STATIC_DATA_FOR_SHOP_EVENT_VALUE = 8;
    public static final int S_ARMORY_EVENT_VALUE = 9;
    public static final int S_IN_APP_PURCHASE_EVENT_VALUE = 10;
    public static final int S_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT_VALUE = 11;
    public static final int S_POST_TO_MARKETPLACE_EVENT_VALUE = 12;
    public static final int S_RETRACT_POST_FROM_MARKETPLACE_EVENT_VALUE = 13;
    public static final int S_PURCHASE_FROM_MARKETPLACE_EVENT_VALUE = 14;
    public static final int S_USE_SKILL_POINT_EVENT_VALUE = 15;
    public static final int S_GENERATE_ATTACK_LIST_EVENT_VALUE = 16;
    public static final int S_PURCHASE_NORM_STRUCTURE_EVENT_VALUE = 17;
    public static final int S_MOVE_OR_ROTATE_NORM_STRUCTURE_EVENT_VALUE = 18;
    public static final int S_SELL_NORM_STRUCTURE_EVENT_VALUE = 19;
    public static final int S_UPGRADE_NORM_STRUCTURE_EVENT_VALUE = 20;
    public static final int S_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT_VALUE = 21;
    public static final int S_REFILL_STAT_WITH_DIAMONDS_EVENT_VALUE = 22;
    public static final int S_FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS_EVENT_VALUE = 23;
    public static final int S_NORM_STRUCT_WAIT_COMPLETE_EVENT_VALUE = 24;
    public static final int S_REDEEM_MARKETPLACE_EARNINGS_EVENT_VALUE = 25;
    public static final int S_CRIT_STRUCTURE_ACTION_EVENT_VALUE = 26;
    public static final int S_LOAD_PLAYER_CITY_EVENT_VALUE = 27;
    public static final int S_RETRIEVE_STATIC_DATA_EVENT_VALUE = 28;
    public static final int S_QUEST_ACCEPT_EVENT_VALUE = 29;
    public static final int S_USER_QUEST_DETAILS_EVENT_VALUE = 30;
    public static final int S_QUEST_REDEEM_EVENT_VALUE = 31;
    public static final int S_PURCHASE_CITY_EXPANSION_EVENT_VALUE = 32;
    public static final int S_EXPANSION_WAIT_COMPLETE_EVENT_VALUE = 33;
    public static final int S_REFILL_STAT_WAIT_COMPLETE_EVENT_VALUE = 34;
    public static final int S_LEVEL_UP_EVENT_VALUE = 35;
    public static final int S_ENABLE_APNS_EVENT_VALUE = 36;
    public static final int S_PURCHASE_MARKETPLACE_LICENSE_EVENT_VALUE = 37;
    public static final int S_USER_CREATE_EVENT_VALUE = 38;
    public static final int S_EQUIP_EQUIPMENT_EVENT_VALUE = 39;
    public static final int S_CHANGE_USER_LOCATION_EVENT_VALUE = 40;
    public static final int S_LOAD_NEUTRAL_CITY_EVENT_VALUE = 41;
    public static final int S_UPDATE_CLIENT_USER_EVENT_VALUE = 51;
    public static final int S_QUEST_COMPLETE_EVENT_VALUE = 52;
    public static final int S_REFERRAL_CODE_USED_EVENT_VALUE = 53;
    
    
    public final int getNumber() { return value; }
    
    public static EventProtocolResponse valueOf(int value) {
      switch (value) {
        case 0: return S_CHAT_EVENT;
        case 1: return S_BATTLE_EVENT;
        case 2: return S_VAULT_EVENT;
        case 3: return S_TASK_ACTION_EVENT;
        case 4: return S_RETRIEVE_USER_EQUIP_FOR_USER;
        case 5: return S_STARTUP_EVENT;
        case 8: return S_RETRIEVE_STATIC_DATA_FOR_SHOP_EVENT;
        case 9: return S_ARMORY_EVENT;
        case 10: return S_IN_APP_PURCHASE_EVENT;
        case 11: return S_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT;
        case 12: return S_POST_TO_MARKETPLACE_EVENT;
        case 13: return S_RETRACT_POST_FROM_MARKETPLACE_EVENT;
        case 14: return S_PURCHASE_FROM_MARKETPLACE_EVENT;
        case 15: return S_USE_SKILL_POINT_EVENT;
        case 16: return S_GENERATE_ATTACK_LIST_EVENT;
        case 17: return S_PURCHASE_NORM_STRUCTURE_EVENT;
        case 18: return S_MOVE_OR_ROTATE_NORM_STRUCTURE_EVENT;
        case 19: return S_SELL_NORM_STRUCTURE_EVENT;
        case 20: return S_UPGRADE_NORM_STRUCTURE_EVENT;
        case 21: return S_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT;
        case 22: return S_REFILL_STAT_WITH_DIAMONDS_EVENT;
        case 23: return S_FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS_EVENT;
        case 24: return S_NORM_STRUCT_WAIT_COMPLETE_EVENT;
        case 25: return S_REDEEM_MARKETPLACE_EARNINGS_EVENT;
        case 26: return S_CRIT_STRUCTURE_ACTION_EVENT;
        case 27: return S_LOAD_PLAYER_CITY_EVENT;
        case 28: return S_RETRIEVE_STATIC_DATA_EVENT;
        case 29: return S_QUEST_ACCEPT_EVENT;
        case 30: return S_USER_QUEST_DETAILS_EVENT;
        case 31: return S_QUEST_REDEEM_EVENT;
        case 32: return S_PURCHASE_CITY_EXPANSION_EVENT;
        case 33: return S_EXPANSION_WAIT_COMPLETE_EVENT;
        case 34: return S_REFILL_STAT_WAIT_COMPLETE_EVENT;
        case 35: return S_LEVEL_UP_EVENT;
        case 36: return S_ENABLE_APNS_EVENT;
        case 37: return S_PURCHASE_MARKETPLACE_LICENSE_EVENT;
        case 38: return S_USER_CREATE_EVENT;
        case 39: return S_EQUIP_EQUIPMENT_EVENT;
        case 40: return S_CHANGE_USER_LOCATION_EVENT;
        case 41: return S_LOAD_NEUTRAL_CITY_EVENT;
        case 51: return S_UPDATE_CLIENT_USER_EVENT;
        case 52: return S_QUEST_COMPLETE_EVENT;
        case 53: return S_REFERRAL_CODE_USED_EVENT;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<EventProtocolResponse>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<EventProtocolResponse>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<EventProtocolResponse>() {
            public EventProtocolResponse findValueByNumber(int number) {
              return EventProtocolResponse.valueOf(number);
            }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.lvl6.proto.ProtocolsProto.getDescriptor().getEnumTypes().get(1);
    }
    
    private static final EventProtocolResponse[] VALUES = {
      S_CHAT_EVENT, S_BATTLE_EVENT, S_VAULT_EVENT, S_TASK_ACTION_EVENT, S_RETRIEVE_USER_EQUIP_FOR_USER, S_STARTUP_EVENT, S_RETRIEVE_STATIC_DATA_FOR_SHOP_EVENT, S_ARMORY_EVENT, S_IN_APP_PURCHASE_EVENT, S_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT, S_POST_TO_MARKETPLACE_EVENT, S_RETRACT_POST_FROM_MARKETPLACE_EVENT, S_PURCHASE_FROM_MARKETPLACE_EVENT, S_USE_SKILL_POINT_EVENT, S_GENERATE_ATTACK_LIST_EVENT, S_PURCHASE_NORM_STRUCTURE_EVENT, S_MOVE_OR_ROTATE_NORM_STRUCTURE_EVENT, S_SELL_NORM_STRUCTURE_EVENT, S_UPGRADE_NORM_STRUCTURE_EVENT, S_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT, S_REFILL_STAT_WITH_DIAMONDS_EVENT, S_FINISH_NORM_STRUCT_WAITTIME_WITH_DIAMONDS_EVENT, S_NORM_STRUCT_WAIT_COMPLETE_EVENT, S_REDEEM_MARKETPLACE_EARNINGS_EVENT, S_CRIT_STRUCTURE_ACTION_EVENT, S_LOAD_PLAYER_CITY_EVENT, S_RETRIEVE_STATIC_DATA_EVENT, S_QUEST_ACCEPT_EVENT, S_USER_QUEST_DETAILS_EVENT, S_QUEST_REDEEM_EVENT, S_PURCHASE_CITY_EXPANSION_EVENT, S_EXPANSION_WAIT_COMPLETE_EVENT, S_REFILL_STAT_WAIT_COMPLETE_EVENT, S_LEVEL_UP_EVENT, S_ENABLE_APNS_EVENT, S_PURCHASE_MARKETPLACE_LICENSE_EVENT, S_USER_CREATE_EVENT, S_EQUIP_EQUIPMENT_EVENT, S_CHANGE_USER_LOCATION_EVENT, S_LOAD_NEUTRAL_CITY_EVENT, S_UPDATE_CLIENT_USER_EVENT, S_QUEST_COMPLETE_EVENT, S_REFERRAL_CODE_USED_EVENT, 
    };
    
    public static EventProtocolResponse valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    
    private final int index;
    private final int value;
    
    private EventProtocolResponse(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    // @@protoc_insertion_point(enum_scope:com.lvl6.proto.EventProtocolResponse)
  }
  
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\017Protocols.proto\022\016com.lvl6.proto\032\013Event" +
      ".proto*\342\n\n\024EventProtocolRequest\022\020\n\014C_CHA" +
      "T_EVENT\020\000\022\022\n\016C_BATTLE_EVENT\020\001\022\021\n\rC_VAULT" +
      "_EVENT\020\002\022\027\n\023C_TASK_ACTION_EVENT\020\003\022\"\n\036C_R" +
      "ETRIEVE_USER_EQUIP_FOR_USER\020\004\022\023\n\017C_START" +
      "UP_EVENT\020\005\022)\n%C_RETRIEVE_STATIC_DATA_FOR" +
      "_SHOP_EVENT\020\010\022\022\n\016C_ARMORY_EVENT\020\t\022\033\n\027C_I" +
      "N_APP_PURCHASE_EVENT\020\n\022.\n*C_RETRIEVE_CUR" +
      "RENT_MARKETPLACE_POSTS_EVENT\020\013\022\037\n\033C_POST" +
      "_TO_MARKETPLACE_EVENT\020\014\022)\n%C_RETRACT_POS",
      "T_FROM_MARKETPLACE_EVENT\020\r\022%\n!C_PURCHASE" +
      "_FROM_MARKETPLACE_EVENT\020\016\022\033\n\027C_USE_SKILL" +
      "_POINT_EVENT\020\017\022 \n\034C_GENERATE_ATTACK_LIST" +
      "_EVENT\020\020\022#\n\037C_PURCHASE_NORM_STRUCTURE_EV" +
      "ENT\020\021\022)\n%C_MOVE_OR_ROTATE_NORM_STRUCTURE" +
      "_EVENT\020\022\022\037\n\033C_SELL_NORM_STRUCTURE_EVENT\020" +
      "\023\022\"\n\036C_UPGRADE_NORM_STRUCTURE_EVENT\020\024\0221\n" +
      "-C_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE" +
      "_EVENT\020\025\022%\n!C_REFILL_STAT_WITH_DIAMONDS_" +
      "EVENT\020\026\0225\n1C_FINISH_NORM_STRUCT_WAITTIME",
      "_WITH_DIAMONDS_EVENT\020\027\022%\n!C_NORM_STRUCT_" +
      "WAIT_COMPLETE_EVENT\020\030\022\'\n#C_REDEEM_MARKET" +
      "PLACE_EARNINGS_EVENT\020\031\022!\n\035C_CRIT_STRUCTU" +
      "RE_ACTION_EVENT\020\032\022\034\n\030C_LOAD_PLAYER_CITY_" +
      "EVENT\020\033\022 \n\034C_RETRIEVE_STATIC_DATA_EVENT\020" +
      "\034\022\030\n\024C_QUEST_ACCEPT_EVENT\020\035\022\036\n\032C_USER_QU" +
      "EST_DETAILS_EVENT\020\036\022\030\n\024C_QUEST_REDEEM_EV" +
      "ENT\020\037\022#\n\037C_PURCHASE_CITY_EXPANSION_EVENT" +
      "\020 \022#\n\037C_EXPANSION_WAIT_COMPLETE_EVENT\020!\022" +
      "%\n!C_REFILL_STAT_WAIT_COMPLETE_EVENT\020\"\022\024",
      "\n\020C_LEVEL_UP_EVENT\020#\022\027\n\023C_ENABLE_APNS_EV" +
      "ENT\020$\022(\n$C_PURCHASE_MARKETPLACE_LICENSE_" +
      "EVENT\020%\022\027\n\023C_USER_CREATE_EVENT\020&\022\033\n\027C_EQ" +
      "UIP_EQUIPMENT_EVENT\020\'\022 \n\034C_CHANGE_USER_L" +
      "OCATION_EVENT\020(\022\035\n\031C_LOAD_NEUTRAL_CITY_E" +
      "VENT\020)\022\023\n\016A_ADMIN_UPDATE\020\254\002*\252\013\n\025EventPro" +
      "tocolResponse\022\020\n\014S_CHAT_EVENT\020\000\022\022\n\016S_BAT" +
      "TLE_EVENT\020\001\022\021\n\rS_VAULT_EVENT\020\002\022\027\n\023S_TASK" +
      "_ACTION_EVENT\020\003\022\"\n\036S_RETRIEVE_USER_EQUIP" +
      "_FOR_USER\020\004\022\023\n\017S_STARTUP_EVENT\020\005\022)\n%S_RE",
      "TRIEVE_STATIC_DATA_FOR_SHOP_EVENT\020\010\022\022\n\016S" +
      "_ARMORY_EVENT\020\t\022\033\n\027S_IN_APP_PURCHASE_EVE" +
      "NT\020\n\022.\n*S_RETRIEVE_CURRENT_MARKETPLACE_P" +
      "OSTS_EVENT\020\013\022\037\n\033S_POST_TO_MARKETPLACE_EV" +
      "ENT\020\014\022)\n%S_RETRACT_POST_FROM_MARKETPLACE" +
      "_EVENT\020\r\022%\n!S_PURCHASE_FROM_MARKETPLACE_" +
      "EVENT\020\016\022\033\n\027S_USE_SKILL_POINT_EVENT\020\017\022 \n\034" +
      "S_GENERATE_ATTACK_LIST_EVENT\020\020\022#\n\037S_PURC" +
      "HASE_NORM_STRUCTURE_EVENT\020\021\022)\n%S_MOVE_OR" +
      "_ROTATE_NORM_STRUCTURE_EVENT\020\022\022\037\n\033S_SELL",
      "_NORM_STRUCTURE_EVENT\020\023\022\"\n\036S_UPGRADE_NOR" +
      "M_STRUCTURE_EVENT\020\024\0221\n-S_RETRIEVE_CURREN" +
      "CY_FROM_NORM_STRUCTURE_EVENT\020\025\022%\n!S_REFI" +
      "LL_STAT_WITH_DIAMONDS_EVENT\020\026\0225\n1S_FINIS" +
      "H_NORM_STRUCT_WAITTIME_WITH_DIAMONDS_EVE" +
      "NT\020\027\022%\n!S_NORM_STRUCT_WAIT_COMPLETE_EVEN" +
      "T\020\030\022\'\n#S_REDEEM_MARKETPLACE_EARNINGS_EVE" +
      "NT\020\031\022!\n\035S_CRIT_STRUCTURE_ACTION_EVENT\020\032\022" +
      "\034\n\030S_LOAD_PLAYER_CITY_EVENT\020\033\022 \n\034S_RETRI" +
      "EVE_STATIC_DATA_EVENT\020\034\022\030\n\024S_QUEST_ACCEP",
      "T_EVENT\020\035\022\036\n\032S_USER_QUEST_DETAILS_EVENT\020" +
      "\036\022\030\n\024S_QUEST_REDEEM_EVENT\020\037\022#\n\037S_PURCHAS" +
      "E_CITY_EXPANSION_EVENT\020 \022#\n\037S_EXPANSION_" +
      "WAIT_COMPLETE_EVENT\020!\022%\n!S_REFILL_STAT_W" +
      "AIT_COMPLETE_EVENT\020\"\022\024\n\020S_LEVEL_UP_EVENT" +
      "\020#\022\027\n\023S_ENABLE_APNS_EVENT\020$\022(\n$S_PURCHAS" +
      "E_MARKETPLACE_LICENSE_EVENT\020%\022\027\n\023S_USER_" +
      "CREATE_EVENT\020&\022\033\n\027S_EQUIP_EQUIPMENT_EVEN" +
      "T\020\'\022 \n\034S_CHANGE_USER_LOCATION_EVENT\020(\022\035\n" +
      "\031S_LOAD_NEUTRAL_CITY_EVENT\020)\022\036\n\032S_UPDATE",
      "_CLIENT_USER_EVENT\0203\022\032\n\026S_QUEST_COMPLETE" +
      "_EVENT\0204\022\036\n\032S_REFERRAL_CODE_USED_EVENT\0205" +
      "B\020B\016ProtocolsProto"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.lvl6.proto.EventProto.getDescriptor(),
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
