package com.github.sanctum.clans.util.events.clans.bank;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import java.math.BigDecimal;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AsyncNewBankEvent extends BankEvent {
	private static final HandlerList HANDLERS = new HandlerList();

	private final Clan clan;
	private final BigDecimal startingBalance;

	public AsyncNewBankEvent(Clan clan, ClanBank clanBank) {
		super(clanBank, true);
		this.clan = clan;
		this.startingBalance = clanBank.getBalance();
	}

	/**
	 * Get the clan whose bank was just created
	 *
	 * @return the Clan
	 */
	public Clan getClan() {
		return clan;
	}

	/**
	 * Get the starting balance of the bank (usually 0 per default configuration).
	 *
	 * @return the initial balance
	 */
	public BigDecimal getStartingBalance() {
		return startingBalance;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
