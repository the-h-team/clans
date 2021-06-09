package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClaimAction;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.util.InteractionType;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ClaimInteractEvent extends ClanEventBuilder {

    private static final HandlerList handlers = new HandlerList();

    private final Player p;

    private final InteractionType type;

    private final Location location;

    private boolean cancelled;

    public ClaimInteractEvent(Player p, Location location, InteractionType type) {
        this.p = p;
        this.location = location;
        this.type = type;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public ClaimAction getClaimUtil() {
        return Claim.action;
    }

    @Override
    public ClanAction getUtil() {
        return DefaultClan.action;
    }

    @Override
    public StringLibrary stringLibrary() {
        return DefaultClan.action;
    }

    public InteractionType getType() {
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

}
