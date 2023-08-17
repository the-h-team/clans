package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.labyrinth.library.TimeWatch;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;

public class DefaultBrokenBlock {

	private final DefaultBlockMeta block;

	private boolean fixed;

	public DefaultBrokenBlock(DefaultBlockMeta meta) {
		this.block = meta;
	}

	public DefaultBlockMeta getMeta() {
		return block;
	}

	public boolean fix(Player player) {
		if (fixed) return false;
		if (getMeta().getTypeFromInteraction() != Material.AIR) {
			BlockState state = getMeta().getParent().getState();
			BlockPlaceEvent event = new BlockPlaceEvent(getMeta().getParent(), state, getMeta().getLocationInteracted().getBlock(), player.getInventory().getItemInMainHand(), player, true, EquipmentSlot.HAND);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				fixed = true;
				getMeta().getParent().setType(getMeta().getTypeFromInteraction());
				event.getBlockReplacedState().setRawData(getMeta().data);
				event.getBlockReplacedState().update(true);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean fix() {
		if (fixed) return false;
		if (getMeta().getTypeFromInteraction() != Material.AIR) {
			fixed = true;
			getMeta().getParent().setType(getMeta().getTypeFromInteraction());
			BlockState state = getMeta().getParent().getState();
			state.setRawData(getMeta().data);
			state.update(true);
			return true;
		}
		return false;
	}

	public TimeWatch.Recording getRecording() {
		return TimeWatch.Recording.subtract(block.time);
	}

}
