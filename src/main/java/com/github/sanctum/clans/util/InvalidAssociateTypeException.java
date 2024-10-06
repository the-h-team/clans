package com.github.sanctum.clans.util;

import org.jetbrains.annotations.NotNull;

public class InvalidAssociateTypeException extends RuntimeException {

	private static final long serialVersionUID = 6434013997656941248L;

	public InvalidAssociateTypeException(@NotNull String message) {
		super(message);
	}

}
