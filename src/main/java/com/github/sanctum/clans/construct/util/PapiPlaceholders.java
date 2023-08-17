package com.github.sanctum.clans.construct.util;

import com.github.sanctum.clans.ClansJavaPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PapiPlaceholders extends PlaceholderExpansion {

	public ClansJavaPlugin plugin;

	public PapiPlaceholders(ClansJavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public @NotNull String getAuthor() {
		return plugin.getDescription().getAuthors().toString();
	}

	@Override
	public @NotNull String getIdentifier() {
		return "clans";
	}

	@Override
	public @NotNull String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, @NotNull String identifier) {
		return onRequest(player, identifier);
	}

	@Override
	public String onRequest(OfflinePlayer player, @NotNull String identifier) {
		return UnifiedPlaceholders.getInstance().translate(identifier, player);
	}
}
	
	

