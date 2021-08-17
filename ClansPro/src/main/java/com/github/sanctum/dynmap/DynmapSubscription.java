package com.github.sanctum.dynmap;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.command.CommandHelpInsertEvent;
import com.github.sanctum.clans.util.events.command.CommandInsertEvent;
import com.github.sanctum.clans.util.events.command.TabInsertEvent;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.link.ClanVentBus;
import com.github.sanctum.link.CycleList;
import com.github.sanctum.link.EventCycle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.dynmap.markers.AreaMarker;

public final class DynmapSubscription implements Listener {

	public DynmapSubscription() {

		DynmapIntegration integration = new DynmapIntegration().applyFormat();

		ClanVentBus.subscribe(CommandHelpInsertEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {

			EventCycle cycle = CycleList.getAddon("Dynmap");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}

			e.insert("&7|&e) &6/clan &fshowclaims");
			e.insert("&7|&e) &6/clan &fhideclaim");

		});

		ClanVentBus.subscribe(TabInsertEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {

			EventCycle cycle = CycleList.getAddon("Dynmap");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}

			List<String> add = new ArrayList<>(Arrays.asList("showclaims", "hideclaim"));
			for (String a : add) {
				if (!e.getArgs(1).contains(a)) {
					e.add(1, a);
				}
			}

		});

		ClanVentBus.subscribe(CommandInsertEvent.class, Vent.Priority.MEDIUM, (e, subscription) -> {

			EventCycle cycle = CycleList.getAddon("Dynmap");

			if (cycle != null && !cycle.isActive()) {
				subscription.remove();
				return;
			}

			Player p = e.getSender();
			int length = e.getArgs().length;
			String[] args = e.getArgs();
			StringLibrary lib = new StringLibrary();
			if (length == 1) {
				if (args[0].equalsIgnoreCase("showclaims")) {
					if (ClansAPI.getInstance().getClanID(p.getUniqueId()) != null) {
						ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
						Clan clan = associate.getClan();
						lib.sendMessage(p, "&e&oUpdating dynmap with claim information..");
						if (Arrays.asList(clan.getOwnedClaimsList()).size() == 0) {
							lib.sendMessage(p, "&c&oClaim mapping task failed. No claims to map.");
							e.setReturn(true);
						}
						if (integration.getFailedAttempt() != null) {
							lib.sendMessage(p, integration.getFailedAttempt());
						}
						if (associate.getPriority().toInt() >= 2) {
							long time = System.currentTimeMillis();
							integration.fillMap(clan.getOwnedClaimsList());
							long complete = (System.currentTimeMillis() - time) / 1000;
							int second = Integer.parseInt(String.valueOf(complete));
							lib.sendMessage(p, "&a&oClaim mapping task completed in &f" + second + "&a&os");
							ClansAPI.getInstance().getPlugin().getLogger().info("- (" + clan.getName() + ") Marker sets successfully updated in accordance to claims.");
						} else {
							lib.sendMessage(p, "&c&oYou do not have clan clearance.");
							e.setReturn(true);
						}
					}
					e.setReturn(true);
				}
				if (args[0].equalsIgnoreCase("hideclaim")) {
					if (ClansAPI.getInstance().getClanID(p.getUniqueId()) != null) {
						ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
						Clan clan = associate.getClan();
						if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
							Claim claim = Claim.from(p.getLocation());
							if (Arrays.asList(clan.getOwnedClaimsList()).contains(claim.getId())) {
								Set<AreaMarker> markers = integration.markerset.getAreaMarkers();
								if (associate.getPriority().toInt() >= 2) {
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
					if (ClansAPI.getInstance().getClanID(p.getUniqueId()) != null) {
						ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
						if (associate.getPriority().toInt() >= e.getUtil().claimingClearance()) {
							Clan clan = associate.getClan();
							Claim claim = Claim.from(p.getLocation());
							if (claim != null) {
								if (Arrays.asList(clan.getOwnedClaimsList()).contains(Claim.ACTION.getClaimID(p.getLocation()))) {
									integration.removeMarker(Claim.ACTION.getClaimID(p.getLocation()));
								} else {
									if (ClansAPI.getInstance().getShieldManager().isEnabled()) {
										if (e.getUtil().overPowerBypass()) {
											Clan clan2 = claim.getClan();
											if (clan.getPower() > clan2.getPower()) {
												integration.removeMarker(claim.getId());
											}
										}
									} else {
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
						if (ClansAPI.getInstance().getClanID(p.getUniqueId()) != null) {
							ClanAssociate associate = ClansAPI.getInstance().getAssociate(p).orElse(null);
							if (associate.getPriority().toInt() >= e.getUtil().unclaimAllClearance()) {
								if (!d.isConfigurationSection(ClansAPI.getInstance().getClanID(p.getUniqueId()).toString() + ".Claims")) {
									e.setReturn(false);
								}
								if (!Objects.requireNonNull(d.getConfigurationSection(ClansAPI.getInstance().getClanID(p.getUniqueId()).toString() + ".Claims")).getKeys(false).isEmpty()) {
									for (String claimID : d.getConfigurationSection(ClansAPI.getInstance().getClanID(p.getUniqueId()).toString() + ".Claims").getKeys(false)) {
										integration.removeMarker(claimID);
									}
								}
							}
						}
					}
				}
			}

		});

	}


}
