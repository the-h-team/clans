package com.github.sanctum.clans.model;

public class ClanError extends Error {

	private static final long serialVersionUID = 1182974750638645627L;

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
