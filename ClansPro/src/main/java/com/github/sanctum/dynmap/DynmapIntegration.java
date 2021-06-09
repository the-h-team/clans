package com.github.sanctum.dynmap;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.api.ClansAPI;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

public class DynmapIntegration {

	private String failedAttempt;

	public MarkerSet markerset;

	public static DynmapAPI dapi = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");


	public DynmapIntegration applyFormat() {
		if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
			try {
				markerset = dapi.getMarkerAPI().createMarkerSet("clans.claim.markerset", "Claims", dapi.getMarkerAPI().getMarkerIcons(), false);
			} catch (NullPointerException e) {
				markerset = dapi.getMarkerAPI().getMarkerSet("clans.claim.markerset");
			}
		}
		return this;
	}

	public void fillMap(String[] ownedClaims) {
		int i = 0;
		for (String claim : ownedClaims) {

			Claim c = ClansAPI.getInstance().getClaimManager().getClaim(claim);
			int cx1 = c.getChunk().getX() * 16;
			int cz1 = c.getChunk().getZ() * 16;

			int cx2 = c.getChunk().getX() * 16 + 16;
			int cz2 = c.getChunk().getZ() * 16 + 16;

			AreaMarker am = markerset.createAreaMarker(c.getId(), c.getClan().getName(), false, c.getChunk().getWorld().getName(), new double[1000], new double[1000], false);
			double[] d1 = {cx1, cx2};
			double[] d2 = {cz1, cz2};
			try {
				am.setCornerLocations(d1, d2);
				am.setLabel(c.getId());
				am.setDescription(c.getClan().getName() + " - " + Arrays.asList(c.getClan().getMembersList()).toString());
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
			if (Bukkit.getVersion().contains("1.16")) {
				setFailedAttempt("&#f5bf42&oA number of claims have already been marked and are being skipped. &f(&#42f5da" + i + "&f)");
			} else {
				setFailedAttempt("&6&oA number of claims have already been marked and are being skipped. &f(&b" + i + "&f)");
			}
		}
	}

	public String getFailedAttempt() {
		return failedAttempt;
	}

	private void setFailedAttempt(String failedAttempt) {
		this.failedAttempt = failedAttempt;
	}

	public void removeMarker(String claimID) {
		Claim c = ClansAPI.getInstance().getClaimManager().getClaim(claimID);
		for (AreaMarker areaMarker : markerset.getAreaMarkers()) {
			if (areaMarker.getMarkerID().equals(c.getId())) {
				areaMarker.deleteMarker();
			}
		}

	}


}