package com.github.sanctum.clans.construct.bank;

import com.github.sanctum.clans.construct.api.BanksAPI;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.event.bank.BankPreTransactionEvent;
import com.github.sanctum.clans.event.bank.BankSetBalanceEvent;
import com.github.sanctum.clans.event.bank.BankTransactionEvent;
import com.github.sanctum.labyrinth.data.EconomyProvision;
import com.github.sanctum.labyrinth.event.LabyrinthVentCall;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class Bank implements ClanBank, Serializable {
	private static final long serialVersionUID = -153639291829056195L;
	final BigDecimal INCREMENT = BigDecimal.valueOf(0.04D);
	BigDecimal balance, interest;
	boolean enabled;
	final String clanId;

	public Bank(@NotNull String clanId) {
		this.balance = BanksAPI.getInstance().startingBalance();
		this.interest = BigDecimal.ZERO;
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
	    final BankPreTransactionEvent preTransactionEvent = // Check for interest, apply if found and increment for deposit.
			    new BankPreTransactionEvent(player, this, (hasInterest() ? amount.add(BigDecimal.valueOf(amount.doubleValue()).multiply(interest.add(INCREMENT))) : amount), clanId, has, BankTransactionEvent.Type.DEPOSIT);
	    return new LabyrinthVentCall<>(preTransactionEvent).run().isSuccess();
    }

    @Override
    public boolean withdraw(Player player, BigDecimal amount) {
        if (!enabled) return false;
        if (amount.signum() != 1) return false;
        final BankPreTransactionEvent preTransactionEvent;
        boolean hasWalletAccount = EconomyProvision.getInstance().isValid();

        preTransactionEvent = new BankPreTransactionEvent(player, this, amount, clanId, has(amount) && hasWalletAccount,
                BankTransactionEvent.Type.WITHDRAWAL);
        return new LabyrinthVentCall<>(preTransactionEvent).run().isSuccess();
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
	    return !(new LabyrinthVentCall<>(event).run()).isCancelled();
    }

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean hasInterest() {
		return interest.doubleValue() > 0;
	}

	@Override
	public BigDecimal getInterest() {
		return interest;
	}

	@Override
	public boolean setInterest(BigDecimal newInterest) {
		this.interest = newInterest;
		return true;
	}
}
