package com.github.sanctum.clans.bridge.internal.vaults;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.internal.VaultsAddon;
import com.github.sanctum.clans.bridge.internal.vaults.events.VaultInteractEvent;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
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

public class VaultMenu extends Menu {

	private final Clan c;

	public VaultMenu(String clanName) {
		super(ClansAPI.getInstance().getPlugin(), clanName, Rows.SIX, Type.SINGULAR, Property.SHAREABLE, Property.CACHEABLE);
		this.c = ClansAPI.getInstance().getClanManager().getClan(ClansAPI.getInstance().getClanManager().getClanID(clanName));
		if (this.c != null) {
			addElement(new InventoryElement.Shared(clanName, this));
			this.key = clanName;
			this.close = close -> {
				FileManager manager = ClanAddon.getAddon(VaultsAddon.class).getFile(Configurable.Type.JSON, "data");
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
				VaultInteractEvent event = ClanVentBus.call(new VaultInteractEvent(c, click.getElement(), click.getParent().getElement(), click.getAttachment(), click.getAction(), click.getClickType(), click.getParent().getParent().getElement().getViewers()));
				if (event.isCancelled()) {
					click.setCancelled(true);
				}
			};
			for (ItemStack it : getInventoryContents(Check.forNull(c.getId(), "Clan '" + clanName + "' not found!"))) {
				if (Objects.equals(it, null)) {
					getInventory().getElement().addItem(new ItemStack(Material.AIR));
				} else {
					getInventory().getElement().addItem(it);
				}
			}
			try {
				registerController();
			} catch (Exception ignored) {
			}
		}
	}

	ItemStack[] getInventoryContents(HUID id) {
		List<ItemStack> list = new ArrayList<>(54);
		ClanAddon vaults = ClanAddon.getAddon(VaultsAddon.class);
		if (vaults != null) {
			FileManager manager = vaults.getFile(Configurable.Type.JSON, "data");
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
