package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.ClanManager;
import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.bank.BankAction;
import com.github.sanctum.clans.construct.bank.BankLog;
import com.github.sanctum.clans.construct.bank.BankPermissions;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.clans.event.bank.messaging.Messages;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.formatting.component.OldComponent;
import com.github.sanctum.labyrinth.formatting.string.ColoredString;
import com.github.sanctum.labyrinth.gui.unity.simple.MemoryDocket;
import com.github.sanctum.labyrinth.interfacing.UnknownGeneric;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.TextLib;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

public class CommandBank extends ClanSubCommand implements Message.Factory {
	public CommandBank() {
		super("bank");
		setAliases(Collections.singletonList("b"));
		setInvisible(!ClansAPI.getDataInstance().isTrue("Clans.banks.enabled"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (args.length == 0) {
			if (!EconomyProvision.getInstance().isValid()) {
				sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
				return true;
			}
			if (BankPermissions.BANKS_USE.not(p)) {
				sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
				return true;
			} else {
				sendMessage(p, Messages.BANKS_HEADER.toString());
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
			if (associate.getPriority().toLevel() == 3) {
				p.spigot().sendMessage(TextLib.getInstance().textSuggestable(
						Messages.BANK_HELP_PREFIX + " ",
						"&7setperm", "Set access to functions",
						"clan bank setperm "
				));
			}
			return true;
		}

		if (args.length == 1) {
			if (!EconomyProvision.getInstance().isValid()) {
				lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
				return true;
			}

			if (associate == null) {
				lib.sendMessage(p, lib.notInClan());
				return true;
			}

			if (!(associate.getClan() instanceof DefaultClan))
				return true;
			DefaultClan clan = (DefaultClan) associate.getClan();
			switch (args[0].toLowerCase()) {
				case "balance":
					if (BankPermissions.BANKS_BALANCE.not(p) || !BankAction.BALANCE.testForPlayer(clan, p)) {
						sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
						return true;
					}
					sendMessage(p, Messages.BANKS_CURRENT_BALANCE.toString()
							.replace("{0}", clan.getBalance().toString()));
					return true;
				case "gui" :
					if (ClansAPI.getDataInstance().getMessages().read(c -> c.getNode("deep-edit").toPrimitive().getBoolean())) {
						MemoryDocket<UnknownGeneric> docket = new MemoryDocket<>(ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.home.bank"));
						docket.setUniqueDataConverter(associate, Clan.Associate.memoryDocketReplacer());
						docket.setNamePlaceholder(":member_name:");
						docket.load();
						docket.toMenu().open(p);
					}
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
				case "send":
					if (BankPermissions.BANKS_WITHDRAW.not(p)) {
						sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
						return true;
					}
					sendMessage(p, "&cInvalid usage.");
					sendMessage(p, Messages.BANK_HELP_PREFIX + " &asend &f<clanName> &6<amount>");
					return true;
				case "setperm":
					if (associate.getPriority().toLevel() != 3) {
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
					if (associate.getPriority().toLevel() != 3) {
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

		if (args.length == 2) {
			if (associate == null) {
				sendMessage(p, "&c" + Messages.PLAYER_NO_CLAN);
				return true;
			}
			if (!EconomyProvision.getInstance().isValid()) {
				lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
				return true;
			}
			final String arg1 = args[0].toLowerCase();
			switch (arg1) {
				case "Send":
					if (BankPermissions.BANKS_WITHDRAW.not(p)) {
						sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
						return true;
					}
					sendMessage(p, "&cInvalid usage.");
					sendMessage(p, Messages.BANK_HELP_PREFIX + " &asend &f<clanName> &6<amount>");
					return true;
				case "deposit":
				case "withdraw":
					try {
						final BigDecimal amount = new BigDecimal(args[1]);
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
					switch (args[1].toLowerCase()) {
						case "balance":
						case "deposit":
						case "withdraw":
						case "viewlog":
							message().append(text(Messages.BANK_HELP_PREFIX + " setperm " + args[1].toLowerCase() + " "))
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

		if (args.length == 3) {
			if (!EconomyProvision.getInstance().isValid()) {
				lib.sendMessage(p, "&c&oNo economy interface found. Bank feature disabled.");
				return true;
			}
			if (associate == null) {
				sendMessage(p, lib.notInClan());
				return true;
			}
			final Clan clan = associate.getClan();
			if (args[0].equalsIgnoreCase("send")) {
				final Clan theBank = associate.getClan();
				final BigDecimal amount = new BigDecimal(args[2]);
				if (amount.signum() != 1) {
					sendMessage(p, Messages.BANK_INVALID_AMOUNT.toString());
					return true;
				}
				ClanManager manager = ClansAPI.getInstance().getClanManager();
				HUID id = manager.getClanID(args[1]);
				if (id == null) {
					sendMessage(p, lib.clanUnknown(args[1]));
					return true;
				}
				final Clan theOtherBank = manager.getClan(id);
				if (BankPermissions.BANKS_WITHDRAW.not(p)) {
					sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
					return true;
				}
				if (theBank.getBalanceDouble() - amount.doubleValue() <= 0) {
					sendMessage(p, Messages.WITHDRAW_ERR_PLAYER.toString()
							.replace("{0}", amount.toString()));
					associate.getClan().broadcast("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					associate.getClan().broadcast("&aOur clan sent money in the amount of &6" + amount + " &ato clan " + theOtherBank.getPalette().toString(theOtherBank.getName()));
					associate.getClan().broadcast("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					theOtherBank.broadcast("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					theOtherBank.broadcast("&6We have received money in the amount of &6" + amount + " &afrom clan " + theBank.getPalette().toString(theBank.getName()));
					theOtherBank.broadcast("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					return true;
				}
				if (theBank.setBalance(theBank.getBalance().subtract(amount)) && theOtherBank.setBalance(theOtherBank.getBalance().add(amount))) {
					sendMessage(p, Messages.WITHDRAW_MSG_PLAYER.toString()
							.replace("{0}", amount.toString()));
				} else {
					sendMessage(p, Messages.DEPOSIT_ERR_PLAYER.toString()
							.replace("{0}", amount.toString()));
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("setperm")) {
				int level;
				try {
					level = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					level = -1;
				}
				if (level < 0 || level > 3) {
					sendMessage(p, "&7Invalid level! Valid levels [0-3]");
					return true;
				}
				switch (args[1].toLowerCase()) {
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
			return true;
		}

		return true;
	}

	@Override
	public List<String> tab(Player p, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, () -> getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, () -> {
					List<String> result = new ArrayList<>();
					Optional<Clan.Associate> associate = ClansAPI.getInstance().getAssociate(p);
					if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("bank")).deploy()) {
						return result;
					}

					if (!associate.isPresent()) {
						return result;
					}


					if (EconomyProvision.getInstance().isValid()) {
						result.add("balance");
						result.add("deposit");
						result.add("send");
						result.add("withdraw");
						Optional.ofNullable(ClansAPI.getInstance().getClanManager().getClan(p)).ifPresent(clan -> {
							if (BankAction.VIEW_LOG.testForPlayer(clan, p)) {
								result.add("viewlog");
							}
							if (associate.get().getPriority().toLevel() == 3) {
								result.add("setperm");
								result.add("viewperms");
							}
						});
						return result;
					}
					return result;
				}).then(TabCompletionIndex.THREE, "setperm", TabCompletionIndex.TWO, () -> {
					List<String> result = new ArrayList<>();
					if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("bank")).deploy()) {
						return result;
					}
					if (EconomyProvision.getInstance().isValid()) {
						ClansAPI.getInstance().getAssociate(p).ifPresent(a -> {
							if (a.getPriority().toLevel() == 3) {
								result.add("balance");
								result.add("deposit");
								result.add("withdraw");
								result.add("viewlog");
							}
						});
						return result;
					}
					return result;
				}).then(TabCompletionIndex.THREE, "deposit", TabCompletionIndex.TWO, () -> {
					List<String> result = new ArrayList<>();
					if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("bank")).deploy()) {
						return result;
					}
					if (EconomyProvision.getInstance().isValid()) {
						result.add("10");
						return result;
					}
					return result;
				}).then(TabCompletionIndex.THREE, "send", TabCompletionIndex.TWO, () -> {
					List<String> result = new ArrayList<>();
					if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("bank")).deploy()) {
						return result;
					}
					if (EconomyProvision.getInstance().isValid()) {
						ClansAPI.getInstance().getClanManager().getClans().forEach(c -> result.add(c.getName()));
						return result;
					}
					return result;
				}).then(TabCompletionIndex.THREE, "withdraw", TabCompletionIndex.TWO, () -> {
					List<String> result = new ArrayList<>();
					if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("bank")).deploy()) {
						return result;
					}
					if (EconomyProvision.getInstance().isValid()) {
						result.add("10");
						return result;
					}
					return result;
				}).then(TabCompletionIndex.FOUR, "setperm", TabCompletionIndex.TWO, () -> {
					List<String> result = new ArrayList<>();
					if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("bank")).deploy()) {
						return result;
					}
					if (EconomyProvision.getInstance().isValid()) {
						result.add("0");
						result.add("1");
						result.add("2");
						result.add("3");
						return result;
					}
					return result;
				}).get();
	}
}
