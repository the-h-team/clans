package com.github.sanctum.clans.bridge.internal.traits.structure;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import com.github.sanctum.labyrinth.formatting.string.FormattedString;
import com.github.sanctum.labyrinth.library.Entities;
import com.github.sanctum.panther.util.RandomObject;
import com.github.sanctum.panther.util.SpecialID;
import java.util.Random;
import java.util.function.BiConsumer;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.jetbrains.annotations.NotNull;

public class TamerWolf {

	final BiConsumer<Wolf, Player> consumer = (wolf, p) -> {
		wolf.setAdult();
		wolf.setTamed(true);
		wolf.setOwner(p);
		wolf.setBreed(false);
		wolf.setCustomName(new FormattedString(ClansAPI.getInstance().getPrefix().toString() + " &eTamed &7#" + SpecialID.builder().setLength(4).build(wolf.getUniqueId())).color().get());
		wolf.setCustomNameVisible(true);
		wolf.setCollarColor(new RandomObject<>(DyeColor.values()).get(new Random().nextInt(16)));
		wolf.setHealth(20);
		wolf.setCanPickupItems(false);
	};
	final TraitHolder holder;

	public TamerWolf(@NotNull TraitHolder holder) {
		this.holder = holder;
	}

	public void spawn(@NotNull Location location) {
		Wolf wolf = Entities.WOLF.spawn(location);
		location.getWorld().spawnParticle(Particle.SMOKE_LARGE, location, 1);
		PlayerSearch tandem = PlayerSearch.of(holder.getName());
		if (tandem != null && tandem.isOnline()) {
			consumer.accept(wolf, tandem.getPlayer().getPlayer());
		} else {
			ClansAPI.getInstance().getPlugin().getLogger().warning("Unable to spawn tamed wolf @ location (" + location + ") due to tamer being offline.");
		}
	}


}
