package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.extra.SimpleLogoCarrier;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.data.container.PersistentContainer;
import com.github.sanctum.labyrinth.library.NamespacedKey;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class LogoGallery implements Savable {

	private final Map<String, SimpleLogoCarrier> carriers = new HashMap<>();
	private final PersistentContainer container;

	public LogoGallery() {
		this.container = LabyrinthProvider.getInstance().getContainer(new NamespacedKey(ClansAPI.getInstance().getPlugin(), "logogallery"));
		for (String id : container.persistentKeySet()) {
			List<String> list = (List<String>) container.get(List.class, id);
			if (list != null) load(id, list);
		}
	}

	public Collection<SimpleLogoCarrier> getLogos() {
		return carriers.values();
	}

	public SimpleLogoCarrier getLogo(String label) {
		return carriers.get(label);
	}

	public SimpleLogoCarrier getLogo(List<String> logo) {
		for (Map.Entry<String, SimpleLogoCarrier> c : carriers.entrySet()) {
			if (Arrays.equals(c.getValue().toRaw(), logo.toArray(new String[0]))) {
				return c.getValue();
			}
		}
		return null;
	}

	public void load(String label, List<String> list) {
		if (!list.isEmpty()) {
			for (Map.Entry<String, SimpleLogoCarrier> c : carriers.entrySet()) {
				if (Arrays.equals(c.getValue().toRaw(), list.toArray(new String[0]))) {
					return;
				}
			}
			if (getLogo(label) != null) return;
			SimpleLogoCarrier def = new SimpleLogoCarrier(list);
			LogoHolder.newAdapter(location -> {
				for (LogoHolder.Carrier.Line line : def.getLines()) {
					for (int i = 1; i < 2; i++) {
						Block up = location.getBlock().getRelative(BlockFace.UP, i);
						if (line.getStand().getLocation().distanceSquared(up.getLocation().add(0.5, 0, 0.5)) <= 1) {
							return def;
						}
					}
				}
				return null;
			}).deploy();
			carriers.put(label, def);
			container.attach(label, list);
		}
	}

	public void remove(String label) {
		if (carriers.containsKey(label)) {
			carriers.get(label).remove();
			carriers.remove(label);
			container.delete(label);
		}
	}

	public void remove(List<String> logo) {
		for (Map.Entry<String, SimpleLogoCarrier> c : carriers.entrySet()) {
			if (Arrays.equals(c.getValue().toRaw(), logo.toArray(new String[0]))) {
				TaskScheduler.of(() -> {
					carriers.get(c.getKey()).remove();
					carriers.remove(c.getKey());
					container.delete(c.getKey());
				}).schedule();
				break;
			}
		}
	}

	/**
	 * Save all captured logo's to the persistent container.
	 */
	@Override
	public void save() {
		for (String k : container.persistentKeySet()) {
			try {
				container.save(k);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void remove() {
		carriers.forEach((label, logo) -> remove(label));
	}


}
