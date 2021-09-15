package com.github.sanctum.clans.bridge.internal.kingdoms.achievement;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.internal.kingdoms.Kingdom;
import com.github.sanctum.clans.bridge.internal.kingdoms.Progressable;
import com.github.sanctum.clans.bridge.internal.kingdoms.Reward;
import com.github.sanctum.clans.bridge.internal.kingdoms.RoundTable;
import com.github.sanctum.clans.bridge.internal.kingdoms.event.KingdomJobCompleteEvent;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PersistentAchievement implements KingdomAchievement {

	private double progression;

	private boolean complete;

	private Progressable parent;

	private Reward<?> reward;

	private final double requirement;

	private final String title;

	private final Set<Player> players;

	private final String description;

	public PersistentAchievement(String title, String description, double progression, double requirement) {
		this.title = title;
		this.players = new HashSet<>();
		this.description = description;
		this.progression = progression;
		this.requirement = requirement;
	}

	@Override
	public @NotNull String getTitle() {
		return this.title;
	}

	@Override
	public @NotNull String getDescription() {
		return this.description;
	}

	@Override
	public @Nullable Progressable getParent() {
		return parent;
	}

	@Override
	public double getRequirement() {
		return this.requirement;
	}

	@Override
	public double getProgression() {
		return this.progression;
	}

	@Override
	public Reward<?> getReward() {
		return reward;
	}

	@Override
	public double progress(double amount) {
		if (this.progression + amount > this.requirement) {
			return 0;
		}
		return this.progression += amount;
	}

	@Override
	public double unprogress(double amount) {
		double d = Math.max(0, this.progression - amount);
		this.progression = d;
		return d;
	}

	@Override
	public void saveProgress(String path) {
		ClanAddon cycle = ClanAddonQuery.getAddon("Kingdoms");
		FileManager file = cycle.getFile(FileType.JSON, "achievements", "data");

		file.getRoot().set(path + "." + getTitle() + ".info", this.getDescription());

		file.getRoot().set(path + "." + getTitle() + ".progression", this.getProgression());

		file.getRoot().set(path + "." + getTitle() + ".requirement", this.getRequirement());

		file.getRoot().save();

	}

	@Override
	public void delete(String path) {
		ClanAddon cycle = ClanAddonQuery.getAddon("Kingdoms");
		FileManager file = cycle.getFile(FileType.JSON, "achievements", "data");
		file.getRoot().set(path, null);
		file.getRoot().save();
	}

	@Override
	public double getPercentage() {
		return Math.round(this.progression * 100 / this.requirement * 100.0) / 100.0;
	}

	@Override
	public boolean activated(Player p) {
		return this.players.contains(p);
	}

	@Override
	public boolean activate(Player p) {
		if (!this.players.contains(p)) {
			return this.players.add(p);
		}
		return false;
	}

	@Override
	public boolean deactivate(Player p) {
		if (this.players.contains(p)) {
			return this.players.remove(p);
		}
		return false;
	}

	@Override
	public boolean isComplete() {

		if (getPercentage() >= 100) {
			if (!this.complete) {
				if (this.parent != null) {
					if (Kingdom.class.isAssignableFrom(this.parent.getClass())) {
						ClanVentBus.call(new KingdomJobCompleteEvent((Kingdom) this.parent, this));
					}
					if (RoundTable.class.isAssignableFrom(this.parent.getClass())) {
						//ClanVentBus.call(new KingdomJobCompleteEvent((Kingdom)this.parent, this));
					}
				}

				this.complete = true;
			}
		}

		return this.complete;
	}

	@Override
	public void setReward(Reward<?> type, Object reward) {
		if (type.get().getClass().isAssignableFrom(reward.getClass())) {
			Reward.assertReward(type);
			if (Double.class.isAssignableFrom(reward.getClass())) {
				this.reward = new Reward<Double>() {
					@Override
					public Double get() {
						return (Double) reward;
					}

					@Override
					public void give(Kingdom kingdom) {
						kingdom.getMembers().forEach(this::give);
					}

					@Override
					public void give(Clan clan) {
						clan.forEach(this::give);
					}

					@Override
					public void give(Clan.Associate associate) {
						Optional.ofNullable(associate.getPlayer().getPlayer()).ifPresent(p -> {
							if (EconomyProvision.getInstance().isValid()) {
								EconomyProvision.getInstance().deposit(BigDecimal.valueOf(get()), p, p.getWorld().getName());
							}
						});
					}
				};
			} else {
				this.reward = new Reward<ItemStack>() {
					@Override
					public ItemStack get() {
						return (ItemStack) reward;
					}

					@Override
					public void give(Kingdom kingdom) {
						kingdom.getMembers().forEach(this::give);
					}

					@Override
					public void give(Clan clan) {
						clan.forEach(this::give);
					}

					@Override
					public void give(Clan.Associate associate) {
						Optional.ofNullable(associate.getPlayer().getPlayer()).ifPresent(p -> {
							p.getWorld().dropItem(p.getLocation(), get());
						});
					}
				};
			}
		} else
			throw new IllegalArgumentException("KingdomAchievement: An invalid reward type was provided, expected [ITEM, MONEY]");
	}

	@Override
	public void setParent(Progressable k) {
		this.parent = k;
	}

}
