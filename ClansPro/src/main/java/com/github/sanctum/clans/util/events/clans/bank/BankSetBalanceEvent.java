package com.github.sanctum.clans.util.events.clans.bank;

import com.github.sanctum.clans.construct.api.ClanBank;
import java.math.BigDecimal;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BankSetBalanceEvent extends BankActionEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final BigDecimal newBalance;
    private boolean cancelled = false;

    public BankSetBalanceEvent(ClanBank clanBank, String clanId, BigDecimal newBalance) {
        super(clanBank, clanId);
        this.newBalance = newBalance;
    }

    /**
     * Get the potential new balance
     *
     * @return the desired balance as a BigDecimal
     */
    public BigDecimal getNewBalance() {
        return newBalance;
    }

    /**
     * Get the potential new balance
     *
     * @return the desired balance as a double
     */
    public double getNewBalanceAsDouble() {
        return newBalance.doubleValue();
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
