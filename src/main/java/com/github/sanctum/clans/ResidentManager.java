package com.github.sanctum.clans;

import com.github.sanctum.clans.model.Claim;
import com.github.sanctum.panther.container.ImmutablePantherCollection;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherSet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ResidentManager {

	private final ClaimManager claimManager;
	private final PantherSet<Claim.Resident> residents = new PantherSet<>();

	public ResidentManager(@NotNull ClaimManager claimManager) {
		this.claimManager = claimManager;
	}

	public void load(@NotNull Claim.Resident resident) {
		residents.add(resident);
	}

	public void remove(@NotNull Claim.Resident resident) {
		residents.remove(resident);
	}

	public @Nullable Claim.Resident getResident(@NotNull Player player) {
		return residents.stream().filter(r -> r.getPlayer().getName().equals(player.getName())).findFirst().orElse(null);
	}

	public @NotNull PantherCollection<Claim.Resident> getResidents() {
		return ImmutablePantherCollection.of(residents);
	}

}
