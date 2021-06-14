package com.github.sanctum.clans.util.events.clans.bank;

import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.labyrinth.event.custom.Vent;

public abstract class BankEvent extends Vent {

    protected final ClanBank clanBank;

    protected BankEvent(ClanBank clanBank, boolean async) {
        super(async);
        this.clanBank = clanBank;
    }

    /**
     * Get the ClanBank associated with this event
     *
     * @return the ClanBank
     */
    public ClanBank getClanBank() {
        return clanBank;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }
}
