package com.github.sanctum.clans.construct.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class BlockMeta {

	private final Material type;
	protected final long time;
	private final Location location;
	protected final Block parent;
	protected final Byte data;

	BlockMeta(Block location, Material type, Byte data) {
		this.data = data;
		this.parent = location;
		this.location = location.getLocation();
		this.type = type;
		this.time = System.currentTimeMillis();
	}

	public Block getParent() {
		return parent;
	}

	public Location getLocationInteracted() {
		return location;
	}

	public Material getTypeFromInteraction() {
		return type;
	}
}
