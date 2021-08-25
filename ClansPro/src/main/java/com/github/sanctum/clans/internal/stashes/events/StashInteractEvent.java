package com.github.sanctum.clans.internal.stashes.events;

import com.github.sanctum.clans.construct.api.Clan;
import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StashInteractEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Clan owner;

	private final Player whoClicked;

	private final InventoryView view;

	private final List<HumanEntity> viewers;

	private final ItemStack clickedItem;

	private boolean cancelled;

	public StashInteractEvent(Clan owner, Player whoClicked, ItemStack clickedItem, InventoryView view, List<HumanEntity> viewers) {
		this.owner = owner;
		this.whoClicked = whoClicked;
		this.clickedItem = clickedItem;
		this.view = view;
		this.viewers = viewers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
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

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}
