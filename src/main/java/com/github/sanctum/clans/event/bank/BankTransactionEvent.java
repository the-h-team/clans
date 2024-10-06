package com.github.sanctum.clans.event.bank;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.event.bank.messaging.Messages;
import java.math.BigDecimal;

import com.github.sanctum.labyrinth.interfacing.Nameable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BankTransactionEvent extends BankEvent {

    public enum Type {
        DEPOSIT, WITHDRAWAL
    }

    protected final Nameable entity;
    protected final BigDecimal amount;
    protected final boolean success;
    protected final Type type;

    public BankTransactionEvent(@NotNull Clan.Bank bank, @NotNull BigDecimal amount, @Nullable Nameable entity, boolean success, Type type) {
        super(bank, false);
        this.entity = entity;
        this.amount = amount;
        this.success = success;
        this.type = type;
    }

    public BankTransactionEvent(BankTransactionEvent event) {
        this(event.getBank(), event.amount, event.entity, event.success, event.type);
    }

    /**
     * Gets the entity associated with this transaction, if any.
     *
     * @return the entity or null if none
     */
    public final @Nullable Nameable getEntity() {
        return entity;
    }

    /**
     * Gets the amount of this transaction.
     *
     * @return a BigDecimal amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Indicates whether the transaction was successful.
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * A transaction may constitute a deposit or withdrawal.
     *
     * @return transaction type
     */
    public Type getTransactionType() {
        return type;
    }

    @Override
    public String toString() {
        switch (type) {
            case DEPOSIT:
                return Messages.TRANSACTION_DEPOSIT.toString()
                        .replace("{0}", this.success ? Messages.TRANSACTION_SUCCESS.toString() : Messages.TRANSACTION_FAILURE.toString())
                        .replace("{1}", entity.getName())
                        .replace("{2}", amount.toString())
                        .replace("{3}", getClan().getName());
            case WITHDRAWAL:
                return Messages.TRANSACTION_WITHDRAW.toString()
                        .replace("{0}", this.success ? Messages.TRANSACTION_SUCCESS.toString() : Messages.TRANSACTION_FAILURE.toString())
                        .replace("{1}", entity.getName())
                        .replace("{2}", amount.toString())
                        .replace("{3}", getClan().getName());
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
