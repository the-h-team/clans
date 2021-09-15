package com.github.sanctum.clans.construct.api;

public class ClanError extends Error {

	public ClanError() {
		super();
	}

	public ClanError(String message) {
		super(message);
	}

	public ClanError(String message, Throwable throwable) {
		super(message, throwable);
	}

}
