package com.github.sanctum.clans.events.core;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.events.ClanEventBuilder;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClaimInteractEvent extends ClanEventBuilder {

	private Block b;

	private final Player p;

	private final Type type;

	private final Location location;

	public ClaimInteractEvent(Player p, Location location, Type type) {
		this.p = p;
		this.location = location;
		this.type = type;
	}

	public ClaimInteractEvent(Player p, Block block, Location location, Type type) {
		this.p = p;
		this.b = block;
		this.location = location;
		this.type = type;
	}

	public Location getLocation() {
		return location;
	}

	public Type getInteraction() {
		return type;
	}

	public Player getPlayer() {
		return p;
	}

	public Claim getClaim() {
		return Claim.from(location);
	}

	public Block getBlock() {
		return this.b != null ? this.b : location.getBlock();
	}

	public ItemStack getItemInMainHand() {
		return p.getInventory().getItemInMainHand();
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	public enum Type {

		BUILD, BREAK, USE

	}
}
