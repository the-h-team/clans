package com.github.sanctum.clans.event.bank;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import com.github.sanctum.panther.util.HUID;
import java.util.concurrent.CompletableFuture;

public abstract class BankActionEvent extends BankEvent {

    protected final String clanId;

    protected BankActionEvent(ClanBank clanBank, String clanId) {
        super(clanBank, false);
        this.clanId = clanId;
    }

    /**
     * Get the direct clanId for this event
     *
     * @return clanId as String
     */
    public String getClanId() {
        return clanId;
    }

    /**
     * Get the clan associated with this bank event
     *
     * @return the Clan whose bank this is
     */
    @Override
    public Clan getClan() {
        return CompletableFuture.supplyAsync(() -> (DefaultClan) ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(clanId))).join();
    }
}
