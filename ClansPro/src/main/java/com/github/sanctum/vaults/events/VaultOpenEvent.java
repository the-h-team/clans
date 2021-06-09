package com.github.sanctum.vaults.events;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class VaultOpenEvent extends ClanEventBuilder implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Clan owner;

	private final Player opener;

	private final Inventory inventory;

	private final List<HumanEntity> viewers;

	private boolean cancelled;

	public VaultOpenEvent(Clan owner, Player opener, Inventory inventory, List<HumanEntity> viewers) {
		this.owner = owner;
		this.opener = opener;
		this.inventory = inventory;
		this.viewers = viewers;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public ClanAction getUtil() {
		return DefaultClan.action;
	}

	@Override
	public StringLibrary stringLibrary() {
		return DefaultClan.action;
	}

	public Clan getOwner() {
		return owner;
	}

	public List<HumanEntity> getViewers() {
		return viewers;
	}

	public Inventory getInventory() {
		return inventory;
	}

	public Player getOpener() {
		return opener;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	public void open() {
		opener.openInventory(inventory);
	}

}
