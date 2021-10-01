package com.github.sanctum.clans.bridge;

public class InvalidAddonException extends Exception {

	private static final long serialVersionUID = -2390940730270348143L;

	public InvalidAddonException(String message) {
		super(message);
	}

	public InvalidAddonException(String message, Throwable cause) {
		super(message, cause);
	}

}
