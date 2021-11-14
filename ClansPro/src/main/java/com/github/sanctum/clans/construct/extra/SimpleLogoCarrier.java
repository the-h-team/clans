package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.api.LogoHolder;
import com.github.sanctum.clans.event.insignia.InsigniaBuildCarrierEvent;
import com.github.sanctum.labyrinth.annotation.Ordinal;
import com.github.sanctum.labyrinth.library.Entities;
import com.github.sanctum.labyrinth.library.HUID;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Schedule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.attribute.Attributable;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.NotNull;

public final class SimpleLogoCarrier implements LogoHolder.Carrier, Iterable<String> {

	final LinkedHashSet<LogoHolder.Carrier.Line> lines = new LinkedHashSet<>();
	final HUID id = HUID.randomID();
	private Chunk chunk;
	Location top;
	private final List<String> logo;

	public SimpleLogoCarrier(List<String> logo) {
		this.logo = new ArrayList<>(logo);
	}

	public void add(Attributable attributable) throws IllegalArgumentException {
		if (attributable instanceof ArmorStand) {
			Line line = new Line((ArmorStand) attributable);
			if (this.chunk == null) {
				this.chunk = line.getStand().getLocation().getChunk();
			}
			lines.add(line);
		} else throw new IllegalArgumentException("Attribute cannot be used for line processing!");
	}

	@Override
	public Location getTop() {
		return top;
	}

	public Chunk getChunk() {
		return chunk;
	}

	@Override
	public LogoHolder getHolder() {
		return null;
	}

	@Ordinal
	HUID getRealId() {
		return id;
	}

	public String getId() {
		return id.toString().replace("-", "").substring(8);
	}

	public int size() {
		return logo.size();
	}

	public String[] toRaw() {
		return logo.toArray(new String[0]);
	}

	public Set<LogoHolder.Carrier.Line> getLines() {
		return lines;
	}

	void remove(LogoHolder.Carrier.Line line) {
		lines.remove(line);
	}

	public void build(Location location) {
		double y = location.getY() + 0.5;
		List<String> t = logo;
		Collections.reverse(t);
		String[] l = t.toArray(new String[0]);
		for (int i = 0; i < l.length; i++) {
			y += 0.2;
			Location loc = new Location(location.getWorld(), location.getX(), y, location.getZ(), location.getYaw(), location.getPitch()).add(0.5, 0, 0.5);
			if (i == l.length - 1) {
				top = loc;
			}
			String name = StringUtils.use(l[i]).translate();
			InsigniaBuildCarrierEvent event = ClanVentBus.call(new InsigniaBuildCarrierEvent(this, i, l.length, name));
			if (!event.isCancelled()) {
				ArmorStand stand = Entities.ARMOR_STAND.spawn(loc, s -> {
					s.setVisible(false);
					s.setSmall(true);
					s.setMarker(true);
					s.setCustomNameVisible(true);
					s.setCustomName(event.getContent());
				});
				add(stand);
			}
		}
	}

	public void build(Location location, Consumer<SimpleLogoCarrier> consumer) {
		consumer.accept(this);
		build(location);
	}

	@Override
	public String toString() {
		return "Carrier{" + "world=" + getChunk().getWorld().getName() + ",x=" + getChunk().getX() + ",z=" + getChunk().getZ() + ",size=" + getLines().size() + ",id=" + getRealId().toString() + '}';
	}

	@NotNull
	@Override
	public Iterator<String> iterator() {
		return logo.iterator();
	}

	@Override
	public void forEach(Consumer<? super String> action) {
		logo.forEach(action);
	}

	@Override
	public Spliterator<String> spliterator() {
		return logo.spliterator();
	}

	final class Line implements LogoHolder.Carrier.Line {

		private final ArmorStand line;

		Line(ArmorStand stand) {
			this.line = stand;
		}

		public HUID getId() {
			return SimpleLogoCarrier.this.id;
		}

		public ArmorStand getStand() {
			return line;
		}

		public void destroy() {
			getStand().remove();
			Schedule.sync(() -> SimpleLogoCarrier.this.remove(this)).run();
		}

	}
}
