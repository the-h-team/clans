package com.github.sanctum.clans.construct.bank.backend;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.bank.BankAction;
import com.github.sanctum.clans.construct.bank.BankBackend;
import com.github.sanctum.clans.construct.bank.BankLog;
import com.github.sanctum.clans.event.bank.BankTransactionEvent;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * SQL-based backend for banks.
 *
 * @since 1.3.3
 */
public class SQLBankBackend implements BankBackend {
    final String clanId;
    final Connection connection;
    final String table;
    final String transactionTable;
    // balance
    transient final PreparedStatement hasBalance;
    transient final PreparedStatement readBalance;
    transient final PreparedStatement updateBalance;
    // levels
    transient final PreparedStatement readBalanceLevel;
    transient final PreparedStatement updateBalanceLevel;
    transient final PreparedStatement readDepositLevel;
    transient final PreparedStatement updateDepositLevel;
    transient final PreparedStatement readWithdrawLevel;
    transient final PreparedStatement updateWithdrawLevel;
    transient final PreparedStatement readViewlogLevel;
    transient final PreparedStatement updateViewlogLevel;
    // disabled
    transient final PreparedStatement readIsDisabled;
    transient final PreparedStatement updateIsDisabled;
    // transaction
    transient final PreparedStatement readTransactions;
    transient final PreparedStatement insertTransaction;
//    transient final PreparedStatement insertTransactionAutoTime;

    public SQLBankBackend(String clanId, Connection connection, String tablePrefix) throws SQLException {
        this.clanId = clanId;
        this.connection = connection;
        table = tablePrefix + "bank_data";
        transactionTable = tablePrefix + "bank_transactions";
        // Check for existing entry
        if (!hasEntry()) {
            initBank();
        }
        // balance
        hasBalance = connection.prepareStatement("SELECT balance FROM " + table + " WHERE clan_id = ? AND balance >= ?");
        hasBalance.setString(1, clanId);
        readBalance = connection.prepareStatement("SELECT balance FROM " + table + " WHERE clan_id = ?");
        readBalance.setString(1, clanId);
        updateBalance = connection.prepareStatement("UPDATE " + table + " SET balance = ? WHERE " + table + ".clan_id = ?");
        updateBalance.setString(2, clanId);
        // levels
        readBalanceLevel = connection.prepareStatement("SELECT balance_level FROM " + table + " WHERE clan_id = ?");
        readBalanceLevel.setString(1, clanId);
        updateBalanceLevel = connection.prepareStatement("UPDATE " + table + " SET balance_level = ? WHERE " + table + ".clan_id = ?");
        updateBalanceLevel.setString(2, clanId);
        readDepositLevel = connection.prepareStatement("SELECT deposit_level FROM " + table + " WHERE clan_id = ?");
        readDepositLevel.setString(1, clanId);
        updateDepositLevel = connection.prepareStatement("UPDATE " + table + " SET deposit_level = ? WHERE " + table + ".clan_id = ?");
        updateDepositLevel.setString(2, clanId);
        readWithdrawLevel = connection.prepareStatement("SELECT withdraw_level FROM " + table + " WHERE clan_id = ?");
        readWithdrawLevel.setString(1, clanId);
        updateWithdrawLevel = connection.prepareStatement("UPDATE " + table + " SET withdraw_level = ? WHERE " + table + ".clan_id = ?");
        updateWithdrawLevel.setString(2, clanId);
        readViewlogLevel = connection.prepareStatement("SELECT viewlog_level FROM " + table + " WHERE clan_id = ?");
        readViewlogLevel.setString(1, clanId);
        updateViewlogLevel = connection.prepareStatement("UPDATE " + table + " SET viewlog_level = ? WHERE " + table + ".clan_id = ?");
        updateViewlogLevel.setString(2, clanId);
        // disabled
        readIsDisabled = connection.prepareStatement("SELECT disabled FROM " + table + " WHERE clan_id = ?");
        readIsDisabled.setString(1, clanId);
        updateIsDisabled = connection.prepareStatement("UPDATE " + table + " SET disabled = ? WHERE " + table + ".clan_id = ?");
        updateIsDisabled.setString(2, clanId);
        // transactions
        readTransactions = connection.prepareStatement("SELECT id, entity, type, amount, t_time FROM " + transactionTable + " WHERE clan_id = ? ORDER BY t_time");
        readTransactions.setString(1, clanId);
        insertTransaction = connection.prepareStatement("INSERT INTO " + transactionTable + " (clan_id, entity, type, amount, t_time) VALUES (?, ?, ?, ?, ?)");
        insertTransaction.setString(1, clanId);
//        insertTransactionAutoTime = connection.prepareStatement("INSERT INTO " + transactionTable + " (clan_id, entity, type, amount) VALUES (?, ?, ?, ?)");
//        insertTransactionAutoTime.setString(1, clanId);
    }

    boolean hasEntry() throws SQLException {
        final PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE clan_id = ?");
        statement.setString(1, clanId);
        return statement.executeQuery().next();
    }

    void initBank() throws SQLException {
        final PreparedStatement initBank;
        initBank = connection.prepareStatement("INSERT INTO " + table + " (clan_id, balance, balance_level, deposit_level, withdraw_level, viewlog_level) VALUES (?, ?, ?, ?, ?, ?)");
        initBank.setString(1, clanId);
        initBank.setBigDecimal(2, ClansAPI.getBankInstance().startingBalance());
        initBank.setInt(3, ClansAPI.getDataInstance().getConfigInt("Clans.banks.default-access.balance"));
        initBank.setInt(4, ClansAPI.getDataInstance().getConfigInt("Clans.banks.default-access.deposit"));
        initBank.setInt(5, ClansAPI.getDataInstance().getConfigInt("Clans.banks.default-access.withdraw"));
        initBank.setInt(6, ClansAPI.getDataInstance().getConfigInt("Clans.banks.default-access.view-log"));
        initBank.executeUpdate();
    }

    @Override
    public CompletableFuture<BigDecimal> readBalance() {
        return CompletableFuture.supplyAsync(this::readBalanceFromSql);
    }

    BigDecimal readBalanceFromSql() {
        try {
            final ResultSet resultSet = readBalance.executeQuery();
            return resultSet.next() ? resultSet.getBigDecimal(1) : null;
        } catch (SQLException e) {
            throw new IllegalStateException("Error reading from database.", e);
        }
    }

    @Override
    public CompletableFuture<Void> updateBalance(BigDecimal balance) {
        return CompletableFuture.runAsync(() -> {
            try {
                synchronized (updateBalance) {
                    updateBalance.setBigDecimal(1, balance);
                    updateBalance.executeUpdate();
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Error updating balance", e);
            }
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
        try {
            final ResultSet resultSet = readIsDisabled.executeQuery();
            return resultSet.next() && resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new IllegalStateException("Error reading from database.", e);
        }
    }

    @Override
    public CompletableFuture<Boolean> updateIsDisabled(boolean isDisabled) {
        return readIsDisabled().thenApply(d -> {
            if (d != isDisabled) {
                try {
                    updateIsDisabled.setBoolean(1, isDisabled);
                    updateIsDisabled.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException("Error updating bank state", e);
                }
            }
            return d;
        });
    }

    @Override
    public CompletableFuture<Integer> readAccess(BankAction action) {
        return CompletableFuture.supplyAsync(() -> {
            final PreparedStatement statement;
            switch (action) {
                case BALANCE:
                    statement = readBalanceLevel;
                    break;
                case DEPOSIT:
                    statement = readDepositLevel;
                    break;
                case WITHDRAW:
                    statement = readWithdrawLevel;
                    break;
                case VIEW_LOG:
                    statement = readViewlogLevel;
                    break;
                default:
                    throw new IllegalStateException("Unsupported action!");
            }
            try {
                final ResultSet resultSet = statement.executeQuery();
                return resultSet.next() ? resultSet.getInt(1) : null;
            } catch (SQLException e) {
                throw new IllegalStateException("Error reading from database.", e);
            }
        });
    }

    @Override
    public CompletableFuture<Integer> updateAccess(BankAction action, int level) {
        return readAccess(action).thenApply(ogLevel -> {
            if (ogLevel != level) {
                try {
                    switch (action) {
                        case BALANCE:
                            synchronized (updateBalanceLevel) {
                                updateBalanceLevel.setInt(1, level);
                                updateBalanceLevel.executeUpdate();
                            }
                            break;
                        case DEPOSIT:
                            synchronized (updateDepositLevel) {
                                updateDepositLevel.setInt(1, level);
                                updateDepositLevel.executeUpdate();
                            }
                            break;
                        case WITHDRAW:
                            synchronized (updateWithdrawLevel) {
                                updateWithdrawLevel.setInt(1, level);
                                updateWithdrawLevel.executeUpdate();
                            }
                            break;
                        case VIEW_LOG:
                            synchronized (updateViewlogLevel) {
                                updateViewlogLevel.setInt(1, level);
                                updateViewlogLevel.executeUpdate();
                            }
                            break;
                        default:
                            throw new IllegalStateException("Unsupported action!");
                    }
                } catch (SQLException e) {
                    throw new IllegalStateException("Error updating bank access", e);
                }
            }
            return ogLevel;
        });
    }

    @Override
    public CompletableFuture<List<BankLog.Transaction>> readTransactions() {
        return CompletableFuture.supplyAsync(this::readTransactionsFunction);
    }

    List<BankLog.Transaction> readTransactionsFunction() {
        final ResultSet resultSet;
        try {
            resultSet = readTransactions.executeQuery();
            if (!resultSet.next()) return ImmutableList.of();
        } catch (SQLException e) {
            throw new IllegalStateException("Error reading from database.", e);
        }
        final ArrayList<BankLog.Transaction> transactions = new ArrayList<>();
        try {
            do {
                final String entity;
                final BankTransactionEvent.Type type;
                final BigDecimal amount;
                final Timestamp timestamp;
                try {
                    entity = resultSet.getString("entity");
                    type = BankTransactionEvent.Type.valueOf(resultSet.getString("type"));
                    amount = resultSet.getBigDecimal("amount");
                    timestamp = resultSet.getTimestamp("t_time");
                } catch (SQLException e) {
                    throw new IllegalStateException("Error reading from database.", e);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                transactions.add(new BankLog.Transaction(entity, type, amount, timestamp.toLocalDateTime()));
            } while(resultSet.next());
        } catch (SQLException e) {
            throw new IllegalStateException("Error reading from database.", e);
        }
        return transactions;
    }

    @Override
    public CompletableFuture<Void> addTransaction(BankLog.Transaction transaction) {
        return CompletableFuture.runAsync(() -> {
            synchronized (insertTransaction) {
                try {
                    insertTransaction.setString(2, transaction.entity);
                    insertTransaction.setString(3, transaction.type.name());
                    insertTransaction.setBigDecimal(4, transaction.amount);
                    insertTransaction.setTimestamp(5, Timestamp.valueOf(transaction.localDateTime));
                    insertTransaction.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException("Error adding to the database!", e);
                }
            }
        });
    }
}
