package com.github.sanctum.link.cycles;

import com.github.sanctum.bounty.Bounty;
import com.github.sanctum.bounty.BountyList;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.events.command.CommandHelpInsertEvent;
import com.github.sanctum.clans.util.events.command.CommandInsertEvent;
import com.github.sanctum.clans.util.events.command.TabInsertEvent;
import com.github.sanctum.clans.util.events.damage.PlayerKillPlayerEvent;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.link.ClanVentBus;
import com.github.sanctum.link.CycleList;
import com.github.sanctum.link.EventCycle;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class BountyCycle extends EventCycle {

	@Override
	public boolean persist() {
		return !EconomyProvision.getInstance().getImplementation().equals("Default | No Economy Bridge") && ClansAPI.getData().getEnabled("Addon." + getName() + ".enabled");
	}

	@Override
	public HUID getId() {
		return super.getId();
	}

	@Override
	public String getName() {
		return "Bounty";
	}

	@Override
	public String getDescription() {
		return "Just like GTA, Mark bounties on players! Earn money for you or your clan.";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"Hempfest"};
	}

	@Override
	public void onLoad() {
		register(new Listener() {
		});
	}

	@Override
	public void onEnable() {

		ClanVentBus.subscribe(PlayerKillPlayerEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			EventCycle cycle = CycleList.getAddon("Bounty");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}

			Player p = e.getKiller();
			if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
				Clan c = ClansAPI.getInstance().getClan(p.getUniqueId());
				Bounty b = BountyList.get(e.getVictim().getUniqueId()).orElse(null);
				if (b != null) {
					if (ClansAPI.getData().getEnabled("Addon.Bounty.settings.take-from-killed")) {
						// take from wallet
						if (ClansAPI.getInstance().isInClan(e.getVictim().getUniqueId())) {
							Clan t = ClansAPI.getInstance().getClan(e.getVictim().getUniqueId());
							// take from victims clan if they are in one
							if (ClansAPI.getData().getEnabled("Addon.Bounty.settings.announce-defeat")) {
								String format = ClansAPI.getData().getMain().getConfig().getString("Addon.Bounty.settings.defeat-message")
										.replace("{KILLER}", p.getName())
										.replace("{KILLER_CLAN}", c.getName())
										.replace("{BOUNTY}", b.getAmount().toString())
										.replace("{PLAYER}", e.getVictim().getName());
								Bukkit.broadcastMessage(StringUtils.use(format).translate());
							}
							if (t.has(b.getAmount())) {
								BigDecimal newBal = t.getBalance().subtract(b.getAmount());
								t.setBalance(newBal);
								if (ClansAPI.getData().getMain().getConfig().getString("Addon.Bounty.settings.deposit-type").equals("PLAYER")) {
									boolean deposit = EconomyProvision.getInstance().deposit(b.getAmount(), p, p.getWorld().getName()).orElse(false);
									if (!deposit) {
										DefaultClan.action.sendMessage(p, "&c&oSomething went wrong with paying you.. This is awkward.");
									}
								} else if (ClansAPI.getData().getMain().getConfig().getString("Addon.Bounty.settings.deposit-type").equals("CLAN")) {
									newBal = c.getBalance().add(b.getAmount());
									c.setBalance(newBal);
									c.broadcast("&2" + e.getVictim().getName() + "'s &abounty was retrieved and they were killed in the process. We gained &6$" + b.getAmount().doubleValue());
									// broadcast bounty retrieval?
								}
								t.broadcast("&c" + e.getVictim().getName() + "'s &6bounty was retrieved and they were killed in the process. We lost &e$" + b.getAmount().doubleValue());
							} else {
								boolean has = EconomyProvision.getInstance().has(b.getAmount(), e.getVictim(), e.getVictim().getWorld().getName()).orElse(false);
								if (has) {
									boolean withdraw = EconomyProvision.getInstance().withdraw(b.getAmount(), e.getVictim(), e.getVictim().getWorld().getName()).orElse(false);
									if (!withdraw) {
										DefaultClan.action.sendMessage(p, "&c&oSomething went wrong.. unable to withdraw money.");
									}
								}
								if (ClansAPI.getData().getMain().getConfig().getString("Addon.Bounty.settings.deposit-type").equals("PLAYER")) {
									boolean deposit = EconomyProvision.getInstance().deposit(b.getAmount(), p, p.getWorld().getName()).orElse(false);
									if (!deposit) {
										DefaultClan.action.sendMessage(p, "&c&oSomething went wrong with paying you.. This is awkward.");
									}
								} else if (ClansAPI.getData().getMain().getConfig().getString("Addon.Bounty.settings.deposit-type").equals("CLAN")) {
									BigDecimal newBal = c.getBalance().add(b.getAmount());
									c.setBalance(newBal);
									c.broadcast("&2" + e.getVictim().getName() + "'s &abounty was retrieved and they were killed in the process. We gained &6$" + b.getAmount().doubleValue());
									// broadcast bounty retrieval?
								}
							}
						} else {
							if (ClansAPI.getData().getEnabled("Addon.Bounty.settings.announce-defeat")) {
								String format = ClansAPI.getData().getMain().getConfig().getString("Addon.Bounty.settings.defeat-message")
										.replace("{KILLER}", p.getName())
										.replace("{KILLER_CLAN}", c.getName())
										.replace("{BOUNTY}", b.getAmount().toString())
										.replace("{PLAYER}", e.getVictim().getName());
								Bukkit.broadcastMessage(StringUtils.use(format).translate());
							}
							boolean has = EconomyProvision.getInstance().has(b.getAmount(), e.getVictim(), e.getVictim().getWorld().getName()).orElse(false);
							if (has) {
								boolean withdraw = EconomyProvision.getInstance().withdraw(b.getAmount(), e.getVictim(), e.getVictim().getWorld().getName()).orElse(false);
								if (!withdraw) {
									DefaultClan.action.sendMessage(p, "&c&oSomething went wrong.. unable to withdraw money.");
								}
							}
							if (ClansAPI.getData().getMain().getConfig().getString("Addon.Bounty.settings.deposit-type").equals("PLAYER")) {
								boolean deposit = EconomyProvision.getInstance().deposit(b.getAmount(), p, p.getWorld().getName()).orElse(false);
								if (!deposit) {
									DefaultClan.action.sendMessage(p, "&c&oSomething went wrong with paying you.. This is awkward.");
								}
							} else if (ClansAPI.getData().getMain().getConfig().getString("Addon.Bounty.settings.deposit-type").equals("CLAN")) {
								BigDecimal newBal = c.getBalance().add(b.getAmount());
								c.setBalance(newBal);
								c.broadcast("&2" + e.getVictim().getName() + "'s &abounty was retrieved and they were killed in the process. We gained &6$" + b.getAmount().doubleValue());
								// broadcast bounty retrieval?
							}
						}

					} else {
						// take no money from player
						// broadcast bounty retrieval?
						if (ClansAPI.getData().getMain().getConfig().getString("Addon.Bounty.settings.deposit-type").equals("PLAYER")) {
							boolean deposit = EconomyProvision.getInstance().deposit(b.getAmount(), p, p.getWorld().getName()).orElse(false);
							if (!deposit) {
								DefaultClan.action.sendMessage(p, "&c&oSomething went wrong with paying you.. This is awkward.");
							}
						} else if (ClansAPI.getData().getMain().getConfig().getString("Addon.Bounty.settings.deposit-type").equals("CLAN")) {
							BigDecimal newBal = c.getBalance().add(b.getAmount());
							c.setBalance(newBal);
						}
						if (ClansAPI.getData().getEnabled("Addon.Bounty.settings.announce-defeat")) {
							String format = ClansAPI.getData().getMain().getConfig().getString("Addon.Bounty.settings.defeat-message")
									.replace("{KILLER}", p.getName())
									.replace("{KILLER_CLAN}", c.getName())
									.replace("{BOUNTY}", b.getAmount().toString())
									.replace("{PLAYER}", e.getVictim().getName());
							Bukkit.broadcastMessage(StringUtils.use(format).translate());
						}
					}
					b.complete();
				}
			}

		});

		ClanVentBus.subscribe(TabInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			if (e.getCommandArgs().length == 1) {
				if (!e.getArgs(1).contains("bounty")) {
					e.add(1, "bounty");
				}
			}
			if (e.getCommandArgs().length == 2) {
				if (e.getCommandArgs()[0].equalsIgnoreCase("bounty")) {
					for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
						if (!e.getArgs(2).contains(op.getName())) {
							e.add(2, op.getName());
						}
					}
				}
			}

		});

		ClanVentBus.subscribe(CommandHelpInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> e.insert("&7|&e) &6/clan &fbounty <playerName> <amount>"));

		ClanVentBus.subscribe(CommandInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			Player p = e.getSender();
			if (e.getArgs().length == 3) {
				if (e.getArgs()[0].equalsIgnoreCase("bounty")) {
					if (ClansAPI.getInstance().isInClan(p.getUniqueId())) {
						Clan c = ClansAPI.getInstance().getClan(p.getUniqueId());
						UUID id = DefaultClan.action.getUserID(e.getArgs()[1]);
						if (id != null) {
							if (Arrays.asList(c.getMembersList()).contains(id.toString())) {
								e.getUtil().sendMessage(p, "&c&oYou cannot put bounties on clan members.");
								e.setReturn(true);
								return;
							}
							if (BountyList.get(c, id) == null) {
								try {
									Double.parseDouble(e.getArgs()[2]);
								} catch (NumberFormatException ex) {
									DefaultClan.action.sendMessage(p, "&cInvalid amount chosen must be ##.## format.");
								}
								double amount = Double.parseDouble(e.getArgs()[2]);
								boolean has = EconomyProvision.getInstance().has(BigDecimal.valueOf(amount), p, p.getWorld().getName()).orElse(false);
								if (has) {
									EconomyProvision.getInstance().withdraw(BigDecimal.valueOf(amount), p, p.getWorld().getName());
									String format = ClansAPI.getData().getMain().getConfig().getString("Addon.Bounty.settings.called-message")
											.replace("{PLAYER}", p.getName())
											.replace("{CLAN}", c.getName())
											.replace("{TARGET}", e.getArgs()[1])
											.replace("{BOUNTY}", e.getArgs()[2]);
									Bukkit.broadcastMessage(StringUtils.use(format).translate());
									FileManager clanFile = ClansAPI.getData().getClanFile(c);
									clanFile.getConfig().set("bounties." + DefaultClan.action.getUserID(e.getArgs()[1]).toString(), amount);
									clanFile.refreshConfig();
								} else {
									DefaultClan.action.sendMessage(p, "&c&oYou don't have enough money for a bounty this big!");
								}
							} else {
								DefaultClan.action.sendMessage(p, "&c&oYour clan has already called a bounty on this target.");
							}
						} else {
							DefaultClan.action.sendMessage(p, DefaultClan.action.playerUnknown(e.getArgs()[1]));
						}
					} else {
						DefaultClan.action.sendMessage(p, DefaultClan.action.notInClan());
					}
					e.setReturn(true);
					return;
				}
			}
			if (e.getArgs().length > 0) {
				if (e.getArgs()[0].equalsIgnoreCase("bounty")) {
					e.getUtil().sendMessage(p, "&cUsage: &6/clan &fbounty <playerName> <amount>");
					e.setReturn(true);
				}
			}

		});

	}

	@Override
	public void onDisable() {

	}
}
