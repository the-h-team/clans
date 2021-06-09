package com.github.sanctum.clans.util.events.clans;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RaidShieldEvent extends ClanEventBuilder implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private int on = ClansAPI.getData().getMain().getConfig().getInt("Clans.raid-shield.up-time");

    private int off = ClansAPI.getData().getMain().getConfig().getInt("Clans.raid-shield.down-time");

    private String shieldOn = "{0} &a&lRAID SHIELD ENABLED";

    private String shieldOff = "{0} &c&lRAID SHIELD DISABLED";

    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public int getStartTime() {
        return on;
    }

    public int getStopTime() {
        return off;
    }

    public String getShieldOn() {
        return shieldOn;
    }

    public String getShieldOff() {
        return shieldOff;
    }

    public boolean shieldOn() {
        return ClansAPI.getInstance().getShieldManager().isEnabled();
    }

    public void setShieldOn(String shieldOn) {
        this.shieldOn = shieldOn;
    }

    public void setShieldOff(String shieldOff) {
        this.shieldOff = shieldOff;
    }

    public void setStartTime(int i) {
        this.on = i;
    }

    public void setStopTime(int i) {
        this.off = i;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
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
