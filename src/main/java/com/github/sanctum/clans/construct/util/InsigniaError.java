package com.github.sanctum.clans.construct.util;

import com.github.sanctum.clans.construct.api.Insignia;

public class InsigniaError extends InstantiationException {

	private static final long serialVersionUID = -2323418870626176815L;
	private final String key;

	public InsigniaError(String key, String message) {
		super(message);
		this.key = key;
	}

	public Insignia getRegistration() {
		return Insignia.get(this.key);
	}

}
