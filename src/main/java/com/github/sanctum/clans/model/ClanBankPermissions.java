package com.github.sanctum.clans.model;

import com.github.sanctum.clans.DataManager;

import java.util.Optional;
import java.util.function.Function;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ClanBankPermissions {
    BANKS_USE("bank"),
    BANKS_BALANCE("bank-balance"),
    BANKS_DEPOSIT("bank-deposit"),
    BANKS_WITHDRAW("bank-withdraw"),
    BANKS_USE_STAR(use -> use.getNode() + ".*", BANKS_USE),
    BANKS_STAR(null, "clans.banks.*");

    private final String messagesFileNode;
    final String hardcoded;

    ClanBankPermissions(String messagesFileNode) {
        this.messagesFileNode = messagesFileNode;
        this.hardcoded = null;
    }

    ClanBankPermissions(Function<ClanBankPermissions, String> generator, ClanBankPermissions ordinal) {
        this(null, generator.apply(ordinal));
    }

    ClanBankPermissions(@Nullable String messagesFileNode, @NotNull String hardcoded) {
        this.messagesFileNode = messagesFileNode;
        this.hardcoded = hardcoded;
    }

    public String getNode() {
        return Optional.ofNullable(messagesFileNode)
                .map(DataManager.Security::getPermission)
                .orElse(hardcoded);
    }

    public boolean not(CommandSender sender) {
        return !Clan.ACTION.test(sender, "clans." + getNode()).deploy();
    }
}

