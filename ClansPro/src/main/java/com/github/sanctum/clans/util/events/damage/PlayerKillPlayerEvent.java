package com.github.sanctum.clans.util.events.damage;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerKillPlayerEvent extends ClanEventBuilder {

    private static final HandlerList handlers = new HandlerList();

    private final Player killer;

    private final Player victim;

    private boolean keepInventory;

    private boolean clearDrops;

    public PlayerKillPlayerEvent(Player p, Player target) {
        this.killer = p;
        this.victim = target;
    }

    public Player getKiller() {
        return killer;
    }

    public Player getVictim() {
        return victim;
    }

    public boolean isClearDrops() {
        return clearDrops;
    }

    public void setClearDrops(boolean clearDrops) {
        this.clearDrops = clearDrops;
    }

    public void setKeepInventory(boolean keepInventory) {
        this.keepInventory = keepInventory;
    }

    public boolean isKeepInventory() {
        return keepInventory;
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
        return new StringLibrary();
    }
}
