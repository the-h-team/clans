package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.labyrinth.task.LabyrinthApplicable;
import com.github.sanctum.labyrinth.task.Task;
import com.github.sanctum.labyrinth.task.TaskMonitor;
import com.github.sanctum.labyrinth.task.TaskPredicate;
import com.github.sanctum.panther.container.PantherArrays;
import com.github.sanctum.panther.container.PantherCollection;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SphericalParticleDisplay {

	private final Particle particle;
	private TaskPredicate<?>[] flags = new TaskPredicate<?>[0];
	private final List<Location> toSpawn = new ArrayList<>();
	private final TaskMonitor monitor = TaskMonitor.getLocalInstance();

	public SphericalParticleDisplay() {
		this.particle = Particle.REDSTONE;
	}

	public SphericalParticleDisplay require(TaskPredicate<?>... flags) {
		this.flags = flags;
		return this;
	}

	public boolean shutdown() {
		boolean result = false;
		for (Player online : Bukkit.getOnlinePlayers()) {
			String taskId = "ClansPro:particle-display;sphere:" + online.getUniqueId();
			Task task = monitor.get(taskId);
			if (task != null) {
				task.cancel();
				if (!result) {
					result = true;
				}
			}
		}
		return result;
	}

	public boolean hide(Player target) {
		String taskId = "ClansPro:particle-display;sphere:" + target.getUniqueId();
		Task task = monitor.get(taskId);
		if (task == null) return false;
		task.cancel();
		return true;
	}

	public boolean show(@NotNull Player target, @Nullable Location location, int radius, int count) {
		if (location != null) {
			location.subtract(0, 9, 0);
			for (int i = 0; i < 360; i += 360 / count) {
				double angle = (i * Math.PI / 180);
				double x = radius * Math.cos(angle);
				double z = radius * Math.sin(angle);
				Location loc = location.add(x, 1, z);
				toSpawn.add(loc);
			}
		}

		String taskId = "ClansPro:particle-display;sphere:" + target.getUniqueId();
		if (monitor.get(taskId) != null) {
			return false;
		}

		PantherCollection<TaskPredicate<?>> collection = PantherArrays.asList(flags);
		collection.add(TaskPredicate.cancelAfter(target));

		new LabyrinthApplicable(taskId) {

			private static final long serialVersionUID = 6931896334112633137L;

			double t = 0;
			final double r = 2;

			private Vector rotateAroundAxisX(Vector v, double angle) {
				angle = Math.toRadians(angle);
				double y, z, cos, sin;
				cos = Math.cos(angle);
				sin = Math.sin(angle);
				y = v.getY() * cos - v.getZ() * sin;
				z = v.getY() * sin + v.getZ() * cos;
				return v.setY(y).setZ(z);
			}

			private Vector rotateAroundAxisY(Vector v, double angle) {
				angle = -angle;
				angle = Math.toRadians(angle);
				double x, z, cos, sin;
				cos = Math.cos(angle);
				sin = Math.sin(angle);
				x = v.getX() * cos + v.getZ() * sin;
				z = v.getX() * -sin + v.getZ() * cos;
				return v.setX(x).setZ(z);
			}

			private Vector rotateAroundAxisZ(Vector v, double angle) {
				angle = Math.toRadians(angle);
				double x, y, cos, sin;
				cos = Math.cos(angle);
				sin = Math.sin(angle);
				x = v.getX() * cos - v.getY() * sin;
				y = v.getX() * sin + v.getY() * cos;
				return v.setX(x).setY(y);
			}

			@Override
			public void run() {
				if (location == null) {
					target.getLocation().subtract(0, 9, 0);
					for (int i = 0; i < 360; i += 360 / count) {
						double angle = (i * Math.PI / 180);
						double x = radius * Math.cos(angle);
						double z = radius * Math.sin(angle);
						Location loc = target.getLocation().add(x, 1, z);
						toSpawn.add(loc);
					}
				}
				t = t + Math.PI / 16;
				double x = r * Math.cos(t);
				double y = 0;
				double z = r * Math.sin(t);
				Vector v = new Vector(x, y, z);

				for (Location loc : toSpawn) {
					v = rotateAroundAxisX(v, 10);
					loc.add(v.getX(), v.getY(), v.getZ());
					target.spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(), 1, new Particle.DustOptions(Color.MAROON, 2));
					target.spawnParticle(Particle.CRIT, loc, 1);
					loc.subtract(v.getX(), v.getY(), v.getZ());

					v = rotateAroundAxisY(v, 10);
					loc.add(v.getX(), v.getY(), v.getZ());
					target.spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(), 1, new Particle.DustOptions(Color.RED, 2));
					target.spawnParticle(Particle.CRIT, loc, 1);
					loc.subtract(v.getX(), v.getY(), v.getZ());

					v = rotateAroundAxisZ(v, 10);
					loc.add(v.getX(), v.getY(), v.getZ());
					target.spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(), 1, new Particle.DustOptions(Color.MAROON, 2));
					target.spawnParticle(Particle.CRIT, loc, 1);
					loc.subtract(v.getX(), v.getY(), v.getZ());

					if (t > Math.PI * 8) {
						this.cancel();
					}
				}

			}
		}.scheduleTimer(0, 1, collection.stream().toArray(TaskPredicate[]::new));
		return true;
	}


}
