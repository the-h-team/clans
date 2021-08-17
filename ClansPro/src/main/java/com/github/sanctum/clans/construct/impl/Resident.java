package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.construct.Claim;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Resident {

	private final Player inhabitant;

	private long joinTime;

	private final Set<Property> properties;

	private Claim claim;

	private final LinkedList<BlockMeta> placed = new LinkedList<>();

	private final LinkedList<BlockMeta> broken = new LinkedList<>();

	public Resident(Player inhabitant) {
		this.properties = new HashSet<>();
		this.inhabitant = inhabitant;
		this.joinTime = System.currentTimeMillis();
	}

	public Player getPlayer() {
		return inhabitant;
	}

	public Claim getLastKnown() {
		return claim;
	}

	public void updateJoinTime(long time) {
		this.joinTime = time;
	}

	public void updateLastKnown(Claim claim) {
		this.claim = claim;
	}

	public Claim getCurrent() {
		return Claim.from(inhabitant.getLocation());
	}

	public boolean hasProperty(Property property) {
		return this.properties.contains(property);
	}

	public void setProperty(Property property, boolean state) {
		if (hasProperty(property)) {
			if (!state) {
				this.properties.remove(property);
			}
		} else {
			if (state) {
				this.properties.add(property);
			}
		}
	}

	/**
	 * Get's the amount of time the resident has been inside their last known claim
	 *
	 * @return The amount of time spent.
	 */
	public long timeActiveInMillis() {
		return (System.currentTimeMillis() - joinTime);
	}

	/**
	 * Adds a block to the residents temporary interaction cache.
	 *
	 * @param placed The block placed by the resident.
	 */
	public void addPlaced(Block placed) {
		this.placed.add(new BlockMeta(placed, placed.getState().getType(), placed.getState().getRawData()));
	}

	/**
	 * Adds a block to the residents temporary interaction cache.
	 *
	 * @param broken The block broken by the resident.
	 */
	public void addBroken(Block broken, Material type, Byte data) {
		this.broken.add(new BlockMeta(broken, type, data));
	}

	public Set<PlacedBlock> getPlacedHistory() {
		return placed.stream().map(PlacedBlock::new).collect(Collectors.toSet());
	}

	public Set<BrokenBlock> getBrokenHistory() {
		return broken.stream().map(BrokenBlock::new).collect(Collectors.toSet());
	}

	public enum Property {
		TRAVERSED, NOTIFIED,
	}

}
