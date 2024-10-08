package com.github.sanctum.clans.impl;

import com.github.sanctum.labyrinth.library.TimeWatch;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class DefaultPlacedBlock {

	private final DefaultBlockMeta block;

	private boolean destroyed;

	public DefaultPlacedBlock(DefaultBlockMeta meta) {
		this.block = meta;
	}

	public DefaultBlockMeta getMeta() {
		return block;
	}

	public boolean destroy(Player player) {
		if (destroyed) return false;
		if (getMeta().parent.getType() != Material.AIR) {
			BlockBreakEvent event = new BlockBreakEvent(getMeta().parent, player);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				destroyed = true;
				getMeta().parent.setType(Material.AIR);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean destroy() {
		if (destroyed) return false;
		if (getMeta().parent.getType() != Material.AIR) {
			destroyed = true;
			getMeta().parent.setType(Material.AIR);
			return true;
		}
		return false;
	}

	public TimeWatch.Recording getRecording() {
		return TimeWatch.Recording.subtract(block.time);
	}

}
