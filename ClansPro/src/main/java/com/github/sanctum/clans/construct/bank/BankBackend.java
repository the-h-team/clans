package com.github.sanctum.clans.construct.bank;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages bank data storage.
 * <p>
 * All operations are exposed as CompletableFutures
 * to allow for async but sequential operations.
 *
 * @since 1.3.3
 */
public interface BankBackend {
    // Replacing fields from Bank.class
    /**
     * Read the bank's balance from the backend.
     *
     * @return a CompletableFuture expressing the bank's balance
     */
    CompletableFuture<BigDecimal> readBalance();

    /**
     * Update the bank's balance on the backend.
     *
     * @param balance a new balance
     * @return a CompletableFuture indicating the update's completion status
     */
    CompletableFuture<Void> updateBalance(BigDecimal balance);

    /**
     * Test the bank's balance to be greater than,
     * equal to or smaller than a given amount.
     *
     * @param testAmount an amount to test
     * @return a CompletableFuture of integer value the -1, 0 or 1
     * dependent on whether the bank's balance is less than, equal to
     * or greater than <code>testAmount</code>, respectively
     */
    CompletableFuture<Integer> compareBalance(@NotNull BigDecimal testAmount);

    /**
     * Read if the bank is enabled from the backend.
     *
     * @return true if the bank is enabled
     */
    CompletableFuture<Boolean> readEnabled();

    /**
     * Update if the bank is enabled on the backend.
     *
     * @param enabled whether the bank is enabled
     */
    CompletableFuture<Void> updateEnabled(boolean enabled);

    // Moving from BankMeta
    /**
     * Read the bank's access map from the backend.
     *
     * @return a CompletableFuture expressing the bank's access map
     */
    CompletableFuture<BankAction.AccessMap> readAccessMap();

    /**
     * Update the bank's access map on the backend.
     *
     * @param accessMap a modified access map
     * @return a CompletableFuture indicating the update's completion status
     */
    CompletableFuture<Void> updateAccessMap(BankAction.AccessMap accessMap);

    /**
     * Read the bank's transaction data from the backend.
     *
     * @return a CompletableFuture expressing the bank's transaction data
     */
    CompletableFuture<List<BankLog.Transaction>> readTransactions();

    /**
     * Add a bank transaction on the backend.
     *
     * @param transaction a new transaction
     * @return a CompletableFuture indicating the completion status of the add
     */
    CompletableFuture<Void> addTransaction(BankLog.Transaction transaction);

    /**
     * Update the bank's log on the backend.
     *
     * @param bankLog a bank log object
     * @return a CompletableFuture indicating the update's completion status
     */
    CompletableFuture<Void> updateBankLog(BankLog bankLog);
}
