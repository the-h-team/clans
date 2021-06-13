package com.github.sanctum.stashes;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.DefaultClan;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class StashContainer {

	private static final Map<String, Inventory> inventories = new HashMap<>();

	public static Inventory getStash(String clanName) {
		return inventories.computeIfAbsent(clanName, name -> {
			Inventory inventory = Bukkit.createInventory(null, 9, DefaultClan.action.color("&3&oStash: &0" + name));
			inventory.setContents(StashListener.getInventoryContents(DefaultClan.action.getClanID(name)));
			return inventory;
		});
	}

	public static void updateContents(Inventory inventory, ItemStack[] newContents) {
		(new BukkitRunnable() {
			public void run() {
				// do stuff
				for (Map.Entry<String, Inventory> entry : inventories.entrySet()) {
					if (entry.getValue().equals(inventory)) {
						entry.getValue().setContents(newContents);
						break;
					}
				}
			}
		}).runTask(ClansPro.getInstance());
	}

	public static void removeFromCache(Inventory inventory) {
		(new BukkitRunnable() {
			public void run() {
				// do stuff
				inventories.entrySet().stream()
						.filter(entry -> entry.getValue().equals(inventory))
						.map(Map.Entry::getKey)
						.forEach(inventories::remove);
			}
		}).runTask(ClansPro.getInstance());
	}

}