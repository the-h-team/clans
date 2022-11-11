package com.github.sanctum.clans.bridge.external.dynmap;

import com.github.sanctum.clans.construct.api.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.panther.container.PantherCollectors;
import com.github.sanctum.panther.container.PantherSet;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;
import org.jetbrains.annotations.NotNull;

public final class DynmapClanMarketSet {

	MarkerSet set;
	final DynmapAPI api = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");

	public DynmapClanMarketSet initialize() {
		if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
			try {
				set = api.getMarkerAPI().createMarkerSet("clans.claim.markerset", "Claims", api.getMarkerAPI().getMarkerIcons(), false);
			} catch (NullPointerException e) {
				set = api.getMarkerAPI().getMarkerSet("clans.claim.markerset");
			}
		}
		return this;
	}

	public PantherSet<AreaMarker> getAll(@NotNull Clan clan) {
		return set.getAreaMarkers().stream().filter(m -> m.getLabel().equals(clan.getName())).collect(PantherCollectors.toSet());
	}

	public String add(@NotNull Claim c) {
		int i = 0;
		int cx1 = c.getChunk().getX() * 16;
		int cz1 = c.getChunk().getZ() * 16;

		int cx2 = c.getChunk().getX() * 16 + 16;
		int cz2 = c.getChunk().getZ() * 16 + 16;

		AreaMarker am = set.createAreaMarker(c.getId(), Optional.ofNullable(((Clan) c.getHolder()).getNickname()).orElse(((Clan) c.getHolder()).getName()), false, c.getChunk().getWorld().getName(), new double[1000], new double[1000], false);
		double[] d1 = {cx1, cx2};
		double[] d2 = {cz1, cz2};
		try {
			am.setCornerLocations(d1, d2);
			am.setDescription(((Clan) c.getHolder()).getName() + " - " + ((Clan) c.getHolder()).getMembers().stream().map(Clan.Associate::getNickname).collect(Collectors.joining(", ")));
			int stroke = 1;
			double strokeOpac = 0.0;
			double Opac = 0.3;
			am.setLineStyle(stroke, strokeOpac, 0xedfffc);
			am.setFillStyle(Opac, 0x42cbf5);
		} catch (NullPointerException e) {
			i++;
		}
		if (i < 1) {
			if (LabyrinthProvider.getInstance().isNew()) {
				return "&#f5bf42&oThis claim has already been marked and is being skipped.";
			} else {
				return "&6&oThis claim has already been marked and is being skipped.";
			}
		}
		return "Operation success.";
	}

	public String addAll(@NotNull Claim... claims) {
		int i = 0;
		for (Claim c : claims) {
			int cx1 = c.getChunk().getX() * 16;
			int cz1 = c.getChunk().getZ() * 16;

			int cx2 = c.getChunk().getX() * 16 + 16;
			int cz2 = c.getChunk().getZ() * 16 + 16;

			AreaMarker am = set.createAreaMarker(c.getId(), ((Clan) c.getHolder()).getName(), false, c.getChunk().getWorld().getName(), new double[1000], new double[1000], false);
			double[] d1 = {cx1, cx2};
			double[] d2 = {cz1, cz2};
			try {
				am.setCornerLocations(d1, d2);
				am.setLabel(c.getId());
				am.setDescription(((Clan) c.getHolder()).getName() + " - " + ((Clan) c.getHolder()).getMembers().stream().map(Clan.Associate::getName).collect(Collectors.joining(", ")));
				int stroke = 1;
				double strokeOpac = 0.0;
				double Opac = 0.3;
				am.setLineStyle(stroke, strokeOpac, 0xedfffc);
				am.setFillStyle(Opac, 0x42cbf5);
			} catch (NullPointerException e) {
				i++;
			}
		}
		if (i > 0) {
			if (LabyrinthProvider.getInstance().isNew()) {
				return "&#f5bf42&oA number of claims have already been marked and are being skipped. &f(&#42f5da" + i + "&f)";
			} else {
				return "&6&oA number of claims have already been marked and are being skipped. &f(&b" + i + "&f)";
			}
		}
		return "Operation success.";
	}

	public void remove(@NotNull Claim c) {
		for (AreaMarker areaMarker : set.getAreaMarkers()) {
			if (areaMarker.getMarkerID().equals(c.getId())) {
				areaMarker.deleteMarker();
			}
		}

	}


}