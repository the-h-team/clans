package com.github.sanctum.clans.bridge.internal.kingdoms;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.labyrinth.library.Items;
import com.github.sanctum.labyrinth.library.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface Reward<T> {

	Reward<ItemStack> ITEM = new Reward<ItemStack>() {
		@Override
		public ItemStack get() {
			return Items.edit().build();
		}

		@Override
		public void give(Kingdom kingdom) {

		}

		@Override
		public void give(Clan clan) {

		}

		@Override
		public void give(Clan.Associate associate) {

		}
	};

	Reward<Double> MONEY = new Reward<Double>() {
		@Override
		public Double get() {
			return 0.0D;
		}

		@Override
		public void give(Kingdom kingdom) {

		}

		@Override
		public void give(Clan clan) {

		}

		@Override
		public void give(Clan.Associate associate) {

		}
	};

	T get();

	void give(Kingdom kingdom);

	void give(Clan clan);

	void give(Clan.Associate associate);

	static void assertReward(Reward<?> reward) {
		if (!ItemStack.class.isAssignableFrom(reward.get().getClass()) && !Double.class.isAssignableFrom(reward.get().getClass())) {
			throw new IllegalStateException("Reward: An invalid achievement reward was provided.");
		}
	}

	static @Nullable Reward<?> testType(String text) {
		if (StringUtils.use(text).containsIgnoreCase("item")) {
			return ITEM;
		}
		if (StringUtils.use(text).containsIgnoreCase("money")) {
			return MONEY;
		}
		return null;
	}

}
