package com.github.sanctum.clans.event.bank.messaging;

import com.github.sanctum.clans.construct.api.ClansAPI;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public enum Messages {
	BANK_HELP_PREFIX(() -> "&6/clan &fbank"),
	// "Valid" section of Message.yml
	BANKS_HEADER(() -> ClansAPI.getDataInstance().getMessageResponse("bank-header")),
	BANKS_GREETING(() -> ClansAPI.getDataInstance().getMessageResponse("bank-greeting")),
	BANKS_GREETING_HOVER(() -> ClansAPI.getDataInstance().getMessageResponse("bank-greeting-hover")),
	BANKS_CURRENT_BALANCE(() -> ClansAPI.getDataInstance().getMessageResponse("bank-current-balance")),
	BANKS_COMMAND_LISTING(() -> ClansAPI.getDataInstance().getMessageResponse("bank-command-listing")),
	BANK_USAGE(() -> ClansAPI.getDataInstance().getMessageResponse("bank-command-usage")),
	HOVER_BALANCE(() -> ClansAPI.getDataInstance().getMessageResponse("bank-hover-balance")),
	HOVER_DEPOSIT(() -> ClansAPI.getDataInstance().getMessageResponse("bank-hover-deposit")),
	HOVER_WITHDRAW(() -> ClansAPI.getDataInstance().getMessageResponse("bank-hover-withdraw")),
	DEPOSIT_MSG_PLAYER(() -> ClansAPI.getDataInstance().getMessageResponse("bank-deposit-message-player")),
	WITHDRAW_MSG_PLAYER(() -> ClansAPI.getDataInstance().getMessageResponse("bank-withdraw-message-player")),
	// "Non-Valid" section
	PLAYER_NO_CLAN(() -> ClansAPI.getDataInstance().getMessageResponse("no-clan")),
	DEPOSIT_ERR_PLAYER(() -> ClansAPI.getDataInstance().getMessageResponse("bank-deposit-error-player")),
	WITHDRAW_ERR_PLAYER(() -> ClansAPI.getDataInstance().getMessageResponse("bank-withdraw-error-player")),
	AMOUNT(() -> ClansAPI.getDataInstance().getMessageResponse("bank-amount")),
	HOVER_NO_AMOUNT(() -> ClansAPI.getDataInstance().getMessageResponse("bank-amount-hover")),
	BANK_INVALID_AMOUNT(() -> ClansAPI.getDataInstance().getMessageResponse("bank-invalid-amount")),
	BANK_INVALID_SUBCOMMAND(() -> ClansAPI.getDataInstance().getMessageResponse("bank-invalid-subcommand")),
	PERM_NOT_PLAYER_COMMAND(() -> ClansAPI.getDataInstance().getMessageResponse("bank-no-permission-command")),
	// new Bank event logging section
	PRETRANSACTION_SUCCESS(() -> ClansAPI.getDataInstance().getMessageResponse("bank-pretransaction-success")),
	PRETRANSACTION_FAILURE(() -> ClansAPI.getDataInstance().getMessageResponse("bank-pretransaction-failure")),
	TRANSACTION_DEPOSIT_PRE(() -> ClansAPI.getDataInstance().getMessageResponse("bank-transaction-deposit-pre")),
	TRANSACTION_DEPOSIT_PRE_CANCELLED(() -> ClansAPI.getDataInstance().getMessageResponse("bank-transaction-deposit-pre-cancelled")),
	TRANSACTION_WITHDRAW_PRE(() -> ClansAPI.getDataInstance().getMessageResponse("bank-transaction-withdraw-pre")),
	TRANSACTION_WITHDRAW_PRE_CANCELLED(() -> ClansAPI.getDataInstance().getMessageResponse("bank-transaction-withdraw-pre-cancelled")),
	TRANSACTION_SUCCESS(() -> ClansAPI.getDataInstance().getMessageResponse("bank-transaction-success")),
	TRANSACTION_FAILURE(() -> ClansAPI.getDataInstance().getMessageResponse("bank-transaction-failed")),
	TRANSACTION_DEPOSIT(() -> ClansAPI.getDataInstance().getMessageResponse("bank-transaction-deposit")),
	TRANSACTION_WITHDRAW(() -> ClansAPI.getDataInstance().getMessageResponse("bank-transaction-withdraw")),
	TRANSACTION_VERBOSE_CLAN_ID(() -> ClansAPI.getDataInstance().getMessageResponse("bank-verbose-clan-id"));

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
