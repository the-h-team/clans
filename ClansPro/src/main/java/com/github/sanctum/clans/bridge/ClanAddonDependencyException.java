package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClanError;

public class ClanAddonDependencyException extends ClanError {

	private static final long serialVersionUID = 202140587363917206L;

	public ClanAddonDependencyException(String msg) {
		super(msg);
	}

}
