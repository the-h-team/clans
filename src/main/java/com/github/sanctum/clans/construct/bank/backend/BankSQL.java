package com.github.sanctum.clans.construct.bank.backend;

/**
 * Various SQL statements for SQLConnector.
 *
 * @since 1.3.3
 */
public enum BankSQL {
    /**
     * <ol>
     *     <li>clan_id</li>
     *     <li>balance</li>
     *     <li>balance_level</li>
     *     <li>deposit_level</li>
     *     <li>withdraw_level</li>
     *     <li>viewlog_level</li>
     * </ol>
     */
    INIT_CLAN("INSERT INTO `?` (clan_id, balance, balance_level, deposit_level, withdraw_level, viewlog_level) VALUES (?, ?, ?, ?, ?, ?)"),
    /**
     * <ol><li>clan_id</li></ol>
     */
    HAS_ENTRY("SELECT * FROM `?` WHERE clan_id = ?"),
    /**
     * <ol>
     *     <li>clan_id</li>
     *     <li>test amount</li>
     * </ol>
     */
    HAS_BALANCE("SELECT balance FROM `?` WHERE clan_id = ? AND balance >= ?"),
    /**
     * <ol><li>clan_id</li></ol>
     */
    READ_BALANCE("SELECT balance FROM `?` WHERE clan_id = ?"),
    /**
     * <ol>
     *     <li>new balance</li>
     *     <li>clan_id</li>
     * </ol>
     */
    UPDATE_BALANCE("UPDATE `?` SET balance = ? WHERE `?`.clan_id = ?"),
    /**
     * <ol><li>clan_id</li></ol>
     */
    READ_BALANCE_LEVEL("SELECT balance_level FROM `?` WHERE clan_id = ?"),
    /**
     * <ol>
     *     <li>new level</li>
     *     <li>clan_id</li>
     * </ol>
     */
    UPDATE_BALANCE_LEVEL("UPDATE `?` SET balance_level = ? WHERE `?`.clan_id = ?"),
    /**
     * <ol><li>clan_id</li></ol>
     */
    READ_DEPOSIT_LEVEL("SELECT deposit_level FROM `?` WHERE clan_id = ?"),
    /**
     * <ol>
     *     <li>new level</li>
     *     <li>clan_id</li>
     * </ol>
     */
    UPDATE_DEPOSIT_LEVEL("UPDATE `?` SET deposit_level = ? WHERE `?`.clan_id = ?"),
    /**
     * <ol><li>clan_id</li></ol>
     */
    READ_WITHDRAW_LEVEL("SELECT withdraw_level FROM `?` WHERE clan_id = ?"),
    /**
     * <ol>
     *     <li>new level</li>
     *     <li>clan_id</li>
     * </ol>
     */
    UPDATE_WITHDRAW_LEVEL("UPDATE `?` SET withdraw_level = ? WHERE `?`.clan_id = ?"),
    /**
     * <ol><li>clan_id</li></ol>
     */
    READ_VIEWLOG_LEVEL("SELECT viewlog_level FROM `?` WHERE clan_id = ?"),
    /**
     * <ol>
     *     <li>new level</li>
     *     <li>clan_id</li>
     * </ol>
     */
    UPDATE_VIEWLOG_LEVEL("UPDATE `?` SET viewlog_level = ? WHERE `?`.clan_id = ?"),
    /**
     * <ol><li>clan_id</li></ol>
     */
    READ_IS_DISABLED("SELECT disabled FROM `?` WHERE clan_id = ?"),
    /**
     * <ol>
     *     <li>isDisabled</li>
     *     <li>clan_id</li>
     * </ol>
     */
    UPDATE_IS_DISABLED("UPDATE `?` SET disabled = ? WHERE `?`.clan_id = ?"),
    /**
     * <ol><li>clan_id</li></ol>
     */
    READ_TRANSACTIONS("SELECT id, entity, type, amount, t_time FROM `?` WHERE clan_id = ? ORDER BY t_time"),
    /**
     * <ol>
     *     <li>clan_id</li>
     *     <li>entity</li>
     *     <li>type</li>
     *     <li>amount</li>
     *     <li>timestamp</li>
     * </ol>
     */
    INSERT_TRANSACTION("INSERT INTO `?` (clan_id, entity, type, amount, t_time) VALUES (?, ?, ?, ?, ?)"),
    ;
    private final String sql;

    BankSQL(String sql) {
        this.sql = sql;
    }

    public String statementFor(String table) {
        return sql.replace("`?`", "`" + table + "`");
    }
}
