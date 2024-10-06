package com.github.sanctum.clans.impl.entity;

import com.github.sanctum.clans.model.Claim;
import com.github.sanctum.clans.model.ResidentInformation;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DefaultClaimResident implements Claim.Resident {

	private final Player inhabitant;
	private final ResidentInformation info;

	public DefaultClaimResident(Player inhabitant) {
		this.inhabitant = inhabitant;
		this.info = new ResidentInformation(this);
	}

	public @NotNull Player getPlayer() {
		return inhabitant;
	}

	@Override
	public @NotNull ResidentInformation getInfo() {
		return this.info;
	}

}
