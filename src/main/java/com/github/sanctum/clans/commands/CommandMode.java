package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.DataManager;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Clearance;
import com.github.sanctum.clans.util.Reservoir;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.impl.DefaultClan;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import java.math.BigDecimal;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandMode extends ClanSubCommand {
	public CommandMode() {
		super("mode");
		setUsage(ClansAPI.getDataInstance().getMessageString("Commands.mode.text"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (associate == null) {
			sendMessage(p, lib.notInClan());
			return true;
		}

		Reservoir r = Reservoir.get(associate.getClan());

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("mode")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("mode")));
				return true;
			}
			if (Clearance.MANAGE_MODE.test(associate)) {
				DefaultClan c = (DefaultClan) associate.getClan();
				if (c.isPeaceful()) {
					if (!ClansAPI.getInstance().isTrial()) {
						if (r == null && ClansAPI.getDataInstance().isTrue("Clans.mode-change.require-reservoir")) {
							lib.sendMessage(p, "&cYour clan must have a reservoir! Craft and place an end crystal on claimed land.");
							return true;
						}
					}
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
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, this.getPermission() + "." + DataManager.Security.getPermission("mode")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("mode")));
				return true;
			}
			if (Clearance.MANAGE_MODE.test(associate)) {
				if (!(associate.getClan() instanceof DefaultClan))
					return true;
				DefaultClan c = (DefaultClan) associate.getClan();
				switch (args[0].toLowerCase()) {
					case "war":
						if (c.isPeaceful()) {
							if (r == null && ClansAPI.getDataInstance().isTrue("Clans.mode-change.require-reservoir")) {
								lib.sendMessage(p, "&cYour clan must have a reservoir! Craft and place an end crystal on claimed land.");
								return true;
							}
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
									Bukkit.broadcastMessage(Clan.ACTION.color(lib.modeAnnounce(args[0], c.getName())));
									lib.sendMessage(p, lib.war());
								} else {
									lib.sendMessage(p, c.getModeCooldown().fullTimeLeft());
									return true;
								}
								return true;
							}
							c.setPeaceful(false);
							Bukkit.broadcastMessage(Clan.ACTION.color(lib.modeAnnounce(args[0], c.getName())));
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
									Bukkit.broadcastMessage(Clan.ACTION.color(lib.modeAnnounce(args[0], c.getName())));
									lib.sendMessage(p, lib.peaceful());
								} else {
									lib.sendMessage(p, c.getModeCooldown().fullTimeLeft());
									return true;
								}
								return true;
							}
							c.setPeaceful(true);
							Bukkit.broadcastMessage(Clan.ACTION.color(lib.modeAnnounce(args[0], c.getName())));
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
			return true;
		}

		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, () -> getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, "war", "peace")
				.get();
	}
}
