package com.github.sanctum.clans.construct.util;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Savable;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.panther.file.Configurable;
import java.io.IOException;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Reservoir extends Savable {

	Entity getEntity();

	Clan getOwner();

	double getPower();

	double getMaxPower();

	void add(double amount);

	void take(double amount);

	void set(double amount);

	void adapt(@NotNull Clan clan);

	boolean destroy();

	default boolean isDamaged() {
		return getPower() < getMaxPower();
	}

	static @Nullable Reservoir get(@NotNull Entity entity) {
		return ReservoirRegistry.map.get(entity);
	}

	static @Nullable Reservoir get(@NotNull Clan clan) {
		return ReservoirRegistry.map.values().stream().filter(r -> r.getOwner() != null && r.getOwner().equals(clan)).findFirst().orElse(null);
	}

	static @NotNull Reservoir of(@NotNull Entity entity) {
		return ReservoirRegistry.map.computeIfAbsent(entity, () -> new Reservoir() {

			double power = 250;
			double maxPower = 10000;
			Clan owner;

			{
				FileManager manager = ClansAPI.getInstance().getFileList().get("reservoirs", "Configuration/Data", Configurable.Type.JSON);
				if (manager.getRoot().exists()) {
					if (manager.getRoot().isNode(entity.getUniqueId().toString())) {
						this.power = manager.getRoot().getNode(entity.getUniqueId().toString()).getNode("power").toPrimitive().getDouble();
					}
				} else {
					try {
						manager.getRoot().create();
					} catch (IOException e) {
						ClansAPI.getInstance().getPlugin().getLogger().severe("Unable to create reservoirs.json file.");
					}
				}
				Location above = new Location(entity.getLocation().getWorld(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getYaw(), entity.getLocation().getPitch()).add(0, 1.5, 0);
				entity.getWorld().getNearbyEntities(above, 1, 3, 1).forEach(e -> {
					if (e instanceof ArmorStand) {
						if (e.isCustomNameVisible()) {
							e.remove();
						}
					}
				});
			}

			@Override
			public void save() {
				FileManager manager = ClansAPI.getInstance().getFileList().get("reservoirs", "Configuration/Data", Configurable.Type.JSON);
				manager.write(t -> t.set(entity.getUniqueId() + ".power", power));
			}

			@Override
			public void remove() {
				FileManager manager = ClansAPI.getInstance().getFileList().get("reservoirs", "Configuration/Data", Configurable.Type.JSON);
				manager.getRoot().getNode(entity.getUniqueId().toString()).delete();
				manager.getRoot().save();
				destroy();
			}

			@Override
			public Entity getEntity() {
				return entity;
			}

			@Override
			public Clan getOwner() {
				return owner;
			}

			@Override
			public double getPower() {
				return power;
			}

			@Override
			public double getMaxPower() {
				return this.maxPower;
			}

			@Override
			public void add(double amount) {
				this.power += amount;
			}

			@Override
			public void take(double amount) {
				this.power = Math.max(0, this.power - amount);
			}

			@Override
			public void set(double amount) {
				this.power = Math.max(0, amount);
			}

			@Override
			public void adapt(@NotNull Clan clan) {
				this.owner = clan;
			}

			@Override
			public boolean destroy() {
				if (getEntity() != null && getEntity().isValid()) {
					ReservoirRegistry.map.remove(getEntity());
					getEntity().remove();
					return true;
				}
				return false;
			}
		});
	}

}
