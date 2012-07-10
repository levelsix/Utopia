package com.lvl6.info;

import java.io.Serializable;

public class AnimatedSpriteOffset implements Serializable {

	private static final long serialVersionUID = 8547110329111943501L;
	private String imgName;
	private CoordinatePair offSet;

	public AnimatedSpriteOffset(String imgName, CoordinatePair offSet) {
		this.imgName = imgName;
		this.offSet = offSet;
	}

	public String getImgName() {
		return imgName;
	}

	public CoordinatePair getOffSet() {
		return offSet;
	}
}
