package com.github.sanctum.clans.construct.bank.backend;

import com.github.sanctum.clans.construct.api.ClansAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;

/**
 * Manages a persistent connection to the bank database.
 *
 * @since 1.3.3
 */
class SQLConnector {
    final Connection connection;
    final String table;
    final String transactionTable;
    transient final EnumMap<BankSQL, PreparedStatement> ps = new EnumMap<>(BankSQL.class);
//    transient final PreparedStatement insertTransactionAutoTime;

    public SQLConnector(Connection connection, String tablePrefix, String clan_data_table) throws SQLException, IllegalArgumentException {
        this.connection = connection;
        table = validateQuotedSubstring(tablePrefix) + "bank_data";
        transactionTable = validateQuotedSubstring(tablePrefix) + "bank_transactions";
        // Check for existing entry
        if (!tablesExist()) {
            createTables(clan_data_table);
        }
        // clan initialization
        ps.put(BankSQL.INIT_CLAN, connection.prepareStatement(BankSQL.INIT_CLAN.statementFor(table)));
        // clan has entry
        ps.put(BankSQL.HAS_ENTRY, connection.prepareStatement(BankSQL.HAS_ENTRY.statementFor(table)));
        // balance
        ps.put(BankSQL.HAS_BALANCE, connection.prepareStatement(BankSQL.HAS_BALANCE.statementFor(table)));
        ps.put(BankSQL.READ_BALANCE, connection.prepareStatement(BankSQL.READ_BALANCE.statementFor(table)));
        ps.put(BankSQL.UPDATE_BALANCE, connection.prepareStatement(BankSQL.UPDATE_BALANCE.statementFor(table)));
        // levels
        ps.put(BankSQL.READ_BALANCE_LEVEL, connection.prepareStatement(BankSQL.READ_BALANCE_LEVEL.statementFor(table)));
        ps.put(BankSQL.UPDATE_BALANCE_LEVEL, connection.prepareStatement(BankSQL.UPDATE_BALANCE_LEVEL.statementFor(table)));
        ps.put(BankSQL.READ_DEPOSIT_LEVEL, connection.prepareStatement(BankSQL.READ_DEPOSIT_LEVEL.statementFor(table)));
        ps.put(BankSQL.UPDATE_DEPOSIT_LEVEL, connection.prepareStatement(BankSQL.UPDATE_DEPOSIT_LEVEL.statementFor(table)));
        ps.put(BankSQL.READ_WITHDRAW_LEVEL, connection.prepareStatement(BankSQL.READ_WITHDRAW_LEVEL.statementFor(table)));
        ps.put(BankSQL.UPDATE_WITHDRAW_LEVEL, connection.prepareStatement(BankSQL.UPDATE_WITHDRAW_LEVEL.statementFor(table)));
        ps.put(BankSQL.READ_VIEWLOG_LEVEL, connection.prepareStatement(BankSQL.READ_VIEWLOG_LEVEL.statementFor(table)));
        ps.put(BankSQL.UPDATE_VIEWLOG_LEVEL, connection.prepareStatement(BankSQL.UPDATE_VIEWLOG_LEVEL.statementFor(table)));
        // disabled
        ps.put(BankSQL.READ_IS_DISABLED, connection.prepareStatement(BankSQL.READ_IS_DISABLED.statementFor(table)));
        ps.put(BankSQL.UPDATE_IS_DISABLED, connection.prepareStatement(BankSQL.UPDATE_IS_DISABLED.statementFor(table)));
        // transactions
        ps.put(BankSQL.READ_TRANSACTIONS, connection.prepareStatement(BankSQL.READ_TRANSACTIONS.statementFor(transactionTable)));
        ps.put(BankSQL.INSERT_TRANSACTION, connection.prepareStatement(BankSQL.INSERT_TRANSACTION.statementFor(transactionTable)));
//        insertTransactionAutoTime = connection.prepareStatement("INSERT INTO " + transactionTable + " (clan_id, entity, type, amount) VALUES (?, ?, ?, ?)");
//        insertTransactionAutoTime.setString(1, clanId);
    }

    boolean tablesExist() throws SQLException {
        final Statement statement;
        synchronized (connection) {
            statement = connection.createStatement();
            try {
                statement.executeQuery("SELECT * FROM `" + table + "`");
                statement.executeQuery("SELECT * FROM `" + transactionTable + "`");
            } catch (SQLException e) {
                return false;
            } finally {
                statement.close();
            }
        }
        return true;
    }

    void createTables(String clan_data_table) throws SQLException, IllegalArgumentException {
        validateQuotedSubstring(clan_data_table);
        synchronized (connection) {
            connection.setAutoCommit(false);
            final Statement statement = connection.createStatement();
            // create tables
            statement.addBatch("CREATE TABLE `" + table + "` (" +
                    "`clan_id` varchar(14) NOT NULL COMMENT 'The clan''s id'," +
                    " `balance` decimal(30,4) NOT NULL DEFAULT 0.0000 COMMENT 'The balance of the clan bank'," +
                    " `balance_level` tinyint(4) NOT NULL DEFAULT 0 COMMENT 'The clan level needed to check its bank balance'," +
                    " `deposit_level` tinyint(4) NOT NULL DEFAULT 1 COMMENT 'The clan level needed to deposit money'," +
                    " `withdraw_level` tinyint(4) NOT NULL DEFAULT 2 COMMENT 'The clan level needed to withdraw money'," +
                    " `viewlog_level` tinyint(4) NOT NULL DEFAULT 3 COMMENT 'The clan level needed to view bank logs'," +
                    " `disabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Indicates whether this clan bank has been disabled'" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Main bank data'");
            statement.addBatch("CREATE TABLE `" + transactionTable + "` (" +
                    "`id` int(11) NOT NULL," +
                    " `clan_id` varchar(14) NOT NULL COMMENT 'The clan id'," +
                    " `entity` varchar(36) NOT NULL DEFAULT '?' COMMENT 'The formatted name of the involved entity'," +
                    " `type` enum('DEPOSIT','WITHDRAWAL') NOT NULL COMMENT 'The transaction type'," +
                    " `amount` decimal(30,4) NOT NULL COMMENT 'The amount of the transaction'," +
                    " `t_time` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'The exact time the transaction took place'" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='All bank transactions'"
            );
            // add indices
            statement.addBatch("ALTER TABLE `" + table + "`" +
                    " ADD PRIMARY KEY (`clan_id`)," +
                    " ADD KEY `balance` (`balance`)," +
                    " ADD KEY `balances` (`clan_id`,`balance`) USING BTREE"
            );
            statement.addBatch("ALTER TABLE `" + transactionTable + "`" +
                    " ADD PRIMARY KEY (`id`)," +
                    " ADD KEY `parties` (`clan_id`,`entity`) USING BTREE," +
                    " ADD KEY `types` (`clan_id`,`type`) USING BTREE," +
                    " ADD KEY `timestamp` (`clan_id`,`t_time`) USING BTREE," +
                    " ADD KEY `amount` (`amount`)," +
                    " ADD KEY `entity` (`entity`)," +
                    " ADD KEY `t_time` (`t_time`)"
            );
            // set field a_i
            statement.addBatch("ALTER TABLE `" + transactionTable + "` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT");
            // setup foreign keys
            statement.addBatch("ALTER TABLE `" + table + "` ADD CONSTRAINT `bank_data_ibfk_1`" +
                    " FOREIGN KEY (`clan_id`) REFERENCES `" + clan_data_table + "` (`clan_id`)"
            );
            statement.addBatch("ALTER TABLE `" + transactionTable + "` ADD CONSTRAINT `bank_transactions_ibfk_1`" +
                    " FOREIGN KEY (`clan_id`) REFERENCES `" + clan_data_table + "` (`clan_id`) ON DELETE CASCADE ON UPDATE CASCADE"
            );
            statement.executeBatch();
            statement.close();
            connection.commit();
            connection.setAutoCommit(true);
        }
    }

    public void shutdown() throws SQLException {
        // cleanup prepared statements
        synchronized (ps) {
            for (PreparedStatement entry : ps.values()) {
                entry.close();
            }
        }
    }

    void initBank(String clanId) throws SQLException {
        synchronized (ps) {
            final PreparedStatement initBank = ps.get(BankSQL.INIT_CLAN);
            initBank.setString(1, clanId);
            initBank.setBigDecimal(2, ClansAPI.getBankInstance().startingBalance());
            initBank.setInt(3, ClansAPI.getDataInstance().getConfigInt("Clans.banks.default-access.balance"));
            initBank.setInt(4, ClansAPI.getDataInstance().getConfigInt("Clans.banks.default-access.deposit"));
            initBank.setInt(5, ClansAPI.getDataInstance().getConfigInt("Clans.banks.default-access.withdraw"));
            initBank.setInt(6, ClansAPI.getDataInstance().getConfigInt("Clans.banks.default-access.view-log"));
            initBank.executeUpdate();
        }
    }

    static String validateQuotedSubstring(String tableName) throws IllegalArgumentException {
        if (tableName.contains(" ") || tableName.contains("`")) {
            throw new IllegalArgumentException("Illegal table substring.");
        }
        return tableName;
    }
}
