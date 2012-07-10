package com.lvl6.info;

import java.io.Serializable;

public class ValidLocationBox implements Serializable {
	private static final long serialVersionUID = 2675770546399582688L;
	private double botLeftX;
	private double botLeftY;
	private double width;
	private double height;
	private String location;

	public ValidLocationBox(double botLeftX, double botLeftY, double width,
			double height, String location) {
		this.botLeftX = botLeftX;
		this.botLeftY = botLeftY;
		this.width = width;
		this.height = height;
		this.location = location;
	}

	public double getBotLeftX() {
		return botLeftX;
	}

	public double getBotLeftY() {
		return botLeftY;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public String getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return "ValidLocationBox [botLeftX=" + botLeftX + ", botLeftY="
				+ botLeftY + ", width=" + width + ", height=" + height
				+ ", location=" + location + "]";
	}
}
