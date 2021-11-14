package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.GUI;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.api.ClearanceLog;
import com.github.sanctum.clans.construct.api.Insignia;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.bank.BankAction;
import com.github.sanctum.clans.construct.bank.BankLog;
import com.github.sanctum.clans.construct.bank.BankPermissions;
import com.github.sanctum.clans.construct.extra.BukkitColor;
import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.clans.construct.extra.FancyLogoAppendage;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.event.associate.AssociateDisplayInfoEvent;
import com.github.sanctum.clans.event.associate.AssociateKickAssociateEvent;
import com.github.sanctum.clans.event.associate.AssociateRenameClanEvent;
import com.github.sanctum.clans.event.associate.AssociateUpdateBaseEvent;
import com.github.sanctum.clans.event.bank.messaging.Messages;
import com.github.sanctum.clans.event.command.CommandInformationAdaptEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.LabyrinthUser;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.PaginatedList;
import com.github.sanctum.labyrinth.formatting.TextChunk;
import com.github.sanctum.labyrinth.formatting.ToolTip;
import com.github.sanctum.labyrinth.formatting.component.OldComponent;
import com.github.sanctum.labyrinth.formatting.string.ColoredString;
import com.github.sanctum.labyrinth.formatting.string.RandomID;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Item;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.labyrinth.task.Schedule;
import com.google.common.base.Strings;
import com.google.common.collect.MapMaker;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.IllegalFormatException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
		array.addAll(Arrays.asList("clans", "cl", "c"));
		setAliases(array);
		setPermission("clanspro");
	}

	private void sendMessage(CommandSender player, String message) {
		player.sendMessage(Clan.ACTION.color(message));
	}

	private String notPlayer() {
		return String.format("[%s] - You aren't a player..", ClansAPI.getInstance().getPlugin().getDescription().getName());
	}

	private List<String> helpMenu(String label) {
		List<String> help = new ArrayList<>();
		FileManager msg = ClansAPI.getInstance().getFileList().get("Messages", "Configuration");
		for (String s : msg.getRoot().getNode("Commands").getKeys(false)) {
			help.add(msg.getRoot().getString("Commands." + s + ".text"));
		}
		CommandInformationAdaptEvent e = ClanVentBus.call(new CommandInformationAdaptEvent(help));
		return e.getMenu().stream().map(s -> s.replace("clan", label)).collect(Collectors.toList());
	}

	static int correctCharLength(char c) {
		switch (c) {
			case ':':
			case 'i':
				return 1;
			case 'l':
				return 2;
			case '*':
			case 't':
				return 3;
			case 'f':
			case 'k':
				return 4;
		}
		return 5;
	}

	static int getFixedLength(String string) {
		return string.chars().reduce(0, (p, i) -> p + correctCharLength((char) i) + 1);
	}

	private boolean isAlphaNumeric(String s) {
		return s != null && s.matches("^[a-zA-Z0-9]*$");
	}

	private final List<String> arguments = new ArrayList<>();

	@Override
	public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) throws IllegalArgumentException {

		Player p = (Player) sender;
		Optional<Clan.Associate> associate = ClansAPI.getInstance().getAssociate(p);

		List<String> result = new ArrayList<>();

		for (ClanAddon cycle : ClanAddonQuery.getRegisteredAddons()) {
			if (cycle.getContext().isActive()) {
				for (ClanSubCommand subCommand : cycle.getContext().getCommands()) {
					if (subCommand.tab(p, alias, args) != null) {
						result.addAll(subCommand.tab(p, alias, args));
					}
				}
			}
		}

		if (args.length == 1) {
			arguments.clear();
			List<String> add = Arrays.asList("create", "war", "logo", "permit", "permissions", "invite", "block", "peace", "forfeit", "surrender", "truce", "mode", "bio", "players", "description", "friendlyfire", "color", "password", "kick", "leave", "message", "chat", "info", "promote", "demote", "tag", "nickname", "list", "base", "setbase", "top", "claim", "unclaim", "passowner", "ally", "enemy", "bank");
			for (String a : add) {
				if (p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission(a))) {
					arguments.add(a);
				}
			}
			for (String a : arguments) {
				if (a.toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(a);
			}
			return result;
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("permit")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("permit"))) {
					return result;
				}
				arguments.clear();
				if (associate.isPresent()) {
					arguments.addAll(associate.get().getClan().getPermissions().stream().map(Map.Entry::getKey).map(Nameable::getName).map(String::toLowerCase).collect(Collectors.toList()));
				} else {
					arguments.addAll(Arrays.stream(Clearance.values()).map(Nameable::getName).map(String::toLowerCase).collect(Collectors.toList()));
				}
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("unclaim")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("unclaim"))) {
					return result;
				}
				arguments.clear();
				arguments.add("all");
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}

			if (args[0].equalsIgnoreCase("top")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("top"))) {
					return result;
				}
				arguments.clear();
				arguments.addAll(Arrays.asList("wins", "money", "power", "kd"));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("logo")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo"))) {
					return result;
				}
				arguments.clear();
				arguments.addAll(Arrays.asList("edit", "apply", "upload", "color", "redraw", "share", "browse", "carriers"));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("claim")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("claim"))) {
					return result;
				}
				arguments.clear();
				arguments.addAll(Arrays.asList("flags", "list"));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("invite")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("invite"))) {
					return result;
				}
				arguments.clear();
				arguments.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("promote")) {
				if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					Clan c = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
					if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("promote"))) {
						return result;
					}
					arguments.clear();
					arguments.addAll(c.getMembers().stream().map(Clan.Associate::getUser).map(LabyrinthUser::getName).collect(Collectors.toList()));
					for (String a : arguments) {
						if (a.toLowerCase().startsWith(args[1].toLowerCase()))
							result.add(a);
					}
					return result;
				}
			}
			if (args[0].equalsIgnoreCase("demote")) {
				if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					Clan c = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
					if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("demote"))) {
						return result;
					}
					arguments.clear();
					arguments.addAll(c.getMembers().stream().map(Clan.Associate::getUser).map(LabyrinthUser::getName).collect(Collectors.toList()));
					for (String a : arguments) {
						if (a.toLowerCase().startsWith(args[1].toLowerCase()))
							result.add(a);
					}
					return result;
				}
			}
			if (args[0].equalsIgnoreCase("block")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("block"))) {
					return result;
				}
				arguments.clear();
				arguments.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("color")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("color"))) {
					return result;
				}
				arguments.clear();
				for (BukkitColor color : BukkitColor.values()) {
					result.add(color.toCode());
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("mode")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("mode"))) {
					return result;
				}
				arguments.clear();
				result.add("war");
				result.add("peace");
				return result;
			}
			if (args[0].equalsIgnoreCase("ally")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("ally"))) {
					return result;
				}
				arguments.clear();
				arguments.add("add");
				arguments.add("remove");
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("enemy")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("enemy"))) {
					return result;
				}
				arguments.clear();
				arguments.add("add");
				arguments.add("remove");
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[1].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("bank")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("bank"))) {
					return result;
				}

				if (!associate.isPresent()) {
					return result;
				}


				if (Bukkit.getPluginManager().isPluginEnabled("Vault") || Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
					arguments.clear();
					arguments.add("balance");
					arguments.add("deposit");
					arguments.add("withdraw");
					Optional.ofNullable(ClansAPI.getInstance().getClanManager().getClan(p)).ifPresent(clan -> {
						if (BankAction.VIEW_LOG.testForPlayer(clan, p)) {
							arguments.add("viewlog");
						}
						if (associate.get().getPriority().toInt() == 3) {
							arguments.add("setperm");
							arguments.add("viewperms");
						}
					});
					for (String a : arguments) {
						if (a.toLowerCase().startsWith(args[1].toLowerCase()))
							result.add(a);
					}
					return result;
				}
			}
			return result;
		}
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("permit")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("permit"))) {
					return result;
				}
				arguments.clear();
				arguments.addAll(Arrays.stream(Clearance.Level.values()).sorted(Integer::compareTo).map(String::valueOf).collect(Collectors.toList()));
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[2].toLowerCase()))
						result.add(a);
				}
				return result;
			}
			if (args[0].equalsIgnoreCase("ally")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("ally"))) {
					return result;
				}
				arguments.clear();
				arguments.addAll(Clan.ACTION.getAllClanNames());
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[2].toLowerCase()))
						result.add(a);
				}
				return result;
			}

			if (args[0].equalsIgnoreCase("enemy")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("enemy"))) {
					return result;
				}
				arguments.clear();
				arguments.addAll(Clan.ACTION.getAllClanNames());
				for (String a : arguments) {
					if (a.toLowerCase().startsWith(args[2].toLowerCase()))
						result.add(a);
				}
				return result;
			}

			if (args[0].equalsIgnoreCase("bank")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("bank"))) {
					return result;
				}
				if (Bukkit.getPluginManager().isPluginEnabled("Vault") || Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
					arguments.clear();
					if (args[1].equalsIgnoreCase("deposit") || args[1].equalsIgnoreCase("withdraw")) {
						arguments.add("10");
					}
					if (args[1].equalsIgnoreCase("setperm")) {
						final Player player = (Player) sender;
						ClansAPI.getInstance().getAssociate(player).ifPresent(a -> {
							if (a.getPriority().toInt() == 3) {
								arguments.add("balance");
								arguments.add("deposit");
								arguments.add("withdraw");
								arguments.add("viewlog");
							}
						});
					}
					for (String a : arguments) {
						if (a.toLowerCase().startsWith(args[2].toLowerCase()))
							result.add(a);
					}
					return result;
				}
			}
			return result;
		}
		if (args.length == 4) {
			if (args[0].equalsIgnoreCase("bank")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("bank"))) {
					return result;
				}
				if (Bukkit.getPluginManager().isPluginEnabled("Vault") || Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
					if (args[1].equalsIgnoreCase("setperm")) {
						arguments.clear();
						arguments.add("0");
						arguments.add("1");
						arguments.add("2");
						arguments.add("3");
					}
					for (String a : arguments) {
						if (a.toLowerCase().startsWith(args[3].toLowerCase()))
							result.add(a);
					}
					return result;
				}
			}
			return result;
		}
		return Collections.emptyList();
	}

	private List<String> clean(String[] args) {
		List<String> list = new ArrayList<>();
		for (String s : args) {
			char first = s.charAt(0);
			String capitalize = String.valueOf(first).toUpperCase();
			String full = capitalize + s.substring(Math.min(1, s.length() - 1)).toLowerCase();
			list.add(full);
		}
		return list;
	}

	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
		if (!(sender instanceof Player)) {
			boolean customMessage = false;
			if (sender instanceof ConsoleCommandSender) {

				for (ClanAddon cycle : ClanAddonQuery.getRegisteredAddons()) {
					if (cycle.getContext().isActive()) {
						for (ClanSubCommand subCommand : cycle.getContext().getCommands()) {
							if (subCommand.getLabel() != null) {
								if (args.length > 0) {
									if (subCommand.getAliases().contains(args[0]) || subCommand.getLabel().equalsIgnoreCase(args[0])) {
										List<String> t = new LinkedList<>(Arrays.asList(args));
										t.removeIf(s -> StringUtils.use(s).containsIgnoreCase(subCommand.getLabel()));
										return subCommand.console(sender, subCommand.getLabel(), t.toArray(new String[0]));
									}
								}
							}
						}
					}
				}
			}
			return true;
		}

        /*
        // VARIABLE CREATION
        //  \/ \/ \/ \/ \/ \/
         */
		int length = args.length;
		Player p = (Player) sender;
		StringLibrary lib = new StringLibrary();
        /*
        //  /\ /\ /\ /\ /\ /\
        //
         */

		for (ClanAddon cycle : ClanAddonQuery.getRegisteredAddons()) {
			if (cycle.getContext().isActive()) {
				for (ClanSubCommand subCommand : cycle.getContext().getCommands()) {
					if (subCommand.getLabel() != null) {
						if (length > 0) {
							if (subCommand.getAliases().contains(args[0]) || subCommand.getLabel().equalsIgnoreCase(args[0])) {
								List<String> t = new LinkedList<>(Arrays.asList(args));
								t.removeIf(s -> StringUtils.use(s).containsIgnoreCase(subCommand.getLabel()));
								return subCommand.player(p, subCommand.getLabel(), t.toArray(new String[0]));
							}
						}
					}
				}
			}
		}

		if (this.getPermission() == null) return true;
		if (!p.hasPermission(this.getPermission())) {
			lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + '"');
			return true;
		}

		if (length == 0) {
			String ping = ClansAPI.getDataInstance().getMessageResponse("Commands.create");
			List<String> list = new LinkedList<>(helpMenu(label));
			new PaginatedList<>(list)
					.limit(lib.menuSize())
					.start((pagination, page, max) -> {
						lib.sendMessage(p, lib.menuTitle());
						Mailer.empty(p).chat(lib.menuBorder()).deploy();
					}).finish(builder -> builder.setPlayer(p).setPrefix(lib.menuBorder())).decorate((pagination, string, page, max, placement) -> Mailer.empty(p).chat(string).deploy()).get(1);

			return true;
		}
		if (!ClansAPI.getDataInstance().getConfig().getRoot().getStringList("Clans.world-whitelist").contains(p.getWorld().getName())) {
			lib.sendMessage(p, "&4&oClan features have been locked within this world.");
			return true;
		}
		if (args[0].equalsIgnoreCase("bank")) {
			if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
				sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
				return true;
			}
			if (BankPermissions.BANKS_USE.not(p)) {
				sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
				return true;
			} else {
				sendMessage(p, lib.getPrefix() + Messages.BANKS_HEADER);
			}
		}
		final Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (length == 1) {
			String args0 = args[0];
			if (args0.equalsIgnoreCase("bank")) { // "bank" print instructions
				if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
					lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
					return true;
				}
				if (associate == null) {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				final Clan clan = associate.getClan();

				final String clan_name = clan.getName();
				final String[] split = Messages.BANKS_GREETING.toString().split("\\{0}");
				final String greetingHover = Messages.BANKS_GREETING_HOVER.toString();
				final String hoverTextMessage = greetingHover.substring(0, greetingHover.indexOf("\n"))
						.replace("{0}", clan_name);
				if (BankPermissions.BANKS_BALANCE.not(p)) {
					p.spigot().sendMessage(TextLib.getInstance().textHoverable(
							split[0], "&o" + p.getName(), split[1],
							hoverTextMessage)
					);
				} else {
					p.spigot().sendMessage(TextLib.getInstance().textRunnable(
							split[0], "&o" + p.getName(), split[1],
							greetingHover.replace("{0}", clan_name),
							"clan bank balance")
					);
				}
				sendMessage(p, Messages.BANKS_COMMAND_LISTING.toString());
				final List<BaseComponent> textComponents = new LinkedList<>();
				p.spigot().sendMessage(TextLib.getInstance().textSuggestable(Messages.BANK_HELP_PREFIX + " ",
						"&7balance", Messages.HOVER_BALANCE.toString(),
						"clan bank balance"));
				textComponents.add(TextLib.getInstance().textSuggestable(
						Messages.BANK_HELP_PREFIX + " &f<",
						"&adeposit", Messages.HOVER_DEPOSIT.toString(),
						"clan bank deposit 1"
				));
				textComponents.add(TextLib.getInstance().textSuggestable(
						"&7,",
						"&cwithdraw", Messages.HOVER_WITHDRAW.toString(),
						"clan bank withdraw 1"
				));
				textComponents.add(new ColoredString("&f> <&7" + Messages.AMOUNT + "&f>",
						ColoredString.ColorType.MC_COMPONENT).toComponent());
				p.spigot().sendMessage(textComponents.toArray(new BaseComponent[0]));
				if (BankAction.VIEW_LOG.testForPlayer(clan, p)) {
					p.spigot().sendMessage(TextLib.getInstance().textSuggestable(
							Messages.BANK_HELP_PREFIX + " ",
							"&7viewlog", "View recent transaction history",
							"clan bank viewlog"
					));
				}
				if (associate.getPriority().toInt() == 3) {
					p.spigot().sendMessage(TextLib.getInstance().textSuggestable(
							Messages.BANK_HELP_PREFIX + " ",
							"&7setperm", "Set access to functions",
							"clan bank setperm "
					));
				}
				return true;
			}
			if (args0.equalsIgnoreCase("create")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("create"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("create")));
					return true;
				}
				lib.sendMessage(p, lib.commandCreate());
				return true;
			}
			if (args0.equalsIgnoreCase("mode")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("mode"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("mode")));
					return true;
				}
				if (associate != null) {
					if (Clearance.MANAGE_MODE.test(associate)) {
						if (!(associate.getClan() instanceof DefaultClan))
							return true;
						DefaultClan c = (DefaultClan) ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
						if (c.isPeaceful()) {
							if (ClansAPI.getDataInstance().isTrue("Clans.mode-change.charge")) {
								double amount = ClansAPI.getDataInstance().getConfig().getRoot().getDouble("Clans.mode-change.amount");
								double balance = EconomyProvision.getInstance().balance(p).orElse(0.0);
								double needed = amount - balance;
								boolean b = EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p, p.getWorld().getName()).orElse(false);
								if (!b) {
									lib.sendMessage(p, lib.notEnough(needed));
									return true;
								}

							}
							if (ClansAPI.getDataInstance().isTrue("Clans.mode-change.timer.use")) {
								if (c.getModeCooldown().isComplete()) {
									c.setPeaceful(false);
									c.getModeCooldown().setCooldown();
									Bukkit.broadcastMessage(Clan.ACTION.color(lib.modeAnnounce("war", c.getName())));
									lib.sendMessage(p, lib.war());
								} else {
									lib.sendMessage(p, c.getModeCooldown().fullTimeLeft());
									return true;
								}
								return true;
							}
							c.setPeaceful(false);
							Bukkit.broadcastMessage(Clan.ACTION.color(lib.modeAnnounce("war", c.getName())));
							lib.sendMessage(p, lib.war());
						} else {
							if (ClansAPI.getDataInstance().isTrue("Clans.mode-change.charge")) {
								double amount = ClansAPI.getDataInstance().getConfig().getRoot().getDouble("Clans.mode-change.amount");
								double balance = EconomyProvision.getInstance().balance(p).orElse(0.0);
								double needed = amount - balance;
								EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p).ifPresent(b -> {
									if (!b) {
										lib.sendMessage(p, lib.notEnough(needed));
									}
								});
							}
							if (ClansAPI.getDataInstance().isTrue("Clans.mode-change.timer.use")) {
								if (c.getModeCooldown().isComplete()) {
									c.setPeaceful(true);
									c.getModeCooldown().setCooldown();
									Bukkit.broadcastMessage(Clan.ACTION.color(lib.modeAnnounce("peace", c.getName())));
									lib.sendMessage(p, lib.peaceful());
								} else {
									lib.sendMessage(p, c.getModeCooldown().fullTimeLeft());
									return true;
								}
								return true;
							}
							c.setPeaceful(true);
							Bukkit.broadcastMessage(Clan.ACTION.color(lib.modeAnnounce("peace", c.getName())));
							lib.sendMessage(p, lib.peaceful());
							return true;
						}
					} else {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("password") || args0.equalsIgnoreCase("pass")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("password"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("password")));
					return true;
				}
				lib.sendMessage(p, lib.commandPassword());
				return true;
			}
			if (args0.equalsIgnoreCase("join")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("join"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("join")));
					return true;
				}
				lib.sendMessage(p, lib.commandJoin());
				return true;
			}
			if (args0.equalsIgnoreCase("permissions") || args0.equalsIgnoreCase("perms")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("permissions"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("permissions")));
					return true;
				}
				if (associate != null) {
					ClearanceLog log = associate.getClan().getPermissions();
					lib.sendMessage(p, "&eOur clan permission list:");
					Mailer m = Mailer.empty(p);
					m.chat("&f&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();
					for (Map.Entry<Clearance, Integer> e : log.stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toList())) {
						Clearance perm = e.getKey();
						int required = e.getValue();
						m.chat(TextLib.getInstance().textSuggestable("&e" + String.join(" ", clean(perm.getName().split("_"))), " &f= {&a" + required + "&f}", "&eClick to edit this permission.", "c permit " + perm.getName() + " ")).deploy();
					}
					m.chat("&f&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();
					FileManager main = ClansAPI.getDataInstance().getConfig();
					String member = main.getRoot().getString("Formatting.Chat.Styles.Full.Member");
					String mod = main.getRoot().getString("Formatting.Chat.Styles.Full.Moderator");
					String admin = main.getRoot().getString("Formatting.Chat.Styles.Full.Admin");
					String owner = main.getRoot().getString("Formatting.Chat.Styles.Full.Owner");
					m.chat("&e0 &f= &2" + member + "&b, &e1 &f= &2" + mod + "&b, &e2 &f= &2" + admin + "&b, &e3 &f= &2" + owner).deploy();
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args0.equalsIgnoreCase("permit")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("permit"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("permit")));
					return true;
				}
				lib.sendMessage(p, lib.commandPermit());
				return true;
			}
			if (args0.equalsIgnoreCase("war")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("war"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("war")));
					return true;
				}

				if (associate == null) {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				War current = ClansAPI.getInstance().getArenaManager().get(associate);
				if (current == null) {
					War joined = ClansAPI.getInstance().getArenaManager().queue(associate);
					if (joined != null) {
						if (!joined.isRunning()) {
							joined.populate();
							lib.sendMessage(p, "&aYou queued in position &7#&6" + joined.getQueue().size() + " &ain team &6" + joined.getTeam(associate.getClan()));
							lib.sendComponent(p, TextLib.getInstance().textRunnable("&eYou can teleport to the arena ", "&6&nnow", "&e, otherwise the war will &6automatically &eteleport you on start when queue reaches the required amount.", "&6&oClick to teleport.", "c war teleport"));
						} else {
							p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 10, 1);
							associate.getClan().broadcast("&a" + associate.getNickname() + " has joined the battle.");
							lib.sendMessage(p, "&aYour team needs you, hurry now!");
							Location loc = joined.getTeam(associate.getClan()).getSpawn();
							ClansAPI.getInstance().getArenaManager().hideAll(joined);
							if (loc != null) {
								p.teleport(loc);
							} else {
								lib.sendMessage(p, "&cYour teams arena spawn isn't setup properly! Contact staff for support.");
							}
						}
					} else {
						lib.sendMessage(p, "&cThere is no space on the battlefield right now.");
					}
				} else {
					lib.sendMessage(p, ClansAPI.getDataInstance().getMessageResponse("already-at-war"));
					if (!current.isRunning()) {
						lib.sendMessage(p, "&cAll teams are still getting ready for battle. Queue still building.");
					}
					GUI.ARENA_SPAWN.get(current).open(p);
				}
				return true;
			}
			if (args0.equalsIgnoreCase("forfeit") || args0.equalsIgnoreCase("surrender")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("forfeit"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("forfeit")));
					return true;
				}
				if (associate != null) {
					War current = ClansAPI.getInstance().getArenaManager().get(associate);
					if (current != null && current.isRunning()) {
						GUI.ARENA_SURRENDER.get(current).open(p);
					} else {
						ClansAPI.getInstance().getArenaManager().leave(associate);
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("truce")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("truce"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("truce")));
					return true;
				}
				if (associate != null) {
					War current = ClansAPI.getInstance().getArenaManager().get(associate);
					if (current != null && current.isRunning()) {
						GUI.ARENA_TRUCE.get(current).open(p);
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("top")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("top"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("top")));
					return true;
				}
				Clan.ACTION.getLeaderboard(ClanAction.LeaderboardType.POWER, p, 1);
				return true;
			}
			if (args0.equalsIgnoreCase("list")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("list"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("list")));
					return true;
				}
				GUI.CLAN_ROSTER_SELECTION.get().open(p);
				return true;
			}
			if (args0.equalsIgnoreCase("claim")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("claim"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("claim")));
					return true;
				}
				if (Claim.ACTION.isEnabled()) {
					if (associate != null) {
						if (Clearance.MANAGE_LAND.test(associate)) {
							Claim.ACTION.claim(p);
						} else {
							lib.sendMessage(p, lib.noClearance());
							return true;
						}
					} else {
						lib.sendMessage(p, lib.notInClan());
						return true;
					}
				} else {
					lib.sendMessage(p, "&c&oYour server doesn't allow the use of clan land-claiming.");
					return true;
				}
				return true;
			}

			if (args0.equalsIgnoreCase("unclaim")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("unclaim"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("unclaim")));
					return true;
				}
				if (associate != null) {
					if (Claim.ACTION.isEnabled()) {
						if (Clearance.MANAGE_LAND.test(associate)) {
							Claim.ACTION.unclaim(p);
						} else {
							lib.sendMessage(p, lib.noClearance());
						}
					} else {
						lib.sendMessage(p, "&c&oYour server doesn't allow the use of clan land-claiming.");
						return true;
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args0.equalsIgnoreCase("chat")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("chat"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("chat")));
					return true;
				}
				if (associate != null) {
					if (associate.getChannel().getId().equals("GLOBAL")) {
						associate.setChannel("CLAN");
						lib.sendMessage(p, lib.commandChat("CLAN"));
						return true;
					}
					if (associate.getChannel().getId().equals("CLAN")) {
						associate.setChannel("ALLY");
						lib.sendMessage(p, lib.commandChat("ALLY"));
						return true;
					}
					if (associate.getChannel().getId().equals("ALLY")) {
						associate.setChannel("GLOBAL");
						lib.sendMessage(p, lib.commandChat("GLOBAL"));
						return true;
					}
				}
				return true;
			}
			if (args0.equalsIgnoreCase("kick")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("kick"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("kick")));
					return true;
				}
				lib.sendMessage(p, lib.commandKick());
				return true;
			}
			if (args0.equalsIgnoreCase("passowner")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("passowner"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("passowner")));
					return true;
				}
				lib.sendMessage(p, lib.commandPassowner());
				return true;
			}
			if (args0.equalsIgnoreCase("players")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("players"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("players")));
					return true;
				}
				Clan.ACTION.getPlayerboard(p, 1);
				return true;
			}
			if (args0.equalsIgnoreCase("tag")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("tag"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("tag")));
					return true;
				}
				lib.sendMessage(p, lib.commandTag());
				return true;
			}
			if (args0.equalsIgnoreCase("color")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("color"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("color")));
					return true;
				}
				lib.sendMessage(p, lib.commandColor());
				lib.sendMessage(p, "&7|&e)&r " + "https://www.digminecraft.com/lists/color_list_pc.php");
				return true;
			}
			if (args0.equalsIgnoreCase("nick") || args0.equalsIgnoreCase("nickname")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("nickname"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("nickname")));
					return true;
				}
				lib.sendMessage(p, lib.commandNick());
				return true;
			}
			if (args0.equalsIgnoreCase("promote")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("promote"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("promote")));
					return true;
				}
				lib.sendMessage(p, lib.commandPromote());
				return true;
			}
			if (args0.equalsIgnoreCase("demote")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("demote"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("demote")));
					return true;
				}
				lib.sendMessage(p, lib.commandDemote());
				return true;
			}
			if (args0.equalsIgnoreCase("ally")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("ally"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("ally")));
					return true;
				}
				lib.sendMessage(p, lib.commandAlly());
				return true;
			}
			if (args0.equalsIgnoreCase("enemy")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("enemy"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("enemy")));
					return true;
				}
				lib.sendMessage(p, lib.commandEnemy());
				return true;
			}
			if (args0.equalsIgnoreCase("leave")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("leave"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("leave")));
					return true;
				}
				Clan.ACTION.removePlayer(p.getUniqueId());
				return true;
			}
			if (args0.equalsIgnoreCase("message")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("message"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("message")));
					return true;
				}
				lib.sendMessage(p, lib.commandMessage());
				return true;
			}
			if (args0.equalsIgnoreCase("base")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("base"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("base")));
					return true;
				}
				if (associate != null) {
					Clan.ACTION.teleport(p, associate.getClan().getBase());
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("logo")) {

				if (associate == null) {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}

				Clan c = associate.getClan();

				List<String> s = c.getValue(List.class, "logo");

				if (s != null) {
					lib.sendMessage(p, "&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					for (String line : s) {
						lib.sendMessage(p, line);
					}
					lib.sendMessage(p, "&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
				} else {
					lib.sendMessage(p, "&cOur clan doesn't have an official insignia");
				}

				return true;
			}
			if (args0.equalsIgnoreCase("setbase")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("setbase"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("setbase")));
					return true;
				}

				if (associate == null) {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}

				Clan clan = associate.getClan();
				if (Clearance.MANAGE_BASE.test(associate)) {
					if (!ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
						AssociateUpdateBaseEvent event = ClanVentBus.call(new AssociateUpdateBaseEvent(p, p.getLocation()));
						if (!event.isCancelled()) {
							clan.setBase(event.getLocation());
						}
					} else {
						if (ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation()).getOwner().getTag().getId().equals(clan.getId().toString())) {
							AssociateUpdateBaseEvent event = ClanVentBus.call(new AssociateUpdateBaseEvent(p, p.getLocation()));
							if (!event.isCancelled()) {
								clan.setBase(event.getLocation());
							}
						} else {
							lib.sendMessage(p, lib.notClaimOwner(ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation()).getClan().getName()));
						}
					}
				} else {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("info") || args0.equalsIgnoreCase("i")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("info"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("info")));
					return true;
				}
				if (associate != null) {
					AssociateDisplayInfoEvent ev = ClanVentBus.call(new AssociateDisplayInfoEvent(associate, AssociateDisplayInfoEvent.Type.PERSONAL));
					if (!ev.isCancelled()) {
						Clan.ACTION.getClanboard(p, 1);
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("friendlyfire") || args0.equalsIgnoreCase("ff")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("friendlyfire"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("friendlyfire")));
					return true;
				}

				if (associate == null) {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				if (Clearance.MANAGE_FRIENDLY_FIRE.test(associate)) {
					if (!(associate.getClan() instanceof DefaultClan))
						return true;
					DefaultClan c = (DefaultClan) ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
					if (ClansAPI.getDataInstance().isTrue("Clans.friendly-fire.timer.use")) {

						if (c.isFriendlyFire()) {
							if (c.getFriendlyCooldown().isComplete()) {
								c.setFriendlyFire(false);
								c.broadcast(lib.friendlyFireOff(p.getName()));
								c.getFriendlyCooldown().setCooldown();
							} else {
								lib.sendMessage(p, c.getFriendlyCooldown().fullTimeLeft());
								return true;
							}
						} else {
							if (c.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDeny());
								return true;
							}
							if (c.getFriendlyCooldown().isComplete()) {
								c.setFriendlyFire(true);
								c.getFriendlyCooldown().setCooldown();
								c.broadcast(lib.friendlyFireOn(p.getName()));
							} else {
								lib.sendMessage(p, c.getFriendlyCooldown().fullTimeLeft());
								return true;
							}
							return true;
						}
						return true;
					} else {
						if (c.isFriendlyFire()) {
							c.setFriendlyFire(false);
							c.broadcast(lib.friendlyFireOff(p.getName()));
						} else {
							if (c.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDeny());
								return true;
							}
							c.setFriendlyFire(true);
							c.broadcast(lib.friendlyFireOn(p.getName()));
							return true;
						}
					}
				} else {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("members")) {

				if (associate != null) {
					Clan.ACTION.getClanboard(p, 1);
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}

			try {

				int pa = Integer.parseInt(args0);

				List<String> list = new LinkedList<>(helpMenu(label));
				new PaginatedList<>(list)
						.limit(lib.menuSize())
						.start((pagination, page, max) -> {
							lib.sendMessage(p, lib.menuTitle());
							Mailer.empty(p).chat(lib.menuBorder()).deploy();
						}).finish(builder -> builder.setPlayer(p).setPrefix(lib.menuBorder())).decorate((pagination, string, page, max, placement) -> Mailer.empty(p).chat(string).deploy()).get(Math.max(pa, 1));
				return true;
			} catch (Exception ignored) {
			}

			lib.sendMessage(p, lib.commandUnknown(label));
			return true;
		}

		if (length == 2) {
			String args0 = args[0];
			String args1 = args[1];
			if (args0.equalsIgnoreCase("claim")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("claim"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("claim")));
					return true;
				}
				if (args1.equalsIgnoreCase("list")) {
					if (associate != null) {
						if (Clearance.MANAGE_LAND.test(associate)) {
							GUI.CLAIM_LIST.get(associate.getClan()).open(p);
						} else {
							lib.sendMessage(p, lib.noClearance());
						}
					} else {
						lib.sendMessage(p, lib.notInClan());
					}
					return true;
				}
				if (args1.equalsIgnoreCase("flags")) {

					if (Claim.ACTION.isEnabled()) {
						if (associate != null) {
							if (!Clearance.MANAGE_LAND.test(associate)) {
								lib.sendMessage(p, lib.noClearance());
								return true;
							}
							Claim test = ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation());
							if (test != null) {
								Set<Claim.Flag> set = Arrays.stream(test.getFlags().clone()).sorted((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getId(), o2.getId())).sorted(Claim.Flag::compareTo).sorted(Comparator.reverseOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
								new PaginatedList<>(set)
										.limit(5)
										.start((pagination, page, max) -> {
											lib.sendMessage(p, "&e&lChunk: &bX:&f" + test.getChunk().getX() + " &bZ:&f" + test.getChunk().getZ());
											new FancyMessage("--------------------------------------").color(org.bukkit.Color.AQUA).style(org.bukkit.ChatColor.STRIKETHROUGH).send(p).deploy();
										})
										.decorate((pagination, f, page, max, placement) -> {
											Message m;
											if (f.isValid()) {
												if (f.isEnabled()) {
													m = new FancyMessage().then(f.getId() + ";").color(org.bukkit.Color.GREEN).then("Enable").color(org.bukkit.Color.YELLOW).then(" ").then("Disable").color(org.bukkit.Color.GRAY).hover("Click to disallow this flag.").action(() -> {
														f.setEnabled(false);
														p.performCommand("c claim flags");
													});
													for (Message.Chunk c : m) {
														String text = c.getText();
														if (text.contains(";")) {
															c.replace(";", " &8" + Strings.repeat(".", ((180 - getFixedLength(text.replace(";", ""))) / 2)) + " ");
														}
													}
												} else {
													m = new FancyMessage().then(f.getId() + ";").color(org.bukkit.Color.GREEN).then("Enable").color(org.bukkit.Color.GRAY).hover("Click to allow this flag.").action(() -> {
														f.setEnabled(true);
														p.performCommand("c claim flags");
													}).then(" ").then("Disable").color(org.bukkit.Color.YELLOW);
													for (Message.Chunk c : m) {
														String text = c.getText();
														if (text.contains(";")) {
															c.replace(";", " &8" + Strings.repeat(".", ((180 - getFixedLength(text.replace(";", ""))) / 2)) + " ");
														}
													}
												}
												m.send(p).deploy();
											} else {
												new FancyMessage("&c&m" + f.getId()).hover("Click to remove me i no longer work.").action(() -> {
													test.remove(f);
													Schedule.sync(() -> p.performCommand("c claim flags")).waitReal(1);
												}).send(p).deploy();
											}
										})
										.finish(builder -> builder.setPrefix("&b&m--------------------------------------").setPlayer(p))
										.get(1);
							} else {
								lib.sendMessage(p, lib.alreadyWild());
							}
						} else {
							lib.sendMessage(p, lib.notInClan());
							return true;
						}
					} else {
						lib.sendMessage(p, "&c&oYour server doesn't allow the use of clan land-claiming.");
						return true;
					}
				}

				return true;
			}
			if (args0.equalsIgnoreCase("permit")) {
				if (associate != null) {
					if (Clearance.MANAGE_PERMS.test(associate)) {
						lib.sendMessage(p, lib.commandPermit());
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
			}
			if (args0.equalsIgnoreCase("war")) {
				if (args1.equalsIgnoreCase("teleport")) {
					if (associate != null && associate.isValid()) {
						War w = ClansAPI.getInstance().getArenaManager().get(associate);
						if (w != null) {
							if (!w.isRunning()) {
								War.Team t = w.getTeam(associate.getClan());
								Location loc = t.getSpawn();
								if (loc == null) {
									lib.sendMessage(p, "&cYour team's spawn location isn't properly setup. Contact staff for support.");
									return true;
								}
								p.teleport(loc);
								lib.sendMessage(p, "&cYou won't be able to hurt anyone until the match starts.");
							} else {
								lib.sendMessage(p, "&cYou can't do this right now! You are already at war.");
							}
						}
					}
					return true;
				}
			}
			if (args0.equalsIgnoreCase("logo")) {

				if (associate == null) {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}

				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo")));
					return true;
				}

				Clan c = associate.getClan();

				if (args1.equalsIgnoreCase("edit")) {

					if (!Clearance.LOGO_EDIT.test(associate)) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}

					new Insignia.Builder("Template:" + p.getUniqueId().toString()).setBorder("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").setColor("&2").setHeight(16).setWidth(16).draw(p);
					lib.sendMessage(p, "&eLoaded personal template.");
					return true;
				}

				if (args1.equalsIgnoreCase("browse")) {
					GUI.LOGO_LIST.get().open(p);
					return true;
				}

				if (args1.equalsIgnoreCase("carriers")) {
					GUI.HOLOGRAM_LIST.get(associate.getClan()).open(p);
					return true;
				}

				if (args1.equalsIgnoreCase("share")) {
					if (!Clearance.LOGO_SHARE.test(associate)) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
					String id = new RandomID().generate();
					Insignia i = Insignia.get("Template:" + p.getUniqueId().toString());
					if (i != null) {
						ClansAPI.getInstance().getLogoGallery().load(id, i.getLines().stream().map(Insignia.Line::toString).collect(Collectors.toList()));
						lib.sendMessage(p, "&aInsignia successfully uploaded to the global logo gallery.");
						GUI.LOGO_LIST.get().open(p);
					} else {
						ItemStack item = p.getInventory().getItemInMainHand();

						if (item.getType() != Material.PAPER && item.getType() != Material.FILLED_MAP) {
							lib.sendMessage(p, "&cInvalid insignia request. Not an insignia print.");
							return true;
						}

						if (!item.hasItemMeta()) {
							lib.sendMessage(p, "&cInvalid insignia request. No lore to process.");
							return true;
						}

						if (item.getItemMeta().getLore() != null) {

							for (String lore : item.getItemMeta().getLore()) {
								if (isAlphaNumeric(ChatColor.stripColor(lore))) {
									lib.sendMessage(p, "&cInvalid insignia request. Error 420");
									return true;
								}
							}

							ClansAPI.getInstance().getLogoGallery().load(id, new ArrayList<>(item.getItemMeta().getLore()));
							lib.sendMessage(p, "&aInsignia successfully uploaded to the global logo gallery.");
							GUI.LOGO_LIST.get().open(p);

						}
					}
					return true;
				}

				if (args1.equalsIgnoreCase("redraw")) {
					lib.sendMessage(p, "&cUsage: &f/c logo &eredraw &r[height] [width]");
					return true;
				}

				if (args1.equalsIgnoreCase("upload")) {
					if (!Clearance.LOGO_UPLOAD.test(associate)) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}

					ItemStack item = p.getInventory().getItemInMainHand();

					if (item.getType() != Material.PAPER && item.getType() != Material.FILLED_MAP) {
						lib.sendMessage(p, "&cInvalid insignia request. Not an insignia print.");
						return true;
					}

					if (!item.hasItemMeta()) {
						lib.sendMessage(p, "&cInvalid insignia request. No lore to process.");
						return true;
					}

					if (item.getItemMeta().getLore() != null) {

						for (String lore : item.getItemMeta().getLore()) {
							if (isAlphaNumeric(ChatColor.stripColor(lore))) {
								lib.sendMessage(p, "&cInvalid insignia request. Error 420");
								return true;
							}
						}

						Mailer mail = associate.getMailer();
						List<String> logo = item.getItemMeta().getLore();
						int size = ChatColor.stripColor(logo.get(0)).length();
						mail.chat("&6&m&l" + Strings.repeat("▬", Math.min(38, size * 2))).deploy();
						FancyLogoAppendage appendage = ClansAPI.getDataInstance().appendStringsToLogo(logo, message -> message.hover("&2Do you like hamburgers?"));
						for (BaseComponent[] b : appendage.append(new TextChunk("Make this"),
								new TextChunk("your clan"),
								new TextChunk("logo?"),
								new TextChunk(" "),
								new TextChunk("&7[&6Yes&7]").bind(new ToolTip.Text("&2Click me to accept")).bind(new ToolTip.Action(() -> {
									c.setValue("logo", new ArrayList<>(logo), false);

									lib.sendMessage(p, "&aPrinted insignia applied to clan container.");
								}))).get()) {
							mail.chat(b).deploy();
						}
						mail.chat("&6&m&l" + Strings.repeat("▬", Math.min(38, size * 2))).deploy();


					} else {
						lib.sendMessage(p, "&cInvalid insignia request. What are you trying to pull...");
						return true;
					}

					return true;
				}

				if (args1.equalsIgnoreCase("apply")) {
					if (!Clearance.LOGO_APPLY.test(associate)) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}

					Insignia i = Insignia.get("Template:" + p.getUniqueId().toString());
					Insignia.copy(c.getId().toString(), i);
					if (i != null) {

						Mailer mail = associate.getMailer();
						List<String> logo = i.getLines().stream().map(Insignia.Line::toString).collect(Collectors.toList());
						int size = ChatColor.stripColor(logo.get(0)).length();
						mail.chat("&6&m&l" + Strings.repeat("▬", Math.min(38, size * 2))).deploy();
						FancyLogoAppendage appendage = ClansAPI.getDataInstance().appendStringsToLogo(logo, message -> message.hover("&2Do you like hamburgers?"));
						for (BaseComponent[] b : appendage.append(new TextChunk("Make this"),
								new TextChunk("your clan"),
								new TextChunk("logo?"),
								new TextChunk(" "),
								new TextChunk("&7[&6Yes&7]").bind(new ToolTip.Text("&2Click me to accept")).bind(new ToolTip.Action(() -> {
									c.setValue("logo", logo, false);

									lib.sendMessage(p, "&aCustom insignia applied to clan container.");
								}))).get()) {
							mail.chat(b).deploy();
						}
						mail.chat("&6&m&l" + Strings.repeat("▬", Math.min(38, size * 2))).deploy();

					} else {
						lib.sendMessage(p, "&cInvalid insignia request. No lore to process.");
					}

					return true;
				}
/*
				if (args1.equalsIgnoreCase("save")) {
					Insignia i = Insignia.get("Template:" + p.getUniqueId().toString());

					if (i != null) {

						lib.sendMessage(p, "&aChanges have been saved as a template.");
						c.setValue("logo_template_" + p.getUniqueId().toString(), i);

					} else {
						lib.sendMessage(p, "&cNo drawing was found.");
					}
					return true;
				}

 */

				if (args1.equalsIgnoreCase("print")) {
					if (!Clearance.LOGO_PRINT.test(associate)) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}

					Insignia i = Insignia.get("Template:" + p.getUniqueId().toString());

					if (i != null) {
						p.getWorld().dropItem(p.getEyeLocation(), new Item.Edit(Material.PAPER).setTitle("(#" + HUID.randomID().toString().substring(0, 4) + ")").setLore(i.getLines().stream().map(Insignia.Line::toString).toArray(String[]::new)).build());

						lib.sendMessage(p, "&aInsignia template printed.");

					} else {
						new Insignia.Builder("Template:" + p.getUniqueId().toString()).setBorder("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").setColor("&2").setHeight(8).setWidth(16).draw(p);
						lib.sendMessage(p, "&cInvalid insignia request. No lore to process.");
					}

					return true;
				}

				final boolean b = !args1.matches("(&#[a-zA-Z0-9]{6})+(&[a-zA-Z0-9])+") && !args1.matches("(&[a-zA-Z0-9])+") && !args1.matches("(&#[a-zA-Z0-9])+") && !args1.matches("(#[a-zA-Z0-9])+");
				if (b) return true;
				new Insignia.Builder(c.getId().toString()).setHeight(16).setWidth(16).setColor(args1).draw(p);

				return true;
			}
			if (args0.equalsIgnoreCase("bank")) {
				if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
					lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
					return true;
				}

				if (associate == null) {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}

				if (!(associate.getClan() instanceof DefaultClan))
					return true;
				DefaultClan clan = (DefaultClan) ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
				switch (args1.toLowerCase()) {
					case "balance":
						if (BankPermissions.BANKS_BALANCE.not(p) || !BankAction.BALANCE.testForPlayer(clan, p)) {
							sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
							return true;
						}
						sendMessage(p, Messages.BANKS_CURRENT_BALANCE.toString()
								.replace("{0}", clan.getBalance().toString()));
						return true;
					case "deposit":
						if (BankPermissions.BANKS_DEPOSIT.not(p) || !BankAction.DEPOSIT.testForPlayer(clan, p)) {
							sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
							return true;
						}
						// msg usage (need amount param)
						sendMessage(p, Messages.BANK_USAGE.toString());
						if (TextLib.getInstance() != null) {
							p.spigot().sendMessage(TextLib.getInstance().textHoverable(
									Messages.BANK_HELP_PREFIX + " ",
									"&7<&fdeposit&7>",
									" ",
									"&7<&c" + Messages.AMOUNT + "&7>",
									Messages.HOVER_DEPOSIT.toString(),
									Messages.HOVER_NO_AMOUNT.toString()
							));
						} else {
							p.spigot().sendMessage(new OldComponent().textHoverable(
									Messages.BANK_HELP_PREFIX + " ",
									"&7<&fdeposit&7>",
									" ",
									"&7<&c" + Messages.AMOUNT + "&7>",
									Messages.HOVER_DEPOSIT.toString(),
									Messages.HOVER_NO_AMOUNT.toString()
							));
						}
						return true;
					case "withdraw":
						if (BankPermissions.BANKS_WITHDRAW.not(p) || !BankAction.WITHDRAW.testForPlayer(clan, p)) {
							sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
							return true;
						}
						// msg usage (need amount param)
						sendMessage(p, Messages.BANK_USAGE.toString());
						if (TextLib.getInstance() != null) {
							p.spigot().sendMessage(TextLib.getInstance().textHoverable(
									Messages.BANK_HELP_PREFIX + " ",
									"&7<&fwithdraw&7>",
									" ",
									"&7<&c" + Messages.AMOUNT + "&7>",
									Messages.HOVER_WITHDRAW.toString(),
									Messages.HOVER_NO_AMOUNT.toString()
							));
						} else {
							p.spigot().sendMessage(new OldComponent().textHoverable(
									Messages.BANK_HELP_PREFIX + " ",
									"&7<&fwithdraw&7>",
									" ",
									"&7<&c" + Messages.AMOUNT + "&7>",
									Messages.HOVER_WITHDRAW.toString(),
									Messages.HOVER_NO_AMOUNT.toString()
							));
						}
						return true;
					case "setperm":
						if (associate.getPriority().toInt() != 3) {
							sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
							return true;
						}
						sendMessage(p, Messages.BANK_USAGE.toString());
						if (TextLib.getInstance() != null) {
							p.spigot().sendMessage(TextLib.getInstance().textHoverable(
									Messages.BANK_HELP_PREFIX + " ",
									"&7<&cperm&7>",
									" ",
									"&7<&clevel&7>",
									"&6Valid options:&7\n&o*&f balance&7\n&o*&f deposit&7\n&o*&f withdraw&7\n&o*&f viewlog",
									"Valid levels: 0-3"
							));
						} else {
							p.spigot().sendMessage(new OldComponent().textHoverable(
									Messages.BANK_HELP_PREFIX + " ",
									"&7<&cperm&7>",
									" ",
									"&7<&clevel&7>",
									"&6Valid options:&7\n&o*&f balance&7\n&o*&f deposit&7\n&o*&f withdraw&7\n&o*&f viewlog",
									"Valid levels: 0-3"
							));
						}
						return true;
					case "viewlog":
						if (!BankAction.VIEW_LOG.testForPlayer(clan, p)) {
							sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
							return true;
						}
						p.sendMessage(BankLog.getForClan(clan).getTransactions().stream().map(Object::toString).toArray(String[]::new));
						return true;
					case "viewperms":
						if (associate.getPriority().toInt() != 3) {
							sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
							return true;
						}
						sendMessage(p, "&6Bank perm levels:");
						sendMessage(p, "Balance&e=&7[&f" + BankAction.BALANCE.getValueInClan(clan) + "&7]");
						sendMessage(p, "Deposit&e=&7[&f" + BankAction.DEPOSIT.getValueInClan(clan) + "&7]");
						sendMessage(p, "Withdraw&e=&7[&f" + BankAction.WITHDRAW.getValueInClan(clan) + "&7]");
						sendMessage(p, "ViewLog&e=&7[&f" + BankAction.VIEW_LOG.getValueInClan(clan) + "&7]");
						return true;
				}
				// msg usage (invalid subcommand)
				sendMessage(p, Messages.BANK_INVALID_SUBCOMMAND.toString());
				return true;
			}
			if (args0.equalsIgnoreCase("block")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("block"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("block")));
					return true;
				}
				Player target = Bukkit.getPlayer(args1);
				if (target != null) {
					if (blockedUsers.containsKey(p)) {
						List<UUID> a = blockedUsers.get(p);
						if (a.contains(target.getUniqueId())) {
							// already blocked
							a.remove(target.getUniqueId());
							blockedUsers.put(p, a);
							lib.sendMessage(p, target.getName() + " &a&ohas been unblocked.");
						} else {
							a.add(target.getUniqueId());
							blockedUsers.put(p, a);
							lib.sendMessage(p, target.getName() + " &c&ohas been blocked.");
							return true;
						}
					} else {
						// make it
						List<UUID> ids = new ArrayList<>();
						ids.add(target.getUniqueId());
						blockedUsers.put(p, ids);
						lib.sendMessage(p, target.getName() + " &c&ohas been blocked.");
						return true;
					}
				}
				return true;
			}
			if (args0.equalsIgnoreCase("invite")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("invite"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("invite")));
					return true;
				}
				Player target = Bukkit.getPlayer(args1);
				if (target != null) {
					if (ClansAPI.getInstance().getAssociate(target).isPresent()) {
						lib.sendMessage(p, "&c&oThis user is already in a clan.");
						return true;
					}

					if (associate == null) {
						lib.sendMessage(p, lib.notInClan());
						return true;
					}
					if (Clearance.INVITE_PLAYERS.test(associate)) {
						if (blockedUsers.containsKey(target)) {
							List<UUID> users = blockedUsers.get(target);
							if (users.contains(p.getUniqueId())) {
								lib.sendMessage(p, "&c&oThis person has you blocked. Unable to receive invitation.");
								return true;
							}
						}
						ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).broadcast(p.getName() + " &e&ohas invited player &6&l" + target.getName());
						lib.sendMessage(target, "&b&o" + p.getName() + " &3invites you to their clan.");
						if (associate.getClan().getPassword() != null) {
							message().append(text("&3|&7> &3Click a button to respond. "))
									.append(text("&b[&nACCEPT&b]")
											.bind(hover("&3Click to accept the request from '" + p.getName() + "'."))
											.bind(command("clan join " + ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getName() + " " + associate.getClan().getPassword())))
									.append(text("&4[&nDENY&4]")
											.bind(hover("&3Click to deny the request from '" + p.getName() + "'."))
											.bind(command("msg " + p.getName() + " No thank you.")))
									.send(target).deploy();
						} else {
							message().append(text("&3|&7> &3Click a button to respond. "))
									.append(text("&b[&nACCEPT&b]")
											.bind(hover("&3Click to accept the request from '" + p.getName() + "'."))
											.bind(command("clan join " + ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getName())))
									.append(text("&4[&nDENY&4]")
											.bind(hover("&3Click to deny the request from '" + p.getName() + "'."))
											.bind(command("msg " + p.getName() + " No thank you.")))
									.send(target).deploy();
						}
					} else {
						lib.sendMessage(p, "&c&oYou do not have clan clearance.");
						return true;
					}
				} else {
					lib.sendMessage(p, "&c&oTarget not found.");
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("create")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("create"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("create")));
					return true;
				}
				if (!isAlphaNumeric(args1)) {
					lib.sendMessage(p, lib.nameInvalid(args1));
					return true;
				}
				if (Clan.ACTION.getAllClanNames().contains(args1)) {
					lib.sendMessage(p, lib.alreadyMade(args1));
					return true;
				}

				LabyrinthUser user = LabyrinthUser.get(p.getName());
				if (user.isValid()) {
					Clan.ACTION.create(p.getUniqueId(), args1, null);
				} else {
					lib.sendMessage(p, "&cYou appear to not be a valid mojang user, enable to proceed.");
				}
				return true;
			}
			if (args0.equalsIgnoreCase("mode")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("mode"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("mode")));
					return true;
				}
				if (associate != null) {
					if (Clearance.MANAGE_MODE.test(associate)) {
						if (!(associate.getClan() instanceof DefaultClan))
							return true;
						DefaultClan c = (DefaultClan) ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
						switch (args1.toLowerCase()) {
							case "war":
								if (c.isPeaceful()) {
									if (ClansAPI.getDataInstance().isTrue("Clans.mode-change.charge")) {
										double amount = ClansAPI.getDataInstance().getConfig().getRoot().getDouble("Clans.mode-change.amount");
										double balance = EconomyProvision.getInstance().balance(p).orElse(0.0);
										double needed = amount - balance;
										EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p).ifPresent(b -> {

											if (!b) {
												lib.sendMessage(p, lib.notEnough(needed));
											}

										});
									}
									if (ClansAPI.getDataInstance().isTrue("Clans.mode-change.timer.use")) {
										if (c.getModeCooldown().isComplete()) {
											c.setPeaceful(false);
											c.getModeCooldown().setCooldown();
											Bukkit.broadcastMessage(Clan.ACTION.color(lib.modeAnnounce(args1, c.getName())));
											lib.sendMessage(p, lib.war());
										} else {
											lib.sendMessage(p, c.getModeCooldown().fullTimeLeft());
											return true;
										}
										return true;
									}
									c.setPeaceful(false);
									Bukkit.broadcastMessage(Clan.ACTION.color(lib.modeAnnounce(args1, c.getName())));
									lib.sendMessage(p, lib.war());
								} else {
									lib.sendMessage(p, lib.alreadyWar());
									return true;
								}
								break;

							case "peace":
								if (c.isPeaceful()) {
									lib.sendMessage(p, lib.alreadyPeaceful());
								} else {
									if (ClansAPI.getDataInstance().isTrue("Clans.mode-change.charge")) {
										double amount = ClansAPI.getDataInstance().getConfig().getRoot().getDouble("Clans.mode-change.amount");
										double balance = EconomyProvision.getInstance().balance(p).orElse(0.0);
										double needed = amount - balance;
										EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p).ifPresent(b -> {

											if (!b) {
												lib.sendMessage(p, lib.notEnough(needed));
											}

										});
									}
									if (ClansAPI.getDataInstance().isTrue("Clans.mode-change.timer.use")) {
										if (c.getModeCooldown().isComplete()) {
											c.setPeaceful(true);
											c.getModeCooldown().setCooldown();
											Bukkit.broadcastMessage(Clan.ACTION.color(lib.modeAnnounce(args1, c.getName())));
											lib.sendMessage(p, lib.peaceful());
										} else {
											lib.sendMessage(p, c.getModeCooldown().fullTimeLeft());
											return true;
										}
										return true;
									}
									c.setPeaceful(true);
									Bukkit.broadcastMessage(Clan.ACTION.color(lib.modeAnnounce(args1, c.getName())));
									lib.sendMessage(p, lib.peaceful());
									return true;
								}
								break;
							default:
								lib.sendMessage(p, "&cUnknown pvp type.");
								break;
						}
					} else {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args0.equalsIgnoreCase("nick") || args0.equalsIgnoreCase("nickname")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("nickname"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("nickname")));
					return true;
				}
				if (associate != null) {
					if (!isAlphaNumeric(args1)) {
						lib.sendMessage(p, lib.nameInvalid(args1));
						return true;
					}
					associate.setNickname(args1);
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("top")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("top"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("top")));
					return true;
				}
				switch (args1.toLowerCase()) {
					case "money":
						Clan.ACTION.getLeaderboard(ClanAction.LeaderboardType.MONEY, p, 1);
						break;
					case "power":
						Clan.ACTION.getLeaderboard(ClanAction.LeaderboardType.POWER, p, 1);
						break;
					case "wins":
						Clan.ACTION.getLeaderboard(ClanAction.LeaderboardType.WINS, p, 1);
						break;
					case "kd":
						Clan.ACTION.getLeaderboard(ClanAction.LeaderboardType.KILLS, p, 1);
						break;
					default:
						lib.sendMessage(p, lib.pageUnknown());
						break;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("players")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("players"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("players")));
					return true;
				}
				try {
					Clan.ACTION.getPlayerboard(p, Integer.parseInt(args1));
				} catch (IllegalFormatException e) {
					lib.sendMessage(p, lib.pageUnknown());
				}
				return true;
			}
			if (args0.equalsIgnoreCase("passowner")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("passowner"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("passowner")));
					return true;
				}
				if (associate != null) {
					UUID target = Clan.ACTION.getUserID(args1);
					if (target != null) {

						if (associate.getPriority().toInt() == 3) {
							if (!associate.getClan().transferOwnership(ClansAPI.getInstance().getAssociate(target).get())) {
								sendMessage(p, lib.playerUnknown("clan member"));
							} else {
								associate.getClan().broadcast("&eClan ownership was transferred to associate " + args1);
							}
						} else {
							lib.sendMessage(p, lib.noClearance());
						}

					} else {
						lib.sendMessage(p, lib.playerUnknown(args1));
						return true;
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args0.equalsIgnoreCase("join")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("join"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("join")));
					return true;
				}
				LabyrinthUser user = LabyrinthUser.get(p.getName());
				if (user.isValid()) {
					Clan.ACTION.joinClan(p.getUniqueId(), args1, "none");
				} else {
					lib.sendMessage(p, "&cYou appear to not be a valid mojang user, enable to proceed.");
				}
				return true;
			}
			if (args0.equalsIgnoreCase("tag")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("tag"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("tag")));
					return true;
				}
				if (associate != null) {
					Clan clan = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
					if (associate.getPriority().toInt() >= Clan.ACTION.tagChangeClearance()) {
						if (!isAlphaNumeric(args1)) {
							lib.sendMessage(p, lib.nameInvalid(args1));
							return true;
						}
						if (args1.length() > ClansAPI.getDataInstance().getConfig().read(f -> f.getInt("Formatting.tag-size"))) {
							Clan.ACTION.sendMessage(p, lib.nameTooLong(args1));
							return true;
						}
						if (Clan.ACTION.getAllClanNames().contains(args1)) {
							lib.sendMessage(p, lib.alreadyMade(args1));
							return true;
						}
						for (String s : ClansAPI.getDataInstance().getConfig().getRoot().getStringList("Clans.name-blacklist")) {
							if (Pattern.compile(Pattern.quote(args1), Pattern.CASE_INSENSITIVE).matcher(s).find()) {
								lib.sendMessage(p, "&c&oThis name is not allowed!");
								return true;
							}
						}
						AssociateRenameClanEvent ev = ClanVentBus.call(new AssociateRenameClanEvent(p, clan.getName(), args1));
						if (!ev.isCancelled()) {
							clan.setName(ev.getTo());
						}
						if (!LabyrinthProvider.getInstance().isLegacy()) {
							clan.getMembers().forEach(a -> {
								OfflinePlayer op = a.getUser().toBukkit();
								if (op.isOnline()) {
									if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
										if (clan.getPalette().isGradient()) {
											Clan c = a.getClan();
											ClanDisplayName.set(op.getPlayer(), ClansAPI.getDataInstance().formatDisplayTag("", c.getPalette().toGradient().context(c.getName()).translate()));
										} else {
											ClanDisplayName.set(op.getPlayer(), ClansAPI.getDataInstance().formatDisplayTag(ClansAPI.getInstance().getClanManager().getClan(op.getUniqueId()).getPalette().toString(), ClansAPI.getInstance().getClanManager().getClan(op.getUniqueId()).getName()));

										}
									}
								}
							});
						}
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("color")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("color"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("color")));
					return true;
				}
				if (associate != null) {
					Clan clan = associate.getClan();
					if (Clearance.MANAGE_COLOR.test(associate)) {

						if (args1.equalsIgnoreCase("random")) {
							clan.getPalette().randomize();
							lib.sendMessage(p, clan.getPalette().isGradient() ? clan.getPalette().toString("The clan color has been updated.") : clan.getPalette().toString() + "The clan color has been updated.");
							return true;
						}

						if (!args1.matches("(&#[a-zA-Z0-9]{6})+(&[a-zA-Z0-9])+") && !args1.matches("(&[a-zA-Z0-9])+") && !args1.matches("(&#[a-zA-Z0-9])+") && !args1.matches("(#[a-zA-Z0-9])+")) {
							lib.sendMessage(p, "&c&oInvalid color format.");
							return true;
						}

						for (String s : ClansAPI.getDataInstance().getConfig().read(c -> c.getStringList("Clans.color-blacklist"))) {

							if (StringUtils.use(args1).containsIgnoreCase(s)) {
								lib.sendMessage(p, "&c&oInvalid color format. Code: '" + s + "' is not allowed.");
								return true;
							}
						}

						clan.getPalette().setStart(args1);
						if (!LabyrinthProvider.getInstance().isLegacy()) {
							clan.getMembers().forEach(a -> {
								OfflinePlayer op = a.getUser().toBukkit();
								try {
									if (op.isOnline()) {
										if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
											if (clan.getPalette().isGradient()) {
												Clan c = a.getClan();
												ClanDisplayName.set(op.getPlayer(), ClansAPI.getDataInstance().formatDisplayTag("", c.getPalette().toGradient().context(c.getName()).translate()));
											} else {
												ClanDisplayName.set(op.getPlayer(), ClansAPI.getDataInstance().formatDisplayTag(ClansAPI.getInstance().getClanManager().getClan(op.getUniqueId()).getPalette().toString(), ClansAPI.getInstance().getClanManager().getClan(op.getUniqueId()).getName()));

											}
										}
									}
								} catch (NullPointerException e) {
									ClansAPI.getInstance().getPlugin().getLogger().severe("- Failed to updated name tags for user " + op.getName() + ".");
								}
							});
						}
						clan.broadcast(args1 + "Our color was changed.");
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("promote")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("promote"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("promote")));
					return true;
				}
				FileManager main = ClansAPI.getDataInstance().getConfig();
				String adminRank = main.getRoot().getString("Formatting.Chat.Styles.Full.Admin");
				String ownerRank = main.getRoot().getString("Formatting.Chat.Styles.Full.Owner");
				if (associate != null) {
					if (Clearance.MANAGE_POSITIONS.test(associate)) {
						UUID tid = Clan.ACTION.getUserID(args1);
						if (tid == null) {
							lib.sendMessage(p, lib.playerUnknown(args1));
							return true;
						}
						Clan.Associate member = associate.getClan().getMember(m -> m.getId().equals(tid));
						if (member == null) return true;
						if (member.getPriority().toInt() >= 2) {
							lib.sendMessage(p, lib.alreadyMax(adminRank, ownerRank));
							return true;
						}
						Clan.ACTION.promotePlayer(tid);
					} else {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("demote")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("demote"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("demote")));
					return true;
				}
				if (associate != null) {
					if (Clearance.MANAGE_POSITIONS.test(associate)) {
						UUID tid = Clan.ACTION.getUserID(args1);
						if (tid == null) {
							lib.sendMessage(p, lib.playerUnknown(args1));
							return true;
						}
						Clan.Associate member = associate.getClan().getMember(m -> m.getId().equals(tid));
						if (member == null) return true;
						if (member.getPriority().toInt() >= associate.getPriority().toInt()) {
							lib.sendMessage(p, lib.noClearance());
							return true;
						}
						Clan.ACTION.demotePlayer(tid);
					} else {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("unclaim")) {
				if (Claim.ACTION.isEnabled()) {
					if (args1.equalsIgnoreCase("all")) {
						if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("unclaimall"))) {
							lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("unclaimall")));
							return true;
						}
						if (associate != null) {
							if (Clearance.MANAGE_ALL_LAND.test(associate)) {
								Claim.ACTION.unclaimAll(p);
							} else {
								lib.sendMessage(p, lib.noClearance());
								return true;
							}
						} else {
							lib.sendMessage(p, lib.notInClan());
							return true;
						}
						return true;
					}
				} else {
					lib.sendMessage(p, "&c&oYour server doesn't allow the use of clan land-claiming.");
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("password") || args0.equalsIgnoreCase("pass")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("password"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("password")));
					return true;
				}
				Clan clan = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
				if (associate != null) {
					if (Clearance.MANAGE_PASSWORD.test(associate)) {
						if (!isAlphaNumeric(args1)) {
							lib.sendMessage(p, lib.passwordInvalid());
							return true;
						}
						clan.setPassword(args1);
					} else {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("kick")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("kick"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("kick")));
					return true;
				}
				if (associate != null) {
					if (Clearance.KICK_MEMBERS.test(associate)) {
						UUID tid = Clan.ACTION.getUserID(args1);
						if (tid == null) {
							lib.sendMessage(p, lib.playerUnknown(args1));
							return true;
						}
						OfflinePlayer target = Bukkit.getOfflinePlayer(tid);
						Clan clan = associate.getClan();
						Clan.Associate member = clan.getMember(m -> m.getId().equals(target.getUniqueId()));
						if (member == null) {
							lib.sendMessage(p, lib.playerUnknown(args1));
							return true;
						}
						if (member.getPriority().toInt() > associate.getPriority().toInt()) {
							lib.sendMessage(p, lib.noClearance());
							return true;
						}
						if (tid.equals(p.getUniqueId())) {
							lib.sendMessage(p, lib.nameInvalid(args1));
							return true;
						}
						AssociateKickAssociateEvent event = ClanVentBus.call(new AssociateKickAssociateEvent(member, associate));
						if (!event.isCancelled()) {
							member.remove();
							String format = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("kick-out"), target.getName());
							String format1 = MessageFormat.format(ClansAPI.getDataInstance().getMessageResponse("kick-in"), p.getName());
							clan.broadcast(format);
							if (target.isOnline()) {
								lib.sendMessage(target.getPlayer(), format1);
							}
						}
					} else {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("members")) {

				if (associate != null) {
					try {
						int page = Integer.parseInt(args1);
						Clan.ACTION.getClanboard(p, page);
					} catch (NumberFormatException e) {
						lib.sendMessage(p, lib.pageUnknown());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("info") || args0.equalsIgnoreCase("i")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("info-other"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("info-other")));
					return true;
				}
				UUID target = Clan.ACTION.getUserID(args1);
				if (target == null) {
					if (!Clan.ACTION.getAllClanNames().contains(args1)) {
						lib.sendMessage(p, lib.clanUnknown(args1));
						return true;
					}
					if (associate != null && args1.equals(associate.getClan().getName())) {
						AssociateDisplayInfoEvent ev = ClanVentBus.queue(new AssociateDisplayInfoEvent(associate, AssociateDisplayInfoEvent.Type.PERSONAL)).join();
						if (!ev.isCancelled()) {
							Clan.ACTION.getClanboard(p, 1);
						}
						return true;
					}
					Clan clan = ClansAPI.getInstance().getClanManager().getClan(ClansAPI.getInstance().getClanManager().getClanID(args1));
					AssociateDisplayInfoEvent ev = ClanVentBus.queue(new AssociateDisplayInfoEvent(associate, p, clan, AssociateDisplayInfoEvent.Type.OTHER)).join();
					if (!ev.isCancelled()) {
						for (String info : clan.getClanInfo()) {

							if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
								sendMessage(p, info.replace(ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId()).getName(), "&6&lUS"));
							} else {
								sendMessage(p, info);
							}
						}
					}
					if (ClansAPI.getDataInstance().ID_MODE.containsKey(p) && ClansAPI.getDataInstance().ID_MODE.get(p).equals("ENABLED")) {
						lib.sendMessage(p, "&7#&fID &7of clan " + '"' + args1 + '"' + " is: &e&o" + ClansAPI.getInstance().getClanManager().getClanID(args1));
					}
					return true;
				}
				ClansAPI.getInstance().getAssociate(target).ifPresent(a -> {
					GUI.MEMBER_INFO.get(a).open(p);
					if (ClansAPI.getDataInstance().ID_MODE.containsKey(p) && ClansAPI.getDataInstance().ID_MODE.get(p).equals("ENABLED")) {
						lib.sendMessage(p, "&7#&fID &7of player " + '"' + args1 + '"' + " clan " + '"' + a.getClan().getName() + '"' + " is: &e&o" + a.getClan().getId().toString());
					}
				});
				return true;
			}
			if (args0.equalsIgnoreCase("message")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("message"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("message")));
					return true;
				}
				Clan clan = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
				if (associate != null)
					clan.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.chat-message-format"), p.getName()) + " " + args1);
				return true;
			}
			if (args0.equalsIgnoreCase("bio")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio")));
					return true;
				}
				if (associate != null) {
					associate.setBio(args1);
				}
				return true;
			}
			if (args0.equalsIgnoreCase("description") || args0.equalsIgnoreCase("desc")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("description"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("description")));
					return true;
				}
				if (associate != null) {
					if (Clearance.MANAGE_DESCRIPTION.test(associate)) {
						Clan c = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
						c.setDescription(args1);
					} else {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
				}
				return true;
			}
			if (args0.equalsIgnoreCase("ally")) {
				Bukkit.dispatchCommand(p, "c ally add " + args1);
				return true;
			}

			if (args0.equalsIgnoreCase("remally")) {
				Bukkit.dispatchCommand(p, "c ally remove " + args1);
				return true;
			}

			if (args0.equalsIgnoreCase("remenemy")) {
				Bukkit.dispatchCommand(p, "c enemy remove " + args1);
				return true;
			}

			if (args0.equalsIgnoreCase("enemy")) {
				Bukkit.dispatchCommand(p, "c enemy add " + args1);
				return true;
			}
			lib.sendMessage(p, lib.commandUnknown(label));
			return true;
		}

		if (length == 3) {
			String args0 = args[0];
			String args1 = args[1];
			String args2 = args[2];
			if (args0.equalsIgnoreCase("permit")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("permit"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("permit")));
					return true;
				}
				if (associate != null) {
					try {
						Integer.parseInt(args2);
					} catch (NumberFormatException ig) {
						lib.sendMessage(p, "&cAn invalid rank level was provided");
						return true;
					}
					ClearanceLog log = associate.getClan().getPermissions();
					log.set(Clearance.LAND_USE, Clearance.Level.ADMIN);
					if (Clearance.MANAGE_PERMS.test(associate)) {

						Clearance target = null;
						for (Map.Entry<Clearance, Integer> entry : log) {
							if (StringUtils.use(entry.getKey().getName()).containsIgnoreCase(args1)) {
								target = entry.getKey();
								break;
							}
						}

						if (target == null) {
							lib.sendMessage(p, "&cUnknown permission.");
							return true;
						}

						int t = Integer.parseInt(args2);

						if (!Arrays.asList(Clearance.Level.values()).contains(t)) {
							lib.sendMessage(p, "&cAn invalid rank level was provided");
							return true;
						}

						log.set(target, t);
						lib.sendMessage(p, "&aClan permission &f" + target.getName() + " &arequired rank level changed to &6" + t);

					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args0.equalsIgnoreCase("color")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("color"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("color")));
					return true;
				}
				if (associate != null) {

					if (!LabyrinthProvider.getService(Service.LEGACY).isNew()) {
						lib.sendMessage(p, "&cOlder version detected! To use gradients the server version must be no lower than 1.16");
						return true;
					}

					Clan clan = associate.getClan();
					if (Clearance.MANAGE_COLOR.test(associate)) {

						if (!args1.matches("(&#[a-zA-Z0-9]{6})+") && !args1.matches("(#[a-zA-Z0-9]{6})+")) {
							lib.sendMessage(p, "&c&oInvalid color format. Only hex is allowed for gradients.");
							return true;
						}

						if (!args2.matches("(&#[a-zA-Z0-9]{6})+") && !args2.matches("(#[a-zA-Z0-9]{6})+")) {
							lib.sendMessage(p, "&c&oInvalid color format. Only hex is allowed for gradients.");
							return true;
						}

						for (String s : ClansAPI.getDataInstance().getConfig().getRoot().getStringList("Clans.color-blacklist")) {

							if (StringUtils.use(args1).containsIgnoreCase(s) || StringUtils.use(args2).containsIgnoreCase(s)) {
								lib.sendMessage(p, "&c&oInvalid color format. Code: '" + s + "' is not allowed.");
								return true;
							}
						}

						if (args2.equalsIgnoreCase("empty") || args2.equalsIgnoreCase("reset")) {
							clan.getPalette().setStart(null);
							clan.getPalette().setEnd(null);
							lib.sendMessage(p, "&aGradient color removed.");
						} else {
							clan.getPalette().setStart(args1);
						}
						if (args2.equalsIgnoreCase("empty") || args2.equalsIgnoreCase("reset")) {
							clan.getPalette().setEnd(null);
							lib.sendMessage(p, "&aGradient color removed.");
						} else {
							clan.getPalette().setEnd(args2);
						}
						clan.broadcast(clan.getPalette().toGradient().context("Our color was changed").translate());
						clan.getMembers().forEach(a -> {
							OfflinePlayer op = a.getUser().toBukkit();
							try {
								if (op.isOnline()) {
									if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
										if (clan.getPalette().isGradient()) {
											Clan c = a.getClan();
											ClanDisplayName.update(p, ClansAPI.getDataInstance().formatDisplayTag("", c.getPalette().toGradient().context(c.getName()).translate()));
										} else {
											ClanDisplayName.update(p, ClansAPI.getDataInstance().formatDisplayTag(ClansAPI.getInstance().getClanManager().getClan(op.getUniqueId()).getPalette().toString(), ClansAPI.getInstance().getClanManager().getClan(op.getUniqueId()).getName()));

										}
									}
								}
							} catch (NullPointerException e) {
								ClansAPI.getInstance().getPlugin().getLogger().severe("- Failed to updated name tags for user " + op.getName() + ".");
							}
						});
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("top")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("top"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("top")));
					return true;
				}
				try {
					switch (args1.toLowerCase()) {
						case "money":
							Clan.ACTION.getLeaderboard(ClanAction.LeaderboardType.MONEY, p, Integer.parseInt(args2));
							break;
						case "power":
							Clan.ACTION.getLeaderboard(ClanAction.LeaderboardType.POWER, p, Integer.parseInt(args2));
							break;
						case "wins":
							Clan.ACTION.getLeaderboard(ClanAction.LeaderboardType.WINS, p, Integer.parseInt(args2));
							break;
						case "kd":
							Clan.ACTION.getLeaderboard(ClanAction.LeaderboardType.KILLS, p, Integer.parseInt(args2));
							break;
						default:
							lib.sendMessage(p, lib.pageUnknown());
							break;
					}
				} catch (NumberFormatException ignored) {
				}
				return true;
			}
			if (args0.equalsIgnoreCase("logo")) {

				if (associate == null) {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}

				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo")));
					return true;
				}

				if (args1.equalsIgnoreCase("redraw")) {
					lib.sendMessage(p, "&cUsage: &f/c logo &eredraw &r[height] [width]");
					return true;
				}

				if (args1.equalsIgnoreCase("color")) {

					if (!Clearance.LOGO_COLOR.test(associate)) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}

					if (!args2.matches("(&#[a-zA-Z0-9]{6})+(&[a-zA-Z0-9])+") && !args2.matches("(&[a-zA-Z0-9])+") && !args2.matches("(&#[a-zA-Z0-9])+") && !args2.matches("(#[a-zA-Z0-9])+")) {
						lib.sendMessage(p, "&c&oInvalid color format.");
						return true;
					}

					Insignia i = Insignia.get("Template:" + p.getUniqueId().toString());

					if (i != null) {

						Mailer msg = Mailer.empty(p);

						i.setSelection(args2);

						msg.chat("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();
						for (BaseComponent[] components : i.get()) {
							p.spigot().sendMessage(components);
						}
						msg.chat("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").deploy();

						lib.sendMessage(p, "&aColor pallet updated to " + args2 + "TEST");
					} else {
						lib.sendMessage(p, "&cNo drawing was found.");
					}

					return true;
				}
				return true;
			}

			if (args0.equalsIgnoreCase("create")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("create"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("create")));
					return true;
				}
				if (!isAlphaNumeric(args1)) {
					lib.sendMessage(p, lib.nameInvalid(args1));
					return true;
				}
				if (Clan.ACTION.getAllClanNames().contains(args1)) {
					lib.sendMessage(p, lib.alreadyMade(args1));
					return true;
				}
				LabyrinthUser user = LabyrinthUser.get(p.getName());
				if (user.isValid()) {
					Clan.ACTION.create(p.getUniqueId(), args1, args2);
				} else {
					lib.sendMessage(p, "&cYou appear to not be a valid mojang user, enable to proceed.");
				}
				return true;
			}
			if (args0.equalsIgnoreCase("join")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("join"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("join")));
					return true;
				}
				LabyrinthUser user = LabyrinthUser.get(p.getName());
				if (user.isValid()) {
					Clan.ACTION.joinClan(p.getUniqueId(), args1, args2);
				} else {
					lib.sendMessage(p, "&cYou appear to not be a valid mojang user, enable to proceed.");
				}
				return true;
			}
			if (args0.equalsIgnoreCase("message")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("message"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("message")));
					return true;
				}
				if (associate != null) {
					Clan clan = associate.getClan();
					clan.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.chat-message-format"), p.getName()) + " " + args1 + " " + args2);
				}
				return true;
			}
			if (args0.equalsIgnoreCase("bank")) {
				if (associate == null) {
					sendMessage(p, "&c" + Messages.PLAYER_NO_CLAN);
					return true;
				}
				if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
					lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
					return true;
				}
				final String arg1 = args1.toLowerCase();
				switch (arg1) {
					case "deposit":
					case "withdraw":
						try {
							final BigDecimal amount = new BigDecimal(args2);
							if (amount.signum() != 1) {
								sendMessage(p, Messages.BANK_INVALID_AMOUNT.toString());
								return true;
							}
							final ClanBank theBank = associate.getClan();
							switch (arg1) {
								case "deposit":
									if (BankPermissions.BANKS_DEPOSIT.not(p)) {
										sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
										return true;
									}
									if (theBank.deposit(p, amount)) {
										sendMessage(p, Messages.DEPOSIT_MSG_PLAYER.toString()
												.replace("{0}", amount.toString()));
									} else {
										sendMessage(p, Messages.DEPOSIT_ERR_PLAYER.toString()
												.replace("{0}", amount.toString()));
									}
									break;
								case "withdraw":
									if (BankPermissions.BANKS_WITHDRAW.not(p)) {
										sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
										return true;
									}
									if (theBank.withdraw(p, amount)) {
										sendMessage(p, Messages.WITHDRAW_MSG_PLAYER.toString()
												.replace("{0}", amount.toString()));
									} else {
										sendMessage(p, Messages.WITHDRAW_ERR_PLAYER.toString()
												.replace("{0}", amount.toString()));
									}
									break;
							}
						} catch (NumberFormatException exception) {
							sendMessage(p, Messages.BANK_INVALID_AMOUNT.toString());
						}
						return true;
					case "setperm":
						sendMessage(p, Messages.BANK_USAGE.toString());
						switch (args2.toLowerCase()) {
							case "balance":
							case "deposit":
							case "withdraw":
							case "viewlog":
								message().append(text(Messages.BANK_HELP_PREFIX + " setperm " + args2.toLowerCase() + " "))
										.append(text("&7<&clevel&7>").bind(hover("Valid levels [0-3]"))).send(p).deploy();
								break;
							default:
								message().append(text(Messages.BANK_HELP_PREFIX + " setperm "))
										.append(text("&7<&cperm&7>").bind(hover("&6Valid options:&7\n&o*&f balance&7\n&o*&f deposit&7\n&o*&f withdraw&7\n&o*&f viewlog")))
										.append(text(" &7<&flevel&7>"))
										.send(p).deploy();
						}
						return true;
					default: // receive subcommand usage message
						sendMessage(p, Messages.BANK_INVALID_SUBCOMMAND.toString());
						return true;
				}
			}
			if (args0.equalsIgnoreCase("bio")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio")));
					return true;
				}
				if (associate != null) {
					associate.setBio(args1 + " " + args2);
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
			if (args0.equalsIgnoreCase("description") || args0.equalsIgnoreCase("desc")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("description"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("description")));
					return true;
				}
				if (associate != null) {
					if (Clearance.MANAGE_DESCRIPTION.test(associate)) {
						Clan c = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
						c.setDescription(args1 + " " + args2);
					} else {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}
				}
				return true;
			}
			if (args0.equalsIgnoreCase("enemy")) {
				if (args1.equalsIgnoreCase("add")) {
					if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("enemy"))) {
						lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("enemy")));
						return true;
					}
					if (associate != null) {
						Clan c = associate.getClan();
						if (!Clearance.MANAGE_RELATIONS.test(associate)) {
							lib.sendMessage(p, lib.noClearance());
							return true;
						}
						if (!Clan.ACTION.getAllClanNames().contains(args2)) {
							lib.sendMessage(p, lib.clanUnknown(args2));
							return true;
						}
						Clan t = ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(args2));
						if (args2.equals(associate.getClan().getName())) {
							lib.sendMessage(p, lib.allianceDenial());
							return true;
						}
						if (c.getRelation().getRivalry().has(t)) {
							lib.sendMessage(p, lib.alreadyEnemies(args2));
							return true;
						}
						List<String> online = new ArrayList<>();
						for (Clan.Associate associate1 : t.getMembers()) {
							if (associate1.getUser().isOnline()) {
								online.add(associate1.getName());
							}
						}
						if (ClansAPI.getDataInstance().isTrue("Clans.relations.enemy.cancel-if-empty")) {
							if (online.isEmpty()) {
								lib.sendMessage(p, "&c&oThis clan has no members online, unable to mark as enemy.");
								return true;
							}
						}
						if (c.getRelation().isNeutral(t)) {
							if (c.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDeny());
								return true;
							}
							if (t.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
								return true;
							}
							c.getRelation().getRivalry().add(t);
							return true;
						}
						if (c.getRelation().getAlliance().has(t)) {
							if (c.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDeny());
								return true;
							}
							if (t.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
								return true;
							}
							c.getRelation().getRivalry().add(t);
							return true;
						}
					}
					return true;
				}
				if (args1.equalsIgnoreCase("remove")) {
					if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("removeenemy"))) {
						lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("removeenemy")));
						return true;
					}
					if (associate != null) {
						Clan c = associate.getClan();
						if (!Clearance.MANAGE_RELATIONS.test(associate)) {
							lib.sendMessage(p, lib.noClearance());
							return true;
						}
						if (!Clan.ACTION.getAllClanNames().contains(args2)) {
							lib.sendMessage(p, lib.clanUnknown(args2));
							return true;
						}
						Clan t = ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(args2));
						if (args2.equals(c.getName())) {
							lib.sendMessage(p, lib.allianceDenial());
							return true;
						}
						if (t.getRelation().getRivalry().has(c)) {
							lib.sendMessage(p, lib.noRemoval(args2));
							return true;
						}
						if (!c.getRelation().getRivalry().has(t)) {
							lib.sendMessage(p, lib.notEnemies(t.getName()));
							return true;
						}
						if (c.isPeaceful()) {
							lib.sendMessage(p, lib.peacefulDeny());
							return true;
						}
						if (t.isPeaceful()) {
							lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
							return true;
						}
						c.getRelation().getRivalry().remove(t);
					} else {
						lib.sendMessage(p, lib.notInClan());
						return true;
					}
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("ally")) {
				if (args1.equalsIgnoreCase("add")) {
					if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("ally"))) {
						lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("ally")));
						return true;
					}
					if (associate != null) {
						Clan c = associate.getClan();
						if (!Clearance.MANAGE_RELATIONS.test(associate)) {
							lib.sendMessage(p, lib.noClearance());
							return true;
						}
						if (!Clan.ACTION.getAllClanNames().contains(args2)) {
							lib.sendMessage(p, lib.clanUnknown(args2));
							return true;
						}
						Clan t = ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(args2));
						if (args2.equals(c.getName())) {
							lib.sendMessage(p, lib.allianceDenial());
							return true;
						}
						if (c.getRelation().getAlliance().has(t)) {
							lib.sendMessage(p, lib.alreadyAllies(args2));
							return true;
						}
						if (t.getRelation().getRivalry().has(c)) {
							lib.sendMessage(p, lib.alreadyEnemies(args2));
							return true;
						}
						if (c.getRelation().isNeutral(t)) {
							if (c.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDeny());
								return true;
							}
							if (t.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
								return true;
							}
							c.getRelation().getAlliance().request(t);
							return true;
						}
						if (c.isPeaceful()) {
							lib.sendMessage(p, lib.peacefulDeny());
							return true;
						}
						if (t.isPeaceful()) {
							lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
							return true;
						}
						c.getRelation().getAlliance().add(t);
					} else {
						lib.sendMessage(p, lib.notInClan());
						return true;
					}
					return true;
				}
				if (args1.equalsIgnoreCase("remove")) {
					if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("removeally"))) {
						lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("removeally")));
						return true;
					}
					if (associate != null) {
						Clan c = associate.getClan();
						if (!Clearance.MANAGE_RELATIONS.test(associate)) {
							lib.sendMessage(p, lib.noClearance());
							return true;
						}
						if (!Clan.ACTION.getAllClanNames().contains(args2)) {
							lib.sendMessage(p, lib.clanUnknown(args2));
							return true;
						}
						Clan t = ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(args2));
						if (args2.equals(c.getName())) {
							lib.sendMessage(p, lib.allianceDenial());
							return true;
						}

						List<String> online = new ArrayList<>();
						t.getMembers().forEach(a -> {
							if (a.getUser().isOnline()) {
								online.add(a.getName());
							}
						});
						if (ClansAPI.getDataInstance().isTrue("Clans.relations.ally.cancel-if-empty")) {
							if (online.isEmpty()) {
								lib.sendMessage(p, "&c&oThis clan has no members online, unable to mark as enemy.");
								return true;
							}
						}

						if (c.getRelation().isNeutral(t)) {
							lib.sendMessage(p, lib.alreadyNeutral(args2));
							return true;
						}
						if (c.isPeaceful()) {
							lib.sendMessage(p, lib.peacefulDeny());
							return true;
						}
						if (t.isPeaceful()) {
							lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
							return true;
						}
						c.getRelation().getAlliance().remove(t);
						t.getRelation().getAlliance().remove(c);
						c.broadcast(lib.neutral(t.getName()));
						t.broadcast(lib.neutral(c.getName()));
					} else {
						lib.sendMessage(p, lib.notInClan());
						return true;
					}
					return true;
				}
			}
			lib.sendMessage(p, lib.commandUnknown(label));
			return true;
		}
		if (length == 4) {
			if (associate == null) {
				sendMessage(p, Messages.PLAYER_NO_CLAN.toString());
				return true;
			}
			final Clan clan = associate.getClan();

			if (args[0].equalsIgnoreCase("logo")) {

				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo")));
					return true;
				}

				if (args[1].equalsIgnoreCase("redraw")) {
					if (!Clearance.LOGO_EDIT.test(associate)) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}

					try {

						int height = Integer.parseInt(args[2]);

						int width = Integer.parseInt(args[3]);

						Insignia i = Insignia.get("Template:" + p.getUniqueId().toString());
						if (i != null) {
							i.remove();
							new Insignia.Builder("Template:" + p.getUniqueId().toString()).setHeight(Math.min(Math.max(height, 6), 16)).setWidth(Math.min(Math.max(width, 3), 22)).draw(p);
							lib.sendMessage(p, "&cYou have reset your current insignia work space.");
						} else {
							lib.sendMessage(p, "&cYou have no insignia to reset.");
						}
					} catch (NumberFormatException e) {
						lib.sendMessage(p, "&cYou entered an invalid amount.");
					}
					return true;
				}

				return true;
			}

			if (args[0].equalsIgnoreCase("bank")) {
				if (!Bukkit.getPluginManager().isPluginEnabled("Vault") && !Bukkit.getPluginManager().isPluginEnabled("Enterprise")) {
					lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
					return true;
				}
				if (args[1].equalsIgnoreCase("setperm")) {
					int level;
					try {
						level = Integer.parseInt(args[3]);
					} catch (NumberFormatException e) {
						level = -1;
					}
					if (level < 0 || level > 3) {
						sendMessage(p, "&7Invalid level! Valid levels [0-3]");
						return true;
					}
					switch (args[2].toLowerCase()) {
						case "balance":
							sendMessage(p, "&7Setting &6balance &7level to &a" + level);
							BankAction.BALANCE.setRankForActionInClan(clan, level);
							return true;
						case "deposit":
							sendMessage(p, "&7Setting &6deposit &7level to &a" + level);
							BankAction.DEPOSIT.setRankForActionInClan(clan, level);
							return true;
						case "withdraw":
							sendMessage(p, "&7Setting &6withdraw &7level to &a" + level);
							BankAction.WITHDRAW.setRankForActionInClan(clan, level);
							return true;
						case "viewlog":
							sendMessage(p, "&7Setting &6viewlog &7level to &a" + level);
							BankAction.VIEW_LOG.setRankForActionInClan(clan, level);
							return true;
						default:
							sendMessage(p, Messages.BANK_USAGE.toString());
							if (TextLib.getInstance() != null) {
								p.spigot().sendMessage(TextLib.getInstance().textHoverable(
										Messages.BANK_HELP_PREFIX + " setperm ",
										"&7<&cperm&7>",
										" &7<&flevel&7>",
										"&6Valid options:&7\n&o*&f balance&7\n&o*&f deposit&7\n&o*&f withdraw&7\n&o*&f viewlog"
								));
							} else {
								p.spigot().sendMessage(new OldComponent().textHoverable(
										Messages.BANK_HELP_PREFIX + " setperm ",
										"&7<&cperm&7>",
										" &7<&flevel&7>",
										"&6Valid options:&7\n&o*&f balance&7\n&o*&f deposit&7\n&o*&f withdraw&7\n&o*&f viewlog"
								));
							}
							return true;
					}
				}
			}
		}

		String args0 = args[0];
		StringBuilder rsn = new StringBuilder();
		for (int i = 1; i < args.length; i++)
			rsn.append(args[i]).append(" ");
		int stop = rsn.length() - 1;
		if (args0.equalsIgnoreCase("message")) {
			Clan clan = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
			clan.broadcast(MessageFormat.format(ClansAPI.getDataInstance().getConfig().getRoot().getString("Formatting.chat-message-format"), p.getName()) + " " + rsn.substring(0, stop));
			return true;
		}
		if (args0.equalsIgnoreCase("bio")) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio")));
				return true;
			}
			if (associate != null) {
				associate.setBio(rsn.substring(0, stop));
			} else {
				lib.sendMessage(p, lib.notInClan());
			}
			return true;
		}
		if (args0.equalsIgnoreCase("description") || args0.equalsIgnoreCase("desc")) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("description"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("description")));
				return true;
			}
			if (associate != null) {
				if (Clearance.MANAGE_DESCRIPTION.test(associate)) {
					Clan c = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
					c.setDescription(rsn.substring(0, stop));
				} else {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}
			} else {
				lib.sendMessage(p, lib.notInClan());
			}
			return true;
		}

		lib.sendMessage(p, lib.commandUnknown(label));
		return true;


	}
}
