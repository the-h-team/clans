package com.github.sanctum.clans.bridge.external.dynmap;

import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanSubCommand;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.StringLibrary;
import com.github.sanctum.labyrinth.data.Configurable;
import com.github.sanctum.labyrinth.data.FileManager;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dynmap.markers.AreaMarker;

public class DynmapCommand extends ClanSubCommand {

	private final DynmapIntegration integration = new DynmapIntegration().dedicateMarkerSet();

	public DynmapCommand(String label) {
		super(label);
		setAliases(Arrays.asList("showclaims", "hideclaim", "unclaim"));
	}

	@Override
	public boolean player(Player p, String label, String[] args) {


		int length = args.length;
		StringLibrary lib = new StringLibrary();
		if (length == 0) {
			if (label.equalsIgnoreCase("showclaims")) {
				if (ClansAPI.getInstance().getClanManager().getClanID(p.getUniqueId()) != null) {
					Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
					Clan clan = associate.getClan();
					lib.sendMessage(p, "&e&oUpdating dynmap with claim information..");
					if (clan.getClaims().length == 0) {
						lib.sendMessage(p, "&c&oClaim mapping task failed. No claims to map.");
						return true;
					}
					if (integration.getFailedAttempt() != null) {
						lib.sendMessage(p, integration.getFailedAttempt());
					}
					if (associate.getPriority().toLevel() >= 2) {
						long time = System.currentTimeMillis();
						integration.fillMap(Arrays.stream(clan.getClaims()).map(Claim::getId).toArray(String[]::new));
						long complete = (System.currentTimeMillis() - time) / 1000;
						int second = Integer.parseInt(String.valueOf(complete));
						lib.sendMessage(p, "&a&oClaim mapping task completed in &f" + second + "&a&os");
						ClansAPI.getInstance().getPlugin().getLogger().info("- (" + clan.getName() + ") Marker sets successfully updated in accordance to claims.");
					} else {
						lib.sendMessage(p, "&c&oYou do not have clan clearance.");
						return true;
					}
				}
				return true;
			}
			if (label.equalsIgnoreCase("hideclaim")) {
				if (ClansAPI.getInstance().getClanManager().getClanID(p.getUniqueId()) != null) {
					Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
					Clan clan = associate.getClan();
					if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
						Claim claim = ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation());
						if (Arrays.stream(clan.getClaims()).anyMatch(c -> c.getId().equals(claim.getId()))) {
							Set<AreaMarker> markers = integration.markerset.getAreaMarkers();
							if (associate.getPriority().toLevel() >= 2) {
								for (AreaMarker am : markers) {
									if (am.getMarkerID().equals(claim.getId())) {
										am.deleteMarker();
										lib.sendMessage(p, "&b&oCurrent claim visibility has been removed from the map.");
										return true;
									}
								}
							} else {
								lib.sendMessage(p, "&c&oYou do not have clan clearance.");
								return true;
							}
							return true;
						}
						lib.sendMessage(p, lib.notClaimOwner(claim.getOwner().getTag().getId()));
					} else {
						lib.sendMessage(p, "This land belongs to: &4&nWilderness&r, and is free to claim.");
						return true;
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					return true;
				}
			}
			if (label.equalsIgnoreCase("unclaim")) {
				if (ClansAPI.getInstance().getClanManager().getClanID(p.getUniqueId()) != null) {
					Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
					if (associate.getPriority().toLevel() >= Clan.ACTION.claimingClearance()) {
						Clan clan = associate.getClan();
						Claim claim = ClansAPI.getInstance().getClaimManager().getClaim(p.getLocation());
						if (claim != null) {
							if (Arrays.stream(clan.getClaims()).anyMatch(c -> c.getId().equals(Claim.ACTION.getClaimID(p.getLocation())))) {
								integration.removeMarker(claim.getId());
							} else {
								if (ClansAPI.getInstance().getShieldManager().isEnabled()) {
									if (Clan.ACTION.isIgnoringShield()) {
										Clan clan2 = ((Clan)claim.getHolder());
										if (clan.getPower() > clan2.getPower()) {
											integration.removeMarker(claim.getId());
										}
									}
								} else {
									Clan clan2 = ((Clan)claim.getHolder());
									if (clan.getPower() > clan2.getPower()) {
										integration.removeMarker(claim.getId());
									}
								}
							}
						}
					}
				}

			}
		}
		if (length == 1) {
			if (label.equalsIgnoreCase("unclaim")) {
				if (args[0].equalsIgnoreCase("all")) {
					FileManager regions = ClansAPI.getInstance().getClaimManager().getFile();
					Configurable d = regions.getRoot();
					if (ClansAPI.getInstance().getClanManager().getClanID(p.getUniqueId()) != null) {
						Clan.Associate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
						if (associate.getPriority().toLevel() >= Clan.ACTION.unclaimAllClearance()) {
							if (!d.isNode(ClansAPI.getInstance().getClanManager().getClanID(p.getUniqueId()).toString() + ".Claims")) {
								return false;
							}
							if (!Objects.requireNonNull(d.getNode(ClansAPI.getInstance().getClanManager().getClanID(p.getUniqueId()).toString() + ".Claims")).getKeys(false).isEmpty()) {
								for (String claimID : d.getNode(ClansAPI.getInstance().getClanManager().getClanID(p.getUniqueId()).toString() + ".Claims").getKeys(false)) {
									integration.removeMarker(claimID);
								}
							}
						}
					}
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
		return null;
	}
}
