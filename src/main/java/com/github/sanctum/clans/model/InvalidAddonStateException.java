package com.github.sanctum.clans.model;

public class InvalidAddonStateException extends ClanError {
	private static final long serialVersionUID = -3319446608602315912L;

	public InvalidAddonStateException(String message) {
		super(message);
	}
}
