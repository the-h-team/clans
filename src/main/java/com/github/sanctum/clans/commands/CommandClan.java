package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.model.ClanAddonRegistry;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.GUI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.google.common.collect.MapMaker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandClan extends Command implements Message.Factory {

	private final ConcurrentMap<Player, List<UUID>> blockedUsers = new MapMaker().
			weakKeys().
			weakValues().
			makeMap();

	public CommandClan() {
		super("clan");
		setDescription("Base command for clans.");
		List<String> array = ClansAPI.getDataInstance().getConfig().getRoot().getStringList("Formatting.aliase");
		array.addAll(Arrays.asList("clans", "cl", "c", "f", "faction"));
		setAliases(array);
	}

	private void sendMessage(CommandSender player, String message) {
		player.sendMessage(Clan.ACTION.color(message));
	}


	private boolean isAlphaNumeric(String s) {
		return s != null && s.matches("^[a-zA-Z0-9]*$");
	}

	@Override
	public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) throws IllegalArgumentException {

		Player p = (Player) sender;

		List<String> result = new ArrayList<>();

		for (Clan.Addon cycle : ClanAddonRegistry.getInstance().get()) {
			if (cycle.getContext().isActive()) {
				for (ClanSubCommand subCommand : cycle.getContext().getCommands()) {
					if (subCommand.tab(p, alias, args) != null) {
						result.addAll(subCommand.tab(p, alias, args));
					}
				}
			}
		}
		for (ClanSubCommand subCommand : ClansAPI.getInstance().getCommandManager().getCommands()) {
			if (subCommand.tab(p, alias, args) != null) {
				result.addAll(subCommand.tab(p, alias, args));
			}
		}
		return result;
	}

	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
		if (!(sender instanceof Player)) {
			if (sender instanceof ConsoleCommandSender) {

				for (Clan.Addon cycle : ClanAddonRegistry.getInstance().get()) {
					if (cycle.getContext().isActive()) {
						for (ClanSubCommand subCommand : cycle.getContext().getCommands()) {
							if (subCommand.getLabel() != null) {
								if (args.length > 0) {
									if (subCommand.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(args[0])) || subCommand.getLabel().equalsIgnoreCase(args[0])) {
										List<String> t = new LinkedList<>(Arrays.asList(args));
										t.removeIf(s -> StringUtils.use(s).containsIgnoreCase(subCommand.getLabel()));
										for (String st : subCommand.getAliases()) {
											t.removeIf(s -> StringUtils.use(s).containsIgnoreCase(st));
										}
										subCommand.setLastLabel(label);
										return subCommand.console(sender, subCommand.getLabel(), t.toArray(new String[0]));
									}
								}
							}
						}
					}
				}
				for (ClanSubCommand subCommand : ClansAPI.getInstance().getCommandManager().getCommands()) {
					if (subCommand.getLabel() != null) {
						if (args.length > 0) {
							if (subCommand.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(args[0])) || subCommand.getLabel().equalsIgnoreCase(args[0])) {
								List<String> t = new LinkedList<>(Arrays.asList(args));
								t.removeIf(s -> StringUtils.use(s).containsIgnoreCase(subCommand.getLabel()));
								subCommand.setLastLabel(label);
								return subCommand.console(sender, subCommand.getLabel(), t.toArray(new String[0]));
							}
						}
					}
				}
			}
		} else {
			int length = args.length;
			Player p = (Player) sender;
			StringLibrary lib = new StringLibrary();

			if (ClansAPI.getDataInstance().isTrue("Clans.use-whitelist")) {
				if (!LabyrinthProvider.getInstance().isModded()) {
					if (!ClansAPI.getDataInstance().getConfig().getRoot().getStringList("Clans.world-whitelist").contains(p.getWorld().getName())) {
						lib.sendMessage(p, "&4&oClan features have been locked within this world.");
						return true;
					}
				} else {
					// TODO: modded check
				}
			}

			if (length > 0) {
				for (Clan.Addon cycle : ClanAddonRegistry.getInstance().get()) {
					if (cycle.getContext().isActive()) {
						for (ClanSubCommand subCommand : cycle.getContext().getCommands()) {
							if (subCommand.getLabel() != null) {
								if (subCommand.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(args[0])) || subCommand.getLabel().equalsIgnoreCase(args[0])) {
									List<String> t = new LinkedList<>(Arrays.asList(args));
									t.removeIf(s -> StringUtils.use(s).containsIgnoreCase(subCommand.getLabel()));
									for (String st : subCommand.getAliases()) {
										t.removeIf(s -> StringUtils.use(s).containsIgnoreCase(st));
									}
									subCommand.setLastLabel(label);
									return subCommand.player(p, args[0], t.toArray(new String[0]));
								}
							}
						}
					}
				}
				for (ClanSubCommand subCommand : ClansAPI.getInstance().getCommandManager().getCommands()) {
					if (subCommand.getLabel() != null) {
						if (subCommand.getAliases().stream().anyMatch(s -> s.equalsIgnoreCase(args[0])) || subCommand.getLabel().equalsIgnoreCase(args[0])) {
							List<String> t = new LinkedList<>(Arrays.asList(args));
							t.removeIf(s -> s.equalsIgnoreCase(subCommand.getLabel()));
							for (String st : subCommand.getAliases()) {
								t.removeIf(s -> s.equalsIgnoreCase(st));
							}
							subCommand.setLastLabel(label);
							return subCommand.player(p, args[0], t.toArray(new String[0]));
						}
					}
				}
			} else {
				if (ClansAPI.getDataInstance().getMessages().read(n -> n.getNode("menu.enabled").toPrimitive().getBoolean())) {
					GUI.MAIN_MENU.get(p).open(p);
				} else p.performCommand("c help");
				return true;
			}
			lib.sendMessage(p, lib.commandUnknown(label));
		}
		return true;
	}
}
