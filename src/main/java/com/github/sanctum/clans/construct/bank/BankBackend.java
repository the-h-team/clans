package com.github.sanctum.clans.construct.bank;

import com.github.sanctum.clans.construct.api.Clan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Tests if the bank has at least the specified amount.
     *
     * @param clan the clan to test the balance of
     * @param amount an amount to test
     * @return a CompletableFuture expressing balance &gt;= <code>amount</code>
     * @implNote It is possible that a bank does not yet have a balance stored
     * in the backend. In this case, the bank's balance will be treated as the
     * default starting balance.
     */
    default CompletableFuture<Boolean> hasBalance(@NotNull Clan clan, @NotNull BigDecimal amount) {
        return compareBalance(clan, amount).thenApply(i -> i >= 0);
    }

    /**
     * Reads the bank's balance from the backend.
     *
     * @param clan the clan to read the balance of
     * @return a CompletableFuture expressing the bank's balance
     */
    CompletableFuture<@Nullable BigDecimal> readBalance(@NotNull Clan clan);

    /**
     * Updates the bank's balance on the backend.
     *
     * @param clan the clan to update the balance of
     * @param balance a new balance
     * @return a CompletableFuture indicating the update's completion status
     */
    CompletableFuture<Void> updateBalance(@NotNull Clan clan, BigDecimal balance);

    /**
     * Tests the bank's balance to be greater than,
     * equal to or smaller than a given amount.
     *
     * @param clan the clan to test the bank balance of
     * @param testAmount an amount to test
     * @return a CompletableFuture of integer value the -1, 0 or 1
     * dependent on whether the bank's balance is less than, equal to
     * or greater than <code>testAmount</code>, respectively
     */
    CompletableFuture<Integer> compareBalance(@NotNull Clan clan, @NotNull BigDecimal testAmount);

    /**
     * Reads if the bank has been disabled from the backend.
     *
     * @param clan the clan to read the bank disabled state of
     * @return a CompletableFuture expressing if the bank has been disabled
     */
    CompletableFuture<Boolean> readIsDisabled(@NotNull Clan clan);

    /**
     * Updates if the bank is disabled on the backend.
     *
     * @param clan the clan to update the bank disabled state of
     * @param isDisabled whether the bank is enabled
     * @return a CompletableFuture expressing the previous disabled state
     */
    CompletableFuture<Boolean> updateIsDisabled(@NotNull Clan clan, boolean isDisabled);

    /**
     * Reads the bank's transaction data from the backend.
     *
     * @param clan the clan to read the bank transaction data of
     * @return a CompletableFuture expressing the bank's transaction data
     */
    CompletableFuture<List<BankLog.Transaction>> readTransactions(@NotNull Clan clan);

    /**
     * Directly update the bank's transaction log on the backend.
     *
     * @param clan the clan to update the transaction log of
     * @param log a new transaction log
     * @return a CompletableFuture expressing the updated transaction log
     */
    CompletableFuture<Void> updateTransactionLog(@NotNull Clan clan, @NotNull BankLog log);

    /**
     * Adds a bank transaction on the backend.
     *
     * @param clan the clan to add the transaction to
     * @param transaction a new transaction
     * @return a CompletableFuture indicating the completion status of the add
     */
    CompletableFuture<Void> addTransaction(@NotNull Clan clan, @NotNull BankLog.Transaction transaction);
}
