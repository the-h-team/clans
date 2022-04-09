package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.labyrinth.data.container.ImmutableLabyrinthCollection;
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.labyrinth.data.container.LabyrinthEntryMap;
import com.github.sanctum.labyrinth.data.container.LabyrinthMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CommandManager {

	final LabyrinthMap<String, ClanSubCommand> commands = new LabyrinthEntryMap<>();

	public LabyrinthCollection<ClanSubCommand> getCommands() {
		return ImmutableLabyrinthCollection.of(commands.values());
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
