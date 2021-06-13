package com.github.sanctum.clans.util.events.damage;

import com.github.sanctum.clans.util.events.ClanEventBuilder;
import org.bukkit.entity.Player;

public class PlayerKillPlayerEvent extends ClanEventBuilder {

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
    public String getName() {
        return getClass().getSimpleName();
    }
}
