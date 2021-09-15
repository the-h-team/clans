package com.github.sanctum.clans.events.core.bank.messaging;

import com.github.sanctum.clans.construct.api.ClansAPI;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public enum Messages {
	BANK_HELP_PREFIX(() -> "&6/clan &fbank"),
	// "Valid" section of Message.yml
	BANKS_HEADER(() -> ClansAPI.getData().getMessageResponse("bank-header")),
	BANKS_GREETING(() -> ClansAPI.getData().getMessageResponse("bank-greeting")),
	BANKS_GREETING_HOVER(() -> ClansAPI.getData().getMessageResponse("bank-greeting-hover")),
	BANKS_CURRENT_BALANCE(() -> ClansAPI.getData().getMessageResponse("bank-current-balance")),
	BANKS_COMMAND_LISTING(() -> ClansAPI.getData().getMessageResponse("bank-command-listing")),
	BANK_USAGE(() -> ClansAPI.getData().getMessageResponse("bank-command-usage")),
	HOVER_BALANCE(() -> ClansAPI.getData().getMessageResponse("bank-hover-balance")),
	HOVER_DEPOSIT(() -> ClansAPI.getData().getMessageResponse("bank-hover-deposit")),
	HOVER_WITHDRAW(() -> ClansAPI.getData().getMessageResponse("bank-hover-withdraw")),
	DEPOSIT_MSG_PLAYER(() -> ClansAPI.getData().getMessageResponse("bank-deposit-message-player")),
	WITHDRAW_MSG_PLAYER(() -> ClansAPI.getData().getMessageResponse("bank-withdraw-message-player")),
	// "Non-Valid" section
	PLAYER_NO_CLAN(() -> ClansAPI.getData().getMessageResponse("no-clan")),
	DEPOSIT_ERR_PLAYER(() -> ClansAPI.getData().getMessageResponse("bank-deposit-error-player")),
	WITHDRAW_ERR_PLAYER(() -> ClansAPI.getData().getMessageResponse("bank-withdraw-error-player")),
	AMOUNT(() -> ClansAPI.getData().getMessageResponse("bank-amount")),
	HOVER_NO_AMOUNT(() -> ClansAPI.getData().getMessageResponse("bank-amount-hover")),
	BANK_INVALID_AMOUNT(() -> ClansAPI.getData().getMessageResponse("bank-invalid-amount")),
	BANK_INVALID_SUBCOMMAND(() -> ClansAPI.getData().getMessageResponse("bank-invalid-subcommand")),
	PERM_NOT_PLAYER_COMMAND(() -> ClansAPI.getData().getMessageResponse("bank-no-permission-command")),
	// new Bank event logging section
	PRETRANSACTION_SUCCESS(() -> ClansAPI.getData().getMessageResponse("bank-pretransaction-success")),
	PRETRANSACTION_FAILURE(() -> ClansAPI.getData().getMessageResponse("bank-pretransaction-failure")),
	TRANSACTION_DEPOSIT_PRE(() -> ClansAPI.getData().getMessageResponse("bank-transaction-deposit-pre")),
	TRANSACTION_DEPOSIT_PRE_CANCELLED(() -> ClansAPI.getData().getMessageResponse("bank-transaction-deposit-pre-cancelled")),
	TRANSACTION_WITHDRAW_PRE(() -> ClansAPI.getData().getMessageResponse("bank-transaction-withdraw-pre")),
	TRANSACTION_WITHDRAW_PRE_CANCELLED(() -> ClansAPI.getData().getMessageResponse("bank-transaction-withdraw-pre-cancelled")),
	TRANSACTION_SUCCESS(() -> ClansAPI.getData().getMessageResponse("bank-transaction-success")),
	TRANSACTION_FAILURE(() -> ClansAPI.getData().getMessageResponse("bank-transaction-failed")),
	TRANSACTION_DEPOSIT(() -> ClansAPI.getData().getMessageResponse("bank-transaction-deposit")),
	TRANSACTION_WITHDRAW(() -> ClansAPI.getData().getMessageResponse("bank-transaction-withdraw")),
	TRANSACTION_VERBOSE_CLAN_ID(() -> ClansAPI.getData().getMessageResponse("bank-verbose-clan-id"));

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
