package com.github.sanctum.clans.construct.bank.backend;

import com.github.sanctum.clans.construct.api.BanksAPI;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.bank.BankBackend;
import com.github.sanctum.clans.construct.bank.BankLog;
import com.github.sanctum.clans.event.bank.BankTransactionEvent;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.file.Primitive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanFileBankBackend implements BankBackend {

    @Override
    public CompletableFuture<@Nullable BigDecimal> readBalance(@NotNull Clan clan) {
        return CompletableFuture.completedFuture(getClanFile(clan))
                .thenApply(cf -> cf.getString("bank-data.balance"))
                .thenApplyAsync(bal -> {
                    if (bal != null) try {
                        return new BigDecimal(bal);
                    } catch (NumberFormatException ignored) {
                        final Logger logger = JavaPlugin.getProvidingPlugin(getClass()).getLogger();
                        logger.warning("Invalid balance found for clan " + clan.getId() + ": " + bal);
                        logger.info("The balance for clan " + clan.getId() + " will be reset to the default.");
                    }
                    return null;
                });
    }

    @Override
    public CompletableFuture<Void> updateBalance(@NotNull Clan clan, BigDecimal balance) {
        final Configurable clanFile = getClanFile(clan);
        return CompletableFuture.runAsync(() -> clanFile.set("bank-data.balance", balance != null ? balance.toString() : null))
                .thenRun(clanFile::save);
    }

    @Override
    public CompletableFuture<Integer> compareBalance(@NotNull Clan clan, @NotNull BigDecimal testAmount) {
        return readBalance(clan).thenApply(bal -> bal != null ? bal : BanksAPI.getInstance().startingBalance())
                .thenApplyAsync(bd -> bd.compareTo(testAmount));
    }

    @Override
    public CompletableFuture<Boolean> readIsDisabled(@NotNull Clan clan) {
        return CompletableFuture.completedFuture(clan)
                .thenApply(ClanFileBankBackend::getClanFile)
                .thenApplyAsync(this::readIsDisabledFunction);
    }

    private boolean readIsDisabledFunction(Configurable clanFile) {
        final Node node = clanFile.getNode("bank-data.disabled");
        if (node.exists()) {
            final Primitive primitive = node.toPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getBoolean();
            }
        }
        return false; // default to false
    }

    @Override
    public CompletableFuture<Boolean> updateIsDisabled(@NotNull Clan clan, boolean isDisabled) {
        final Configurable clanFile = getClanFile(clan);
        return readIsDisabled(clan).thenApply(b -> {
            if (b != isDisabled) {
                clanFile.set("bank-data.disabled", isDisabled);
                clanFile.save();
            }
            return b;
        });
    }

    @Override
    public CompletableFuture<List<BankLog.Transaction>> readTransactions(@NotNull Clan clan) {
        return CompletableFuture.completedFuture(clan)
                .thenApply(ClanFileBankBackend::getClanFile)
                .thenApplyAsync(this::readTransactionsFunction);
    }

    List<BankLog.Transaction> readTransactionsFunction(Configurable clanFile) {
        final ArrayList<BankLog.Transaction> transactions = new ArrayList<>();
        clanFile.getStringList("bank-data.transactions")
                .parallelStream()
                .map(ClanFileBankBackend::decodeTransaction)
                .forEach(transactions::add);
        return transactions;
    }

    @Override
    public CompletableFuture<Void> addTransaction(@NotNull Clan clan, @NotNull BankLog.Transaction transaction) {
        final Configurable clanFile = getClanFile(clan);
        return CompletableFuture.runAsync(() -> {
            final ArrayList<String> list = new ArrayList<>(clanFile.getStringList("bank-data.transactions"));
            list.add(encodeTransaction(transaction));
            clanFile.set("bank-data.transactions", list);
        }).thenRun(clanFile::save);
    }

    @Override
    public CompletableFuture<Void> updateTransactionLog(@NotNull Clan clan, @NotNull BankLog bankLog) {
        final Configurable clanFile = getClanFile(clan);
        return CompletableFuture.runAsync(() -> {
            final List<String> representations = new ArrayList<>();
            for (BankLog.Transaction transaction : bankLog.getTransactions()) {
                representations.add(encodeTransaction(transaction));
            }
            clanFile.set("bank-data.transactions", representations);
        }).thenRun(clanFile::save);
    }

    private static Configurable getClanFile(Clan clan) {
        return ClansAPI.getDataInstance().getClanFile(clan).getRoot();
    }

    // csv
    static String encodeTransaction(BankLog.Transaction transaction) {
        return transaction.entity + "," + transaction.type.name() + "," + transaction.amount + "," + transaction.localDateTime;
    }

    // csv, special handling in case entity contained comma(s)
    static BankLog.Transaction decodeTransaction(String encoded) throws IllegalArgumentException, DateTimeParseException {
        String[] split = encoded.split(",");
        if (split.length > 4) {
            final StringBuilder sb = new StringBuilder(split[0]);
            for (int i = 1; i < split.length - 3; ++i) {
                sb.append(",").append(split[i]);
            }
            final String[] newArr = new String[4];
            newArr[0] = sb.toString();
            newArr[1] = split[split.length - 3];
            newArr[2] = split[split.length - 2];
            newArr[3] = split[split.length - 1];
            split = newArr;
        }
        return new BankLog.Transaction(
                split[0],
                BankTransactionEvent.Type.valueOf(split[1]),
                new BigDecimal(split[2]),
                LocalDateTime.parse(split[3])
        );
    }
}
