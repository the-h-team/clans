package com.github.sanctum.clans.model;

import com.github.sanctum.clans.impl.DefaultBlockMeta;
import com.github.sanctum.clans.impl.DefaultBrokenBlock;
import com.github.sanctum.clans.impl.DefaultPlacedBlock;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public class ResidentInformation {

	private final Set<Trigger> properties = new HashSet<>();
	private final Claim.Resident resident;
	private final Set<DefaultBlockMeta> placed = new LinkedHashSet<>();
	private final Set<DefaultBlockMeta> broken = new LinkedHashSet<>();
	Claim lastKnown;
	long timeEntered;

	public ResidentInformation(@NotNull Claim.Resident resident) {
		this.resident = resident;
	}

	public Claim getCurrent() {
		return ClansAPI.getInstance().getClaimManager().getClaim(resident.getPlayer().getLocation());
	}

	public Claim getLastKnown() {
		return lastKnown;
	}

	public long getTimeEntered() {
		return timeEntered;
	}

	public long getTimeActive() {
		return (System.currentTimeMillis() - timeEntered);
	}

	public void setLastKnown(Claim lastKnown) {
		this.lastKnown = lastKnown;
	}

	public void setTimeEntered(long timeEntered) {
		this.timeEntered = timeEntered;
	}

	public boolean hasProperty(Trigger property) {
		return this.properties.contains(property);
	}

	public void setProperty(Trigger property, boolean state) {
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
	 * Adds a block to the residents temporary interaction cache.
	 *
	 * @param placed The block placed by the resident.
	 */
	public void addPlaced(Block placed) {
		this.placed.add(new DefaultBlockMeta(placed, placed.getState().getType(), placed.getState().getRawData()));
	}

	/**
	 * Adds a block to the residents temporary interaction cache.
	 *
	 * @param broken The block broken by the resident.
	 */
	public void addBroken(Block broken, Material type, Byte data) {
		this.broken.add(new DefaultBlockMeta(broken, type, data));
	}

	public Set<DefaultPlacedBlock> getPlacedHistory() {
		return placed.stream().map(DefaultPlacedBlock::new).collect(Collectors.toSet());
	}

	public Set<DefaultBrokenBlock> getBrokenHistory() {
		return broken.stream().map(DefaultBrokenBlock::new).collect(Collectors.toSet());
	}

	public enum Trigger {
		TRAVERSED, NOTIFIED, LEAVING,
	}
}
