package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.construct.DataManager;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.extra.BukkitColor;
import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.library.StringUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class CommandColor extends ClanSubCommand {
	public CommandColor() {
		super("color");
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		StringLibrary lib = Clan.ACTION;
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);

		if (associate == null) {
			sendMessage(p, lib.notInClan());
			return true;
		}

		if (args.length == 0) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("color")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("color")));
				return true;
			}
			lib.sendMessage(p, lib.commandColor());
			lib.sendMessage(p, "&7|&e)&r " + "https://www.digminecraft.com/lists/color_list_pc.php");
			return true;
		}

		if (args.length == 1) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("color")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("color")));
				return true;
			}
			Clan clan = associate.getClan();
			if (Clearance.MANAGE_COLOR.test(associate)) {

				if (args[0].equalsIgnoreCase("empty") || args[0].equalsIgnoreCase("reset")) {
					clan.getPalette().setStart("&f");
					clan.getPalette().setEnd(null);
					lib.sendMessage(p, "&aGradient color removed.");
					clan.getMembers().forEach(a -> {
						OfflinePlayer op = a.getTag().getPlayer();
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
					return true;
				}

				if (args[0].equalsIgnoreCase("random")) {
					clan.getPalette().randomize();
					lib.sendMessage(p, clan.getPalette().isGradient() ? clan.getPalette().toString("The clan color has been updated.") : clan.getPalette() + "The clan color has been updated.");
					clan.getMembers().forEach(a -> {
						OfflinePlayer op = a.getTag().getPlayer();
						try {
							if (op.isOnline()) {
								if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
									if (clan.getPalette().isGradient()) {
										ClanDisplayName.set(op.getPlayer(), ClansAPI.getDataInstance().formatDisplayTag("", associate.getClan().getPalette().toGradient().context(associate.getClan().getName()).translate()));
									} else {
										ClanDisplayName.set(op.getPlayer(), ClansAPI.getDataInstance().formatDisplayTag(ClansAPI.getInstance().getClanManager().getClan(op.getUniqueId()).getPalette().toString(), ClansAPI.getInstance().getClanManager().getClan(op.getUniqueId()).getName()));

									}
								}
							}
						} catch (NullPointerException e) {
							ClansAPI.getInstance().getPlugin().getLogger().severe("- Failed to updated name tags for user " + op.getName() + ".");
						}
					});
					return true;
				}

				if (!args[0].matches("(&#[a-zA-Z0-9]{6})+") && !args[0].matches("(#[a-zA-Z0-9]{6})+") && !args[0].matches("(&#[a-zA-Z0-9]{6})+(&[a-zA-Z0-9])+") && !args[0].matches("(&[a-zA-Z0-9])+") && !args[0].matches("(&#[a-zA-Z0-9])+") && !args[0].matches("(#[a-zA-Z0-9])+")) {
					lib.sendMessage(p, "&c&oInvalid color format.");
					return true;
				}

				for (String s : ClansAPI.getDataInstance().getConfig().read(c -> c.getStringList("Clans.color-blacklist"))) {

					if (StringUtils.use(args[0]).containsIgnoreCase(s)) {
						lib.sendMessage(p, "&c&oInvalid color format. Code: '" + s + "' is not allowed.");
						return true;
					}
				}

				clan.getPalette().setStart(args[0]);
				if (!LabyrinthProvider.getInstance().isLegacy()) {
					clan.getMembers().forEach(a -> {
						OfflinePlayer op = a.getTag().getPlayer();
						try {
							if (op.isOnline()) {
								if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
									if (clan.getPalette().isGradient()) {
										ClanDisplayName.set(op.getPlayer(), ClansAPI.getDataInstance().formatDisplayTag("", associate.getClan().getPalette().toGradient().context(associate.getClan().getName()).translate()));
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
				clan.broadcast(args[0] + "Our color was changed.");
			} else {
				lib.sendMessage(p, lib.noClearance());
			}
			return true;
		}

		if (args.length == 2) {
			if (!Clan.ACTION.test(p, "clanspro." + DataManager.Security.getPermission("color")).deploy()) {
				lib.sendMessage(p, lib.noPermission(this.getPermission() + "." + DataManager.Security.getPermission("color")));
				return true;
			}

			if (!LabyrinthProvider.getService(Service.LEGACY).isNew()) {
				lib.sendMessage(p, "&cOlder version detected! To use gradients the server version must be no lower than 1.16");
				return true;
			}

			Clan clan = associate.getClan();
			if (Clearance.MANAGE_COLOR.test(associate)) {

				if (args[0].equalsIgnoreCase("empty") || args[0].equalsIgnoreCase("reset")) {
					clan.getPalette().setStart("&f");
					clan.getPalette().setEnd(null);
					lib.sendMessage(p, "&aGradient color removed.");
					clan.getMembers().forEach(a -> {
						OfflinePlayer op = a.getTag().getPlayer();
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
					return true;
				}

				if (args[1].equalsIgnoreCase("empty") || args[1].equalsIgnoreCase("reset")) {
					clan.getPalette().setEnd(null);
					lib.sendMessage(p, "&aGradient color removed.");
					clan.getMembers().forEach(a -> {
						OfflinePlayer op = a.getTag().getPlayer();
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
					return true;
				}

				if (!args[0].matches("(&#[a-zA-Z0-9]{6})+") && !args[0].matches("(#[a-zA-Z0-9]{6})+")) {
					lib.sendMessage(p, "&c&oInvalid color format. Only hex is allowed for gradients.");
					return true;
				}

				if (!args[1].matches("(&#[a-zA-Z0-9]{6})+") && !args[1].matches("(#[a-zA-Z0-9]{6})+")) {
					lib.sendMessage(p, "&c&oInvalid color format. Only hex is allowed for gradients.");
					return true;
				}

				for (String s : ClansAPI.getDataInstance().getConfig().getRoot().getStringList("Clans.color-blacklist")) {

					if (StringUtils.use(args[0]).containsIgnoreCase(s) || StringUtils.use(args[1]).containsIgnoreCase(s)) {
						lib.sendMessage(p, "&c&oInvalid color format. Code: '" + s + "' is not allowed.");
						return true;
					}
				}
				clan.getPalette().setStart(args[0]);
				clan.getPalette().setEnd(args[1]);
				clan.broadcast(clan.getPalette().toGradient().context("Our color was changed").translate());
				clan.getMembers().forEach(a -> {
					OfflinePlayer op = a.getTag().getPlayer();
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
			return true;
		}

		return true;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args).then(TabCompletionIndex.ONE, () -> getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, () -> {
					List<String> list = new ArrayList<>();
					for (BukkitColor c : BukkitColor.values()) {
						list.add(c.toCode());
					}
					list.add("reset");
					return list;
				}).get();
	}
}
