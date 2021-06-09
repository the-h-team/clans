package com.github.sanctum.dynmap;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.command.CommandHelpInsertEvent;
import com.github.sanctum.clans.util.events.command.CommandInsertEvent;
import com.github.sanctum.clans.util.events.command.TabInsertEvent;
import com.github.sanctum.labyrinth.data.FileManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.dynmap.markers.AreaMarker;

public final class DynmapListener implements Listener {

	public DynmapIntegration integration = new DynmapIntegration().applyFormat();

	public ClanAction getUtil() {
		return DefaultClan.action;
	}

	@EventHandler
	public void onHelp(CommandHelpInsertEvent e) {
		e.insert("&7|&e) &6/clan &fshowclaims");
		e.insert("&7|&e) &6/clan &fhideclaim");
	}

	@EventHandler
	public void onTab(TabInsertEvent e) {
		List<String> add = new ArrayList<>(Arrays.asList("showclaims", "hideclaim"));
		for (String a : add) {
			if (!e.getArgs(1).contains(a)) {
				e.add(1, a);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onClanCommand(CommandInsertEvent e) {
		Player p = e.getSender();
		int length = e.getArgs().length;
		String[] args = e.getArgs();
		StringLibrary lib = new StringLibrary();
		if (length == 1) {
			if (args[0].equalsIgnoreCase("showclaims")) {
				if (DefaultClan.action.getClanID(p.getUniqueId()) != null) {
					Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
					lib.sendMessage(p, "&e&oUpdating dynmap with claim information..");
					if (Arrays.asList(clan.getOwnedClaimsList()).size() == 0) {
						lib.sendMessage(p, "&c&oClaim mapping task failed. No claims to map.");
						e.setReturn(true);
					}
					if (integration.getFailedAttempt() != null) {
						lib.sendMessage(p, integration.getFailedAttempt());
					}
					if (getUtil().getRankPower(p.getUniqueId()) >= 2) {
						long time = System.currentTimeMillis();
						integration.fillMap(clan.getOwnedClaimsList());
						long complete = (System.currentTimeMillis() - time) / 1000;
						int second = Integer.parseInt(String.valueOf(complete));
						lib.sendMessage(p, "&a&oClaim mapping task completed in &f" + second + "&a&os");
						ClansPro.getInstance().getLogger().info("- (" + clan.getName() + ") Marker sets successfully updated in accordance to claims.");
					} else {
						lib.sendMessage(p, "&c&oYou do not have clan clearance.");
						e.setReturn(true);
					}
				}
				e.setReturn(true);
			}
			if (args[0].equalsIgnoreCase("hideclaim")) {
				if (DefaultClan.action.getClanID(p.getUniqueId()) != null) {
					Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
					if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
						Claim claim = Claim.from(p.getLocation());
						if (Arrays.asList(clan.getOwnedClaimsList()).contains(claim.getId())) {
							Set<AreaMarker> markers = integration.markerset.getAreaMarkers();
							if (getUtil().getRankPower(p.getUniqueId()) >= 2) {
								for (AreaMarker am : markers) {
									if (am.getMarkerID().equals(claim.getId())) {
										am.deleteMarker();
										lib.sendMessage(p, "&b&oCurrent claim visibility has been removed from the map.");
										e.setReturn(true);
									}
								}
							} else {
								lib.sendMessage(p, "&c&oYou do not have clan clearance.");
								e.setReturn(true);
							}
							e.setReturn(true);
						}
						lib.sendMessage(p, lib.notClaimOwner(claim.getOwner()));
					} else {
						lib.sendMessage(p, "This land belongs to: &4&nWilderness&r, and is free to claim.");
						e.setReturn(true);
					}
				} else {
					lib.sendMessage(p, lib.notInClan());
					e.setReturn(true);
				}
			}
			if (args[0].equalsIgnoreCase("unclaim")) {
				if (DefaultClan.action.getClanID(p.getUniqueId()) != null) {
					if (getUtil().getRankPower(p.getUniqueId()) >= getUtil().claimingClearance()) {
						Clan clan = ClansAPI.getInstance().getClan(p.getUniqueId());
						if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
							if (Arrays.asList(clan.getOwnedClaimsList()).contains(Claim.action.getClaimID(p.getLocation()))) {
								integration.removeMarker(Claim.action.getClaimID(p.getLocation()));
							} else {
								if (ClansAPI.getInstance().getShieldManager().isEnabled()) {
									if (getUtil().overPowerBypass()) {
										Claim claim = Claim.from(p.getLocation());
										Clan clan2 = claim.getClan();
										if (clan.getPower() > clan2.getPower()) {
											integration.removeMarker(claim.getId());
										}
									}
								} else {
									Claim claim = Claim.from(p.getLocation());
									Clan clan2 = claim.getClan();
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
		if (length == 2) {
			if (args[0].equalsIgnoreCase("unclaim")) {
				if (args[1].equalsIgnoreCase("all")) {
					FileManager regions = ClansAPI.getInstance().getClaimManager().getFile();
					FileConfiguration d = regions.getConfig();
					if (DefaultClan.action.getClanID(p.getUniqueId()) != null) {
						if (getUtil().getRankPower(p.getUniqueId()) >= getUtil().unclaimAllClearance()) {
							if (!d.isConfigurationSection(getUtil().getClanID(p.getUniqueId()) + ".Claims")) {
								e.setReturn(false);
							}
							if (!Objects.requireNonNull(d.getConfigurationSection(getUtil().getClanID(p.getUniqueId()) + ".Claims")).getKeys(false).isEmpty()) {
								for (String claimID : d.getConfigurationSection(getUtil().getClanID(p.getUniqueId()) + ".Claims").getKeys(false)) {
									integration.removeMarker(claimID);
								}
							}
						}
					}
				}
			}
		}
	}


}
