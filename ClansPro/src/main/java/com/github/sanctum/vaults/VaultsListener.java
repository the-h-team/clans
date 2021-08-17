package com.github.sanctum.vaults;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.vaults.events.VaultInteractEvent;
import com.github.sanctum.vaults.events.VaultOpenEvent;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class VaultsListener implements Listener {


	@EventHandler(priority = EventPriority.NORMAL)
	public void onSignUse(PlayerInteractEvent event) {
		Player p = event.getPlayer();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			try {
				Material mat = event.getClickedBlock().getType();
				if (mat.name().toLowerCase().contains("sign")) {
					Sign sign = (Sign) event.getClickedBlock().getState();
					if (sign.getLine(1).equals(Clan.ACTION.color("&0+-&7[&3&lVault&7]&0-+"))) {
						String clan = ChatColor.stripColor(sign.getLine(2));
						if (Clan.ACTION.getAllClanNames().contains(clan)) {
							if (ClansAPI.getInstance().getClaimManager().isInClaim(p.getLocation())) {
								Claim claim = Claim.from(p.getLocation());
								assert claim != null;
								if (ClansAPI.getInstance().isInClan(p.getUniqueId()) && !ClansAPI.getInstance().getClanID(p.getUniqueId()).toString().equals(ClansAPI.getInstance().getClanID(clan))) {
									if (!claim.getClan().getAllyList().contains(ClansAPI.getInstance().getClanID(p.getUniqueId()).toString())) {
										return;
									}
								} else {
									if (!Arrays.asList(claim.getClan().getMemberIds()).contains(p.getName())) {
										return;
									}
								}
							}
							String name = ClansAPI.getInstance().getClanName(ClansAPI.getInstance().getClanID(clan));
							Inventory pull = VaultContainer.getVault(name);
							VaultOpenEvent e = new VaultOpenEvent(ClansAPI.getInstance().getClan(clan), p, pull, pull.getViewers());
							Bukkit.getPluginManager().callEvent(e);
							if (!e.isCancelled()) {
								e.open();
							}
						} else {
							Clan.ACTION.sendMessage(p, "&c&oThis vault has since been abandoned...");
						}
					}
				}
			} catch (NullPointerException e) {
				ClansAPI.getInstance().getPlugin().getLogger().severe(String.format("[%s] - There was a problem while using a vault sign.", ClansAPI.getInstance().getPlugin().getDescription().getName()));
				e.printStackTrace();
			}
		}

	}

	@EventHandler
	public void onManagedInventoryClick(final InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		for (Clan c : ClansAPI.getInstance().getClanManager().getClans().list()) {
			Inventory i = VaultContainer.getVault(c.getName());
			if (i.equals(e.getClickedInventory())) {
				(new BukkitRunnable() {
					public void run() {
						// do stuff
						VaultInteractEvent event = new VaultInteractEvent(c, p, e.getCurrentItem(), e.getView(), e.getViewers());
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
			Inventory i = VaultContainer.getVault(c.getName());
			if (i.equals(e.getInventory())) {
				saveInventory(c.getId().toString(), i);
				break;
			}
		}
	}


	private void saveInventory(String clanID, Inventory inv) {
		Clan c = ClansAPI.getInstance().getClan(clanID);
		int size = c.setValue("vault", inv.getContents()).length;
	}

	public static ItemStack[] getInventoryContents(String clanID) {
		Clan c = ClansAPI.getInstance().getClan(clanID);
		ItemStack[] content = new ItemStack[54];
		ItemStack[] copy = c.getValue(ItemStack[].class, "vault");
		if (copy != null) {
			System.arraycopy(copy, 0, content, 0, 54);
		}
		return content;
	}


}
