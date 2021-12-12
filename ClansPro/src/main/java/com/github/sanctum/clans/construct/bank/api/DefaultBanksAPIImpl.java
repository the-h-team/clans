package com.github.sanctum.clans.construct.bank.api;

import com.github.sanctum.clans.construct.api.BanksAPI;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.bank.BankMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

/**
 * The default, standalone API implementation.
 *
 * @implNote Relocated from {@link BanksAPI}.
 */
public class DefaultBanksAPIImpl implements BanksAPI {
    private final JavaPlugin providingPlugin = JavaPlugin.getProvidingPlugin(ClanBank.class);

    @Override
    public ClanBank getBank(Clan clan) {
        return BankMeta.get(clan).getBank().orElseThrow(IllegalStateException::new);
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
}
