// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Admin.proto

package com.lvl6.proto;

public final class AdminProto {
  private AdminProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface AdminChangeRequestProtoOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // optional .com.lvl6.proto.AdminChangeRequestProto.StaticDataReloadType staticDataReloadType = 1;
    boolean hasStaticDataReloadType();
    com.lvl6.proto.AdminProto.AdminChangeRequestProto.StaticDataReloadType getStaticDataReloadType();
    
    // optional int32 salePercentOff = 2;
    boolean hasSalePercentOff();
    int getSalePercentOff();
    
    // optional float multipleOfRecruitsBaseReward = 3;
    boolean hasMultipleOfRecruitsBaseReward();
    float getMultipleOfRecruitsBaseReward();
  }
  public static final class AdminChangeRequestProto extends
      com.google.protobuf.GeneratedMessage
      implements AdminChangeRequestProtoOrBuilder {
    // Use AdminChangeRequestProto.newBuilder() to construct.
    private AdminChangeRequestProto(Builder builder) {
      super(builder);
    }
    private AdminChangeRequestProto(boolean noInit) {}
    
    private static final AdminChangeRequestProto defaultInstance;
    public static AdminChangeRequestProto getDefaultInstance() {
      return defaultInstance;
    }
    
    public AdminChangeRequestProto getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.lvl6.proto.AdminProto.internal_static_com_lvl6_proto_AdminChangeRequestProto_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.lvl6.proto.AdminProto.internal_static_com_lvl6_proto_AdminChangeRequestProto_fieldAccessorTable;
    }
    
    public enum StaticDataReloadType
        implements com.google.protobuf.ProtocolMessageEnum {
      ALL(0, 0),
      BUILD_STRUCT_JOBS(1, 1),
      CITIES(2, 2),
      DEFEAT_TYPE_JOBS(3, 3),
      EQUIPMENT(4, 4),
      QUESTS(5, 5),
      TASK_EQUIP_REQUIREMENTS(6, 6),
      TASKS(7, 7),
      UPGRADE_STRUCT_JOBS(8, 8),
      STRUCTURES(9, 9),
      POSSESS_EQUIP_JOBS(10, 10),
      LEVELS_REQUIRED_EXPERIENCE(11, 11),
      NEUTRAL_CITY_ELEMS(12, 12),
      ;
      
      public static final int ALL_VALUE = 0;
      public static final int BUILD_STRUCT_JOBS_VALUE = 1;
      public static final int CITIES_VALUE = 2;
      public static final int DEFEAT_TYPE_JOBS_VALUE = 3;
      public static final int EQUIPMENT_VALUE = 4;
      public static final int QUESTS_VALUE = 5;
      public static final int TASK_EQUIP_REQUIREMENTS_VALUE = 6;
      public static final int TASKS_VALUE = 7;
      public static final int UPGRADE_STRUCT_JOBS_VALUE = 8;
      public static final int STRUCTURES_VALUE = 9;
      public static final int POSSESS_EQUIP_JOBS_VALUE = 10;
      public static final int LEVELS_REQUIRED_EXPERIENCE_VALUE = 11;
      public static final int NEUTRAL_CITY_ELEMS_VALUE = 12;
      
      
      public final int getNumber() { return value; }
      
      public static StaticDataReloadType valueOf(int value) {
        switch (value) {
          case 0: return ALL;
          case 1: return BUILD_STRUCT_JOBS;
          case 2: return CITIES;
          case 3: return DEFEAT_TYPE_JOBS;
          case 4: return EQUIPMENT;
          case 5: return QUESTS;
          case 6: return TASK_EQUIP_REQUIREMENTS;
          case 7: return TASKS;
          case 8: return UPGRADE_STRUCT_JOBS;
          case 9: return STRUCTURES;
          case 10: return POSSESS_EQUIP_JOBS;
          case 11: return LEVELS_REQUIRED_EXPERIENCE;
          case 12: return NEUTRAL_CITY_ELEMS;
          default: return null;
        }
      }
      
      public static com.google.protobuf.Internal.EnumLiteMap<StaticDataReloadType>
          internalGetValueMap() {
        return internalValueMap;
      }
      private static com.google.protobuf.Internal.EnumLiteMap<StaticDataReloadType>
          internalValueMap =
            new com.google.protobuf.Internal.EnumLiteMap<StaticDataReloadType>() {
              public StaticDataReloadType findValueByNumber(int number) {
                return StaticDataReloadType.valueOf(number);
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
        return com.lvl6.proto.AdminProto.AdminChangeRequestProto.getDescriptor().getEnumTypes().get(0);
      }
      
      private static final StaticDataReloadType[] VALUES = {
        ALL, BUILD_STRUCT_JOBS, CITIES, DEFEAT_TYPE_JOBS, EQUIPMENT, QUESTS, TASK_EQUIP_REQUIREMENTS, TASKS, UPGRADE_STRUCT_JOBS, STRUCTURES, POSSESS_EQUIP_JOBS, LEVELS_REQUIRED_EXPERIENCE, NEUTRAL_CITY_ELEMS, 
      };
      
      public static StaticDataReloadType valueOf(
          com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
        if (desc.getType() != getDescriptor()) {
          throw new java.lang.IllegalArgumentException(
            "EnumValueDescriptor is not for this type.");
        }
        return VALUES[desc.getIndex()];
      }
      
      private final int index;
      private final int value;
      
      private StaticDataReloadType(int index, int value) {
        this.index = index;
        this.value = value;
      }
      
      // @@protoc_insertion_point(enum_scope:com.lvl6.proto.AdminChangeRequestProto.StaticDataReloadType)
    }
    
    private int bitField0_;
    // optional .com.lvl6.proto.AdminChangeRequestProto.StaticDataReloadType staticDataReloadType = 1;
    public static final int STATICDATARELOADTYPE_FIELD_NUMBER = 1;
    private com.lvl6.proto.AdminProto.AdminChangeRequestProto.StaticDataReloadType staticDataReloadType_;
    public boolean hasStaticDataReloadType() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public com.lvl6.proto.AdminProto.AdminChangeRequestProto.StaticDataReloadType getStaticDataReloadType() {
      return staticDataReloadType_;
    }
    
    // optional int32 salePercentOff = 2;
    public static final int SALEPERCENTOFF_FIELD_NUMBER = 2;
    private int salePercentOff_;
    public boolean hasSalePercentOff() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public int getSalePercentOff() {
      return salePercentOff_;
    }
    
    // optional float multipleOfRecruitsBaseReward = 3;
    public static final int MULTIPLEOFRECRUITSBASEREWARD_FIELD_NUMBER = 3;
    private float multipleOfRecruitsBaseReward_;
    public boolean hasMultipleOfRecruitsBaseReward() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    public float getMultipleOfRecruitsBaseReward() {
      return multipleOfRecruitsBaseReward_;
    }
    
    private void initFields() {
      staticDataReloadType_ = com.lvl6.proto.AdminProto.AdminChangeRequestProto.StaticDataReloadType.ALL;
      salePercentOff_ = 0;
      multipleOfRecruitsBaseReward_ = 0F;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeEnum(1, staticDataReloadType_.getNumber());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeInt32(2, salePercentOff_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeFloat(3, multipleOfRecruitsBaseReward_);
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeEnumSize(1, staticDataReloadType_.getNumber());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(2, salePercentOff_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeFloatSize(3, multipleOfRecruitsBaseReward_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static com.lvl6.proto.AdminProto.AdminChangeRequestProto parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.lvl6.proto.AdminProto.AdminChangeRequestProto parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.lvl6.proto.AdminProto.AdminChangeRequestProto parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.lvl6.proto.AdminProto.AdminChangeRequestProto parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.lvl6.proto.AdminProto.AdminChangeRequestProto parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.lvl6.proto.AdminProto.AdminChangeRequestProto parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.lvl6.proto.AdminProto.AdminChangeRequestProto parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.lvl6.proto.AdminProto.AdminChangeRequestProto parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.lvl6.proto.AdminProto.AdminChangeRequestProto parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.lvl6.proto.AdminProto.AdminChangeRequestProto parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.lvl6.proto.AdminProto.AdminChangeRequestProto prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements com.lvl6.proto.AdminProto.AdminChangeRequestProtoOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.lvl6.proto.AdminProto.internal_static_com_lvl6_proto_AdminChangeRequestProto_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.lvl6.proto.AdminProto.internal_static_com_lvl6_proto_AdminChangeRequestProto_fieldAccessorTable;
      }
      
      // Construct using com.lvl6.proto.AdminProto.AdminChangeRequestProto.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        staticDataReloadType_ = com.lvl6.proto.AdminProto.AdminChangeRequestProto.StaticDataReloadType.ALL;
        bitField0_ = (bitField0_ & ~0x00000001);
        salePercentOff_ = 0;
        bitField0_ = (bitField0_ & ~0x00000002);
        multipleOfRecruitsBaseReward_ = 0F;
        bitField0_ = (bitField0_ & ~0x00000004);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.lvl6.proto.AdminProto.AdminChangeRequestProto.getDescriptor();
      }
      
      public com.lvl6.proto.AdminProto.AdminChangeRequestProto getDefaultInstanceForType() {
        return com.lvl6.proto.AdminProto.AdminChangeRequestProto.getDefaultInstance();
      }
      
      public com.lvl6.proto.AdminProto.AdminChangeRequestProto build() {
        com.lvl6.proto.AdminProto.AdminChangeRequestProto result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private com.lvl6.proto.AdminProto.AdminChangeRequestProto buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        com.lvl6.proto.AdminProto.AdminChangeRequestProto result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public com.lvl6.proto.AdminProto.AdminChangeRequestProto buildPartial() {
        com.lvl6.proto.AdminProto.AdminChangeRequestProto result = new com.lvl6.proto.AdminProto.AdminChangeRequestProto(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.staticDataReloadType_ = staticDataReloadType_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.salePercentOff_ = salePercentOff_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.multipleOfRecruitsBaseReward_ = multipleOfRecruitsBaseReward_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.lvl6.proto.AdminProto.AdminChangeRequestProto) {
          return mergeFrom((com.lvl6.proto.AdminProto.AdminChangeRequestProto)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(com.lvl6.proto.AdminProto.AdminChangeRequestProto other) {
        if (other == com.lvl6.proto.AdminProto.AdminChangeRequestProto.getDefaultInstance()) return this;
        if (other.hasStaticDataReloadType()) {
          setStaticDataReloadType(other.getStaticDataReloadType());
        }
        if (other.hasSalePercentOff()) {
          setSalePercentOff(other.getSalePercentOff());
        }
        if (other.hasMultipleOfRecruitsBaseReward()) {
          setMultipleOfRecruitsBaseReward(other.getMultipleOfRecruitsBaseReward());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 8: {
              int rawValue = input.readEnum();
              com.lvl6.proto.AdminProto.AdminChangeRequestProto.StaticDataReloadType value = com.lvl6.proto.AdminProto.AdminChangeRequestProto.StaticDataReloadType.valueOf(rawValue);
              if (value == null) {
                unknownFields.mergeVarintField(1, rawValue);
              } else {
                bitField0_ |= 0x00000001;
                staticDataReloadType_ = value;
              }
              break;
            }
            case 16: {
              bitField0_ |= 0x00000002;
              salePercentOff_ = input.readInt32();
              break;
            }
            case 29: {
              bitField0_ |= 0x00000004;
              multipleOfRecruitsBaseReward_ = input.readFloat();
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // optional .com.lvl6.proto.AdminChangeRequestProto.StaticDataReloadType staticDataReloadType = 1;
      private com.lvl6.proto.AdminProto.AdminChangeRequestProto.StaticDataReloadType staticDataReloadType_ = com.lvl6.proto.AdminProto.AdminChangeRequestProto.StaticDataReloadType.ALL;
      public boolean hasStaticDataReloadType() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public com.lvl6.proto.AdminProto.AdminChangeRequestProto.StaticDataReloadType getStaticDataReloadType() {
        return staticDataReloadType_;
      }
      public Builder setStaticDataReloadType(com.lvl6.proto.AdminProto.AdminChangeRequestProto.StaticDataReloadType value) {
        if (value == null) {
          throw new NullPointerException();
        }
        bitField0_ |= 0x00000001;
        staticDataReloadType_ = value;
        onChanged();
        return this;
      }
      public Builder clearStaticDataReloadType() {
        bitField0_ = (bitField0_ & ~0x00000001);
        staticDataReloadType_ = com.lvl6.proto.AdminProto.AdminChangeRequestProto.StaticDataReloadType.ALL;
        onChanged();
        return this;
      }
      
      // optional int32 salePercentOff = 2;
      private int salePercentOff_ ;
      public boolean hasSalePercentOff() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public int getSalePercentOff() {
        return salePercentOff_;
      }
      public Builder setSalePercentOff(int value) {
        bitField0_ |= 0x00000002;
        salePercentOff_ = value;
        onChanged();
        return this;
      }
      public Builder clearSalePercentOff() {
        bitField0_ = (bitField0_ & ~0x00000002);
        salePercentOff_ = 0;
        onChanged();
        return this;
      }
      
      // optional float multipleOfRecruitsBaseReward = 3;
      private float multipleOfRecruitsBaseReward_ ;
      public boolean hasMultipleOfRecruitsBaseReward() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      public float getMultipleOfRecruitsBaseReward() {
        return multipleOfRecruitsBaseReward_;
      }
      public Builder setMultipleOfRecruitsBaseReward(float value) {
        bitField0_ |= 0x00000004;
        multipleOfRecruitsBaseReward_ = value;
        onChanged();
        return this;
      }
      public Builder clearMultipleOfRecruitsBaseReward() {
        bitField0_ = (bitField0_ & ~0x00000004);
        multipleOfRecruitsBaseReward_ = 0F;
        onChanged();
        return this;
      }
      
      // @@protoc_insertion_point(builder_scope:com.lvl6.proto.AdminChangeRequestProto)
    }
    
    static {
      defaultInstance = new AdminChangeRequestProto(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:com.lvl6.proto.AdminChangeRequestProto)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_lvl6_proto_AdminChangeRequestProto_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_lvl6_proto_AdminChangeRequestProto_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\013Admin.proto\022\016com.lvl6.proto\"\312\003\n\027AdminC" +
      "hangeRequestProto\022Z\n\024staticDataReloadTyp" +
      "e\030\001 \001(\0162<.com.lvl6.proto.AdminChangeRequ" +
      "estProto.StaticDataReloadType\022\026\n\016salePer" +
      "centOff\030\002 \001(\005\022$\n\034multipleOfRecruitsBaseR" +
      "eward\030\003 \001(\002\"\224\002\n\024StaticDataReloadType\022\007\n\003" +
      "ALL\020\000\022\025\n\021BUILD_STRUCT_JOBS\020\001\022\n\n\006CITIES\020\002" +
      "\022\024\n\020DEFEAT_TYPE_JOBS\020\003\022\r\n\tEQUIPMENT\020\004\022\n\n" +
      "\006QUESTS\020\005\022\033\n\027TASK_EQUIP_REQUIREMENTS\020\006\022\t" +
      "\n\005TASKS\020\007\022\027\n\023UPGRADE_STRUCT_JOBS\020\010\022\016\n\nST",
      "RUCTURES\020\t\022\026\n\022POSSESS_EQUIP_JOBS\020\n\022\036\n\032LE" +
      "VELS_REQUIRED_EXPERIENCE\020\013\022\026\n\022NEUTRAL_CI" +
      "TY_ELEMS\020\014B\014B\nAdminProto"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_com_lvl6_proto_AdminChangeRequestProto_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_com_lvl6_proto_AdminChangeRequestProto_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_com_lvl6_proto_AdminChangeRequestProto_descriptor,
              new java.lang.String[] { "StaticDataReloadType", "SalePercentOff", "MultipleOfRecruitsBaseReward", },
              com.lvl6.proto.AdminProto.AdminChangeRequestProto.class,
              com.lvl6.proto.AdminProto.AdminChangeRequestProto.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
