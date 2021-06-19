package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.construct.Claim;
import com.google.common.collect.MapMaker;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Resident {

	private final Player inhabitant;

	private long joinTime;

	private final Set<Property> properties;

	private Claim claim;

	private final ConcurrentMap<Block, Long> lastPlaced = new MapMaker().
			weakKeys().
			weakValues().
			makeMap();

	private final ConcurrentMap<Block, Long> lastBroken = new MapMaker().
			weakKeys().
			weakValues().
			makeMap();

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
		lastPlaced.put(placed, System.currentTimeMillis());
	}

	/**
	 * Adds a block to the residents temporary interaction cache.
	 *
	 * @param broken The block broken by the resident.
	 */
	public void addBroken(Block broken) {
		lastBroken.put(broken, System.currentTimeMillis());
	}

	public Map<Block, Long> getPlacedHistory() {
		return lastPlaced;
	}

	public Map<Block, Long> getBrokenHistory() {
		return lastBroken;
	}

	public enum Property {
		TRAVERSED, NOTIFIED,
	}

}
