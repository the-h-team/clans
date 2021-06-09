package com.github.sanctum.clans.util.events.clans.bank.messaging;

import com.github.sanctum.clans.construct.api.ClansAPI;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public enum Messages {
    BANK_HELP_PREFIX(() -> "&6/clan &fbank"),
    // "Valid" section of Message.yml
    BANKS_HEADER(() -> ClansAPI.getData().getMessage("bank-header")),
    BANKS_GREETING(() -> ClansAPI.getData().getMessage("bank-greeting")),
    BANKS_GREETING_HOVER(() -> ClansAPI.getData().getMessage("bank-greeting-hover")),
    BANKS_CURRENT_BALANCE(() -> ClansAPI.getData().getMessage("bank-current-balance")),
    BANKS_COMMAND_LISTING(() -> ClansAPI.getData().getMessage("bank-command-listing")),
    BANK_USAGE(() -> ClansAPI.getData().getMessage("bank-command-usage")),
    HOVER_BALANCE(() -> ClansAPI.getData().getMessage("bank-hover-balance")),
    HOVER_DEPOSIT(() -> ClansAPI.getData().getMessage("bank-hover-deposit")),
    HOVER_WITHDRAW(() -> ClansAPI.getData().getMessage("bank-hover-withdraw")),
    DEPOSIT_MSG_PLAYER(() -> ClansAPI.getData().getMessage("bank-deposit-message-player")),
    WITHDRAW_MSG_PLAYER(() -> ClansAPI.getData().getMessage("bank-withdraw-message-player")),
    // "Non-Valid" section
    PLAYER_NO_CLAN(() -> ClansAPI.getData().getMessage("no-clan")),
    DEPOSIT_ERR_PLAYER(() -> ClansAPI.getData().getMessage("bank-deposit-error-player")),
    WITHDRAW_ERR_PLAYER(() -> ClansAPI.getData().getMessage("bank-withdraw-error-player")),
    AMOUNT(() -> ClansAPI.getData().getMessage("bank-amount")),
    HOVER_NO_AMOUNT(() -> ClansAPI.getData().getMessage("bank-amount-hover")),
    BANK_INVALID_AMOUNT(() -> ClansAPI.getData().getMessage("bank-invalid-amount")),
    BANK_INVALID_SUBCOMMAND(() -> ClansAPI.getData().getMessage("bank-invalid-subcommand")),
    PERM_NOT_PLAYER_COMMAND(() -> ClansAPI.getData().getMessage("bank-no-permission-command")),
    // new Bank event logging section
    PRETRANSACTION_SUCCESS(() -> ClansAPI.getData().getMessage("bank-pretransaction-success")),
    PRETRANSACTION_FAILURE(() -> ClansAPI.getData().getMessage("bank-pretransaction-failure")),
    TRANSACTION_DEPOSIT_PRE(() -> ClansAPI.getData().getMessage("bank-transaction-deposit-pre")),
    TRANSACTION_DEPOSIT_PRE_CANCELLED(() -> ClansAPI.getData().getMessage("bank-transaction-deposit-pre-cancelled")),
    TRANSACTION_WITHDRAW_PRE(() -> ClansAPI.getData().getMessage("bank-transaction-withdraw-pre")),
    TRANSACTION_WITHDRAW_PRE_CANCELLED(() -> ClansAPI.getData().getMessage("bank-transaction-withdraw-pre-cancelled")),
    TRANSACTION_SUCCESS(() -> ClansAPI.getData().getMessage("bank-transaction-success")),
    TRANSACTION_FAILURE(() -> ClansAPI.getData().getMessage("bank-transaction-failed")),
    TRANSACTION_DEPOSIT(() -> ClansAPI.getData().getMessage("bank-transaction-deposit")),
    TRANSACTION_WITHDRAW(() -> ClansAPI.getData().getMessage("bank-transaction-withdraw")),
    TRANSACTION_VERBOSE_CLAN_ID(() -> ClansAPI.getData().getMessage("bank-verbose-clan-id"));

    private final Supplier<String> s;

    Messages(Supplier<String> getter) {
        s = getter;
    }

    @Nullable
    public String get() {
        return s.get();
    }

    @Override
    public String toString() {
        final String s = get();
        return (s != null) ? s : "null";
    }

}
