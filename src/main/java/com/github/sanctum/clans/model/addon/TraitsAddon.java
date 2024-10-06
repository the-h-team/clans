package com.github.sanctum.clans.model.addon;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.addon.traits.TraitManager;
import com.github.sanctum.clans.model.addon.traits.command.TraitCommand;
import com.github.sanctum.clans.model.addon.traits.listener.AbilitiesListener;
import com.github.sanctum.clans.model.addon.traits.structure.TraitHolder;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.labyrinth.data.service.PlayerSearch;
import com.github.sanctum.labyrinth.formatting.string.FormattedString;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class TraitsAddon extends Clan.Addon {
	@Override
	public void onLoad() {
		getContext().stage(new AbilitiesListener());
		getContext().stage(new TraitCommand());
	}

	@Override
	public void onEnable() {
		PlayerSearch.values().forEach(search -> TraitManager.getInstance().getOrCreate(search.getPlayer()));
	}

	@Override
	public void onDisable() {
		TraitManager.getInstance().getAll().forEach(TraitHolder::save);
	}

	@Override
	public boolean isPersistent() {
		return ClansAPI.getDataInstance().isTrue("Addon.Traits.enabled") && !getApi().isTrial();
	}

	@Override
	public @NotNull String getName() {
		return "Traits";
	}

	@Override
	public @NotNull String getDescription() {
		return "An addon that introduces a class and skill trait system, complimenting kingdoms.";
	}

	@Override
	public @NotNull String getVersion() {
		return "1.0";
	}

	@Override
	public @NotNull String[] getAuthors() {
		return new String[]{"Hempfest"};
	}

	@Override
	public String onPlaceholder(OfflinePlayer player, String param) {
		if (player != null) {
			FormattedString string = new FormattedString(param);
			TraitHolder holder = TraitManager.getInstance().get(player);
			if (string.contains("member_primary_trait")) {
				return holder.getPrimary().getName();
			}
			if (string.contains("member_secondary_trait")) {
				return holder.getSecondary() != null ? holder.getSecondary().getName() : "Not assigned.";
			}
		}
		return null;
	}
}
