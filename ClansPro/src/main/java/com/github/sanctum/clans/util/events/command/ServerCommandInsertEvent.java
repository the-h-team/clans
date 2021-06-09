package com.github.sanctum.clans.util.events.command;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.actions.ClanAction;
import com.github.sanctum.clans.util.StringLibrary;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called to add custom console-side '/clan' subcommands.
 *
 * @author msanders5984
 */
@Deprecated
public final class ServerCommandInsertEvent extends ClanEventBuilder {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String[] args;
    private String errorMessage;
    private boolean isCommand;

    /**
     * Create a new ServerCommandInsertEvent with the given arguments.
     *
     * @param args arguments
     */
    public ServerCommandInsertEvent(String[] args) {
        this.args = args;
    }

    /**
     * Get the arguments provided to this event.
     *
     * @return array of arguments
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Get the custom error message, if one has been set.
     *
     * @return an Optional describing the message
     */
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    /**
     * Set a custom error message to log to the console.
     *
     * @param errorMessage custom message or null to disable
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Get whether the args represent a valid subcommand.
     *
     * @return args matched valid subcommand
     */
    public boolean getReturn() {
        return this.isCommand;
    }

    /**
     * Set whether the args specified represent a valid subcommand.
     *
     * @param isCommand args match valid subcommand
     */
    public void setReturn(boolean isCommand) {
        this.isCommand = isCommand;
    }

    /**
     * Get the Console's CommandSender.
     *
     * <p>Useful for messaging.</p>
     *
     * @return server's ConsoleSender
     */
    public ConsoleCommandSender getConsoleSender() {
        return Bukkit.getConsoleSender();
    }

    // builder utils

    @Override
    public ClanAction getUtil() {
        return DefaultClan.action;
    }

    @Override
    public StringLibrary stringLibrary() {
        return DefaultClan.action;
    }

    // event impl

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
