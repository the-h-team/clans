package com.github.sanctum.clans.model.addon;

import com.github.sanctum.clans.model.ClanAddonRegistry;
import com.github.sanctum.clans.model.ClanVentBus;
import com.github.sanctum.clans.model.addon.bounty.Bounty;
import com.github.sanctum.clans.model.addon.bounty.BountyCommand;
import com.github.sanctum.clans.model.addon.bounty.BountyList;
import com.github.sanctum.clans.model.BanksAPI;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.event.command.CommandInformationAdaptEvent;
import com.github.sanctum.clans.event.player.PlayerKillPlayerEvent;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.panther.event.Vent;
import java.math.BigDecimal;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BountyAddon extends Clan.Addon {

	@Override
	public boolean isPersistent() {
		return EconomyProvision.getInstance().isValid() && !getApi().isTrial();
	}

	@Override
	public @NotNull String getName() {
		return "Bounty";
	}

	@Override
	public @NotNull String getDescription() {
		return "Mark bounties on players! Earn money for you or your clan.";
	}

	@Override
	public @NotNull String getVersion() {
		return "1.0";
	}

	@Override
	public String[] getAuthors() {
		return new String[]{"Hempfest"};
	}

	@Override
	public void onLoad() {
		getContext().stage(new BountyCommand("bounty"));
	}

	@Override
	public void onEnable() {

		ClanVentBus.subscribe(PlayerKillPlayerEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			Clan.Addon cycle = ClanAddonRegistry.getInstance().get("Bounty");

			if (cycle != null && !cycle.getContext().isActive()) {
				subscription.remove();
				return;
			}

			Player p = e.getPlayer();
			if (ClansAPI.getInstance().getAssociate(p.getUniqueId()).isPresent()) {
				Clan c = ClansAPI.getInstance().getClanManager().getClan(p.getUniqueId());
				Bounty b = BountyList.get(e.getVictim().getUniqueId()).orElse(null);
				if (b != null) {
					if (ClansAPI.getDataInstance().isTrue("Addon.Bounty.settings.take-from-killed")) {
						// take from wallet
						if (ClansAPI.getInstance().getAssociate(e.getVictim().getUniqueId()).isPresent()) {
							Clan t = ClansAPI.getInstance().getClanManager().getClan(e.getVictim().getUniqueId());
							// take from victims clan if they are in one
							if (ClansAPI.getDataInstance().isTrue("Addon.Bounty.settings.announce-defeat")) {
								String format = ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.defeat-message")
										.replace("{KILLER}", p.getName())
										.replace("{KILLER_CLAN}", c.getName())
										.replace("{BOUNTY}", b.getAmount().toString())
										.replace("{PLAYER}", e.getVictim().getName());
								Bukkit.broadcastMessage(StringUtils.use(format).translate());
							}
							if (BanksAPI.getInstance().getBank(t).has(b.getAmount())) {
								BigDecimal newBal = BanksAPI.getInstance().getBank(t).getBalance().subtract(b.getAmount());
								BanksAPI.getInstance().getBank(t).setBalance(newBal);
								if (ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.deposit-type").equals("PLAYER")) {
									boolean deposit = EconomyProvision.getInstance().deposit(b.getAmount(), p, p.getWorld().getName()).orElse(false);
									if (!deposit) {
										Clan.ACTION.sendMessage(p, "&c&oSomething went wrong with paying you.. This is awkward.");
									}
								} else if (ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.deposit-type").equals("CLAN")) {
									newBal = BanksAPI.getInstance().getBank(c).getBalance().add(b.getAmount());
									BanksAPI.getInstance().getBank(c).setBalance(newBal);
									c.broadcast("&2" + e.getVictim().getName() + "'s &abounty was retrieved and they were killed in the process. We gained &6$" + b.getAmount().doubleValue());
									// broadcast bounty retrieval?
								}
								t.broadcast("&c" + e.getVictim().getName() + "'s &6bounty was retrieved and they were killed in the process. We lost &e$" + b.getAmount().doubleValue());
							} else {
								boolean has = EconomyProvision.getInstance().has(b.getAmount(), e.getVictim(), e.getVictim().getWorld().getName()).orElse(false);
								if (has) {
									boolean withdraw = EconomyProvision.getInstance().withdraw(b.getAmount(), e.getVictim(), e.getVictim().getWorld().getName()).orElse(false);
									if (!withdraw) {
										Clan.ACTION.sendMessage(p, "&c&oSomething went wrong.. unable to withdraw money.");
									}
								}
								if (ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.deposit-type").equals("PLAYER")) {
									boolean deposit = EconomyProvision.getInstance().deposit(b.getAmount(), p, p.getWorld().getName()).orElse(false);
									if (!deposit) {
										Clan.ACTION.sendMessage(p, "&c&oSomething went wrong with paying you.. This is awkward.");
									}
								} else if (ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.deposit-type").equals("CLAN")) {
									BigDecimal newBal = BanksAPI.getInstance().getBank(c).getBalance().add(b.getAmount());
									BanksAPI.getInstance().getBank(c).setBalance(newBal);
									c.broadcast("&2" + e.getVictim().getName() + "'s &abounty was retrieved and they were killed in the process. We gained &6$" + b.getAmount().doubleValue());
								}
							}
						} else {
							if (ClansAPI.getDataInstance().isTrue("Addon.Bounty.settings.announce-defeat")) {
								String format = ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.defeat-message")
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
									Clan.ACTION.sendMessage(p, "&c&oSomething went wrong.. unable to withdraw money.");
								}
							}
							if (ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.deposit-type").equals("PLAYER")) {
								boolean deposit = EconomyProvision.getInstance().deposit(b.getAmount(), p, p.getWorld().getName()).orElse(false);
								if (!deposit) {
									Clan.ACTION.sendMessage(p, "&c&oSomething went wrong with paying you.. This is awkward.");
								}
							} else if (ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.deposit-type").equals("CLAN")) {
								BigDecimal newBal = BanksAPI.getInstance().getBank(c).getBalance().add(b.getAmount());
								BanksAPI.getInstance().getBank(c).setBalance(newBal);
								c.broadcast("&2" + e.getVictim().getName() + "'s &abounty was retrieved and they were killed in the process. We gained &6$" + b.getAmount().doubleValue());
								// broadcast bounty retrieval?
							}
						}

					} else {
						// take no money from player
						// broadcast bounty retrieval?
						if (ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.deposit-type").equals("PLAYER")) {
							boolean deposit = EconomyProvision.getInstance().deposit(b.getAmount(), p, p.getWorld().getName()).orElse(false);
							if (!deposit) {
								Clan.ACTION.sendMessage(p, "&c&oSomething went wrong with paying you.. This is awkward.");
							}
						} else if (ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.deposit-type").equals("CLAN")) {
							BigDecimal newBal = BanksAPI.getInstance().getBank(c).getBalance().add(b.getAmount());
							BanksAPI.getInstance().getBank(c).setBalance(newBal);
						}
						if (ClansAPI.getDataInstance().isTrue("Addon.Bounty.settings.announce-defeat")) {
							String format = ClansAPI.getDataInstance().getConfig().getRoot().getString("Addon.Bounty.settings.defeat-message")
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

		ClanVentBus.subscribe(CommandInformationAdaptEvent.class, Vent.Priority.HIGH, (e, subscription) -> e.insert("&7|&e) &6/clan &fbounty <playerName> <amount>"));

	}

	@Override
	public void onDisable() {

	}
}
