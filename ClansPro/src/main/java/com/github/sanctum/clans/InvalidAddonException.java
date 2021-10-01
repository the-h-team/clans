package com.github.sanctum.clans;

public class InvalidAddonException extends Exception {

	public InvalidAddonException(String message) {
		super(message);
	}

	public InvalidAddonException(String message, Throwable cause) {
		super(message, cause);
	}

}
