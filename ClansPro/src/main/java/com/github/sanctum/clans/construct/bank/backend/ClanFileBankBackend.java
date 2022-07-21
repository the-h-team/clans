package com.github.sanctum.clans.construct.bank.backend;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.bank.BankAction;
import com.github.sanctum.clans.construct.bank.BankBackend;
import com.github.sanctum.clans.construct.bank.BankLog;
import com.github.sanctum.clans.construct.bank.BankMeta;
import com.github.sanctum.clans.event.bank.BankTransactionEvent;
import com.github.sanctum.panther.file.Configurable;
import com.github.sanctum.panther.file.Node;
import com.github.sanctum.panther.file.Primitive;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

/**
 * Clan file based backend for banks.
 *
 * @since 1.3.3
 */
public class ClanFileBankBackend implements BankBackend {
    final Configurable clanFile;
    final Field mapOnAccessMapField;

    public ClanFileBankBackend(@NotNull Clan clan) {
        clanFile = ClansAPI.getDataInstance().getClanFile(clan).getRoot();
        try {
            mapOnAccessMapField = BankAction.AccessMap.class.getDeclaredField("acl");
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public CompletableFuture<BigDecimal> readBalance() {
        return CompletableFuture.completedFuture(new BigDecimal(clanFile.getString("bank-data.balance")));
    }

    @Override
    public CompletableFuture<Void> updateBalance(BigDecimal balance) {
        return CompletableFuture.runAsync(() -> {
            clanFile.set("bank-data.balance", balance != null ? balance.toString() : null);
            clanFile.save();
        });
    }

    @Override
    public CompletableFuture<Integer> compareBalance(@NotNull BigDecimal testAmount) {
        return readBalance().thenApply(bd -> bd.compareTo(testAmount));
    }

    @Override
    public CompletableFuture<Boolean> readIsDisabled() {
        return CompletableFuture.supplyAsync(this::readIsDisabledFunction);
    }

    boolean readIsDisabledFunction() {
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
    public CompletableFuture<Boolean> updateIsDisabled(boolean isDisabled) {
        return readIsDisabled().thenApply(b -> {
            if (b != isDisabled) {
                clanFile.set("bank-data.disabled", isDisabled);
                clanFile.save();
            }
            return b;
        });
    }

    @Override
    public CompletableFuture<Integer> readAccess(BankAction action) {
        return CompletableFuture.supplyAsync(() -> {
            final Node node = clanFile.getNode("bank-data.access-map." + action.name());
            if (node.exists()) {
                final Primitive primitive = node.toPrimitive();
                if (primitive.isInt()) return primitive.getInt();
            }
            return -1;
        });
    }

    BankAction.AccessMap readAccessMapFunction() {
        final Node node = clanFile.getNode("bank-data.access-map");
        final BankAction.AccessMap accessMap = new BankAction.AccessMap();
        final Set<String> keys = node.getKeys(false);
        if (keys.isEmpty()) return accessMap;
        final Map<BankAction, Integer> internal;
        // cue ugly reflection because i can't change the class yet:D
        try {
            //noinspection unchecked
            internal = (Map<BankAction, Integer>) mapOnAccessMapField.get(accessMap);
        } catch (IllegalAccessException | ClassCastException e) {
            throw new IllegalStateException(e);
        }
        for (String key : keys) {
            final BankAction action;
            try {
                action = BankAction.valueOf(key);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e);
            }
            // get value
            final Primitive primitive = node.getNode(key).toPrimitive();
            if (primitive.isInt()) {
                internal.put(action, primitive.getInt());
            }
        }
        return accessMap;
    }

    @Override
    public CompletableFuture<Integer> updateAccess(BankAction action, int level) {
        return readAccess(action).thenApply(ogLvl -> {
            if (level != ogLvl) {
                clanFile.set("bank-data.access-map." + action.name(), level);
            }
            return ogLvl;
        });
    }

    CompletableFuture<Void> updateAccessMap(BankAction.AccessMap accessMap) {
        final Map<BankAction, Integer> internal;
        // cue ugly reflection because i can't change the class yet:D
        try {
            //noinspection unchecked
            internal = (Map<BankAction, Integer>) mapOnAccessMapField.get(accessMap);
        } catch (IllegalAccessException | ClassCastException e) {
            throw new IllegalStateException(e);
        }
        final Node node = clanFile.getNode("bank-data.access-map");
        for (Map.Entry<BankAction, Integer> entry : internal.entrySet()) {
            node.getNode(entry.getKey().name()).set(entry.getValue());
        }
        clanFile.save();
        return null;
    }

    @Override
    public CompletableFuture<List<BankLog.Transaction>> readTransactions() {
        return CompletableFuture.supplyAsync(this::readTransactionsFunction);
    }

    List<BankLog.Transaction> readTransactionsFunction() {
        final ArrayList<BankLog.Transaction> transactions = new ArrayList<>();
        clanFile.getStringList("bank-data.transactions")
                .parallelStream()
                .map(ClanFileBankBackend::decodeTransaction)
                .forEach(transactions::add);
        return transactions;
    }

    @Override
    public CompletableFuture<Void> addTransaction(BankLog.Transaction transaction) {
        return CompletableFuture.runAsync(() -> {
            final ArrayList<String> list = new ArrayList<>(clanFile.getStringList("bank-data.transactions"));
            list.add(encodeTransaction(transaction));
            clanFile.set("bank-data.transactions", list);
            clanFile.save();
        });
    }

    public CompletableFuture<Void> updateBankLog(BankLog bankLog) {
        return CompletableFuture.runAsync(() -> {
            final List<String> representations = new ArrayList<>();
            for (BankLog.Transaction transaction : bankLog.getTransactions()) {
                representations.add(encodeTransaction(transaction));
            }
            clanFile.set("bank-data.transactions", representations);
            clanFile.save();
        });
    }

    public static void saveOldFormat(Clan clan) {
        final ClanFileBankBackend backend = new ClanFileBankBackend(clan);
        final BankMeta bankMeta = BankMeta.get(clan);
        if (!clan.isConsole()) {
            bankMeta.getBank().map(b -> backend.updateBalance(b.getBalance())).ifPresent(CompletableFuture::join);
            bankMeta.getAccessMap().ifPresent(am -> {
                backend.updateAccess(BankAction.BALANCE, BankAction.BALANCE.getValueInClan(clan)).join();
                backend.updateAccess(BankAction.DEPOSIT, BankAction.DEPOSIT.getValueInClan(clan)).join();
                backend.updateAccess(BankAction.WITHDRAW, BankAction.WITHDRAW.getValueInClan(clan)).join();
                backend.updateAccess(BankAction.VIEW_LOG, BankAction.VIEW_LOG.getValueInClan(clan)).join();
            });
            bankMeta.getBankLog().map(backend::updateBankLog).ifPresent(CompletableFuture::join);
        }
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
