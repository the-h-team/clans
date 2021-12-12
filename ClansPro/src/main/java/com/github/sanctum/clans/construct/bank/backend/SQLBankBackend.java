package com.github.sanctum.clans.construct.bank.backend;

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
    final SQLConnector connector;
//    transient final PreparedStatement insertTransactionAutoTime;

    public SQLBankBackend(String clanId, SQLConnector connector) throws SQLException, IllegalArgumentException {
        this.clanId = clanId;
        this.connector = connector;
        // Check for existing entry
        if (!hasEntry()) {
            connector.initBank(clanId);
        }
    }

    boolean hasEntry() throws SQLException {
        synchronized (connector.ps) {
            final PreparedStatement hasEntry = connector.ps.get(BankSQL.HAS_ENTRY);
            hasEntry.setString(1, clanId);
            try (final ResultSet resultSet = hasEntry.executeQuery()) {
                return resultSet.next();
            } catch (SQLException ignored) {
                return false;
            }
        }
    }

    @Override
    public CompletableFuture<Boolean> hasBalance(BigDecimal amount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                synchronized (connector.ps) {
                    final PreparedStatement hasBalance = connector.ps.get(BankSQL.HAS_BALANCE);
                    hasBalance.setString(1, clanId);
                    hasBalance.setBigDecimal(2, amount);
                    try (final ResultSet resultSet = hasBalance.executeQuery()) {
                        return resultSet.next();
                    }
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Error reading from database.", e);
            }
        });
    }

    @Override
    public CompletableFuture<BigDecimal> readBalance() {
        return CompletableFuture.supplyAsync(this::readBalanceFromSql);
    }

    BigDecimal readBalanceFromSql() {
        synchronized (connector.ps) {
            final PreparedStatement readBalance = connector.ps.get(BankSQL.READ_BALANCE);
            try {
                readBalance.setString(1, clanId);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            try (final ResultSet resultSet = readBalance.executeQuery()) {
                return resultSet.next() ? resultSet.getBigDecimal(1) : null;
            } catch (SQLException e) {
                throw new IllegalStateException("Error reading from database.", e);
            }
        }
    }

    @Override
    public CompletableFuture<Void> updateBalance(BigDecimal balance) {
        return CompletableFuture.runAsync(() -> {
            try {
                synchronized (connector.ps) {
                    final PreparedStatement updateBalance = connector.ps.get(BankSQL.UPDATE_BALANCE);
                    updateBalance.setBigDecimal(1, balance);
                    updateBalance.setString(2, clanId);
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
        synchronized (connector.ps) {
            final PreparedStatement readIsDisabled = connector.ps.get(BankSQL.READ_IS_DISABLED);
            try {
                readIsDisabled.setString(1, clanId);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            try (final ResultSet resultSet = readIsDisabled.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean(1);
            } catch (SQLException e) {
                throw new IllegalStateException("Error reading from database.", e);
            }
        }
    }

    @Override
    public CompletableFuture<Boolean> updateIsDisabled(boolean isDisabled) {
        return readIsDisabled().thenApply(d -> {
            if (d != isDisabled) {
                try {
                    synchronized (connector.ps) {
                        final PreparedStatement updateIsDisabled = connector.ps.get(BankSQL.UPDATE_IS_DISABLED);
                        updateIsDisabled.setBoolean(1, isDisabled);
                        updateIsDisabled.setString(2, clanId);
                        updateIsDisabled.executeUpdate();
                    }
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
            final ResultSet resultSet;
            try {
                synchronized (connector.ps) {
                    final PreparedStatement statement;
                    switch (action) {
                        case BALANCE:
                            statement = connector.ps.get(BankSQL.READ_BALANCE_LEVEL);
                            break;
                        case DEPOSIT:
                            statement = connector.ps.get(BankSQL.READ_DEPOSIT_LEVEL);
                            break;
                        case WITHDRAW:
                            statement = connector.ps.get(BankSQL.READ_WITHDRAW_LEVEL);
                            break;
                        case VIEW_LOG:
                            statement = connector.ps.get(BankSQL.READ_VIEWLOG_LEVEL);
                            break;
                        default:
                            throw new IllegalStateException("Unsupported action!");
                    }
                    statement.setString(1, clanId);
                    resultSet = statement.executeQuery();
                }
                final Integer access = resultSet.next() ? resultSet.getInt(1) : null;
                resultSet.close();
                return access;
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
                    synchronized (connector.ps) {
                        final PreparedStatement statement;
                        switch (action) {
                            case BALANCE:
                                statement = connector.ps.get(BankSQL.UPDATE_BALANCE_LEVEL);
                                break;
                            case DEPOSIT:
                                statement = connector.ps.get(BankSQL.UPDATE_DEPOSIT_LEVEL);
                                break;
                            case WITHDRAW:
                                statement = connector.ps.get(BankSQL.UPDATE_WITHDRAW_LEVEL);
                                break;
                            case VIEW_LOG:
                                statement = connector.ps.get(BankSQL.UPDATE_VIEWLOG_LEVEL);
                                break;
                            default:
                                throw new IllegalStateException("Unsupported action!");
                        }
                        statement.setInt(1, level);
                        statement.setString(2, clanId);
                        statement.executeUpdate();
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
        synchronized (connector.ps) {
            final PreparedStatement statement = connector.ps.get(BankSQL.READ_TRANSACTIONS);
            try {
                statement.setString(1, clanId);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            try (ResultSet tempResultSet = statement.executeQuery()) {
                if (!tempResultSet.next()) return ImmutableList.of();
                resultSet = tempResultSet;
            } catch (SQLException e) {
                throw new IllegalStateException("Error reading from database.", e);
            }
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
            resultSet.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Error reading from database.", e);
        }
        return transactions;
    }

    @Override
    public CompletableFuture<Void> addTransaction(BankLog.Transaction transaction) {
        return CompletableFuture.runAsync(() -> {
            synchronized (connector.ps) {
                try {
                    final PreparedStatement insertTransaction = connector.ps.get(BankSQL.INSERT_TRANSACTION);
                    insertTransaction.setString(1, clanId);
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

    static String validateQuotedSubstring(String tableName) throws IllegalArgumentException {
        if (tableName.contains(" ") || tableName.contains("`")) {
            throw new IllegalArgumentException("Illegal table substring.");
        }
        return tableName;
    }
}
