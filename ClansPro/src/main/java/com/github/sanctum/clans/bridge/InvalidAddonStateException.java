package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.extra.ClanError;

public class InvalidAddonStateException extends ClanError {
	private static final long serialVersionUID = -3319446608602315912L;

	public InvalidAddonStateException(String message) {
		super(message);
	}
}
