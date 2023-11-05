package com.github.sanctum.clans.bridge.external.dynmap;

import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Clearance;
import com.github.sanctum.clans.construct.util.StringLibrary;
import com.github.sanctum.clans.event.associate.AssociateObtainLandEvent;
import com.github.sanctum.labyrinth.formatting.completion.SimpleTabCompletion;
import com.github.sanctum.labyrinth.formatting.completion.TabCompletionIndex;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.panther.event.Subscribe;
import com.github.sanctum.panther.event.Vent;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DynmapCommand extends ClanSubCommand {

	final DynmapClanMarkerSet integration;
	final boolean showDefault;

	public DynmapCommand(String label, DynmapClanMarkerSet marketSet) {
		super(label);
		setPermission("clans.dynmap");
		this.integration = marketSet;
		this.showDefault = ClansAPI.getDataInstance().isTrue("Addon.Dynmap.show-by-default");
	}

	@Subscribe(priority = Vent.Priority.HIGHEST)
	public void onInteract(AssociateObtainLandEvent e) {
		if (!e.isCancelled()) {
			if (showDefault) integration.add(e.getClaim());
		}
	}

	public boolean test(Player target, String permission) {
		if (permission != null && !permission.isEmpty()) {
			if (target.hasPermission(permission)) {
				return true;
			} else {
				Mailer.empty(target).chat("&cYou don't have permission " + permission).deploy();
				return false;
			}
		} else {
			return true;
		}
	}

	@Override
	public boolean player(Player p, String label, String[] args) {
		int length = args.length;
		StringLibrary lib = new StringLibrary();
		Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
		if (!testPermission(p)) return true;
		if (length == 1) {
			if (args[0].equalsIgnoreCase("show")) {
				if (!test(p, "clans.dynmap.show")) return true;
				if (associate != null) {
					Clan clan = associate.getClan();
					lib.sendMessage(p, "&e&oUpdating dynmap with claim information..");
					if (clan.getClaims().length == 0) {
						lib.sendMessage(p, "&c&oClaim mapping task failed. No claims to map.");
						return true;
					}
					if (Clearance.MANAGE_ALL_LAND.test(associate)) {
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
				if (!test(p, "clans.dynmap.hide")) return true;
				if (associate != null) {
					if (Clearance.MANAGE_ALL_LAND.test(associate)) {
						Clan clan = associate.getClan();
						Claim claim = ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation());
						if (claim != null) {
							if (Arrays.stream(clan.getClaims()).anyMatch(c -> c.getId().equals(ClansAPI.getInstance().getClaimManager().getId(p.getLocation())))) {
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
					if (!test(p, "clans.dynmap.hide")) return true;
					if (associate != null) {
						if (Clearance.MANAGE_ALL_LAND.test(associate)) {
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
					if (!test(p, "clans.dynmap.show")) return true;
					if (associate != null) {
						Clan clan = associate.getClan();
						lib.sendMessage(p, "&e&oUpdating dynmap with claim information..");
						if (clan.getClaims().length == 0) {
							lib.sendMessage(p, "&c&oClaim mapping task failed. No claims to map.");
							return true;
						}
						if (Clearance.MANAGE_ALL_LAND.test(associate)) {
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
