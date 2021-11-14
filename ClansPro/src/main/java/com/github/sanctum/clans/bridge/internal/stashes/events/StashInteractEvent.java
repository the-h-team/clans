package com.github.sanctum.clans.bridge.internal.stashes.events;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.event.ClanEvent;
import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class StashInteractEvent extends ClanEvent {

	private final Clan owner;
	private final Player whoClicked;
	private final InventoryView view;
	private final InventoryAction action;
	private final List<HumanEntity> viewers;
	private final ItemStack clickedItem;
	private final ClickType type;

	public StashInteractEvent(Clan owner, Player whoClicked, ItemStack clickedItem, InventoryView view, InventoryAction action, ClickType type, List<HumanEntity> viewers) {
		super(false);
		this.owner = owner;
		this.type = type;
		this.action = action;
		this.whoClicked = whoClicked;
		this.clickedItem = clickedItem;
		this.view = view;
		this.viewers = viewers;
	}

	@Override
	public Clan getClan() {
		return owner;
	}

	public InventoryAction getAction() {
		return action;
	}

	public ClickType getType() {
		return type;
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
