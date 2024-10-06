package com.github.sanctum.clans.model;

import com.github.sanctum.clans.util.ClanError;

public class InvalidAddonStateException extends ClanError {
	private static final long serialVersionUID = -3319446608602315912L;

	public InvalidAddonStateException(String message) {
		super(message);
	}
}
