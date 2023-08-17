package com.github.sanctum.clans.construct.impl.entity;

import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.ResidencyInfo;
import com.github.sanctum.clans.construct.impl.DefaultBlockMeta;
import com.github.sanctum.clans.construct.impl.DefaultBrokenBlock;
import com.github.sanctum.clans.construct.impl.DefaultPlacedBlock;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DefaultClaimResident implements Claim.Resident {

	private final Player inhabitant;
	private final ResidencyInfo info;

	public DefaultClaimResident(Player inhabitant) {
		this.inhabitant = inhabitant;
		this.info = new ResidencyInfo(this);
	}

	public @NotNull Player getPlayer() {
		return inhabitant;
	}

	@Override
	public @NotNull ResidencyInfo getInfo() {
		return this.info;
	}

}
