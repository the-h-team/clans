package com.github.sanctum.clans.construct.bank;

import org.bukkit.command.CommandSender;

public enum BankPermissions {
    BANKS_STAR("clanspro.banks.*"),
    BANKS_USE("clanspro.banks.use"),
    BANKS_USE_STAR("clanspro.banks.use.*"),
    BANKS_BALANCE("clanspro.banks.use.balance"),
    BANKS_DEPOSIT("clanspro.banks.use.deposit"),
    BANKS_WITHDRAW("clanspro.banks.use.withdraw");

    public final String node;

    BankPermissions(String s) {
        this.node = s;
    }

    public boolean not(CommandSender sender) {
        return !sender.hasPermission(node);
    }
}

