package com.github.sanctum.clans.bridge.internal.stashes.events;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.events.ClanEventBuilder;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class StashOpenEvent extends ClanEventBuilder {

	private final Clan owner;
	private final Player opener;
	private final Menu inventory;
	private final List<HumanEntity> viewers;

	public StashOpenEvent(Clan owner, Player opener, Menu inventory, List<HumanEntity> viewers) {
		this.owner = owner;
		this.opener = opener;
		this.inventory = inventory;
		this.viewers = viewers;
	}

	public Clan getOwner() {
		return owner;
	}

	public List<HumanEntity> getViewers() {
		return viewers;
	}

	public Menu getInventory() {
		return inventory;
	}

	public Player getOpener() {
		return opener;
	}

	public void open() {
		getInventory().open(opener);
	}

}
