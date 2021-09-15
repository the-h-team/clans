package com.github.sanctum.clans.bridge.internal.vaults;

import com.github.sanctum.clans.construct.api.ClansAPI;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class VaultContainer {

	private static final Map<String, Inventory> inventories = new HashMap<>();

	public static Inventory getVault(String clanName) {
		return inventories.computeIfAbsent(clanName, name -> {
			Inventory inventory = Bukkit.createInventory(null, 54, name);
			inventory.setContents(VaultsListener.getInventoryContents(ClansAPI.getInstance().getClanID(name)));
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
		}).runTask(ClansAPI.getInstance().getPlugin());
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
		}).runTask(ClansAPI.getInstance().getPlugin());
	}

}
