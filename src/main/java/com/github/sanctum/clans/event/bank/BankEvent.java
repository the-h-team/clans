package com.github.sanctum.clans.event.bank;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.event.ClanEvent;

/**
 * An event involving a clan bank.
 */
public abstract class BankEvent extends ClanEvent {

    protected final ClanBank clanBank;

    protected BankEvent(ClanBank clanBank, boolean async) {
        super(clanBank.getClan(), async);
        this.clanBank = clanBank;
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
    public final ClanBank getBank() {
        return clanBank;
    }

}
