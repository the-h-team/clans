package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.util.InteractionType;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClaimInteractEvent extends ClanEventBuilder {

    private final Player p;

    private final InteractionType type;

    private final Location location;

    public ClaimInteractEvent(Player p, Location location, InteractionType type) {
        this.p = p;
        this.location = location;
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public InteractionType getInteraction() {
        return type;
    }

    public Player getPlayer() {
        return p;
    }

    public Claim getClaim() {
        return Claim.from(location);
    }

    public Block getBlock() {
        return location.getBlock();
    }

    public ItemStack getItemInMainHand() {
        return p.getInventory().getItemInMainHand();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}
