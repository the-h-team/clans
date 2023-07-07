package com.github.sanctum.clans.bridge.internal.borders;

import com.github.sanctum.labyrinth.data.container.Region;
import com.github.sanctum.panther.util.HUID;
import org.bukkit.World;

public class BorderRegion extends Region {
	public BorderRegion(int xmin, int xmax, int ymin, int ymax, int zmin, int zmax, World world, HUID id) {
		super(world, xmin, xmax, ymin, ymax, zmin, zmax, id);
	}
}
