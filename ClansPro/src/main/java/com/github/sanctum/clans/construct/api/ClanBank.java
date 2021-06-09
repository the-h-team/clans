package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.bank.BankMeta;
import java.math.BigDecimal;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

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

	// TODO: introduce inter-clan payments in feature update
//    /**
//     * Take an amount from the specified bank and deposit into this bank
//     * @return true if successful
//     */
//    boolean takeFrom(ClanBank payer, BigDecimal amount);

//    /**
//     * Send an amount from this bank to another specified bank
//     * @return true if successful
//     */
//    boolean sendTo(ClanBank payee, BigDecimal amount);

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

	interface API {
		API defaultImpl = new API() {
			private final JavaPlugin providingPlugin = JavaPlugin.getProvidingPlugin(ClanBank.class);

			@Override
			public ClanBank getBank(Clan clan) {
				return BankMeta.get(clan).getBank().orElseThrow(IllegalStateException::new);
			}

			@Override
			public BigDecimal startingBalance() {
				final String string = ClansAPI.getData().getMain().getConfig().getString("Clans.banks.starting-balance");
				if (string == null) {
					providingPlugin.getLogger().severe("Error reading starting-balance, returning 0!");
				} else {
					try {
						return new BigDecimal(string);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						providingPlugin.getLogger().severe("Improperly formatted starting-balance!");
						providingPlugin.getLogger().info("Using 0.");
					}
				}
				return BigDecimal.ZERO;
			}

			@Override
			public @Nullable BigDecimal maxBalance() {
				final String string = ClansAPI.getData().getMain().getConfig().getString("Clans.banks.maximum-balance");
				if (string != null) {
					try {
						return new BigDecimal(string);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						providingPlugin.getLogger().severe("Improperly formatted maximum-balance!");
						providingPlugin.getLogger().info("Maximum not set.");
					}
				}
				return null;
			}

			@Override
			public LogLevel logToConsole() {
				final int anInt = ClansAPI.getData().getMain().getConfig().getInt("Clans.banks.log-level");
				if (anInt < 0 || anInt > 2) {
					providingPlugin.getLogger().severe("Invalid log level! Using 1 - Quiet");
					return LogLevel.QUIET;
				}
				return LogLevel.values()[anInt];
			}
		};

		/**
		 * Describes a level of output logged to the console.
		 */
		enum LogLevel {
			SILENT, QUIET, VERBOSE
		}

		/**
		 * Gets the bank associated with a clan.
		 *
		 * @param clan the desired clan
		 * @return a Bank; if it does not exist it is created
		 */
		ClanBank getBank(Clan clan);

		/**
		 * Set the default balance of newly-created Banks.
		 *
		 * @return the starting balance of new banks
		 */
		default BigDecimal startingBalance() {
			return BigDecimal.ZERO;
		}

		/**
		 * This value reflects the maximum balance of Banks if configured.
		 * <p>
		 * Returns null if no set maximum.
		 *
		 * @return the maximum balance or null
		 */
		default @Nullable BigDecimal maxBalance() {
			return null;
		}

		/**
		 * Set the transaction logging level.
		 *
		 * @return a {@link LogLevel} representing desired verbosity
		 */
		default LogLevel logToConsole() {
			return LogLevel.SILENT;
		}
	}
}
