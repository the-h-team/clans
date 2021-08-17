package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.construct.actions.ClaimAction;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.Resident;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Encapsulated data for a clan owned chunk of land.
 */
public class Claim {
	private final String[] id;
	private final int[] pos;
	private boolean active;

	public static final ClaimAction ACTION = new ClaimAction();

	protected Claim(String[] ID, int[] POS, boolean active) {
		this.id = ID;
		this.pos = POS;
		this.active = active;
	}

	/**
	 * Attempt converting a given location to a claim.
	 *
	 * @param loc The location to convert
	 * @return A clan claim or null if the location has no id.
	 */
	public static Claim from(Location loc) {
		return ACTION.getClaimID(loc) != null ? ClansAPI.getInstance().getClaimManager().getClaim(ACTION.getClaimID(loc)) : null;
	}

	/**
	 * Attempt converting a given location to a claim.
	 *
	 * @param chunk The chunk to convert
	 * @return A clan claim or null if the location has no id.
	 */
	public static Claim from(Chunk chunk) {
		return ClansAPI.getInstance().getClaimManager().getId(chunk.getX(), chunk.getZ(), chunk.getWorld().getName()) != null ? ClansAPI.getInstance().getClaimManager().getClaim(ClansAPI.getInstance().getClaimManager().getId(chunk.getX(), chunk.getZ(), chunk.getWorld().getName())) : null;
	}

	/**
	 * Attempt converting a given player to a claim resident.
	 * <p>
	 * This will return null if the given player isn't within a claim.
	 *
	 * @param p The player to search for
	 * @return A resident object.
	 */
	public static Resident getResident(Player p) {
		return ClansAPI.getData().RESIDENTS.stream().filter(r -> r.getPlayer().getName().equals(p.getName())).findFirst().orElse(null);
	}

	/**
	 * Get who owns this claim.
	 *
	 * @return The owner of this claim.
	 */
	public Clan getClan() {
		return ClansAPI.getInstance().getClan(getOwner());
	}

	/**
	 * Get the id for this claim.
	 *
	 * @return Gets the claimID from the claim object
	 */
	public String getId() {
		return this.id[1];
	}

	/**
	 * Get who owns this claim by their id.
	 *
	 * @return Gets the clanID of the claim object.
	 */
	public String getOwner() {
		return this.id[0];
	}

	/**
	 * Get the exact chunk location for this claim.
	 *
	 * @return Gets the specific chunk of the claim location.
	 */
	public Chunk getChunk() {
		return getLocation().getChunk();
	}

	/**
	 * Get the claim's position list
	 * <p>
	 * [0] = chunkX, [1] = chunkZ
	 *
	 * @return The claims position list
	 */
	public int[] getPos() {
		return pos;
	}

	/**
	 * Get the claim's id list
	 * <p>
	 * [0] = clanID , [1] = claimID, [2] = worldName
	 *
	 * @return The claims id list
	 */
	public String[] getKey() {
		return id;
	}

	/**
	 * Get the claims activity status.
	 *
	 * @return true if the claim is visible
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Set the claims activity. Making this false will make the claim appear invisible.
	 *
	 * @param active The status to update the claim to.
	 */
	public void setActivity(boolean active) {
		this.active = active;
		ClansAPI.getInstance().getClaimManager().getFile().getConfig().set(getOwner() + ".Claims." + getId() + ".active", active);
		ClansAPI.getInstance().getClaimManager().getFile().saveConfig();
	}

	/**
	 * Get the chunk centered location for this claim.
	 *
	 * @return Gets the centered location of the claim objects chunk.
	 */
	public Location getLocation() {
		int x = this.pos[0];
		int y = 110;
		int z = this.pos[1];
		String world = this.id[2];
		Location teleportLocation = (new Location(Bukkit.getWorld(world), (x << 4), y, (z << 4))).add(7.0D, 0.0D, 7.0D);
		if (Bukkit.getVersion().contains("1.12"))
			return teleportLocation;
		if (!ACTION.hasSurface(teleportLocation))
			teleportLocation = (new Location(Bukkit.getWorld(world), (x << 4), y, (z << 4))).add(7.0D, 10.0D, 7.0D);
		return teleportLocation;
	}

	/**
	 * Get all residents for this claim.
	 *
	 * @return Gets a list of all known online residents within the claim.
	 */
	public List<Resident> getResidents() {
		List<Resident> query = new ArrayList<>();
		for (Resident r : ClansAPI.getData().RESIDENTS) {
			if (r.getCurrent().getId().equals(getId())) {
				query.add(r);
			}
		}
		return query;
	}

	/**
	 * Get a claim resident by player-name
	 *
	 * @param name The player name to search for
	 * @return A resident object for the given claim
	 */
	public Resident getResident(String name) {
		return ClansAPI.getData().RESIDENTS.stream().filter(r -> r.getPlayer().getName().equals(name)).findFirst().orElse(null);
	}

	/**
	 * Completely delete/un-claim this clan land.
	 */
	public void remove() {
		ClansAPI.getInstance().getClaimManager().getFile().getConfig().set(getOwner() + ".Claims." + getId(), null);
		ClansAPI.getInstance().getClaimManager().getFile().saveConfig();
		ClansAPI.getInstance().getClaimManager().refresh();
	}

}
