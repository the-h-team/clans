package com.github.sanctum.stashes;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.stashes.events.StashInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class StashListener implements Listener {

	@EventHandler
	public void onManagedInventoryClick(final InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		for (Clan c : ClansAPI.getInstance().getClanManager().getClans().list()) {
			Inventory i = StashContainer.getStash(c.getName());
			if (i.equals(e.getClickedInventory())) {
				(new BukkitRunnable() {
					public void run() {
						// do stuff
						StashInteractEvent event = new StashInteractEvent(c, p, e.getCurrentItem(), e.getView(), e.getViewers());
						Bukkit.getPluginManager().callEvent(event);
						if (event.isCancelled()) {
							e.setCancelled(true);
						}
					}
				}).runTask(ClansAPI.getInstance().getPlugin());
				break;
			}
		}
	}

	@EventHandler
	public void onManagedInventoryClose(InventoryCloseEvent e) {
		for (Clan c : ClansAPI.getInstance().getClanManager().getClans().list()) {
			Inventory i = StashContainer.getStash(c.getName());
			if (i.equals(e.getInventory())) {
				saveInventory(c.getId().toString(), i);
				break;
			}
		}
	}

	private void saveInventory(String clanID, Inventory inv) {
		Clan c = ClansAPI.getInstance().getClan(clanID);
		c.setValue("stash", inv.getContents());
	}

	public static ItemStack[] getInventoryContents(String clanID) {
		Clan c = ClansAPI.getInstance().getClan(clanID);
		ItemStack[] content = new ItemStack[9];
		ItemStack[] copy = c.getValue(ItemStack[].class, "stash");
		if (copy != null) {
			System.arraycopy(copy, 0, content, 0, 9);
		}
		return content;
	}

}
