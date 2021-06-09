package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.construct.Claim;
import com.google.common.collect.MapMaker;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Resident {

	private final Player inhabitant;

	private long joinTime;

	private boolean notificationSent;

	private boolean traversedDifferent;

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

	public boolean isNotificationSent() {
		return notificationSent;
	}

	public void setNotificationSent(boolean sent) {
		this.notificationSent = sent;
	}

	public boolean hasTraversedDifferent() {
		return traversedDifferent;
	}

	public void setTraversedDifferent(boolean traversed) {
		this.traversedDifferent = traversed;
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

}
