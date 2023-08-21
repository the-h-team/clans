package com.github.sanctum.clans.construct.bank;

import com.github.sanctum.clans.construct.api.BanksAPI;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.bank.BankPreTransactionEvent;
import com.github.sanctum.clans.event.bank.BankTransactionEvent;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.event.LabyrinthVentCall;
import java.math.BigDecimal;

import com.github.sanctum.labyrinth.interfacing.Nameable;
import com.github.sanctum.panther.util.HUID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BankImpl implements ClanBank {
    final HUID clanId;

    BankImpl(@NotNull HUID clanId) {
        this.clanId = clanId;
    }

    @Override
    public boolean deposit(@NotNull BigDecimal amount, Nameable entity) {
        if (!getBackend().readIsDisabled(getClan()).join()) throw new DisabledException(clanId.toString());
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("amount cannot be negative");
        boolean success = true;
        if (entity instanceof Clan.Associate && ((Clan.Associate) entity).isPlayer()) {
            final Player p = ((Clan.Associate) entity).getAsPlayer().getPlayer();
            //noinspection DataFlowIssue
            success = EconomyProvision.getInstance().has(amount, p, p.getWorld().getName()).orElse(false);
        }
        return new LabyrinthVentCall<>(new BankPreTransactionEvent(
                this,
                amount,
                entity,
                success,
                BankTransactionEvent.Type.DEPOSIT
        )).run().isSuccess();
    }

    @Override
    public boolean withdraw(@NotNull BigDecimal amount, Nameable entity) {
        if (!getBackend().readIsDisabled(getClan()).join()) throw new DisabledException(clanId.toString());
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("amount cannot be negative");
        return new LabyrinthVentCall<>(new BankPreTransactionEvent(
                this,
                amount,
                entity,
                EconomyProvision.getInstance().isValid(),
                BankTransactionEvent.Type.WITHDRAWAL
        )).run().isSuccess();
    }

    @Override
    public boolean has(@NotNull BigDecimal amount) {
        return getBackend().compareBalance(getClan(), amount).join() >= 0;
    }

    @Override
    public @NotNull BigDecimal getBalance() {
        return getBackend().readBalance(getClan()).join();
    }

    @Override
    public boolean setBalance(@NotNull BigDecimal newBalance) {
        if (!getBackend().readIsDisabled(getClan()).join()) throw new DisabledException(clanId.toString());
        getBackend().updateBalance(getClan(), newBalance).join();
        return true;
    }

    @Override
    public @NotNull BankLog getLog() {
        return getBackend().readTransactions(getClan()).thenApply(transactions -> {
            final BankLog log = new BankLog();
            log.transactions.addAll(transactions);
            return log;
        }).join();
    }

    @Override
    public @NotNull Clan getClan() {
        return ClansAPI.getInstance().getClanManager().getClan(clanId);
    }

    public void setEnabled(boolean enabled) {
        getBackend().updateIsDisabled(getClan(), enabled).join();
    }

    private static BankBackend getBackend() {
        final BanksAPI instance = BanksAPI.getInstance();
        if (instance instanceof DefaultBanksAPIImpl) {
            return ((DefaultBanksAPIImpl) instance).getBackend();
        }
        throw new UnsupportedOperationException("This implementation only works with DefaultBanksAPIImpl.");
    }
}
