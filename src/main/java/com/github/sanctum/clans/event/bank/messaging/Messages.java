package com.github.sanctum.clans.event.bank.messaging;

import com.github.sanctum.clans.model.ClansAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public enum Messages {
	BANK_HELP_PREFIX(null, "&6/clan &fbank"),
	// "Valid" section of Message.yml
	BANKS_HEADER("bank-header"),
	BANKS_GREETING("bank-greeting"),
	BANKS_GREETING_HOVER("bank-greeting-hover"),
	BANKS_CURRENT_BALANCE("bank-current-balance"),
	BANKS_COMMAND_LISTING("bank-command-listing"),
	BANK_USAGE("bank-command-usage"),
	HOVER_BALANCE("bank-hover-balance"),
	HOVER_DEPOSIT("bank-hover-deposit"),
	HOVER_WITHDRAW("bank-hover-withdraw"),
	DEPOSIT_MSG_PLAYER("bank-deposit-message-player"),
	WITHDRAW_MSG_PLAYER("bank-withdraw-message-player"),
	// "Non-Valid" section
	PLAYER_NO_CLAN("no-clan"),
	DEPOSIT_ERR_PLAYER("bank-deposit-error-player"),
	WITHDRAW_ERR_PLAYER("bank-withdraw-error-player"),
	AMOUNT("bank-amount"),
	HOVER_NO_AMOUNT("bank-amount-hover"),
	BANK_INVALID_AMOUNT("bank-invalid-amount"),
	BANK_INVALID_SUBCOMMAND("bank-invalid-subcommand"),
	PERM_NOT_PLAYER_COMMAND("bank-no-permission-command"),
	// new Bank event logging section
	PRETRANSACTION_SUCCESS("bank-pretransaction-success"),
	PRETRANSACTION_FAILURE("bank-pretransaction-failure"),
	TRANSACTION_DEPOSIT_PRE("bank-transaction-deposit-pre"),
	TRANSACTION_DEPOSIT_PRE_CANCELLED("bank-transaction-deposit-pre-cancelled"),
	TRANSACTION_WITHDRAW_PRE("bank-transaction-withdraw-pre"),
	TRANSACTION_WITHDRAW_PRE_CANCELLED("bank-transaction-withdraw-pre-cancelled"),
	TRANSACTION_SUCCESS("bank-transaction-success"),
	TRANSACTION_FAILURE("bank-transaction-failed"),
	TRANSACTION_DEPOSIT("bank-transaction-deposit"),
	TRANSACTION_WITHDRAW("bank-transaction-withdraw"),
	TRANSACTION_VERBOSE_CLAN_ID("bank-verbose-clan-id");

	final @Nullable String messagesNode;
	final @Nullable String hardcoded;

	Messages(@NotNull String messagesNode) {
		this.messagesNode = messagesNode;
		this.hardcoded = null;
	}

	Messages(@Nullable String messagesNode, @NotNull String hardcoded) {
		this.messagesNode = messagesNode;
		this.hardcoded = hardcoded;
	}

	@Nullable
	public String get() {
		return Optional.ofNullable(messagesNode)
				.map(ClansAPI.getDataInstance()::getMessageResponse)
				.orElse(hardcoded);
    }

    @Override
    public String toString() {
		return String.valueOf(get());
    }
}
