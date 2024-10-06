package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.ClanManager;
import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.BanksAPI;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.clans.model.ClanBankPermissions;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.impl.DefaultClan;
import com.github.sanctum.clans.event.bank.messaging.Messages;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.formatting.component.OldComponent;
import com.github.sanctum.labyrinth.formatting.string.ColoredString;
import com.github.sanctum.labyrinth.gui.unity.simple.MemoryDocket;
import com.github.sanctum.labyrinth.interfacing.UnknownGeneric;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.panther.util.HUID;
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
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.bank.text"));
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
			if (ClanBankPermissions.BANKS_USE.not(p)) {
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
			if (ClanBankPermissions.BANKS_BALANCE.not(p)) {
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
			if (Clearance.VIEW_BANK_LOG.test(associate)) {
				p.spigot().sendMessage(TextLib.getInstance().textSuggestable(
						Messages.BANK_HELP_PREFIX + " ",
						"&7viewlog", "View recent transaction history",
						"clan bank viewlog"
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
					if (ClanBankPermissions.BANKS_BALANCE.not(p) || !Clearance.BANK_BALANCE.test(associate)) {
						sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
						return true;
					}
					sendMessage(p, Messages.BANKS_CURRENT_BALANCE.toString()
							.replace("{0}", BanksAPI.getInstance().getBank(clan).getBalance().toString()));
					return true;
				case "gui" :
					MemoryDocket<UnknownGeneric> docket = new MemoryDocket<>(ClansAPI.getDataInstance().getMessages().getRoot().getNode("menu.home.bank"));
					docket.setUniqueDataConverter(associate, Clan.Associate.memoryDocketReplacer());
					docket.setNamePlaceholder(":member_name:");
					docket.load().toMenu().open(p);
					return true;
				case "deposit":
					if (ClanBankPermissions.BANKS_DEPOSIT.not(p) || !Clearance.BANK_DEPOSIT.test(associate)) {
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
					if (ClanBankPermissions.BANKS_WITHDRAW.not(p) || !Clearance.BANK_WITHDRAW.test(associate)) {
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
					if (ClanBankPermissions.BANKS_WITHDRAW.not(p)) {
						sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
						return true;
					}
					sendMessage(p, "&cInvalid usage.");
					sendMessage(p, Messages.BANK_HELP_PREFIX + " &asend &f<clanName> &6<amount>");
					return true;
				case "viewlog":
					if (!Clearance.VIEW_BANK_LOG.test(associate)) {
						sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
						return true;
					}
					p.sendMessage(BanksAPI.getInstance().getBank(clan).getLog().getTransactions().stream().map(Object::toString).toArray(String[]::new));
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
				case "send":
					if (ClanBankPermissions.BANKS_WITHDRAW.not(p)) {
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
						final Clan.Bank theBank = BanksAPI.getInstance().getBank(associate.getClan());
						switch (arg1) {
							case "deposit":
								if (ClanBankPermissions.BANKS_DEPOSIT.not(p)) {
									sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
									return true;
								}
								if (theBank.deposit(amount, associate)) {
									sendMessage(p, Messages.DEPOSIT_MSG_PLAYER.toString()
											.replace("{0}", amount.toString()));
								} else {
									sendMessage(p, Messages.DEPOSIT_ERR_PLAYER.toString()
											.replace("{0}", amount.toString()));
								}
								break;
							case "withdraw":
								if (ClanBankPermissions.BANKS_WITHDRAW.not(p)) {
									sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
									return true;
								}
								if (theBank.withdraw(amount, associate)) {
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
				final Clan.Bank theBank = BanksAPI.getInstance().getBank(clan);
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
				final Clan theOtherClan = manager.getClan(id);
				if (ClanBankPermissions.BANKS_WITHDRAW.not(p)) {
					sendMessage(p, Messages.PERM_NOT_PLAYER_COMMAND.toString());
					return true;
				}
				if (!theBank.has(amount)) {
					sendMessage(p, Messages.WITHDRAW_ERR_PLAYER.toString()
							.replace("{0}", amount.toString()));
					return true;
				}

				if (theBank.withdraw(amount, () -> "Sent to " + theOtherClan.getName())) {
					if (BanksAPI.getInstance().getBank(theOtherClan).deposit(amount, () -> "Received from " + clan.getName())) {
						associate.getClan().broadcast("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
						associate.getClan().broadcast("&aOur clan sent money in the amount of &6" + amount + " &ato clan " + theOtherClan.getPalette().toString(theOtherClan.getName()));
						associate.getClan().broadcast("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
						theOtherClan.broadcast("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
						theOtherClan.broadcast("&6We have received money in the amount of &6" + amount + " &afrom clan " + clan.getPalette().toString(clan.getName()));
						theOtherClan.broadcast("&f&l&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
					}
				} else {
					sendMessage(p, Messages.DEPOSIT_ERR_PLAYER.toString()
							.replace("{0}", amount.toString()));
				}
				return true;
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
					if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("bank")).deploy()) {
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
							if (Clearance.VIEW_BANK_LOG.test(associate.get())) {
								result.add("viewlog");
							}
						});
						return result;
					}
					return result;
				}).then(TabCompletionIndex.THREE, "deposit", TabCompletionIndex.TWO, () -> {
					List<String> result = new ArrayList<>();
					if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("bank")).deploy()) {
						return result;
					}
					if (EconomyProvision.getInstance().isValid()) {
						result.add("10");
						return result;
					}
					return result;
				}).then(TabCompletionIndex.THREE, "send", TabCompletionIndex.TWO, () -> {
					List<String> result = new ArrayList<>();
					if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("bank")).deploy()) {
						return result;
					}
					if (EconomyProvision.getInstance().isValid()) {
						ClansAPI.getInstance().getClanManager().getClans().forEach(c -> result.add(c.getName()));
						return result;
					}
					return result;
				}).then(TabCompletionIndex.THREE, "withdraw", TabCompletionIndex.TWO, () -> {
					List<String> result = new ArrayList<>();
					if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("bank")).deploy()) {
						return result;
					}
					if (EconomyProvision.getInstance().isValid()) {
						result.add("10");
						return result;
					}
					return result;
				}).then(TabCompletionIndex.FOUR, "send", TabCompletionIndex.TWO, () -> {
					List<String> result = new ArrayList<>();
					if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("bank")).deploy()) {
						return result;
					}
					if (EconomyProvision.getInstance().isValid()) {
						result.add("10");
						return result;
					}
					return result;
				}).get();
	}
}
