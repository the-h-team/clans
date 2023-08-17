package com.github.sanctum.clans.construct.bank.frontend;

import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.bank.BankBackend;
import com.github.sanctum.clans.event.bank.BankPreTransactionEvent;
import com.github.sanctum.clans.event.bank.BankTransactionEvent;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.event.LabyrinthVentCall;
import java.math.BigDecimal;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The new bank implementation.
 *
 * @since 1.3.3
 */
public class BankImpl implements ClanBank {
    final String clanId;
    final BankBackend backend;

    BankImpl(@NotNull String clanId, @NotNull BankBackend backend) {
        this.clanId = clanId;
        this.backend = backend;
    }

    @Override // TODO: remove player from this signature
    public boolean deposit(Player player, BigDecimal amount) {
        if (!backend.readIsDisabled().join()) return false;
        if (amount.signum() != 1) return false;
        return new LabyrinthVentCall<>(new BankPreTransactionEvent(
                player,
                this,
                amount,
                clanId,
                EconomyProvision.getInstance().has(amount, player, player.getWorld().getName()).orElse(false),
                BankTransactionEvent.Type.DEPOSIT
        )).run().isSuccess();
    }

    @Override // TODO: remove player from this signature
    public boolean withdraw(Player player, BigDecimal amount) {
        if (!backend.readIsDisabled().join()) return false;
        if (amount.signum() != 1) return false;
        return new LabyrinthVentCall<>(new BankPreTransactionEvent(
                player,
                this,
                amount,
                clanId,
                EconomyProvision.getInstance().isValid(),
                BankTransactionEvent.Type.WITHDRAWAL
        )).run().isSuccess();
    }

    @Override
    public boolean has(BigDecimal amount) {
        return backend.compareBalance(amount).join() >= 0;
    }

    @Override
    public BigDecimal getBalance() {
        return backend.readBalance().join();
    }

    @Override
    public boolean setBalance(BigDecimal newBalance) {
        backend.updateBalance(newBalance).join();
        return true; // TODO: remove boolean return + default impl on super
    }

    public void setEnabled(boolean enabled) {
        backend.updateIsDisabled(enabled).join();
    }
}
