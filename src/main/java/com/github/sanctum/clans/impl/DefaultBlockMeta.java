package com.github.sanctum.clans.impl;

import com.github.sanctum.panther.annotation.Note;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

@Note("This is an internal class only")
public final class DefaultBlockMeta {

	private final Material type;
	final long time;
	private final Location location;
	final Block parent;
	final Byte data;

	@Deprecated()
	public DefaultBlockMeta(Block location, Material type, Byte data) {
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
