package com.lvl6.info;

import java.io.Serializable;

import com.lvl6.proto.InfoProto.CritStructType;
import com.lvl6.proto.InfoProto.StructOrientation;

public class UserCritstruct implements Serializable {
	private static final long serialVersionUID = 6138379448783635860L;
	private CritStructType type;
	private CoordinatePair coords;
	private StructOrientation orientation;

	public UserCritstruct(CritStructType type, CoordinatePair coords,
			StructOrientation orientation) {
		this.type = type;
		this.coords = coords;
		this.orientation = orientation;
	}

	public CritStructType getType() {
		return type;
	}

	public CoordinatePair getCoords() {
		return coords;
	}

	public StructOrientation getOrientation() {
		return orientation;
	}

	@Override
	public String toString() {
		return "UserCritstruct [type=" + type + ", coords=" + coords
				+ ", orientation=" + orientation + "]";
	}
}
