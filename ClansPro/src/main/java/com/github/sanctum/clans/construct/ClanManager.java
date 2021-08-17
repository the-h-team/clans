package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.labyrinth.formatting.UniformedComponents;
import java.util.LinkedList;
import java.util.List;

public class ClanManager {

	private final List<Clan> CLANS = new LinkedList<>();

	public UniformedComponents<Clan> getClans() {
		return UniformedComponents.accept(CLANS);
	}

	/**
	 * Load a custom implementation of a clan into cache.
	 *
	 * @param c The clan object to load into cache.
	 * @return true if the clan successfully got added to cache.
	 */
	public boolean load(Clan c) {
		if (c == null) throw new IllegalArgumentException("The provided clan cannot be null!");

		if (CLANS.stream().noneMatch(cl -> cl.getName().equals(c.getName()))) {
			CLANS.add(c);
			return true;
		}

		return false;
	}

	public <T extends Clan> T cast(Class<T> clanImpl, Clan clan) {
		if (clanImpl.isAssignableFrom(clan.getClass())) {
			return (T) clan;
		}
		return null;
	}

	/**
	 * Delete a clan from cache.
	 * <p>
	 * If the specified clan shares a persistent data space it will also get removed.
	 *
	 * @param c The clan to delete.
	 * @return true if the clan was able to be removed.
	 */
	public synchronized boolean delete(Clan c) {
		try {
			for (ClanAssociate associate : c.getMembers()) {
				if (!associate.getPlayer().getUniqueId().equals(c.getOwner().getPlayer().getUniqueId())) {
					associate.kick();
				}
			}
			if (ClansAPI.getData().getClanFile(c).exists()) {
				ClansAPI.getData().getClanFile(c).delete();
			}
			return CLANS.remove(c);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Clears the clan and associate cache base and reloads from file.
	 */
	public synchronized void refresh() {
		for (Clan c : CLANS) {
			try {
				c.save();
			} catch (Exception ignored) {
			}
		}
		CLANS.clear();
		for (String clanID : Clan.ACTION.getAllClanIDs()) {
			DefaultClan instance = new DefaultClan(clanID);
			ClansAPI.getInstance().getClanManager().load(instance);
		}

	}

}
