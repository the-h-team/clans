package com.github.sanctum.clans.construct.bank;

import com.github.sanctum.clans.event.bank.BankTransactionEvent;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.sanctum.labyrinth.interfacing.Nameable;
import net.md_5.bungee.api.ChatColor;

public class BankLog implements Serializable {
    private static final long serialVersionUID = -3400485111996318187L;

    public static class Transaction implements Serializable {
        private static final long serialVersionUID = -679847970264259944L;
        public final String entity;
        public final BankTransactionEvent.Type type;
        public final BigDecimal amount;
        public final LocalDateTime localDateTime;

        public Transaction(String entity, BankTransactionEvent.Type type, BigDecimal amount) {
            this(entity, type, amount, LocalDateTime.now());
        }

        public Transaction(String entity, BankTransactionEvent.Type type, BigDecimal amount, LocalDateTime localDateTime) {
            this.entity = entity;
            this.type = type;
            this.amount = amount;
            this.localDateTime = localDateTime;
        }

        @Override
        public String toString() {
            return ChatColor.translateAlternateColorCodes('&',
                    String.format("&6%s %s &f{0} &7at &f%s".replace("{0}", amount.toString()),
                            entity,
                            (type == BankTransactionEvent.Type.DEPOSIT ? "&adeposited" : "&cwithdrew"),
                            localDateTime.format(DateTimeFormatter.ofPattern("h:mma '&7on&f' MMM dd',' yyyy"))));
        }

        public static Transaction from(BankTransactionEvent e) {
            return new Transaction(unwrapNameable(e.getEntity()), e.getTransactionType(), e.getAmount());
        }
    }

    final List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(BankTransactionEvent e) {
        transactions.add(new Transaction(unwrapNameable(e.getEntity()), e.getTransactionType(), e.getAmount()));
    }

    public void addTransaction(BankTransactionEvent e, LocalDateTime localDateTime) {
        transactions.add(new Transaction(unwrapNameable(e.getEntity()), e.getTransactionType(), e.getAmount(), localDateTime));
    }

    public final List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    private static String unwrapNameable(Nameable entity) {
        if (entity != null) { // Handles associates too
            return entity.getName();
        } else {
            return "";
        }
    }
}
