package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.construct.util.ShieldTamper;

/**
 * The manager for the state of the raid-shield.
 */
public class ShieldManager {

	private final ShieldTamper TAMPER;

	private boolean ENABLED;

	public ShieldManager() {
		this.ENABLED = false;
		this.TAMPER = new ShieldTamper();
	}

	/**
	 * @return true if the raid shield is currently up.
	 */
	public boolean isEnabled() {
		return ENABLED;
	}

	/**
	 * Update the status of the raid shield.
	 *
	 * @param enabled the state to set the raid shield to.
	 */
	public void setEnabled(boolean enabled) {
		this.ENABLED = enabled;
	}

	/**
	 * Gets the raid shield tampering object.
	 *
	 * @return The current GUI shield tamper.
	 */
	public ShieldTamper getTamper() {
		return TAMPER;
	}
}
