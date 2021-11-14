package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.bank.BankMeta;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import java.math.BigDecimal;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public interface BanksAPI {

	static BanksAPI getInstance() {
		BanksAPI instance = LabyrinthProvider.getInstance().getServicesManager().load(BanksAPI.class);
		if (instance != null) return instance;
		BanksAPI fresh = new BanksAPI() {
			private final JavaPlugin providingPlugin = JavaPlugin.getProvidingPlugin(ClanBank.class);

			@Override
			public ClanBank getBank(Clan clan) {
				return BankMeta.get(clan).getBank().orElseThrow(IllegalStateException::new);
			}

			@Override
			public BigDecimal startingBalance() {
				final String string = ClansAPI.getDataInstance().getConfigString("Clans.banks.starting-balance");
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
				final String string = ClansAPI.getDataInstance().getConfigString("Clans.banks.maximum-balance");
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
				final int anInt = ClansAPI.getDataInstance().getConfigInt("Clans.banks.log-level");
				if (anInt < 0 || anInt > 2) {
					providingPlugin.getLogger().severe("Invalid log level! Using 1 - Quiet");
					return LogLevel.QUIET;
				}
				return LogLevel.values()[anInt];
			}
		};
		LabyrinthProvider.getInstance().getServicesManager().register(fresh, ClansAPI.getInstance().getPlugin(), ServicePriority.Normal);
		return fresh;
	}

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
	 * Get the transaction logging level.
	 *
	 * @return a {@link LogLevel} representing desired verbosity
	 */
	default LogLevel logToConsole() {
		return LogLevel.SILENT;
	}
}
