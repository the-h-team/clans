package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.construct.api.ClansAPI;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SphericalParticleDisplay {

	private final Particle particle;
	private final Location location;
	private final List<Location> toSpawn = new ArrayList<>();

	public SphericalParticleDisplay(Particle particle, Location location) {
		this.particle = particle;
		location.subtract(0, 9, 0);
		this.location = location;
	}

	public SphericalParticleDisplay configure(int radius, int count) {
		for (int i = 0; i < 360; i += 360 / count) {
			double angle = (i * Math.PI / 180);
			double x = radius * Math.cos(angle);
			double z = radius * Math.sin(angle);
			Location loc = location.add(x, 1, z);
			toSpawn.add(loc);
		}
		return this;
	}

	public void spawn(Player target) {


		new BukkitRunnable() {

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

				if (target == null || !target.isOnline()) {
					cancel();
					return;
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
					target.spawnParticle(Particle.FLASH, loc, 1);
					loc.subtract(v.getX(), v.getY(), v.getZ());

					v = rotateAroundAxisY(v, 10);
					loc.add(v.getX(), v.getY(), v.getZ());
					target.spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(), 1, new Particle.DustOptions(Color.RED, 2));
					target.spawnParticle(Particle.FLASH, loc, 1);
					loc.subtract(v.getX(), v.getY(), v.getZ());

					v = rotateAroundAxisZ(v, 10);
					loc.add(v.getX(), v.getY(), v.getZ());
					target.spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(), 1, new Particle.DustOptions(Color.MAROON, 2));
					target.spawnParticle(Particle.FLASH, loc, 1);
					loc.subtract(v.getX(), v.getY(), v.getZ());

					if (t > Math.PI * 8) {
						this.cancel();
					}
				}

			}
		}.runTaskTimer(ClansAPI.getInstance().getPlugin(), 0, 1);
	}


}
