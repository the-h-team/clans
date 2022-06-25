package com.github.sanctum.clans.construct.extra;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface Reservoir {

	Entity getEntity();

	double getPower();

	void add(double amount);

	void take(double amount);

	void set(double amount);

	boolean destroy();

	static @NotNull Reservoir get(@NotNull Entity entity) {
		return ReservoirBundle.map.get(entity);
	}

	static @NotNull Reservoir of(@NotNull Entity entity) {
		return ReservoirBundle.map.computeIfAbsent(entity, () -> new Reservoir() {
			double power;
			@Override
			public Entity getEntity() {
				return entity;
			}

			@Override
			public double getPower() {
				return power;
			}

			@Override
			public void add(double amount) {
				this.power += amount;
			}

			@Override
			public void take(double amount) {
				this.power -= amount;
			}

			@Override
			public void set(double amount) {
				this.power = amount;
			}

			@Override
			public boolean destroy() {
				if (getEntity() != null && getEntity().isValid()) {
					ReservoirBundle.map.remove(getEntity());
					getEntity().remove();
					return true;
				}
				return false;
			}
		});
	}

}
