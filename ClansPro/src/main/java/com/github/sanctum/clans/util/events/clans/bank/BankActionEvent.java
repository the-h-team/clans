package com.github.sanctum.clans.util.events.clans.bank;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClansAPI;
import java.util.concurrent.CompletableFuture;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class BankActionEvent extends BankEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    protected final String clanId;

    protected BankActionEvent(ClanBank clanBank, String clanId) {
        super(clanBank, false);
        this.clanId = clanId;
    }

    /**
     * Get the direct clanId for this event
     *
     * @return clanId as String
     */
    public String getClanId() {
        return clanId;
    }

    /**
     * Get the clan associated with this bank event
     *
     * @return the Clan whose bank this is
     */
    public DefaultClan getClan() {
        return CompletableFuture.supplyAsync(() -> (DefaultClan) ClansAPI.getInstance().getClan(clanId)).join();
    }

    @Override
    public @NotNull
    HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
