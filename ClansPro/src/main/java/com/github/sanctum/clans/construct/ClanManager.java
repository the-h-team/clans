package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.data.DataManager;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.HUID;
import java.util.LinkedList;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class ClanManager {

	public LinkedList<Clan> getClans() {
		return ClansAPI.getData().CLANS;
	}

	/**
	 * Load a custom implementation of a clan into cache.
	 *
	 * @param c The clan object to load into cache.
	 * @return true if the clan successfully got added to cache.
	 */
	public boolean load(Clan c) {
		if (c == null) throw new IllegalArgumentException("The provided clan cannot be null!");

		if (ClansAPI.getData().CLANS.stream().noneMatch(cl -> cl.getName().equals(c.getName()))) {
			ClansAPI.getData().CLANS.add(c);
			return true;
		}

		return false;
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
			for (ClanAssociate associate : c.getMembers().list()) {
				if (!associate.getPlayer().getUniqueId().equals(c.getOwner())) {
					associate.kick();
				}
			}
			ClansAPI.getData().get(c.getOwner()).getConfig().set("Clan.id", null);
			ClansAPI.getData().get(c.getOwner()).saveConfig();

			if (ClansAPI.getData().getClanFile(c).exists()) {
				ClansAPI.getData().getClanFile(c).delete();
			}
			return ClansAPI.getData().CLANS.remove(c);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Clears the clan and associate cache base and reloads from file.
	 */
	public synchronized void refresh() {

		ClansAPI.getData().CLANS.clear();
		ClansAPI.getData().ASSOCIATES.clear();

		for (String clanID : DefaultClan.action.getAllClanIDs()) {
			DefaultClan instance = new DefaultClan(clanID);
			ClansAPI.getInstance().getClanManager().load(instance);
		}

		ClansPro.getInstance().getLogger().info("- Reacquiring user data.");
		for (OfflinePlayer pl : Bukkit.getOfflinePlayers()) {

			HUID id = null;

			FileManager user = ClansAPI.getData().get(pl.getUniqueId());
			if (user.exists()) {
				if (user.getConfig().getString("Clan.id") != null) {
					if (Objects.requireNonNull(user.getConfig().getString("Clan.id")).length() != 14) {
						throw new UnsupportedOperationException("[ClansPro] - Clan ID " + user.getConfig().getString("Clan.id") + " invalid, expected format ####-####-####", new Throwable(user.getConfig().getString("Clan.id")));
					}
					FileManager clan = DataManager.FileType.CLAN_FILE.get(user.getConfig().getString("Clan.id"));
					if (!clan.exists()) {
						user.getConfig().set("Clan", null);
						user.saveConfig();
					}
					id = HUID.fromString(Objects.requireNonNull(user.getConfig().getString("Clan.id")));
				}
			}

			if (id != null) {
				if (!ClansPro.getInstance().getAssociate(pl).isPresent()) {
					ClansAPI.getData().ASSOCIATES.add(new ClanAssociate(pl));
				}
			}
		}

	}

}
