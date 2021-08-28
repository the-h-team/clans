package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.labyrinth.data.FileManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ClaimManager {

	private final FileManager regions;

	public ClaimManager() {
		this.regions = DataManager.FileType.MISC_FILE.get("Regions", "Configuration");
	}

	public boolean isInClaim(Location location) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans().list()) {
			boolean test = Arrays.stream(clan.getOwnedClaims()).anyMatch(c -> location.getChunk().getX() == c.getPos()[0] && location.getChunk().getZ() == c.getPos()[1] && location.getWorld().getName().equals(c.getKey()[2]));
			if (test) {
				return test;
			}
		}
		return false;
	}

	public boolean isInClaim(int x, int z, String world) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans().list()) {
			boolean test = Arrays.stream(clan.getOwnedClaims()).anyMatch(c -> x == c.getPos()[0] && z == c.getPos()[1] && world.equals(c.getKey()[2]));
			if (test) {
				return test;
			}
		}
		return false;
	}

	public boolean isInClaim(Chunk chunk) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans().list()) {
			boolean test = Arrays.stream(clan.getOwnedClaims()).anyMatch(c -> chunk.getX() == c.getPos()[0] && chunk.getZ() == c.getPos()[1] && chunk.getWorld().getName().equals(c.getKey()[2]));
			if (test) {
				return test;
			}
		}
		return false;
	}

	public String getId(Location location) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans().list()) {
			String id = Arrays.stream(clan.getOwnedClaims()).filter(c -> location.getChunk().getX() == c.getPos()[0] && location.getChunk().getZ() == c.getPos()[1] && location.getWorld().getName().equals(c.getKey()[2])).map(Claim::getId).findFirst().orElse(null);
			if (id != null) {
				return id;
			}
		}
		return null;
	}

	public String getId(int x, int z, String world) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans().list()) {
			String id = Arrays.stream(clan.getOwnedClaims()).filter(c -> x == c.getPos()[0] && z == c.getPos()[1] && world.equals(c.getKey()[2])).map(Claim::getId).findFirst().orElse(null);
			if (id != null) {
				return id;
			}
		}
		return null;
	}

	public Claim getClaim(String claimID) {
		for (Clan clan : ClansAPI.getInstance().getClanManager().getClans().list()) {
			Claim claim = Arrays.stream(clan.getOwnedClaims()).filter(c -> c.getId().equals(claimID)).findFirst().orElse(null);
			if (claim != null) {
				return claim;
			}
		}
		return null;
	}

	public FileManager getFile() {
		return regions;
	}

	public Set<Claim> getClaims() {
		Set<Claim> claims = new HashSet<>();
		for (Clan c : ClansAPI.getInstance().getClanManager().getClans().list()) {
			claims.addAll(Arrays.asList(c.getOwnedClaims()));
		}
		return claims;
	}

	public boolean load(Claim claim) {
		if (claim.getClan() instanceof DefaultClan) {
			DefaultClan clan = (DefaultClan) claim.getClan();
			clan.addClaim(claim);
			return true;
		}
		return false;
	}

	/**
	 * Clear and re-load all persistent claims.
	 */
	public void refresh() {

		for (Clan c : ClansAPI.getInstance().getClanManager().getClans().list()) {
			if (c instanceof DefaultClan) {
				DefaultClan clan = (DefaultClan) c;
				clan.resetClaims();
			}
		}

		FileConfiguration d = regions.getConfig();
		for (String clan : d.getKeys(false)) {
			for (String claimID : Objects.requireNonNull(d.getConfigurationSection(clan + ".Claims")).getKeys(false)) {
				int x = d.getInt(clan + ".Claims." + claimID + ".X");
				int z = d.getInt(clan + ".Claims." + claimID + ".Z");
				String w = d.getString(clan + ".Claims." + claimID + ".World");
				String[] ID = {clan, claimID, w};
				int[] pos = {x, z};
				Claim c = new Claim(ID, pos, true);
				load(c);
				if (!getFile().getConfig().isBoolean(clan + ".Claims." + claimID + ".active")) {
					getFile().getConfig().set(clan + ".Claims." + claimID + ".active", true);
					getFile().saveConfig();
				}
			}
		}
	}


}
