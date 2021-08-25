package com.github.sanctum.clans.events.core.bank;

import com.github.sanctum.clans.construct.api.ClanBank;
import com.github.sanctum.clans.events.core.bank.messaging.Messages;
import java.math.BigDecimal;
import org.bukkit.entity.Player;

public class BankTransactionEvent extends BankActionEvent {

    public enum Type {
        DEPOSIT, WITHDRAWAL
    }

    protected final Player player;
    protected final BigDecimal amount;
    protected final boolean success;
    protected final Type type;

    public BankTransactionEvent(Player player, ClanBank clanBank, BigDecimal amount, String clanId, boolean success, Type type) {
        super(clanBank, clanId);
        this.player = player;
        this.amount = amount;
        this.success = success;
        this.type = type;
    }

    public BankTransactionEvent(BankTransactionEvent event) {
        super(event.clanBank, event.clanId);
        this.player = event.player;
        this.amount = event.amount;
        this.success = event.success;
        this.type = event.type;
    }

    /**
     * Get the player associated with this transaction
     *
     * @return Player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the ClanBank associated with this transaction
     *
     * @return the ClanBank
     */
    @SuppressWarnings("EmptyMethod")
    @Override
    public ClanBank getClanBank() {
        return super.getClanBank();
    }

    /**
     * Get the amount involved with this transaction
     *
     * @return a BigDecimal amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Denotes whether or not the transaction was successful
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * A transaction may constitute a deposit or withdrawal
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
                        .replace("{1}", player.getName())
                        .replace("{2}", amount.toString())
                        .replace("{3}", getClan().getName());
            case WITHDRAWAL:
                return Messages.TRANSACTION_WITHDRAW.toString()
                        .replace("{0}", this.success ? Messages.TRANSACTION_SUCCESS.toString() : Messages.TRANSACTION_FAILURE.toString())
                        .replace("{1}", player.getName())
                        .replace("{2}", amount.toString())
                        .replace("{3}", getClan().getName());
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
