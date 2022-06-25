package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.labyrinth.placeholders.Placeholder;
import com.github.sanctum.labyrinth.placeholders.PlaceholderIdentifier;
import com.github.sanctum.labyrinth.placeholders.PlaceholderTranslation;
import com.github.sanctum.labyrinth.placeholders.PlaceholderVariable;
import org.jetbrains.annotations.NotNull;

public class LabyrinthPlaceholders implements PlaceholderTranslation {

	public ClansJavaPlugin plugin;
	private final PlaceholderIdentifier identifier = () -> "clanspro";
	private final Placeholder[] placeholders = new Placeholder[]{Placeholder.ANGLE_BRACKETS, Placeholder.CURLEY_BRACKETS, Placeholder.PERCENT};

	public LabyrinthPlaceholders(ClansJavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public @NotNull PlaceholderIdentifier getIdentifier() {
		return identifier;
	}

	@Override
	public @NotNull Placeholder[] getPlaceholders() {
		return placeholders;
	}

	@Override
	public String onTranslation(String parameter, PlaceholderVariable variable) {
		return UnifiedPlaceholders.getInstance().translate(parameter, variable.isPlayer() ? variable.getAsPlayer() : null);
	}
}
	
	

