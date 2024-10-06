package com.github.sanctum.clans.model.addon.vaults.events;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.event.ClanEvent;
import com.github.sanctum.labyrinth.gui.unity.construct.Menu;
import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class VaultOpenEvent extends ClanEvent {

	private final Clan owner;
	private final Player opener;
	private final Menu inventory;
	private final List<HumanEntity> viewers;

	public VaultOpenEvent(Clan owner, Player opener, Menu inventory, List<HumanEntity> viewers) {
		super(false);
		this.owner = owner;
		this.opener = opener;
		this.inventory = inventory;
		this.viewers = viewers;
		// logo gallery
		// Claim flag abstraction
	}

	public Clan getClan() {
		return owner;
	}

	public List<HumanEntity> getViewers() {
		return viewers;
	}

	public Menu getVault() {
		return inventory;
	}

	public Player getOpener() {
		return opener;
	}

	public void open() {
		getVault().open(opener);
	}

}
