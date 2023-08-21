package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.bank.BankLog;
import com.github.sanctum.labyrinth.interfacing.Nameable;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public interface ClanBank {
	/**
	 * Thrown when an action is attempted on a bank that has been disabled.
	 */
	class DisabledException extends RuntimeException {
		private static final long serialVersionUID = 4810589456756119379L;

		public DisabledException(String clanId) {
			super("The bank for [" + clanId + "] is disabled.");
		}
	}

	/**
	 * Deposits an amount into the bank.
	 *
	 * @param amount the amount to deposit
	 * @param source the source of the deposit
	 * @return true if the deposit was successful
	 * @throws DisabledException if the bank is disabled
	 * @throws IllegalArgumentException if {@code amount} is negative
	 */
	boolean deposit(@NotNull BigDecimal amount, Nameable source) throws DisabledException, IllegalArgumentException;

	/**
	 * Withdraws an amount from the bank.
	 *
	 * @param amount the amount to withdraw
	 * @param recipient the recipient of the withdrawal
	 * @return true if the withdrawal was successful
	 * @throws DisabledException if the bank is disabled
	 * @throws IllegalArgumentException if {@code amount} is negative
	 */
	boolean withdraw(@NotNull BigDecimal amount, Nameable recipient) throws DisabledException, IllegalArgumentException;

	/**
	 * Checks if the bank has an amount.
	 *
	 * @return true if the bank has at least amount
	 * @throws IllegalArgumentException if {@code amount} is negative
	 */
	boolean has(@NotNull BigDecimal amount) throws IllegalArgumentException;

	/**
	 * Gets the balance of the bank.
	 *
	 * @return the balance as a double
	 */
	default double getBalanceDouble() {
		return getBalance().doubleValue();
	}

	/**
	 * Gets the balance of the bank.
	 *
	 * @return the balance as a BigDecimal
	 */
	@NotNull BigDecimal getBalance();

	/**
	 * Sets the balance of the bank.
	 *
	 * @param newBalance the desired balance as a double
	 * @return true if successful
	 * @throws DisabledException if the bank is disabled
	 */
	default boolean setBalanceDouble(double newBalance) throws DisabledException {
		return setBalance(BigDecimal.valueOf(newBalance));
	}

	/**
	 * Sets the balance of the bank.
	 *
	 * @param newBalance the desired balance as a BigDecimal
	 * @return true if successful
	 * @throws DisabledException if the bank is disabled
	 */
	boolean setBalance(@NotNull BigDecimal newBalance) throws DisabledException;

	/**
	 * Gets a copy of the log of the bank's transactions.
	 *
	 * @return the bank's transaction log
	 */
	@NotNull BankLog getLog();

	/**
	 * Gets the clan this bank belongs to.
	 *
	 * @return the bank's clan
	 */
	@NotNull Clan getClan();

}
