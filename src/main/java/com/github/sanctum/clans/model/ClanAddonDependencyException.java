package com.github.sanctum.clans.model;

import com.github.sanctum.clans.util.ClanError;

public class ClanAddonDependencyException extends ClanError {

	private static final long serialVersionUID = 202140587363917206L;

	public ClanAddonDependencyException(String msg) {
		super(msg);
	}

}
