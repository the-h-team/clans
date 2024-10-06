package com.github.sanctum.clans.impl;

import com.github.sanctum.clans.model.Claim;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.EntityHolder;
import com.github.sanctum.clans.model.InvasiveEntity;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.HUID;
import com.github.sanctum.panther.util.ParsedHUID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

@SerializableAs("Claim")
public final class DefaultClaim implements Claim {

	private final Map<String, Flag> flags = new HashMap<>();
	private final String[] id;
	private final int[] pos;
	private boolean active;

	/**
	 * @deprecated For internal use only!!!
	 */
	@Deprecated
	public DefaultClaim() {
		this.id = null;
		this.pos = null;
	}

	public DefaultClaim(int x, int z, String clanId, String claimId, String world, boolean active) {
		this.id = new String[]{clanId, claimId, world};
		this.pos = new int[]{x, z};
		this.active = active;
	}

	@Override
	public EntityHolder getHolder() {
		if (getOwner() == null) return null;
		return getOwner().getAsClan();
	}

	/**
	 * Get the id for this claim.
	 *
	 * @return Gets the claimID from the claim object
	 */
	@Override
	public String getId() {
		return this.id[1];
	}

	/**
	 * Get who owns this claim by their id.
	 *
	 * @return Gets the owner of the claim object.
	 */
	@Override
	public InvasiveEntity getOwner() {
		String id = this.id[0];
		ParsedHUID test = HUID.parseID(id);
		if (test.isValid()) {
			return ClansAPI.getInstance().getClanManager().getClan(test.toID());
		}
		return ClansAPI.getInstance().getAssociate(UUID.fromString(id)).orElse(null);
	}

	/**
	 * Get the exact chunk location for this claim.
	 *
	 * @return Gets the specific chunk of the claim location.
	 */
	@Override
	public Chunk getChunk() {
		return getLocation().getChunk();
	}

	@Override
	public Flag getFlag(String id) {
		return flags.get(id);
	}

	@Override
	public Flag[] getFlags() {
		return flags.values().toArray(new Flag[0]);
	}

	/**
	 * Get the claim's position list
	 * <p>
	 * [0] = chunkX, [1] = chunkZ
	 *
	 * @return The claims position list
	 */
	@Override
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
	@Override
	public String[] getKey() {
		return id;
	}

	/**
	 * Get the claims activity status.
	 *
	 * @return true if the claim is visible
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	/**
	 * Set the claims activity. Making this false will make the claim appear invisible.
	 *
	 * @param active The status to update the claim to.
	 */
	@Override
	public void setActive(boolean active) {
		this.active = active;
		ClansAPI.getInstance().getClaimManager().getFile().getRoot().set(getOwner().getTag().getId() + "." + getId() + ".active", active);
		ClansAPI.getInstance().getClaimManager().getFile().getRoot().save();
	}

	@Override
	public void register(Flag... flags) {
		for (Flag f : flags) {
			try {
				this.flags.put(f.getId(), f.clone().updateCustom());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void remove(Flag flag) {
		flags.values().stream().filter(f -> f.getId().equals(flag.getId())).findFirst().ifPresent(fl -> TaskScheduler.of(() -> flags.remove(fl.getId())).schedule());
		Node claim = ClansAPI.getInstance().getClaimManager().getFile().getRoot().getNode(getOwner().getTag().getId() + "." + getId());
		if (claim.getNode("flags").getNode(flag.getId()).get() != null) {
			claim.getNode("flags").getNode(flag.getId()).set(null);
			claim.save();
		}
	}

	boolean hasSurface(Location location) {
		Block feet = location.getBlock();
		if (!feet.getType().equals(Material.AIR) && !feet.getLocation().add(0.0D, 1.0D, 0.0D).getBlock().getType().equals(Material.AIR))
			return false;
		Block head = feet.getRelative(BlockFace.UP);
		if (!head.getType().equals(Material.AIR))
			return false;
		Block ground = feet.getRelative(BlockFace.DOWN);
		return ground.getType().isSolid();
	}

	/**
	 * Get the chunk centered location for this claim.
	 *
	 * @return Gets the centered location of the claim objects chunk.
	 */
	@Override
	public Location getLocation() {
		int x = this.pos[0];
		int y = 110;
		int z = this.pos[1];
		String world = this.id[2];
		Location teleportLocation = (new Location(Bukkit.getWorld(world), (x << 4), y, (z << 4))).add(7.0D, 0.0D, 7.0D);
		if (LabyrinthProvider.getInstance().isLegacy())
			return teleportLocation;
		if (!hasSurface(teleportLocation))
			teleportLocation = (new Location(Bukkit.getWorld(world), (x << 4), y, (z << 4))).add(7.0D, 10.0D, 7.0D);
		return teleportLocation;
	}

	/**
	 * Get all residents for this claim.
	 *
	 * @return Gets a list of all known online residents within the claim.
	 */
	@Override
	public List<Resident> getResidents() {
		return ClansAPI.getInstance().getClaimManager().getResidentManager().getResidents().stream().filter(r -> r.getInfo().getCurrent().getId().equals(getId())).collect(Collectors.toList());
	}

	/**
	 * Get a claim resident by player-name
	 *
	 * @param name The player name to search for
	 * @return A resident object for the given claim
	 */
	@Override
	public Resident getResident(String name) {
		return ClansAPI.getInstance().getClaimManager().getResidentManager().getResidents().stream().filter(r -> r.getInfo().getCurrent().getId().equals(getId()) && r.getPlayer().getName().equals(name)).findFirst().orElse(null);
	}

	/**
	 * Completely delete/un-claim this clan land.
	 */
	@Override
	public void remove() {
		if (!((Clan) getHolder()).isConsole()) {
			Node n = ClansAPI.getInstance().getClaimManager().getFile().read(c -> c.getNode(getOwner().getTag().getId() + "." + getId()));
			n.set(null);
			n.save();
		}
		if (getHolder() instanceof DefaultClan) {
			DefaultClan cl = (DefaultClan) getHolder();
			cl.removeClaim(this);
		}
	}

	@Override
	public void save() {
		if (((Clan) getHolder()).isConsole()) return;
		FileManager file = ClansAPI.getInstance().getClaimManager().getFile();
		Node claim = file.getRoot().getNode(getOwner().getTag().getId() + "." + getId());
		claim.getNode("x").set(getChunk().getX());
		claim.getNode("z").set(getChunk().getZ());
		claim.getNode("world").set(getChunk().getWorld().getName());
		claim.getNode("active").set(this.active);
		for (Flag f : getFlags()) {
			claim.getNode("flags").getNode(f.getId()).set(f.isEnabled());
		}
		claim.save();
	}

	@NotNull
	@Override
	public Iterator<Block> iterator() {
		List<Block> list = new ArrayList<>();
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 256; y++) {
				for (int z = 0; z < 16; z++) {
					list.add(getChunk().getBlock(x, y, z));
				}
			}
		}
		return list.iterator();
	}

	@Override
	public void forEach(Consumer<? super Block> action) {
		iterator().forEachRemaining(action);
	}

	@Override
	public Spliterator<Block> spliterator() {
		List<Block> list = new ArrayList<>();
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 256; y++) {
				for (int z = 0; z < 16; z++) {
					list.add(getChunk().getBlock(x, y, z));
				}
			}
		}
		return list.spliterator();
	}

	@Override
	public String toString() {
		return "Claim{id=" + getId() + ",owner=" + getOwner().getTag().getId() + ",location=" + getLocation() + "}";
	}
}
