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
    C_CLERIC_HEAL_EVENT(4, 4),
    C_STARTUP_EVENT(5, 5),
    C_RETRIEVE_TASKS_FOR_CITY_EVENT(6, 6),
    C_RETRIEVE_QUESTS_FOR_CITY_EVENT(7, 7),
    C_RETRIEVE_EQUIPS_FOR_ARMORY_EVENT(8, 8),
    C_ARMORY_EVENT(9, 9),
    C_IN_APP_PURCHASE_EVENT(10, 10),
    C_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT(11, 11),
    C_POST_TO_MARKETPLACE_EVENT(12, 12),
    C_RETRACT_POST_FROM_MARKETPLACE_EVENT(13, 13),
    C_PURCHASE_FROM_MARKETPLACE_EVENT(14, 14),
    C_USE_SKILL_POINT_EVENT(15, 15),
    C_GENERATE_ATTACK_LIST_EVENT(16, 16),
    C_PURCHASE_NORM_STRUCTURE_EVENT(17, 17),
    C_MOVE_NORM_STRUCTURE_EVENT(18, 18),
    C_SELL_NORM_STRUCTURE_EVENT(19, 19),
    C_UPGRADE_NORM_STRUCTURE_EVENT(20, 20),
    C_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT(21, 21),
    C_REFILL_STAT_WITH_DIAMONDS_EVENT(22, 22),
    C_FINISH_NORM_STRUCT_BUILD_WITH_DIAMONDS_EVENT(23, 23),
    C_NORM_STRUCT_BUILDS_COMPLETE_EVENT(24, 24),
    C_REDEEM_MARKETPLACE_EARNINGS(25, 25),
    A_ADMIN_UPDATE(26, 300),
    ;
    
    public static final int C_CHAT_EVENT_VALUE = 0;
    public static final int C_BATTLE_EVENT_VALUE = 1;
    public static final int C_VAULT_EVENT_VALUE = 2;
    public static final int C_TASK_ACTION_EVENT_VALUE = 3;
    public static final int C_CLERIC_HEAL_EVENT_VALUE = 4;
    public static final int C_STARTUP_EVENT_VALUE = 5;
    public static final int C_RETRIEVE_TASKS_FOR_CITY_EVENT_VALUE = 6;
    public static final int C_RETRIEVE_QUESTS_FOR_CITY_EVENT_VALUE = 7;
    public static final int C_RETRIEVE_EQUIPS_FOR_ARMORY_EVENT_VALUE = 8;
    public static final int C_ARMORY_EVENT_VALUE = 9;
    public static final int C_IN_APP_PURCHASE_EVENT_VALUE = 10;
    public static final int C_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT_VALUE = 11;
    public static final int C_POST_TO_MARKETPLACE_EVENT_VALUE = 12;
    public static final int C_RETRACT_POST_FROM_MARKETPLACE_EVENT_VALUE = 13;
    public static final int C_PURCHASE_FROM_MARKETPLACE_EVENT_VALUE = 14;
    public static final int C_USE_SKILL_POINT_EVENT_VALUE = 15;
    public static final int C_GENERATE_ATTACK_LIST_EVENT_VALUE = 16;
    public static final int C_PURCHASE_NORM_STRUCTURE_EVENT_VALUE = 17;
    public static final int C_MOVE_NORM_STRUCTURE_EVENT_VALUE = 18;
    public static final int C_SELL_NORM_STRUCTURE_EVENT_VALUE = 19;
    public static final int C_UPGRADE_NORM_STRUCTURE_EVENT_VALUE = 20;
    public static final int C_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT_VALUE = 21;
    public static final int C_REFILL_STAT_WITH_DIAMONDS_EVENT_VALUE = 22;
    public static final int C_FINISH_NORM_STRUCT_BUILD_WITH_DIAMONDS_EVENT_VALUE = 23;
    public static final int C_NORM_STRUCT_BUILDS_COMPLETE_EVENT_VALUE = 24;
    public static final int C_REDEEM_MARKETPLACE_EARNINGS_VALUE = 25;
    public static final int A_ADMIN_UPDATE_VALUE = 300;
    
    
    public final int getNumber() { return value; }
    
    public static EventProtocolRequest valueOf(int value) {
      switch (value) {
        case 0: return C_CHAT_EVENT;
        case 1: return C_BATTLE_EVENT;
        case 2: return C_VAULT_EVENT;
        case 3: return C_TASK_ACTION_EVENT;
        case 4: return C_CLERIC_HEAL_EVENT;
        case 5: return C_STARTUP_EVENT;
        case 6: return C_RETRIEVE_TASKS_FOR_CITY_EVENT;
        case 7: return C_RETRIEVE_QUESTS_FOR_CITY_EVENT;
        case 8: return C_RETRIEVE_EQUIPS_FOR_ARMORY_EVENT;
        case 9: return C_ARMORY_EVENT;
        case 10: return C_IN_APP_PURCHASE_EVENT;
        case 11: return C_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT;
        case 12: return C_POST_TO_MARKETPLACE_EVENT;
        case 13: return C_RETRACT_POST_FROM_MARKETPLACE_EVENT;
        case 14: return C_PURCHASE_FROM_MARKETPLACE_EVENT;
        case 15: return C_USE_SKILL_POINT_EVENT;
        case 16: return C_GENERATE_ATTACK_LIST_EVENT;
        case 17: return C_PURCHASE_NORM_STRUCTURE_EVENT;
        case 18: return C_MOVE_NORM_STRUCTURE_EVENT;
        case 19: return C_SELL_NORM_STRUCTURE_EVENT;
        case 20: return C_UPGRADE_NORM_STRUCTURE_EVENT;
        case 21: return C_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT;
        case 22: return C_REFILL_STAT_WITH_DIAMONDS_EVENT;
        case 23: return C_FINISH_NORM_STRUCT_BUILD_WITH_DIAMONDS_EVENT;
        case 24: return C_NORM_STRUCT_BUILDS_COMPLETE_EVENT;
        case 25: return C_REDEEM_MARKETPLACE_EARNINGS;
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
      C_CHAT_EVENT, C_BATTLE_EVENT, C_VAULT_EVENT, C_TASK_ACTION_EVENT, C_CLERIC_HEAL_EVENT, C_STARTUP_EVENT, C_RETRIEVE_TASKS_FOR_CITY_EVENT, C_RETRIEVE_QUESTS_FOR_CITY_EVENT, C_RETRIEVE_EQUIPS_FOR_ARMORY_EVENT, C_ARMORY_EVENT, C_IN_APP_PURCHASE_EVENT, C_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT, C_POST_TO_MARKETPLACE_EVENT, C_RETRACT_POST_FROM_MARKETPLACE_EVENT, C_PURCHASE_FROM_MARKETPLACE_EVENT, C_USE_SKILL_POINT_EVENT, C_GENERATE_ATTACK_LIST_EVENT, C_PURCHASE_NORM_STRUCTURE_EVENT, C_MOVE_NORM_STRUCTURE_EVENT, C_SELL_NORM_STRUCTURE_EVENT, C_UPGRADE_NORM_STRUCTURE_EVENT, C_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT, C_REFILL_STAT_WITH_DIAMONDS_EVENT, C_FINISH_NORM_STRUCT_BUILD_WITH_DIAMONDS_EVENT, C_NORM_STRUCT_BUILDS_COMPLETE_EVENT, C_REDEEM_MARKETPLACE_EARNINGS, A_ADMIN_UPDATE, 
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
    S_CLERIC_HEAL_EVENT(4, 4),
    S_STARTUP_EVENT(5, 5),
    S_RETRIEVE_TASKS_FOR_CITY_EVENT(6, 6),
    S_RETRIEVE_QUESTS_FOR_CITY_EVENT(7, 7),
    S_RETRIEVE_EQUIPS_FOR_ARMORY_EVENT(8, 8),
    S_ARMORY_EVENT(9, 9),
    S_IN_APP_PURCHASE_EVENT(10, 10),
    S_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT(11, 11),
    S_POST_TO_MARKETPLACE_EVENT(12, 12),
    S_RETRACT_POST_FROM_MARKETPLACE_EVENT(13, 13),
    S_PURCHASE_FROM_MARKETPLACE_EVENT(14, 14),
    S_USE_SKILL_POINT_EVENT(15, 15),
    S_GENERATE_ATTACK_LIST_EVENT(16, 16),
    S_PURCHASE_NORM_STRUCTURE_EVENT(17, 17),
    S_MOVE_NORM_STRUCTURE_EVENT(18, 18),
    S_SELL_NORM_STRUCTURE_EVENT(19, 19),
    S_UPGRADE_NORM_STRUCTURE_EVENT(20, 20),
    S_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT(21, 21),
    S_REFILL_STAT_WITH_DIAMONDS_EVENT(22, 22),
    S_FINISH_NORM_STRUCT_BUILD_WITH_DIAMONDS_EVENT(23, 23),
    S_NORM_STRUCT_BUILDS_COMPLETE_EVENT(24, 24),
    S_REDEEM_MARKETPLACE_EARNINGS(25, 25),
    ;
    
    public static final EventProtocolResponse S_LEVEL_UP_EVENT = S_SELL_NORM_STRUCTURE_EVENT;
    public static final EventProtocolResponse S_UPDATE_CLIENT_USER_EVENT = S_UPGRADE_NORM_STRUCTURE_EVENT;
    public static final int S_CHAT_EVENT_VALUE = 0;
    public static final int S_BATTLE_EVENT_VALUE = 1;
    public static final int S_VAULT_EVENT_VALUE = 2;
    public static final int S_TASK_ACTION_EVENT_VALUE = 3;
    public static final int S_CLERIC_HEAL_EVENT_VALUE = 4;
    public static final int S_STARTUP_EVENT_VALUE = 5;
    public static final int S_RETRIEVE_TASKS_FOR_CITY_EVENT_VALUE = 6;
    public static final int S_RETRIEVE_QUESTS_FOR_CITY_EVENT_VALUE = 7;
    public static final int S_RETRIEVE_EQUIPS_FOR_ARMORY_EVENT_VALUE = 8;
    public static final int S_ARMORY_EVENT_VALUE = 9;
    public static final int S_IN_APP_PURCHASE_EVENT_VALUE = 10;
    public static final int S_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT_VALUE = 11;
    public static final int S_POST_TO_MARKETPLACE_EVENT_VALUE = 12;
    public static final int S_RETRACT_POST_FROM_MARKETPLACE_EVENT_VALUE = 13;
    public static final int S_PURCHASE_FROM_MARKETPLACE_EVENT_VALUE = 14;
    public static final int S_USE_SKILL_POINT_EVENT_VALUE = 15;
    public static final int S_GENERATE_ATTACK_LIST_EVENT_VALUE = 16;
    public static final int S_PURCHASE_NORM_STRUCTURE_EVENT_VALUE = 17;
    public static final int S_MOVE_NORM_STRUCTURE_EVENT_VALUE = 18;
    public static final int S_SELL_NORM_STRUCTURE_EVENT_VALUE = 19;
    public static final int S_UPGRADE_NORM_STRUCTURE_EVENT_VALUE = 20;
    public static final int S_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT_VALUE = 21;
    public static final int S_REFILL_STAT_WITH_DIAMONDS_EVENT_VALUE = 22;
    public static final int S_FINISH_NORM_STRUCT_BUILD_WITH_DIAMONDS_EVENT_VALUE = 23;
    public static final int S_NORM_STRUCT_BUILDS_COMPLETE_EVENT_VALUE = 24;
    public static final int S_REDEEM_MARKETPLACE_EARNINGS_VALUE = 25;
    public static final int S_LEVEL_UP_EVENT_VALUE = 19;
    public static final int S_UPDATE_CLIENT_USER_EVENT_VALUE = 20;
    
    
    public final int getNumber() { return value; }
    
    public static EventProtocolResponse valueOf(int value) {
      switch (value) {
        case 0: return S_CHAT_EVENT;
        case 1: return S_BATTLE_EVENT;
        case 2: return S_VAULT_EVENT;
        case 3: return S_TASK_ACTION_EVENT;
        case 4: return S_CLERIC_HEAL_EVENT;
        case 5: return S_STARTUP_EVENT;
        case 6: return S_RETRIEVE_TASKS_FOR_CITY_EVENT;
        case 7: return S_RETRIEVE_QUESTS_FOR_CITY_EVENT;
        case 8: return S_RETRIEVE_EQUIPS_FOR_ARMORY_EVENT;
        case 9: return S_ARMORY_EVENT;
        case 10: return S_IN_APP_PURCHASE_EVENT;
        case 11: return S_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT;
        case 12: return S_POST_TO_MARKETPLACE_EVENT;
        case 13: return S_RETRACT_POST_FROM_MARKETPLACE_EVENT;
        case 14: return S_PURCHASE_FROM_MARKETPLACE_EVENT;
        case 15: return S_USE_SKILL_POINT_EVENT;
        case 16: return S_GENERATE_ATTACK_LIST_EVENT;
        case 17: return S_PURCHASE_NORM_STRUCTURE_EVENT;
        case 18: return S_MOVE_NORM_STRUCTURE_EVENT;
        case 19: return S_SELL_NORM_STRUCTURE_EVENT;
        case 20: return S_UPGRADE_NORM_STRUCTURE_EVENT;
        case 21: return S_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT;
        case 22: return S_REFILL_STAT_WITH_DIAMONDS_EVENT;
        case 23: return S_FINISH_NORM_STRUCT_BUILD_WITH_DIAMONDS_EVENT;
        case 24: return S_NORM_STRUCT_BUILDS_COMPLETE_EVENT;
        case 25: return S_REDEEM_MARKETPLACE_EARNINGS;
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
      S_CHAT_EVENT, S_BATTLE_EVENT, S_VAULT_EVENT, S_TASK_ACTION_EVENT, S_CLERIC_HEAL_EVENT, S_STARTUP_EVENT, S_RETRIEVE_TASKS_FOR_CITY_EVENT, S_RETRIEVE_QUESTS_FOR_CITY_EVENT, S_RETRIEVE_EQUIPS_FOR_ARMORY_EVENT, S_ARMORY_EVENT, S_IN_APP_PURCHASE_EVENT, S_RETRIEVE_CURRENT_MARKETPLACE_POSTS_EVENT, S_POST_TO_MARKETPLACE_EVENT, S_RETRACT_POST_FROM_MARKETPLACE_EVENT, S_PURCHASE_FROM_MARKETPLACE_EVENT, S_USE_SKILL_POINT_EVENT, S_GENERATE_ATTACK_LIST_EVENT, S_PURCHASE_NORM_STRUCTURE_EVENT, S_MOVE_NORM_STRUCTURE_EVENT, S_SELL_NORM_STRUCTURE_EVENT, S_UPGRADE_NORM_STRUCTURE_EVENT, S_RETRIEVE_CURRENCY_FROM_NORM_STRUCTURE_EVENT, S_REFILL_STAT_WITH_DIAMONDS_EVENT, S_FINISH_NORM_STRUCT_BUILD_WITH_DIAMONDS_EVENT, S_NORM_STRUCT_BUILDS_COMPLETE_EVENT, S_REDEEM_MARKETPLACE_EARNINGS, S_LEVEL_UP_EVENT, S_UPDATE_CLIENT_USER_EVENT, 
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
      ".proto*\226\007\n\024EventProtocolRequest\022\020\n\014C_CHA" +
      "T_EVENT\020\000\022\022\n\016C_BATTLE_EVENT\020\001\022\021\n\rC_VAULT" +
      "_EVENT\020\002\022\027\n\023C_TASK_ACTION_EVENT\020\003\022\027\n\023C_C" +
      "LERIC_HEAL_EVENT\020\004\022\023\n\017C_STARTUP_EVENT\020\005\022" +
      "#\n\037C_RETRIEVE_TASKS_FOR_CITY_EVENT\020\006\022$\n " +
      "C_RETRIEVE_QUESTS_FOR_CITY_EVENT\020\007\022&\n\"C_" +
      "RETRIEVE_EQUIPS_FOR_ARMORY_EVENT\020\010\022\022\n\016C_" +
      "ARMORY_EVENT\020\t\022\033\n\027C_IN_APP_PURCHASE_EVEN" +
      "T\020\n\022.\n*C_RETRIEVE_CURRENT_MARKETPLACE_PO",
      "STS_EVENT\020\013\022\037\n\033C_POST_TO_MARKETPLACE_EVE" +
      "NT\020\014\022)\n%C_RETRACT_POST_FROM_MARKETPLACE_" +
      "EVENT\020\r\022%\n!C_PURCHASE_FROM_MARKETPLACE_E" +
      "VENT\020\016\022\033\n\027C_USE_SKILL_POINT_EVENT\020\017\022 \n\034C" +
      "_GENERATE_ATTACK_LIST_EVENT\020\020\022#\n\037C_PURCH" +
      "ASE_NORM_STRUCTURE_EVENT\020\021\022\037\n\033C_MOVE_NOR" +
      "M_STRUCTURE_EVENT\020\022\022\037\n\033C_SELL_NORM_STRUC" +
      "TURE_EVENT\020\023\022\"\n\036C_UPGRADE_NORM_STRUCTURE" +
      "_EVENT\020\024\0221\n-C_RETRIEVE_CURRENCY_FROM_NOR" +
      "M_STRUCTURE_EVENT\020\025\022%\n!C_REFILL_STAT_WIT",
      "H_DIAMONDS_EVENT\020\026\0222\n.C_FINISH_NORM_STRU" +
      "CT_BUILD_WITH_DIAMONDS_EVENT\020\027\022\'\n#C_NORM" +
      "_STRUCT_BUILDS_COMPLETE_EVENT\020\030\022!\n\035C_RED" +
      "EEM_MARKETPLACE_EARNINGS\020\031\022\023\n\016A_ADMIN_UP" +
      "DATE\020\254\002*\270\007\n\025EventProtocolResponse\022\020\n\014S_C" +
      "HAT_EVENT\020\000\022\022\n\016S_BATTLE_EVENT\020\001\022\021\n\rS_VAU" +
      "LT_EVENT\020\002\022\027\n\023S_TASK_ACTION_EVENT\020\003\022\027\n\023S" +
      "_CLERIC_HEAL_EVENT\020\004\022\023\n\017S_STARTUP_EVENT\020" +
      "\005\022#\n\037S_RETRIEVE_TASKS_FOR_CITY_EVENT\020\006\022$" +
      "\n S_RETRIEVE_QUESTS_FOR_CITY_EVENT\020\007\022&\n\"",
      "S_RETRIEVE_EQUIPS_FOR_ARMORY_EVENT\020\010\022\022\n\016" +
      "S_ARMORY_EVENT\020\t\022\033\n\027S_IN_APP_PURCHASE_EV" +
      "ENT\020\n\022.\n*S_RETRIEVE_CURRENT_MARKETPLACE_" +
      "POSTS_EVENT\020\013\022\037\n\033S_POST_TO_MARKETPLACE_E" +
      "VENT\020\014\022)\n%S_RETRACT_POST_FROM_MARKETPLAC" +
      "E_EVENT\020\r\022%\n!S_PURCHASE_FROM_MARKETPLACE" +
      "_EVENT\020\016\022\033\n\027S_USE_SKILL_POINT_EVENT\020\017\022 \n" +
      "\034S_GENERATE_ATTACK_LIST_EVENT\020\020\022#\n\037S_PUR" +
      "CHASE_NORM_STRUCTURE_EVENT\020\021\022\037\n\033S_MOVE_N" +
      "ORM_STRUCTURE_EVENT\020\022\022\037\n\033S_SELL_NORM_STR",
      "UCTURE_EVENT\020\023\022\"\n\036S_UPGRADE_NORM_STRUCTU" +
      "RE_EVENT\020\024\0221\n-S_RETRIEVE_CURRENCY_FROM_N" +
      "ORM_STRUCTURE_EVENT\020\025\022%\n!S_REFILL_STAT_W" +
      "ITH_DIAMONDS_EVENT\020\026\0222\n.S_FINISH_NORM_ST" +
      "RUCT_BUILD_WITH_DIAMONDS_EVENT\020\027\022\'\n#S_NO" +
      "RM_STRUCT_BUILDS_COMPLETE_EVENT\020\030\022!\n\035S_R" +
      "EDEEM_MARKETPLACE_EARNINGS\020\031\022\024\n\020S_LEVEL_" +
      "UP_EVENT\020\023\022\036\n\032S_UPDATE_CLIENT_USER_EVENT" +
      "\020\024B\020B\016ProtocolsProto"
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
