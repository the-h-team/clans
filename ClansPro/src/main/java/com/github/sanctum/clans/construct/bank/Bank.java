package com.github.sanctum.clans.construct.bank;

import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.util.events.clans.bank.BankPreTransactionEvent;
import com.github.sanctum.clans.util.events.clans.bank.BankSetBalanceEvent;
import com.github.sanctum.clans.util.events.clans.bank.BankTransactionEvent;
import com.github.sanctum.labyrinth.data.AdvancedHook;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.data.VaultHook;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public final class Bank implements ClanBank, Serializable {
    private static final long serialVersionUID = -153639291829056195L;
    protected static final PluginManager PM = Bukkit.getServer().getPluginManager();
    protected BigDecimal balance;
    protected boolean enabled;
    protected final String clanId;

    public Bank(@NotNull String clanId) {
        this.balance = API.defaultImpl.startingBalance();
        this.enabled = true;
        this.clanId = clanId;
    }

    @Override
    public boolean deposit(Player player, BigDecimal amount) {
        if (!enabled) return false;
        if (amount.signum() != 1) return false;
        final boolean has;
        Optional<Boolean> opt = EconomyProvision.getInstance().has(amount, player, player.getWorld().getName());

        has = opt.orElse(false);
        final BankPreTransactionEvent preTransactionEvent =
                new BankPreTransactionEvent(player, this, amount, clanId, has, BankTransactionEvent.Type.DEPOSIT);
        PM.callEvent(preTransactionEvent);
        return preTransactionEvent.isSuccess();
    }

    @Override
    public boolean withdraw(Player player, BigDecimal amount) {
        if (!enabled) return false;
        if (amount.signum() != 1) return false;
        final BankPreTransactionEvent preTransactionEvent;
        boolean hasWalletAccount = false;

        if (VaultHook.getEconomy() != null) {
            hasWalletAccount = VaultHook.getEconomy().hasAccount(player);
        } else {
            if (AdvancedHook.getEconomy() != null) {
                hasWalletAccount = AdvancedHook.getEconomy().hasWalletAccount(player);
            }
        }

        preTransactionEvent = new BankPreTransactionEvent(player, this, amount, clanId, has(amount) && hasWalletAccount,
                BankTransactionEvent.Type.WITHDRAWAL);
        PM.callEvent(preTransactionEvent);
        return preTransactionEvent.isSuccess();
    }

    @Override
    public boolean has(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    @Override
    public double getBalanceDouble() {
        return balance.doubleValue();
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public boolean setBalance(BigDecimal newBalance) {
        ClanBank.super.setBalance(newBalance);
        final BankSetBalanceEvent event = new BankSetBalanceEvent(this, clanId, newBalance);
        PM.callEvent(event);
        return !event.isCancelled();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
