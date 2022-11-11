package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.construct.api.Claim;
import java.util.HashSet;
import java.util.Set;

public final class FlagManager {

	private final ClaimManager claimManager;
	private final Set<Claim.Flag> flags = new HashSet<>();

	FlagManager(ClaimManager claimManager) {
		this.claimManager = claimManager;
	}

	public Set<Claim.Flag> getFlags() {
		return flags;
	}

	public boolean register(Claim.Flag... flag) {
		for (Claim.Flag f : flag) {
			if (!flags.add(f)) {
				return false;
			}
		}
		return true;
	}

	public void unregister(Claim.Flag flag) {
		for (Claim c : claimManager.getClaims()) {
			c.remove(flag);
		}
		flags.remove(flag);
	}

}
