package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.bank.DefaultBanksAPIImpl;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public interface BanksAPI {

	static BanksAPI getInstance() {
		BanksAPI instance = LabyrinthProvider.getInstance().getServicesManager().load(BanksAPI.class);
		if (instance != null) return instance;
		BanksAPI fresh = new DefaultBanksAPIImpl();
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
