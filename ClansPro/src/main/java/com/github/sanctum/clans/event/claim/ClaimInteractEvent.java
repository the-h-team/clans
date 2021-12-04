package com.github.sanctum.clans.event.claim;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a user interacts with a clan claim.
 */
public class ClaimInteractEvent extends ClaimEvent {

	private Block b;

	private final Type type;

	private final Location location;

	public ClaimInteractEvent(Player p, Location location, Type type) {
		super(ClaimEvent.dummy, p.getUniqueId(), ClansAPI.getInstance().getClaimManager().getClaim(location));
		this.location = location;
		this.type = type;
	}

	public ClaimInteractEvent(Player p, Block block, Location location, Type type) {
		super(ClaimEvent.dummy, p.getUniqueId(), ClansAPI.getInstance().getClaimManager().getClaim(location));
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

	public Block getBlock() {
		return this.b != null ? this.b : location.getBlock();
	}

	public ItemStack getItemInMainHand() {
		return getPlayer().getInventory().getItemInMainHand();
	}

	@Override
	public @Nullable Clan.Associate getAssociate() {
		return ClansAPI.getInstance().getAssociate(getPlayer()).orElse(super.getAssociate());
	}

	@Override
	public Clan getClan() {
		return ((Clan)getClaim().getHolder());
	}

	public enum Type {

		BUILD, BREAK, USE

	}
}
