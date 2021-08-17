package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.labyrinth.library.TimeWatch;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

public class BrokenBlock {

	private final BlockMeta block;

	private boolean fixed;

	public BrokenBlock(BlockMeta meta) {
		this.block = meta;
	}

	public BlockMeta getMeta() {
		return block;
	}

	public boolean fix() {
		if (fixed) return false;
		if (getMeta().getOriginalType() != Material.AIR) {
			fixed = true;
			getMeta().parent.setType(getMeta().getOriginalType());
			BlockState state = getMeta().parent.getState();
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
