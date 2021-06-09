package com.github.sanctum.clans.util.events.clans.bank;

import com.github.sanctum.clans.construct.api.ClanBank;
import org.bukkit.event.Event;

public abstract class BankEvent extends Event {

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
}
