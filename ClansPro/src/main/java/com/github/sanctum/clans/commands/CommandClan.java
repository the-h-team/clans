package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanAddonQuery;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.UI;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Insignia;
import com.github.sanctum.clans.construct.bank.BankAction;
import com.github.sanctum.clans.construct.bank.BankLog;
import com.github.sanctum.clans.construct.bank.BankPermissions;
import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.clans.construct.extra.ClanWar;
import com.github.sanctum.clans.construct.extra.Color;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.events.command.CommandHelpInsertEvent;
import com.github.sanctum.clans.events.command.CommandInsertEvent;
import com.github.sanctum.clans.events.command.ServerCommandInsertEvent;
import com.github.sanctum.clans.events.command.TabInsertEvent;
import com.github.sanctum.clans.events.core.ClanTagChangeEvent;
import com.github.sanctum.clans.events.core.bank.messaging.Messages;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.formatting.PaginatedList;
import com.github.sanctum.labyrinth.formatting.component.OldComponent;
import com.github.sanctum.labyrinth.formatting.string.ColoredString;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.Item;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.library.TextLib;
import com.google.common.collect.MapMaker;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CommandClan extends Command {

	private final ConcurrentMap<Player, List<UUID>> blockedUsers = new MapMaker().
			weakKeys().
			weakValues().
			makeMap();

	public CommandClan() {
		super("clan");
		setDescription("Base command for clans.");
		List<String> array = ClansAPI.getData().getMain().getConfig().getStringList("Formatting.aliase");
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
		FileManager msg = ClansAPI.getInstance().getFileList().find("Messages", "Configuration");
		for (String s : msg.getConfig().getConfigurationSection("Commands").getKeys(false)) {
			help.add(msg.getConfig().getString("Commands." + s + ".text"));
		}
		CommandHelpInsertEvent e = ClanVentBus.call(new CommandHelpInsertEvent(help));
		return e.getHelpMenu().stream().map(s -> s.replace("clan", label)).collect(Collectors.toList());
	}

	private boolean isAlphaNumeric(String s) {
		return s != null && s.matches("^[a-zA-Z0-9]*$");
	}

	private final List<String> arguments = new ArrayList<>();

	@Override
	public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) throws IllegalArgumentException {

		Player p = (Player) sender;
		Optional<ClanAssociate> associate = ClansAPI.getInstance().getAssociate(p);

		List<String> result = new ArrayList<>();

		for (ClanAddon cycle : ClanAddonQuery.getRegisteredAddons()) {
			if (cycle.isActive()) {
				for (ClanSubCommand subCommand : cycle.getCommands()) {
					if (subCommand.tab(p, alias, args) != null) {
						result.addAll(subCommand.tab(p, alias, args));
					}
				}
			}
		}

		if (args.length == 1) {
			arguments.clear();
			List<String> add = Arrays.asList("create", "war", "logo", "invite", "block", "peace", "forfeit", "mode", "bio", "players", "description", "friendlyfire", "color", "password", "kick", "leave", "message", "chat", "info", "promote", "demote", "tag", "nickname", "list", "base", "setbase", "top", "claim", "unclaim", "passowner", "ally", "enemy", "bank");
			for (String a : add) {
				if (p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission(a))) {
					arguments.add(a);
				}
			}
			TabInsertEvent event = ClanVentBus.call(new TabInsertEvent(args));
			arguments.addAll(event.getArgs(1));
			for (String a : arguments) {
				if (a.toLowerCase().startsWith(args[0].toLowerCase()))
					result.add(a);
			}
			return result;
		}
		if (args.length == 2) {
			TabInsertEvent event = ClanVentBus.call(new TabInsertEvent(args));
			arguments.addAll(event.getArgs(2));

			for (String t : event.getArgs(2)) {
				if (t.toLowerCase().startsWith(args[1].toLowerCase()))
					result.add(t);
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
			if (args[0].equalsIgnoreCase("war")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("war"))) {
					return result;
				}
				arguments.clear();
				arguments.addAll(Clan.ACTION.getAllClanNames());
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
				arguments.addAll(Arrays.asList("edit", "apply", "upload", "color", "redraw"));
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
					Clan c = ClansAPI.getInstance().getClan(p.getUniqueId());
					if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("promote"))) {
						return result;
					}
					arguments.clear();
					arguments.addAll(c.getMembers().stream().map(ClanAssociate::getPlayer).map(OfflinePlayer::getName).collect(Collectors.toList()));
					for (String a : arguments) {
						if (a.toLowerCase().startsWith(args[1].toLowerCase()))
							result.add(a);
					}
					return result;
				}
			}
			if (args[0].equalsIgnoreCase("demote")) {
				if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
					Clan c = ClansAPI.getInstance().getClan(p.getUniqueId());
					if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("demote"))) {
						return result;
					}
					arguments.clear();
					arguments.addAll(c.getMembers().stream().map(ClanAssociate::getPlayer).map(OfflinePlayer::getName).collect(Collectors.toList()));
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
			if (args[0].equalsIgnoreCase("peace")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("peace"))) {
					return result;
				}
				arguments.clear();
				arguments.addAll(Clan.ACTION.getAllClanNames());
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
				for (Color color : Color.values()) {
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
					ClansAPI.getInstance().getClan(p).ifPresent(clan -> {
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
			TabInsertEvent event = ClanVentBus.call(new TabInsertEvent(args));
			arguments.addAll(event.getArgs(3));

			for (String t : event.getArgs(3)) {
				if (t.toLowerCase().startsWith(args[2].toLowerCase()))
					result.add(t);
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

	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
		if (!(sender instanceof Player)) {
			boolean customMessage = false;
			if (sender instanceof ConsoleCommandSender) {

				for (ClanAddon cycle : ClanAddonQuery.getRegisteredAddons()) {
					if (cycle.isActive()) {
						for (ClanSubCommand subCommand : cycle.getCommands()) {
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

				final ServerCommandInsertEvent event = ClanVentBus.call(new ServerCommandInsertEvent(args));
				if (event.getReturn()) return true;
				event.getErrorMessage().map(s -> StringUtils.use(s).translate())
						.ifPresent(sender::sendMessage);
				if (event.getErrorMessage().isPresent()) customMessage = true;
			}
			if (!customMessage) sender.sendMessage(notPlayer());
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
			if (cycle.isActive()) {
				for (ClanSubCommand subCommand : cycle.getCommands()) {
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

		CommandInsertEvent event = ClanVentBus.call(new CommandInsertEvent(p, args));
		if (event.isCommand()) {
			return event.isCommand();
		}

		if (length == 0) {
			String ping = ClansAPI.getData().getMessage("Commands.create");
			List<String> list = new LinkedList<>(helpMenu(label));
			new PaginatedList<>(list)
					.limit(lib.menuSize())
					.start((pagination, page, max) -> {
						lib.sendMessage(p, lib.menuTitle());
						Message.form(p).send(lib.menuBorder());
					}).finish(builder -> builder.setPlayer(p).setPrefix(lib.menuBorder())).decorate((pagination, string, page, max, placement) -> Message.form(p).send(string)).get(1);

			return true;
		}
		if (this.getPermission() == null) throw new IllegalStateException("Permission cannot be null!");
		if (!p.hasPermission(this.getPermission())) {
			lib.sendMessage(p, "&4&oYou don't have permission " + '"' + this.getPermission() + '"');
			return true;
		}
		if (!ClansAPI.getData().getMain().getConfig().getStringList("Clans.world-whitelist").contains(p.getWorld().getName())) {
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
		final ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
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
					if (associate.getPriority().toInt() >= Clan.ACTION.modeChangeClearance()) {
						if (!(associate.getClan() instanceof DefaultClan))
							return true;
						DefaultClan c = (DefaultClan) ClansAPI.getInstance().getClan(p.getUniqueId());
						if (c.isPeaceful()) {
							if (ClansAPI.getData().getEnabled("Clans.mode-change.charge")) {
								double amount = ClansAPI.getData().getMain().getConfig().getDouble("Clans.mode-change.amount");
								double balance = EconomyProvision.getInstance().balance(p).orElse(0.0);
								double needed = amount - balance;
								EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p, p.getWorld().getName()).ifPresent(b -> {
									if (!b) {
										lib.sendMessage(p, lib.notEnough(needed));
									}
								});

							}
							if (ClansAPI.getData().getEnabled("Clans.mode-change.timer.use")) {
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
							if (ClansAPI.getData().getEnabled("Clans.mode-change.charge")) {
								double amount = ClansAPI.getData().getMain().getConfig().getDouble("Clans.mode-change.amount");
								double balance = EconomyProvision.getInstance().balance(p).orElse(0.0);
								double needed = amount - balance;
								EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p).ifPresent(b -> {
									if (!b) {
										lib.sendMessage(p, lib.notEnough(needed));
									}
								});
							}
							if (ClansAPI.getData().getEnabled("Clans.mode-change.timer.use")) {
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
			if (args0.equalsIgnoreCase("war")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("war"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("war")));
					return true;
				}
				lib.sendMessage(p, ClansAPI.getData().getMessage("war-usage"));
				return true;
			}
			if (args0.equalsIgnoreCase("peace")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("peace"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("peace")));
					return true;
				}
				lib.sendMessage(p, ClansAPI.getData().getMessage("peace-usage"));
				return true;
			}
			if (args0.equalsIgnoreCase("forfeit")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("forfeit"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("forfeit")));
					return true;
				}
				if (associate != null) {
					if (associate.getPriority().toInt() >= ClansAPI.getData().getInt("Clans.war.clearance")) {
						Clan.ACTION.forfeitWar(associate);
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
				UI.select(UI.Singular.ROSTER_ORGANIZATION).open(p);
				return true;
			}
			if (args0.equalsIgnoreCase("claim")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("claim"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("claim")));
					return true;
				}
				if (Claim.ACTION.isEnabled()) {
					if (associate != null) {
						if (associate.getPriority().toInt() >= Clan.ACTION.claimingClearance()) {
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
						if (associate.getPriority().toInt() >= Clan.ACTION.claimingClearance()) {
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
					if (associate.getChat().equals("GLOBAL")) {
						associate.setChat("CLAN");
						lib.sendMessage(p, lib.commandChat("CLAN"));
						return true;
					}
					if (associate.getChat().equals("CLAN")) {
						associate.setChat("ALLY");
						lib.sendMessage(p, lib.commandChat("ALLY"));
						return true;
					}
					if (associate.getChat().equals("ALLY")) {
						associate.setChat("GLOBAL");
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
					Clan.ACTION.teleportBase(p);
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
				if (associate.getPriority().toInt() >= Clan.ACTION.baseClearance()) {
					if (!ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
						clan.setBase(p.getLocation());
					} else {
						if (Claim.from(p.getLocation()).getOwner().equals(clan.getId().toString())) {
							clan.setBase(p.getLocation());
						} else {
							lib.sendMessage(p, lib.notClaimOwner(Claim.from(p.getLocation()).getClan().getName()));
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
					try {
						Clan.ACTION.getClanboard(p, 1);
					} catch (NullPointerException e) {
						e.printStackTrace();
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

				if (associate.getPriority().toInt() >= Clan.ACTION.friendlyFireClearance()) {
					if (!(associate.getClan() instanceof DefaultClan))
						return true;
					DefaultClan c = (DefaultClan) ClansAPI.getInstance().getClan(p.getUniqueId());
					if (ClansAPI.getData().getEnabled("Clans.friendly-fire.timer.use")) {

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
							Message.form(p).send(lib.menuBorder());
						}).finish(builder -> builder.setPlayer(p).setPrefix(lib.menuBorder())).decorate((pagination, string, page, max, placement) -> Message.form(p).send(string)).get(Math.max(pa, 1));
				return true;
			} catch (Exception ignored) {
			}

			lib.sendMessage(p, lib.commandUnknown(label));
			return true;
		}

		if (length == 2) {
			String args0 = args[0];
			String args1 = args[1];

			if (args0.equalsIgnoreCase("logo")) {

				if (associate == null) {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}

				if (LabyrinthProvider.getInstance().isLegacy()) {
					lib.sendMessage(p, "&cLegacy detected! This feature only works for MC 1.16 or higher.");
					return true;
				}

				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo")));
					return true;
				}

				Clan c = associate.getClan();

				if (args1.equalsIgnoreCase("edit")) {

					if (associate.getPriority().toInt() < ClansAPI.getData().getInt("Clans.logo-edit-clearance")) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}

					new Insignia.Builder("Template:" + p.getUniqueId().toString()).setBorder("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬").setColor("&2").setHeight(16).setWidth(16).draw(p);
					lib.sendMessage(p, "&eLoaded personal template.");
					return true;
				}

				if (args1.equalsIgnoreCase("redraw")) {
					lib.sendMessage(p, "&cUsage: &f/c logo &eredraw &r[height] [width]");
					return true;
				}

				if (args1.equalsIgnoreCase("upload")) {

					if (associate.getPriority().toInt() < ClansAPI.getData().getInt("Clans.logo-upload-clearance")) {
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

						c.setValue("logo", new ArrayList<>(item.getItemMeta().getLore()), false);

						lib.sendMessage(p, "&aPrinted insignia applied to clan container.");

					} else {


						return true;
					}

					return true;
				}

				if (args1.equalsIgnoreCase("apply")) {

					if (associate.getPriority().toInt() < ClansAPI.getData().getInt("Clans.logo-apply-clearance")) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}

					Insignia i = Insignia.get("Template:" + p.getUniqueId().toString());
					Insignia.copy(c.getId().toString(), i);
					if (i != null) {

						c.setValue("logo", i.getLines().stream().map(Insignia.Line::toString).collect(Collectors.toList()), false);

						lib.sendMessage(p, "&aCustom insignia applied to clan container.");

					}

					return true;
				}
/*
				if (args1.equalsIgnoreCase("saveProgress")) {
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

					if (associate.getPriority().toInt() < ClansAPI.getData().getInt("Clans.logo-print-clearance")) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}

					Insignia i = Insignia.get("Template:" + p.getUniqueId().toString());

					if (i != null) {
						p.getWorld().dropItem(p.getEyeLocation(), new Item.Edit(Material.PAPER).setTitle("(#" + HUID.randomID().toString().substring(0, 4) + ")").setLore(i.getLines().stream().map(Insignia.Line::toString).toArray(String[]::new)).build());

						lib.sendMessage(p, "&aInsignia template printed.");

					}

					return true;
				}

				new Insignia.Builder(c.getId().toString()).setHeight(16).setWidth(16).setColor(args1).draw(p);

				return true;
			}

			if (args0.equalsIgnoreCase("peace")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("peace"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("peace")));
					return true;
				}

				if (associate == null) {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}

				if (ClansAPI.getData().arenaRedTeamFile().getConfig().getKeys(false).isEmpty() || ClansAPI.getData().arenaBlueTeamFile().getConfig().getKeys(false).isEmpty()) {
					lib.sendMessage(p, "&c&oThe war arena isn't properly setup. Unable to execute command.");
					return true;
				}
				if (associate.getPriority().toInt() >= ClansAPI.getData().getInt("Clans.war.clearance")) {
					if (ClansAPI.getInstance().getClanID(args1) != null) {
						Clan target = ClansAPI.getInstance().getClan(ClansAPI.getInstance().getClanID(args1));
						DefaultClan c = (DefaultClan) associate.getClan();
						if (c.getName().equals(target.getName())) {
							lib.sendMessage(p, "&c&oCannot use own clan name.");
							return true;
						}
						if (c.getCurrentWar() != null) {
							if (!c.getCurrentWar().warActive()) {
								c.getCurrentWar().deny(target);
								target.broadcast("&f&o" + c.getName() + " has declined our request for war.");
								lib.sendMessage(p, "&c&oWar request denied.");
							} else {
								lib.sendMessage(p, "&c&oA war is already taking place.");
								return true;
							}
							return true;
						}

					} else {
						lib.sendMessage(p, lib.clanUnknown(args1));
						return true;
					}
				} else {
					lib.sendMessage(p, lib.noClearance());
					return true;
				}
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

				if (ClansAPI.getData().arenaRedTeamFile().getConfig().getKeys(false).isEmpty() || ClansAPI.getData().arenaBlueTeamFile().getConfig().getKeys(false).isEmpty()) {
					lib.sendMessage(p, "&c&oThe war arena isn't properly setup. Unable to execute command.");
					return true;
				}
				if (associate.getPriority().toInt() >= ClansAPI.getData().getInt("Clans.war.clearance")) {
					if (ClansAPI.getInstance().getClanID(args1) != null) {
						Clan target = ClansAPI.getInstance().getClan(ClansAPI.getInstance().getClanID(args1));
						DefaultClan c = (DefaultClan) associate.getClan();
						if (c.getName().equals(target.getName())) {
							lib.sendMessage(p, "&c&oCannot use own clan name.");
							return true;
						}
						if (c.getCurrentWar() != null) {
							if (!c.getCurrentWar().warActive()) {
								if (!c.getCurrentWar().getTargeted().getWarInvites().isEmpty() && c.getCurrentWar().getTargeted().getWarInvites().stream().filter(cl -> cl.getName().equals(c.getName())).findFirst().orElse(null) != null) {
									c.getCurrentWar().accept(target);
								} else {
									// wait for a response for your invite
									lib.sendMessage(p, "&c&oThe other clan hasn't yet agreed to a full on war.");
									return true;
								}
							} else {
								lib.sendMessage(p, "&c&oAlready in a war.");
								return true;
							}
						} else {
							lib.sendMessage(p, "&c&oYou are not invited to or requesting a war.");
							lib.sendMessage(p, "&c&oTry /&fclan war <clanName> <timeInSeconds>");
						}

					} else {
						lib.sendMessage(p, lib.clanUnknown(args1));
					}
				} else {
					lib.sendMessage(p, lib.noClearance());
				}
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
				DefaultClan clan = (DefaultClan) ClansAPI.getInstance().getClan(p.getUniqueId());
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

					if (associate.getPriority().toInt() >= Clan.ACTION.invitationClearance()) {
						if (blockedUsers.containsKey(target)) {
							List<UUID> users = blockedUsers.get(target);
							if (users.contains(p.getUniqueId())) {
								lib.sendMessage(p, "&c&oThis person has you blocked. Unable to receive invitation.");
								return true;
							}
						}
						ClansAPI.getInstance().getClan(p.getUniqueId()).broadcast(p.getName() + " &e&ohas invited player &6&l" + target.getName());
						lib.sendMessage(target, "&b&o" + p.getName() + " &3invites you to their clan.");
						if (Bukkit.getVersion().contains("1.16") || Bukkit.getVersion().contains("1.17")) {
							TextComponent text = new TextComponent("§3|§7> §3Click a button to respond. ");
							TextComponent click = new TextComponent("§b[§nACCEPT§b]");
							TextComponent clickb = new TextComponent(" §7| ");
							TextComponent click2 = new TextComponent("§4[§nDENY§4]");
							click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new net.md_5.bungee.api.chat.hover.content.Text("§3Click to accept the request from '" + p.getName() + "'."))));
							click2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new net.md_5.bungee.api.chat.hover.content.Text("§3Click to deny the request from '" + p.getName() + "'."))));
							if (associate.getClan().getPassword() != null) {
								click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan join " + ClansAPI.getInstance().getClan(p.getUniqueId()).getName() + " " + associate.getClan().getPassword()));
							} else {
								click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan join " + ClansAPI.getInstance().getClan(p.getUniqueId()).getName()));
							}
							click2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/msg " + p.getName() + " No thank you."));
							text.addExtra(click);
							text.addExtra(clickb);
							text.addExtra(click2);
							target.spigot().sendMessage(text);
						} else {
							TextComponent text = new TextComponent("§3|§7> §3Click a button to respond. ");
							TextComponent click = new TextComponent("§b[§nACCEPT§b]");
							TextComponent clickb = new TextComponent(" §7| ");
							TextComponent click2 = new TextComponent("§4[§nDENY§4]");
							click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§3Click to accept the request from '" + p.getName() + "'.")).create()));
							click2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder("§3Click to deny the request from '" + p.getName() + "'.")).create()));
							if (associate.getClan().getPassword() != null) {
								click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan join " + ClansAPI.getInstance().getClan(p.getUniqueId()).getName() + " " + associate.getClan().getPassword()));
							} else {
								click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan join " + ClansAPI.getInstance().getClan(p.getUniqueId()).getName()));
							}
							click2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/msg " + p.getName() + " No thank you."));
							text.addExtra(click);
							text.addExtra(clickb);
							text.addExtra(click2);
							target.spigot().sendMessage(text);
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

				Clan.ACTION.create(p.getUniqueId(), args1, null);
				return true;
			}
			if (args0.equalsIgnoreCase("mode")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("mode"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("mode")));
					return true;
				}
				if (associate != null) {
					if (associate.getPriority().toInt() >= Clan.ACTION.modeChangeClearance()) {
						if (!(associate.getClan() instanceof DefaultClan))
							return true;
						DefaultClan c = (DefaultClan) ClansAPI.getInstance().getClan(p.getUniqueId());
						switch (args1.toLowerCase()) {
							case "war":
								if (c.isPeaceful()) {
									if (ClansAPI.getData().getEnabled("Clans.mode-change.charge")) {
										double amount = ClansAPI.getData().getMain().getConfig().getDouble("Clans.mode-change.amount");
										double balance = EconomyProvision.getInstance().balance(p).orElse(0.0);
										double needed = amount - balance;
										EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p).ifPresent(b -> {

											if (!b) {
												lib.sendMessage(p, lib.notEnough(needed));
											}

										});
									}
									if (ClansAPI.getData().getEnabled("Clans.mode-change.timer.use")) {
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
									if (ClansAPI.getData().getEnabled("Clans.mode-change.charge")) {
										double amount = ClansAPI.getData().getMain().getConfig().getDouble("Clans.mode-change.amount");
										double balance = EconomyProvision.getInstance().balance(p).orElse(0.0);
										double needed = amount - balance;
										EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p).ifPresent(b -> {

											if (!b) {
												lib.sendMessage(p, lib.notEnough(needed));
											}

										});
									}
									if (ClansAPI.getData().getEnabled("Clans.mode-change.timer.use")) {
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
					associate.changeNickname(args1);
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
							if (!associate.getClan().transferOwnership(target)) {
								sendMessage(p, lib.playerUnknown("clan member"));
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
				Clan.ACTION.joinClan(p.getUniqueId(), args1, "none");
				return true;
			}
			if (args0.equalsIgnoreCase("tag")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("tag"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("tag")));
					return true;
				}
				if (associate != null) {
					Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
					if (associate.getPriority().toInt() >= Clan.ACTION.tagChangeClearance()) {
						if (!isAlphaNumeric(args1)) {
							lib.sendMessage(p, lib.nameInvalid(args1));
							return true;
						}
						if (args1.length() > ClansAPI.getData().getMain().readValue(f -> f.getInt("Formatting.tag-size"))) {
							Clan.ACTION.sendMessage(p, lib.nameTooLong(args1));
							return true;
						}
						if (Clan.ACTION.getAllClanNames().contains(args1)) {
							lib.sendMessage(p, lib.alreadyMade(args1));
							return true;
						}
						for (String s : ClansAPI.getData().getMain().getConfig().getStringList("Clans.name-blacklist")) {
							if (Pattern.compile(Pattern.quote(args1), Pattern.CASE_INSENSITIVE).matcher(s).find()) {
								event.getUtil().sendMessage(p, "&c&oThis name is not allowed!");
								return true;
							}
						}
						ClanTagChangeEvent ev = ClanVentBus.call(new ClanTagChangeEvent(p, clan, clan.getName(), args1));
						if (!ev.isCancelled()) {
							clan.setName(ev.getToName());
						}
						for (String s : clan.getMemberIds()) {
							OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(s));
							if (op.isOnline()) {
								if (ClansAPI.getData().prefixedTagsAllowed()) {
									ClanDisplayName.update(p, ClansAPI.getData().prefixedTag(ClansAPI.getInstance().getClan(op.getUniqueId()).getColor(), ClansAPI.getInstance().getClan(op.getUniqueId()).getName()));
								}
							}
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
					if (associate.getPriority().toInt() >= Clan.ACTION.colorChangeClearance()) {

						if (!args1.matches("(&#[a-zA-Z0-9]{6})+(&[a-zA-Z0-9])+") && !args1.matches("(&#[a-zA-Z0-9]{6})+|(&[a-zA-Z0-9])+")) {
							lib.sendMessage(p, "&c&oInvalid color format.");
							return true;
						}

						for (String s : ClansAPI.getData().getMain().getConfig().getStringList("Clans.color-blacklist")) {

							if (StringUtils.use(args1).containsIgnoreCase(s)) {
								lib.sendMessage(p, "&c&oInvalid color format. Code: '" + s + "' is not allowed.");
								return true;
							}
						}

						clan.setColor(args1);
						for (String s : clan.getMemberIds()) {
							OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(s));
							try {
								if (op.isOnline()) {
									if (ClansAPI.getData().prefixedTagsAllowed()) {
										ClanDisplayName.update(p, ClansAPI.getData().prefixedTag(ClansAPI.getInstance().getClan(op.getUniqueId()).getColor(), ClansAPI.getInstance().getClan(op.getUniqueId()).getName()));
									}
								}
							} catch (NullPointerException e) {
								ClansAPI.getInstance().getPlugin().getLogger().severe("- Failed to updated name tags for user " + op.getName() + ".");
							}
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
			if (args0.equalsIgnoreCase("promote")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("promote"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("promote")));
					return true;
				}
				FileManager main = ClansAPI.getData().getMain();
				String adminRank = main.getConfig().getString("Formatting.Chat.Styles.Full.Admin");
				String ownerRank = main.getConfig().getString("Formatting.Chat.Styles.Full.Owner");
				if (associate != null) {
					if (associate.getPriority().toInt() >= Clan.ACTION.positionClearance()) {
						UUID tid = Clan.ACTION.getUserID(args1);
						if (tid == null) {
							lib.sendMessage(p, lib.playerUnknown(args1));
							return true;
						}
						ClanAssociate member = associate.getClan().getMember(m -> m.getPlayer().getUniqueId().equals(tid));
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
					if (associate.getPriority().toInt() >= Clan.ACTION.positionClearance()) {
						UUID tid = Clan.ACTION.getUserID(args1);
						if (tid == null) {
							lib.sendMessage(p, lib.playerUnknown(args1));
							return true;
						}
						ClanAssociate member = associate.getClan().getMember(m -> m.getPlayer().getUniqueId().equals(tid));
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
							if (associate.getPriority().toInt() >= Clan.ACTION.unclaimAllClearance()) {
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
				Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
				if (associate != null) {
					if (associate.getPriority().toInt() >= Clan.ACTION.passwordClearance()) {
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
					if (associate.getPriority().toInt() >= Clan.ACTION.kickClearance()) {
						UUID tid = Clan.ACTION.getUserID(args1);
						if (tid == null) {
							lib.sendMessage(p, lib.playerUnknown(args1));
							return true;
						}
						OfflinePlayer target = Bukkit.getOfflinePlayer(tid);
						Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
						if (!Arrays.asList(clan.getMemberIds()).contains(target.getUniqueId().toString())) {
							lib.sendMessage(p, lib.playerUnknown(args1));
							return true;
						}
						ClanAssociate member = associate.getClan().getMember(m -> m.getPlayer().getUniqueId().equals(tid));
						if (member == null) return true;
						if (member.getPriority().toInt() > associate.getPriority().toInt()) {
							lib.sendMessage(p, lib.noClearance());
							return true;
						}
						if (tid.equals(p.getUniqueId())) {
							lib.sendMessage(p, lib.nameInvalid(args1));
							return true;
						}
						ClanAssociate tar = ClansAPI.getInstance().getAssociate(target).orElse(null);
						if (tar == null) {
							sendMessage(p, "&4Something went wrong. Contact an administrator.");
							throw new IllegalStateException("The specified target exists within the clan but has no known associate representation");
						}
						tar.kick();
						String format = MessageFormat.format(ClansAPI.getData().getMessage("kick-out"), target.getName());
						String format1 = MessageFormat.format(ClansAPI.getData().getMessage("kick-in"), p.getName());
						clan.broadcast(format);
						if (target.isOnline()) {
							lib.sendMessage(target.getPlayer(), format1);
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
						Clan.ACTION.getClanboard(p, 1);
						return true;
					}
					Clan clan = ClansAPI.getInstance().getClan(ClansAPI.getInstance().getClanID(args1));
					for (String info : clan.getClanInfo()) {
						if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
							sendMessage(p, info.replace(ClansAPI.getInstance().getClan(p.getUniqueId()).getName(), "&6&lUS"));
						} else {
							sendMessage(p, info);
						}
					}
					if (ClansAPI.getData().ID_MODE.containsKey(p) && ClansAPI.getData().ID_MODE.get(p).equals("ENABLED")) {
						lib.sendMessage(p, "&7#&fID &7of clan " + '"' + args1 + '"' + " is: &e&o" + ClansAPI.getInstance().getClanID(args1));
					}
					return true;
				}
				Clan c = ClansAPI.getInstance().getClan(target);
				if (c != null) {
					UI.select(UI.Singular.MEMBER_INFO, target).open(p);
					if (ClansAPI.getData().ID_MODE.containsKey(p) && ClansAPI.getData().ID_MODE.get(p).equals("ENABLED")) {
						lib.sendMessage(p, "&7#&fID &7of player " + '"' + args1 + '"' + " clan " + '"' + c.getName() + '"' + " is: &e&o" + c.getId().toString());
					}
				} else {
					lib.sendMessage(p, args1 + " &c&oisn't in a clan.");
					return true;
				}
				return true;
			}
			if (args0.equalsIgnoreCase("message")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("message"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("message")));
					return true;
				}
				Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
				if (associate != null)
					clan.broadcast(MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Formatting.chat-message-format"), p.getName()) + " " + args1);
				return true;
			}
			if (args0.equalsIgnoreCase("bio")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio")));
					return true;
				}
				if (associate != null) {
					associate.changeBio(args1);
				}
				return true;
			}
			if (args0.equalsIgnoreCase("description") || args0.equalsIgnoreCase("desc")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("description"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("description")));
					return true;
				}
				if (associate != null) {
					if (associate.getPriority().toInt() >= Clan.ACTION.descriptionChangeClearance()) {
						Clan c = ClansAPI.getInstance().getClan(p.getUniqueId());
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

				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("logo")));
					return true;
				}

				if (args1.equalsIgnoreCase("redraw")) {
					lib.sendMessage(p, "&cUsage: &f/c logo &eredraw &r[height] [width]");
					return true;
				}

				if (args1.equalsIgnoreCase("color")) {

					if (associate.getPriority().toInt() < ClansAPI.getData().getInt("Clans.logo-color-clearance")) {
						lib.sendMessage(p, lib.noClearance());
						return true;
					}

					if (!args2.matches("(&#[a-zA-Z0-9]{6})+(&[a-zA-Z0-9])+") && !args2.matches("(&#[a-zA-Z0-9]{6})+|(&[a-zA-Z0-9])+")) {
						lib.sendMessage(p, "&c&oInvalid color format.");
						return true;
					}

					Insignia i = Insignia.get("Template:" + p.getUniqueId().toString());

					if (i != null) {

						Message msg = Message.form(p);

						i.setSelection(args2);

						msg.send("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
						for (BaseComponent[] components : i.get()) {
							p.spigot().sendMessage(components);
						}
						msg.send("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

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
				Clan.ACTION.create(p.getUniqueId(), args1, args2);
				return true;
			}
			if (args0.equalsIgnoreCase("join")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("join"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("join")));
					return true;
				}
				Clan.ACTION.joinClan(p.getUniqueId(), args1, args2);
				return true;
			}
			if (args0.equalsIgnoreCase("message")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("message"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("message")));
					return true;
				}
				if (associate != null) {
					Clan clan = associate.getClan();
					clan.broadcast(MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Formatting.chat-message-format"), p.getName()) + " " + args1 + " " + args2);
				}
				return true;
			}
			if (args0.equalsIgnoreCase("war")) {
				if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("war"))) {
					lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("war")));
					return true;
				}
				if (ClansAPI.getData().arenaRedTeamFile().getConfig().getKeys(false).isEmpty() || ClansAPI.getData().arenaBlueTeamFile().getConfig().getKeys(false).isEmpty()
						|| ClansAPI.getData().arenaBlueTeamFile().getConfig().getString("re-spawn") == null || ClansAPI.getData().arenaRedTeamFile().getConfig().getString("re-spawn") == null) {
					lib.sendMessage(p, "&c&oThe war arena isn't properly setup. Unable to execute command.");
					return true;
				}
				if (associate != null) {
					if (associate.getPriority().toInt() >= ClansAPI.getData().getInt("Clans.war.clearance")) {
						if (ClansAPI.getInstance().getClanID(args1) != null) {
							Clan target = ClansAPI.getInstance().getClan(ClansAPI.getInstance().getClanID(args1));
							DefaultClan c = (DefaultClan) associate.getClan();
							if (c.getName().equals(target.getName())) {
								lib.sendMessage(p, "&c&oYou cannot use your own clan name.");
								return true;
							}
							if (c.getCurrentWar() == null) {
								if (c.isPeaceful()) {
									lib.sendMessage(p, Clan.ACTION.peacefulDeny());
									return true;
								}
								if (target.isPeaceful()) {
									lib.sendMessage(p, Clan.ACTION.peacefulDenyOther(target.getName()));
									return true;
								}
								int online = 0;
								for (String id : target.getMemberIds()) {
									if (Bukkit.getOfflinePlayer(UUID.fromString(id)).isOnline()) {
										online++;
									}
								}
								if (online == 0) {
									lib.sendMessage(p, "&c&oTarget clan has no member's online.");
									return true;
								}
								if (online < ClansAPI.getData().getInt("Clans.war.online-needed")) {
									lib.sendMessage(p, "&c&oTarget clan doesn't have enough clan members online.");
									return true;
								}
								online = 0;
								for (String id : c.getMemberIds()) {
									if (Bukkit.getOfflinePlayer(UUID.fromString(id)).isOnline()) {
										online++;
									}
								}
								if (online < ClansAPI.getData().getInt("Clans.war.online-needed")) {
									lib.sendMessage(p, "&c&oYour clan doesn't have enough clan members online.");
									return true;
								}
								try {
									if (Integer.parseInt(args2) < ClansAPI.getData().getInt("Clans.war.battle-length-seconds") || Integer.parseInt(args2) > ClansAPI.getData().getInt("Clans.war.battle-length-max")) {
										lib.sendMessage(p, "&c&oInvalid time request.");
										return true;
									}
									c.setCurrentWar(new ClanWar(c));
									c.getCurrentWar().request(target, Integer.parseInt(args2));
								} catch (NumberFormatException e) {
									lib.sendMessage(p, "&c&oInvalid time request.");
									return true;
								}
								long minute = TimeUnit.SECONDS.toMinutes(Integer.parseInt(args2)) - (TimeUnit.SECONDS.toHours(Integer.parseInt(args2)) * 60);
								c.broadcast("&4&o" + target.getName() + " &8&orequest for a " + minute + " minute pvp match has been sent.");
								target.broadcast("&4&o" + c.getName() + " &8&ois requesting a " + minute + " minute match with us.");
								for (String id : target.getMemberIds()) {
									OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(id));
									if (player.isOnline()) {
										TextComponent component = TextLib.getInstance().textRunnable("&7Click to respond. ", "&a[ACCEPT]", " ", "&c[DENY]", "&aClick to accept.", "&cClick to deny.", "c war " + c.getName(), "c peace " + c.getName());
										lib.sendComponent(player.getPlayer(), component);
									}
								}
							} else {
								if (!c.getCurrentWar().warActive()) {
									if (!c.getCurrentWar().getTargeted().getWarInvites().isEmpty() && c.getCurrentWar().getTargeted().getWarInvites().stream().filter(cl -> cl.getName().equals(target.getName())).findFirst().orElse(null) != null) {
										c.getCurrentWar().accept(target);
									} else {
										// wait for a resposne for your invite
										lib.sendMessage(p, ClansAPI.getData().getMessage("clan-not-ready"));
										return true;
									}
								} else {
									lib.sendMessage(p, ClansAPI.getData().getMessage("already-at-war"));
									return true;
								}
								return true;
							}

						} else {
							lib.sendMessage(p, lib.clanUnknown(args1));
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
								if (TextLib.getInstance() != null) {
									p.spigot().sendMessage(TextLib.getInstance().textHoverable(
											Messages.BANK_HELP_PREFIX + " setperm " + args2.toLowerCase() + " ",
											"&7<&clevel&7>",
											"Valid levels [0-3]"
									));
								} else {
									p.spigot().sendMessage(new OldComponent().textHoverable(
											Messages.BANK_HELP_PREFIX + " setperm " + args2.toLowerCase() + " ",
											"&7<&clevel&7>",
											"Valid levels [0-3]"
									));
								}
								break;
							default:
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
					associate.changeBio(args1 + " " + args2);
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
					if (associate.getPriority().toInt() >= Clan.ACTION.descriptionChangeClearance()) {
						Clan c = ClansAPI.getInstance().getClan(p.getUniqueId());
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
						if (!Clan.ACTION.getAllClanNames().contains(args2)) {
							lib.sendMessage(p, lib.clanUnknown(args2));
							return true;
						}
						Clan t = ClansAPI.getInstance().getClan(args2);
						if (args2.equals(associate.getClan().getName())) {
							lib.sendMessage(p, lib.allianceDenial());
							return true;
						}
						if (c.getEnemyList().contains(t.getId().toString())) {
							lib.sendMessage(p, lib.alreadyEnemies(args2));
							return true;
						}
						List<String> online = new ArrayList<>();
						for (String mem : t.getMemberIds()) {
							OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(mem));
							if (op.isOnline()) {
								online.add(op.getName());
							}
						}
						if (ClansAPI.getData().getEnabled("Clans.relations.enemy.cancel-if-empty")) {
							if (online.isEmpty()) {
								lib.sendMessage(p, "&c&oThis clan has no members online, unable to mark as enemy.");
								return true;
							}
						}
						if (c.isNeutral(t.getId().toString())) {
							if (c.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDeny());
								return true;
							}
							if (t.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
								return true;
							}
							c.addEnemy(t.getId());
							return true;
						}
						if (c.getAllyList().contains(t.getId().toString())) {
							if (c.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDeny());
								return true;
							}
							if (t.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
								return true;
							}
							c.addEnemy(t.getId());
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
						if (!Clan.ACTION.getAllClanNames().contains(args2)) {
							lib.sendMessage(p, lib.clanUnknown(args2));
							return true;
						}
						Clan t = ClansAPI.getInstance().getClan(args2);
						if (args2.equals(c.getName())) {
							lib.sendMessage(p, lib.allianceDenial());
							return true;
						}
						if (t.getEnemyList().contains(c.getId().toString())) {
							lib.sendMessage(p, lib.noRemoval(args2));
							return true;
						}
						if (!c.getEnemyList().contains(t.getId().toString())) {
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
						c.removeEnemy(t.getId());
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
						if (!Clan.ACTION.getAllClanNames().contains(args2)) {
							lib.sendMessage(p, lib.clanUnknown(args2));
							return true;
						}
						Clan t = ClansAPI.getInstance().getClan(args2);
						if (args2.equals(c.getName())) {
							lib.sendMessage(p, lib.allianceDenial());
							return true;
						}
						if (c.getAllyList().contains(t.getId().toString())) {
							lib.sendMessage(p, lib.alreadyAllies(args2));
							return true;
						}
						if (t.getEnemyList().contains(c.getId().toString())) {
							lib.sendMessage(p, lib.alreadyEnemies(args2));
							return true;
						}
						if (c.isNeutral(t.getId().toString())) {
							if (c.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDeny());
								return true;
							}
							if (t.isPeaceful()) {
								lib.sendMessage(p, lib.peacefulDenyOther(t.getName()));
								return true;
							}
							c.sendAllyRequest(t.getId());
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
						c.addAlly(t.getId());
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
						if (!Clan.ACTION.getAllClanNames().contains(args2)) {
							lib.sendMessage(p, lib.clanUnknown(args2));
							return true;
						}
						Clan t = ClansAPI.getInstance().getClan(args2);
						if (args2.equals(c.getName())) {
							lib.sendMessage(p, lib.allianceDenial());
							return true;
						}

						List<String> online = new ArrayList<>();
						for (String mem : t.getMemberIds()) {
							OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(mem));
							if (op.isOnline()) {
								online.add(op.getName());
							}
						}
						if (ClansAPI.getData().getEnabled("Clans.relations.ally.cancel-if-empty")) {
							if (online.isEmpty()) {
								lib.sendMessage(p, "&c&oThis clan has no members online, unable to mark as enemy.");
								return true;
							}
						}

						if (c.isNeutral(t.getId().toString())) {
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
						c.removeAlly(t.getId());
						t.removeAlly(c.getId());
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

					if (associate.getPriority().toInt() < ClansAPI.getData().getInt("Clans.logo-edit-clearance")) {
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
			Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
			clan.broadcast(MessageFormat.format(ClansAPI.getData().getMain().getConfig().getString("Formatting.chat-message-format"), p.getName()) + " " + rsn.substring(0, stop));
			return true;
		}
		if (args0.equalsIgnoreCase("bio")) {
			if (!p.hasPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio"))) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("bio")));
				return true;
			}
			if (associate != null) {
				associate.changeBio(rsn.substring(0, stop));
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
				if (associate.getPriority().toInt() >= Clan.ACTION.descriptionChangeClearance()) {
					Clan c = ClansAPI.getInstance().getClan(p.getUniqueId());
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
