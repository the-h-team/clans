package io.github.sanctum.clans.api.action;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Thrown when an action is called with invalid arguments.
 *
 * @since 3.0.0
 * @see ActionRunner
 * @author ms5984
 */
public class InvalidArgumentsException extends RuntimeException {
    private static final long serialVersionUID = 3421856585687631875L;
    private final Map<String, ?> args;

    /**
     * Create a new exception with a message and arguments map.
     *
     * @param message the message
     * @param args the arguments map
     */
    public InvalidArgumentsException(@NotNull String message, @NotNull Map<String, ?> args) {
        super(message);
        this.args = args;
    }

    /**
     * Create a new exception with a message, arguments map and throwable.
     *
     * @param message the message
     * @param args the arguments map
     * @param cause the cause throwable
     */
    public InvalidArgumentsException(@NotNull String message, @NotNull Map<String, ?> args, Throwable cause) {
        super(message, cause);
        this.args = args;
    }

    /**
     * Get the arguments map.
     *
     * @return the arguments map
     */
    public Map<String, ?> getArgs() {
        return args;
    }
}
