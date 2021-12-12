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
    /**
     * Test if the bank has at least the specified amount.
     *
     * @param amount an amount to test
     * @return a CompletableFuture expressing balance &gt;= <code>amount</code>
     */
    default CompletableFuture<Boolean> hasBalance(BigDecimal amount) {
        return compareBalance(amount).thenApply(i -> i >= 0);
    }

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
     * Read if the bank has been disabled from the backend.
     *
     * @return a CompletableFuture expressing if the bank has been disabled
     */
    CompletableFuture<Boolean> readIsDisabled();

    /**
     * Update if the bank is disabled on the backend.
     *
     * @param isDisabled whether the bank is enabled
     * @return a CompletableFuture expressing the previous disabled state
     */
    CompletableFuture<Boolean> updateIsDisabled(boolean isDisabled);

    // Moving from BankMeta
    /**
     * Read the bank's access level for a particular action from the backend.
     *
     * @return a CompletableFuture expressing the action's access level
     */
    CompletableFuture<Integer> readAccess(BankAction action);

    /**
     * Update the bank's access level for a particular action on the backend.
     *
     * @param action an action
     * @param level a clan access level
     * @return a CompletableFuture expressing the previous access level
     */
    CompletableFuture<Integer> updateAccess(BankAction action, int level);

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
}
