package com.github.sanctum.clans.construct.bank;

import com.github.sanctum.clans.construct.DataManager;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public enum BankPermissions {
    BANKS_USE("bank"),
    BANKS_BALANCE("bank-balance"),
    BANKS_DEPOSIT("bank-deposit"),
    BANKS_WITHDRAW("bank-withdraw"),
    BANKS_USE_STAR(use -> use.getNode() + ".*", BANKS_USE),
    BANKS_STAR(null, "clanspro.banks.*");

    private final String messagesFileNode;
    final String hardcoded;

    BankPermissions(String messagesFileNode) {
        this.messagesFileNode = messagesFileNode;
        this.hardcoded = null;
    }

    BankPermissions(Function<BankPermissions, String> generator, BankPermissions ordinal) {
        this(null, generator.apply(ordinal));
    }

    BankPermissions(@Nullable String messagesFileNode, @NotNull String hardcoded) {
        this.messagesFileNode = messagesFileNode;
        this.hardcoded = hardcoded;
    }

    public String getNode() {
        return Optional.ofNullable(messagesFileNode)
                .map(DataManager.Security::getPermission)
                .orElse(hardcoded);
    }

    public boolean not(CommandSender sender) {
        return !sender.hasPermission(getNode());
    }
}

