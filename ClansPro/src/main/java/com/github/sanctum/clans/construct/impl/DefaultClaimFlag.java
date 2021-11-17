package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.labyrinth.data.service.AccessibleConstants;
import com.github.sanctum.labyrinth.data.service.Constant;

public final class DefaultClaimFlag {

	public static final Claim.Flag OWNER_ONLY = new Claim.Flag("owner-only", false) {
		private static final long serialVersionUID = -2457820931594042806L;
	};

	public static final Claim.Flag NO_FLAMMABLES = new Claim.Flag("no-flammables", false) {
		private static final long serialVersionUID = -1963184579094331210L;
	};

	public static final Claim.Flag NO_EXPLOSIVES = new Claim.Flag("no-explosives", false) {
		private static final long serialVersionUID = -2994029703433239569L;
	};

	public static final Claim.Flag CUSTOM_TITLES = new Claim.Flag("custom-titles", false) {
		private static final long serialVersionUID = -5626158771460431576L;
	};

	public static Claim.Flag[] values() {
		return AccessibleConstants.of(DefaultClaimFlag.class).get(Claim.Flag.class).deploy().submit().join().stream().map(Constant::getValue).toArray(Claim.Flag[]::new);
	}

	public static Claim.Flag valueOf(String name) {
		for (Constant<Claim.Flag> constant : AccessibleConstants.of(DefaultClaimFlag.class).get(Claim.Flag.class).deploy().submit().join()) {
			if (constant.getName().equals(name)) {
				return constant.getValue();
			}
		}
		return null;
	}

}
