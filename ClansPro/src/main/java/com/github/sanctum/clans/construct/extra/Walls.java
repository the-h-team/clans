package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.TaskService;
import com.github.sanctum.labyrinth.data.container.CollectionTask;
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.labyrinth.data.container.LabyrinthList;
import com.github.sanctum.labyrinth.library.DirectivePoint;
import com.github.sanctum.labyrinth.library.HUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Walls implements Buildable {

	Material material;
	Location location;
	int length;
	int width;
	int height;

	public Walls setPoint(Location location) {
		this.location = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		return this;
	}

	public Walls setMaterial(Material material) {
		this.material = material;
		return this;
	}

	public Walls setHeight(int height) {
		this.height = height;
		return this;
	}

	public Walls setLength(int length) {
		this.length = length;
		return this;
	}

	public Walls setWidth(int width) {
		this.width = width;
		return this;
	}

	@Override
	public void build() {
		LabyrinthCollection<Block> collection = new LabyrinthList<>();
		Block b = location.getBlock();
		DirectivePoint pi = Buildable.getPoint(location.getYaw());
		for (int i = 0; i < length; i++) {
			Block l = pi.getLocation(b.getLocation(), i).getBlock();
			collection.add(l);
			if (i + 1 == length) {
				b = l;
			}
		}
		pi = DirectivePoint.getRight(DirectivePoint.getRight(pi));
		for (int i = 0; i < width; i++) {
			Block l = pi.getLocation(b.getLocation(), i).getBlock();
			collection.add(l);
			if (i + 1 == width) {
				b = l;
			}
		}
		pi = DirectivePoint.getRight(DirectivePoint.getRight(pi));
		for (int i = 0; i < length; i++) {
			Block l = pi.getLocation(b.getLocation(), i).getBlock();
			collection.add(l);
			if (i + 1 == length) {
				b = l;
			}
		}
		pi = DirectivePoint.getRight(DirectivePoint.getRight(pi));
		for (int i = 0; i < width; i++) {
			Block l = pi.getLocation(b.getLocation(), i).getBlock();
			collection.add(l);
			if (i + 1 == width) {
				b = l;
			}
		}
		CollectionTask<Block> task = CollectionTask.processSilent(collection.stream().toArray(Block[]::new), "Square-" + HUID.randomID(), 1, s -> s.setType(material));
		LabyrinthProvider.getInstance().getScheduler(TaskService.SYNCHRONOUS).repeat(task, 120, 120);
		for (int i = 0; i < Math.max(0, height - 1); i++) {
			new Walls().setMaterial(material).setWidth(width).setHeight(1).setLength(length).setPoint(location.add(0, 1, 0)).build();
		}
	}
}
