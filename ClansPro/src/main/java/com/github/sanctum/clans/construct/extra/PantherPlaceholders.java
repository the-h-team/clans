package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.panther.placeholder.Placeholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PantherPlaceholders implements Placeholder.Translation {

	public ClansJavaPlugin plugin;
	private final Placeholder.Identifier identifier = () -> "clans";
	private final Placeholder[] placeholders = new Placeholder[]{Placeholder.ANGLE_BRACKETS, Placeholder.CURLEY_BRACKETS, Placeholder.PERCENT};

	public PantherPlaceholders(ClansJavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public @NotNull Placeholder.Identifier getIdentifier() {
		return identifier;
	}

	@Override
	public @NotNull Placeholder[] getPlaceholders() {
		return placeholders;
	}

	@Override
	public String onTranslation(String parameter, Placeholder.Variable variable) {
		return UnifiedPlaceholders.getInstance().translate(parameter, variable.get() instanceof OfflinePlayer ? (OfflinePlayer) variable.get() : null);
	}
}
	
	

