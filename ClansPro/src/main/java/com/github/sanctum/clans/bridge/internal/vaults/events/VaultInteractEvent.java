package com.github.sanctum.clans.bridge.internal.vaults.events;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.events.ClanEventBuilder;
import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class VaultInteractEvent extends ClanEventBuilder {

	private final Clan owner;
	private final Player whoClicked;
	private final InventoryView view;
	private final List<HumanEntity> viewers;
	private final ItemStack clickedItem;

	public VaultInteractEvent(Clan owner, Player whoClicked, ItemStack clickedItem, InventoryView view, List<HumanEntity> viewers) {
		this.owner = owner;
		this.whoClicked = whoClicked;
		this.clickedItem = clickedItem;
		this.view = view;
		this.viewers = viewers;
	}

	public Clan getOwner() {
		return owner;
	}

	public InventoryView getView() {
		return view;
	}

	public ItemStack getClickedItem() {
		return clickedItem;
	}

	public Player getWhoClicked() {
		return whoClicked;
	}

	public List<HumanEntity> getViewers() {
		return viewers;
	}
}
