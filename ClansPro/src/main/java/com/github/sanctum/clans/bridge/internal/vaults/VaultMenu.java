package com.github.sanctum.clans.bridge.internal.vaults;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.bridge.internal.vaults.events.VaultInteractEvent;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.service.Check;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import com.github.sanctum.labyrinth.gui.unity.impl.InventoryElement;
import com.github.sanctum.labyrinth.gui.unity.impl.PreProcessElement;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VaultMenu extends Menu {

	public VaultMenu(String clanName) {
		super(ClansAPI.getInstance().getPlugin(), clanName, Rows.SIX, Type.SINGULAR, Property.SHAREABLE, Property.CACHEABLE);
		addElement(new InventoryElement.Shared(clanName, this));
		this.key = clanName;
		this.close = close -> {
			Clan c = ClansAPI.getInstance().getClan(ClansAPI.getInstance().getClanID(clanName));
			c.setValue("vault", close.getMain().getContents(), false);
		};

		this.click = click -> {
			Clan c = ClansAPI.getInstance().getClan(ClansAPI.getInstance().getClanID(clanName));
			VaultInteractEvent event = ClanVentBus.call(new VaultInteractEvent(c, click.getElement(), click.getParent().getElement(), click.getAttachment(), click.getParent().getParent().getElement().getViewers()));
			if (event.isCancelled()) {
				click.setCancelled(true);
			}
		};

		for (ItemStack it : getInventoryContents(Check.forNull(ClansAPI.getInstance().getClanID(clanName), "Clan '" + clanName + "' not found!"))) {
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

	ItemStack[] getInventoryContents(String clanID) {
		Clan c = ClansAPI.getInstance().getClan(clanID);
		ItemStack[] content = new ItemStack[54];
		ItemStack[] copy = c.getValue(ItemStack[].class, "vault");
		if (copy != null) {
			System.arraycopy(copy, 0, content, 0, 54);
		}
		return content;
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
