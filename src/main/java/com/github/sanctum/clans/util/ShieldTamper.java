package com.github.sanctum.clans.util;

/**
 * Used to encapsulate live raid shield modifications from the GUI.
 */
public class ShieldTamper {

	private int up;

	private boolean isOff;

	private int down;

	public void setUpOverride(int up) {
		this.up = up;
	}

	public void setDownOverride(int down) {
		this.down = down;
	}

	public int getUpTime() {
		return up;
	}

	public int getDownTime() {
		return down;
	}

	public void setIsOff(boolean isOff) {
		this.isOff = isOff;
	}

	public boolean isOff() {
		return isOff;
	}


}
