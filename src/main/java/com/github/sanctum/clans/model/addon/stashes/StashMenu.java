package com.github.sanctum.clans.model.addon.stashes;

import com.github.sanctum.clans.model.ClanVentBus;
import com.github.sanctum.clans.model.addon.StashesAddon;
import com.github.sanctum.clans.model.addon.stashes.events.StashInteractEvent;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.labyrinth.data.DataTable;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.impl.InventoryElement;
import com.github.sanctum.labyrinth.gui.unity.impl.PreProcessElement;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.util.Check;
import com.github.sanctum.panther.util.HUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StashMenu extends Menu {

	final Clan c;

	public StashMenu(String clanName) {
		super(ClansAPI.getInstance().getPlugin(), clanName, Rows.ONE, Type.SINGULAR, Property.SHAREABLE, Property.CACHEABLE);
		c = ClansAPI.getInstance().getClanManager().getClan(ClansAPI.getInstance().getClanManager().getClanID(clanName));
		if (this.c != null) {
			addElement(new InventoryElement.Shared(clanName, this));
			this.key = clanName + "-stash";
			this.close = close -> {
				FileManager manager = Clan.Addon.getAddon(StashesAddon.class).getFile(Configurable.Type.JSON, "data");
				Node keys = manager.getRoot().getNode(c.getId().toString());
				keys.delete();
				int key = 0;
				DataTable table = DataTable.newTable();
				for (ItemStack item : close.getMain().getContents()) {
					if (item != null && item.getType() != Material.AIR) {
						table.set(c.getId() + "." + key, item);
						key++;
					}
				}
				manager.write(table, true);
			};

			this.click = click -> {
				StashInteractEvent event = ClanVentBus.call(new StashInteractEvent(c, click.getElement(), click.getParent().getElement(), click.getAttachment(), click.getAction(), click.getClickType(), click.getParent().getParent().getElement().getViewers()));
				if (event.isCancelled()) {
					click.setCancelled(true);
				}
			};

			if (oldExists(c.getId())) {
				for (ItemStack it : getInventoryContentsOld(Check.forNull(c.getId(), "Clan '" + clanName + "' not found!"))) {
					if (Objects.equals(it, null)) {
						getInventory().getElement().addItem(new ItemStack(Material.AIR));
					} else {
						getInventory().getElement().addItem(it);
					}
				}
				c.removeValue("stash");
			} else {
				for (ItemStack it : getInventoryContentNew(Check.forNull(c.getId(), "Clan '" + clanName + "' not found!"))) {
					if (Objects.equals(it, null)) {
						getInventory().getElement().addItem(new ItemStack(Material.AIR));
					} else {
						getInventory().getElement().addItem(it);
					}
				}
			}
			try {
				registerController();
			} catch (Exception ignored) {
			}
		}
	}

	boolean oldExists(HUID id) {
		Clan c = ClansAPI.getInstance().getClanManager().getClan(id);
		return c.getValue(ItemStack[].class, "stash") != null;
	}

	ItemStack[] getInventoryContentsOld(HUID clanID) {
		Clan c = ClansAPI.getInstance().getClanManager().getClan(clanID);
		ItemStack[] content = new ItemStack[9];
		ItemStack[] copy = c.getValue(ItemStack[].class, "stash");
		if (copy != null) {
			System.arraycopy(copy, 0, content, 0, 9);
		}
		return content;
	}

	ItemStack[] getInventoryContentNew(HUID id) {
		List<ItemStack> list = new ArrayList<>(9);
		Clan.Addon stashes = Clan.Addon.getAddon(StashesAddon.class);
		if (stashes != null) {
			FileManager manager = stashes.getFile(Configurable.Type.JSON, "data");
			Node keys = manager.getRoot().getNode(id.toString());
			for (String index : keys.getKeys(false)) {
				ItemStack item = keys.getNode(index).get(ItemStack.class);
				list.add(item);
			}
		}
		return list.toArray(new ItemStack[0]);
	}

	@Override
	public InventoryElement getInventory() {
		return getElement(e -> e instanceof InventoryElement);
	}

	@Override
	public void open(Player player) {
		if (this.process != null) {
			PreProcessElement element = new PreProcessElement(this, player, player.getOpenInventory());
			this.process.apply(element);
		}
		getInventory().open(player);
	}
}
