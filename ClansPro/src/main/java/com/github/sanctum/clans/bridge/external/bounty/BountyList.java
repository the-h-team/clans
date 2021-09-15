package com.github.sanctum.clans.bridge.external.bounty;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileManager;
import java.util.Optional;
import java.util.UUID;

public final class BountyList {

	public static Bounty get(Clan charge, UUID target) {
		FileManager clanFile = ClansAPI.getData().getClanFile(charge);
		double result = clanFile.getRoot().getDouble("bounties." + target.toString());
		return result > 0 ? new Bounty(charge, target, result) : null;
	}

	public static Optional<Bounty> get(UUID target) {
		Optional<Bounty> result = Optional.empty();
		for (Clan cache : ClansAPI.getInstance().getClanManager().getClans().list()) {
			if (get(cache, target) != null) {
				result = Optional.ofNullable(get(cache, target));
				break;
			}
		}
		return result;
	}

}
