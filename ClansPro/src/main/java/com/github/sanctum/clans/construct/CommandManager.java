package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.panther.container.ImmutablePantherCollection;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherEntryMap;
import com.github.sanctum.panther.container.PantherMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CommandManager {

	final PantherMap<String, ClanSubCommand> commands = new PantherEntryMap<>();

	public PantherCollection<ClanSubCommand> getCommands() {
		return ImmutablePantherCollection.of(commands.values());
	}

	public @Nullable ClanSubCommand getCommand(@NotNull String label) {
		return commands.get(label);
	}

	public void register(@NotNull ClanSubCommand subCommand) {
		commands.computeIfAbsent(subCommand.getLabel(), subCommand);
	}

	public void unregister(@NotNull String label) {
		commands.remove(label);
	}

}
