package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import java.util.LinkedList;

public class ClanManager {

	public LinkedList<Clan> getClans() {
		return ClansAPI.getData().CLANS;
	}

	public boolean load(Clan c) {
		if (c == null) throw new IllegalArgumentException("The provided clan cannot be null!");

		if (ClansAPI.getData().CLANS.stream().noneMatch(cl -> cl.getName().equals(c.getName()))) {
			ClansAPI.getData().CLANS.add(c);
			return true;
		}
		return false;
	}

	public boolean delete(Clan c) {
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

}
