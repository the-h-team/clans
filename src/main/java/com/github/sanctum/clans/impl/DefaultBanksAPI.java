package com.github.sanctum.clans.impl;

import com.github.sanctum.clans.model.BanksAPI;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClanBankBackend;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.backend.ClanFileBankBackend;
import com.github.sanctum.panther.util.HUID;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public class DefaultBanksAPI implements BanksAPI {
    private final JavaPlugin providingPlugin = JavaPlugin.getProvidingPlugin(Clan.Bank.class);
    private final Map<HUID, DefaultClanBank> banks = new HashMap<>();
    private ClanBankBackend backend = new ClanFileBankBackend();

    @Override
    public Clan.Bank getBank(Clan clan) {
        return banks.computeIfAbsent(clan.getId(), DefaultClanBank::new);
    }

    @Override
    public BigDecimal startingBalance() {
        final String string = ClansAPI.getDataInstance().getConfigString("Clans.banks.starting-balance");
        if (string == null) {
            providingPlugin.getLogger().severe("Error reading starting-balance, returning 0!");
        } else {
            try {
                return new BigDecimal(string);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                providingPlugin.getLogger().severe("Improperly formatted starting-balance!");
                providingPlugin.getLogger().info("Using 0.");
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public @Nullable BigDecimal maxBalance() {
        final String string = ClansAPI.getDataInstance().getConfigString("Clans.banks.maximum-balance");
        if (string != null) {
            try {
                return new BigDecimal(string);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                providingPlugin.getLogger().severe("Improperly formatted maximum-balance!");
                providingPlugin.getLogger().info("Maximum not set.");
            }
        }
        return null;
    }

    @Override
    public LogLevel logToConsole() {
        final int anInt = ClansAPI.getDataInstance().getConfigInt("Clans.banks.log-level");
        if (anInt < 0 || anInt > 2) {
            providingPlugin.getLogger().severe("Invalid log level! Using 1 - Quiet");
            return LogLevel.QUIET;
        }
        return LogLevel.values()[anInt];
    }

    public ClanBankBackend getBackend() {
        return backend;
    }

    public void setBackend(ClanBankBackend backend) {
        this.backend = backend;
    }

    public static void save(Clan clan) {
    }
}
