package com.github.sanctum.clans.events.core.bank;

import com.github.sanctum.clans.construct.api.ClanBank;
import java.math.BigDecimal;
import org.bukkit.event.Cancellable;

public class BankSetBalanceEvent extends BankActionEvent implements Cancellable {

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
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
