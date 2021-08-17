package com.github.sanctum.clans.construct.bank;

import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.impl.DefaultClan;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

public enum BankAction {
    BALANCE("balance"),
    DEPOSIT("deposit"),
    WITHDRAW("withdraw"),
    VIEW_LOG("view-log");

    private final String line;

    BankAction(String confLine) {
        this.line = confLine;
    }

    public static class AccessMap implements Serializable {
        private static final long serialVersionUID = -265409254564104601L;
        private final Map<BankAction, Integer> acl = new HashMap<>();

        public AccessMap() {
            for (BankAction value : BankAction.values()) {
                acl.computeIfAbsent(value, BankAction::getConfigDefault);
            }
        }

        private void setForClan(Clan clan) {
            if (!(clan instanceof DefaultClan))
                return;
            DefaultClan targ = (DefaultClan) clan;
            BankMeta.get(targ).storeAccessMap(this);
        }

        private static AccessMap getForClan(Clan clan) {
            if (!(clan instanceof DefaultClan))
                return null;
            DefaultClan targ = (DefaultClan) clan;
            return BankMeta.get(targ).getAccessMap().orElseGet(() -> CompletableFuture.supplyAsync(AccessMap::new).join());
        }
    }

    public int getConfigDefault() {
        return ClansAPI.getData().getMain().getConfig().getInt("Clans.banks.default-access." + line, 2);
    }

    public int getValueInClan(Clan clan) {
        return AccessMap.getForClan(clan).acl.get(this);
    }

    public boolean testForPlayer(Clan clan, Player player) {
        ClanAssociate associate = ClansAPI.getInstance().getAssociate(player).orElse(null);
        return associate != null && associate.getPriority().toInt() >= AccessMap.getForClan(clan).acl.get(this);
    }

    public void setRankForActionInClan(Clan clan, int rank) {
        if (rank < 0 || rank > 3) throw new IllegalArgumentException("Rank must be between 0-3");
        final AccessMap accessMap = AccessMap.getForClan(clan);
        accessMap.acl.put(this, rank);
        accessMap.setForClan(clan);
    }
}
