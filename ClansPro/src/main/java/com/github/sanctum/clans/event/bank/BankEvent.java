package com.github.sanctum.clans.event.bank;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.bank.BankMeta;
import com.github.sanctum.clans.event.ClanEvent;

public abstract class BankEvent extends ClanEvent {

    protected final ClanBank clanBank;

    protected BankEvent(ClanBank clanBank, boolean async) {
        super(async);
        this.clanBank = clanBank;
    }

    @Override
    public Clan getClan() {
        return clanBank instanceof Clan ? (Clan)clanBank : BankMeta.get(clanBank).getClan();
    }

    /**
     * Get the ClanBank associated with this event
     *
     * @return the ClanBank
     */
    public ClanBank getClanBank() {
        return clanBank;
    }


}
