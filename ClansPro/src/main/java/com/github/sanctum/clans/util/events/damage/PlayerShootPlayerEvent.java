package com.github.sanctum.clans.util.events.damage;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerShootPlayerEvent extends ClanEventBuilder {

    private static final HandlerList handlers = new HandlerList();

    private final Player shooter;

    private final Player shot;

    private boolean cancelled;

    public PlayerShootPlayerEvent(Player p, Player target) {
        this.shooter = p;
        this.shot = target;
    }

    public Player getShooter() {
        return shooter;
    }

    public Player getShot() {
        return shot;
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

    public boolean canHurt() {
        return cancelled;
    }

    public void setCanHurt(boolean b) {
        if (b)
            this.cancelled = false;
        if (!b)
            this.cancelled = true;
    }
}
