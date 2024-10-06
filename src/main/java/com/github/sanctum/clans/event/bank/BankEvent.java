package com.github.sanctum.clans.event.bank;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.event.ClanEvent;

/**
 * An event involving a clan bank.
 */
public abstract class BankEvent extends ClanEvent {

    protected final Clan.Bank bank;

    protected BankEvent(Clan.Bank bank, boolean async) {
        super(bank.getClan(), async);
        this.bank = bank;
    }

    /**
     * Gets the clan involved in this event.
     *
     * @return the clan
     */
    @Override
    public final Clan getClan() {
        return super.getClan();
    }

    /**
     * Gets the clan bank associated with this event.
     *
     * @return the clan bank
     */
    public final Clan.Bank getBank() {
        return bank;
    }

}
