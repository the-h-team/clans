package com.github.sanctum.clans.construct.extra.misc;

import com.github.sanctum.clans.construct.api.Insignia;

public class InsigniaError extends InstantiationException {

	private final String key;

	public InsigniaError(String key, String message) {
		super(message);
		this.key = key;
	}

	public Insignia getRegistration() {
		return Insignia.get(this.key);
	}

}
