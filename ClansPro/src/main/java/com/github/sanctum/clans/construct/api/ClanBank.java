package com.github.sanctum.clans.construct.api;

import java.math.BigDecimal;
import org.bukkit.entity.Player;

public interface ClanBank {
	/**
	 * Take an amount from the player and deposit into the bank.
	 *
	 * @return true if successful
	 */
	boolean deposit(Player player, BigDecimal amount);

	/**
	 * Withdraw an amount from the bank and give to the player.
	 *
	 * @return true if successful
	 */
	boolean withdraw(Player player, BigDecimal amount);

	/**
	 * Take an amount from the specified bank and deposit into this bank
	 *
	 * @return true if successful
	 */
	default boolean takeFrom(ClanBank payer, BigDecimal amount) {
		return payer.sendTo(this, amount);
	}

	/**
	 * Send an amount from this bank to another specified bank
	 *
	 * @return true if successful
	 */
	default boolean sendTo(ClanBank payee, BigDecimal amount) {
		if (has(amount)) {
			setBalance(getBalance().subtract(amount));
			payee.setBalance(payee.getBalance().add(amount));
			return true;
		}
		return false;
	}

	/**
	 * Check if the bank has an amount.
	 *
	 * @return true if the bank has at least amount
	 */
	boolean has(BigDecimal amount);

	/**
	 * Get the balance of the bank.
	 *
	 * @return balance as double
	 */
	default double getBalanceDouble() {
		return getBalance().doubleValue();
	}

	/**
	 * Get the balance of the bank.
	 *
	 * @return balance as BigDecimal
	 */
	BigDecimal getBalance();

	/**
	 * Set the balance of the bank.
	 *
	 * @param newBalance the desired balance as a double
	 * @return true if successful
	 * @throws IllegalArgumentException if desired balance is negative
	 */
	@SuppressWarnings("UnusedReturnValue")
	default boolean setBalanceDouble(double newBalance) {
		return setBalance(BigDecimal.valueOf(newBalance));
	}

	/**
	 * Set the balance of the bank.
	 *
	 * @param newBalance the desired balance as BigDecimal
	 * @return true if successful
	 * @throws IllegalArgumentException if desired balance is negative
	 */
	@SuppressWarnings("UnusedReturnValue")
	default boolean setBalance(BigDecimal newBalance) {
		if (newBalance.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException();
		return false;
	}

}
