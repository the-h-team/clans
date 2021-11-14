package com.github.sanctum.clans.event.bank;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import java.math.BigDecimal;

public class AsyncNewBankEvent extends BankEvent {

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
	@Override
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
}
