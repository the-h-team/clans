package com.github.sanctum.clans.bridge.external.dynmap;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.external.DynmapAddon;
import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DynmapCommand extends ClanSubCommand {

	final DynmapClanMarketSet integration;

	public DynmapCommand(String label, DynmapClanMarketSet marketSet) {
		super(label);
		this.integration = marketSet;
	}

	@Override
	public boolean player(Player p, String label, String[] args) {


		int length = args.length;
		StringLibrary lib = new StringLibrary();
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (length == 1) {
			if (args[0].equalsIgnoreCase("show")) {
				if (associate != null) {
					Clan clan = associate.getClan();
					lib.sendMessage(p, "&e&oUpdating dynmap with claim information..");
					if (clan.getClaims().length == 0) {
						lib.sendMessage(p, "&c&oClaim mapping task failed. No claims to map.");
						return true;
					}
					if (associate.getPriority().toLevel() >= Clearance.MANAGE_ALL_LAND.getDefault()) {
						long time = System.currentTimeMillis();
						Claim c = ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation());
						if (c != null) {
							String response = integration.add(c);
							long complete = (System.currentTimeMillis() - time) / 1000;
							int second = Integer.parseInt(String.valueOf(complete));
							lib.sendMessage(p, "&a&oClaim mapping task completed in &f" + second + "&a&os");
							lib.sendMessage(p, "&fInfo: &6" + response);
						}
					} else {
						lib.sendMessage(p, "&c&oYou do not have clan clearance.");
						return true;
					}
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("hide")) {
				if (associate != null) {
					if (associate.getPriority().toLevel() >= Clearance.MANAGE_ALL_LAND.getDefault()) {
						Clan clan = associate.getClan();
						Claim claim = ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation());
						if (claim != null) {
							if (Arrays.stream(clan.getClaims()).anyMatch(c -> c.getId().equals(Claim.ACTION.getClaimID(p.getLocation())))) {
								integration.remove(claim);
							} else {
								if (ClansAPI.getInstance().getShieldManager().isEnabled()) {
									if (Clan.ACTION.isIgnoringShield()) {
										Clan clan2 = ((Clan) claim.getHolder());
										if (clan.getPower() > clan2.getPower()) {
											integration.remove(claim);
										}
									}
								} else {
									Clan clan2 = ((Clan) claim.getHolder());
									if (clan.getPower() > clan2.getPower()) {
										integration.remove(claim);
									}
								}
							}
							lib.sendMessage(p, "&aThe claim you stand in is no longer visible on the globe.");
						} else {
							lib.sendMessage(p, lib.alreadyWild());
						}
					} else {
						lib.sendMessage(p, lib.noClearance());
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
				}
				return true;
			}
		}
		if (length == 2) {
			if (args[0].equalsIgnoreCase("hide")) {
				if (args[1].equalsIgnoreCase("all")) {
					if (associate != null) {
						if (associate.getPriority().toLevel() >= Clearance.MANAGE_ALL_LAND.getDefault()) {
							for (Claim c : associate.getClaims()) {
								integration.remove(c);
							}
						}
					}
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("show")) {
				if (args[1].equalsIgnoreCase("all")) {
					if (associate != null) {
						Clan clan = associate.getClan();
						lib.sendMessage(p, "&e&oUpdating dynmap with claim information..");
						if (clan.getClaims().length == 0) {
							lib.sendMessage(p, "&c&oClaim mapping task failed. No claims to map.");
							return true;
						}
						if (associate.getPriority().toLevel() >= Clearance.MANAGE_ALL_LAND.getDefault()) {
							long time = System.currentTimeMillis();
							String response = integration.addAll(clan.getClaims());
							long complete = (System.currentTimeMillis() - time) / 1000;
							int second = Integer.parseInt(String.valueOf(complete));
							lib.sendMessage(p, "&a&oClaim mapping task completed in &f" + second + "&a&os");
							lib.sendMessage(p, "&fInfo: &6" + response);
						} else {
							lib.sendMessage(p, lib.noClearance());
							return true;
						}
					}
					return true;
				}
			}
		}

		return true;
	}

	@Override
	public boolean console(CommandSender sender, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> tab(Player player, String label, String[] args) {
		return SimpleTabCompletion.of(args)
				.then(TabCompletionIndex.ONE, getBaseCompletion(args))
				.then(TabCompletionIndex.TWO, getLabel(), TabCompletionIndex.ONE, "show", "hide")
				.then(TabCompletionIndex.THREE, "show", TabCompletionIndex.TWO, "all")
				.then(TabCompletionIndex.THREE, "hide", TabCompletionIndex.TWO, "all")
				.get();
	}
}
