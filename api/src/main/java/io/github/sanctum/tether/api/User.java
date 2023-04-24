package io.github.sanctum.tether.api;

import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identified;

/**
 * Represents an end user.
 * <p>
 * This is generally a player, but could also be console or a custom entity.
 *
 * @since 0.0.1
 */
public interface User extends ForwardingAudience, Identified {
    /**
     * Whether this user represents a console (non-player).
     *
     * @return true if this user is a console
     */
    default boolean isConsole() {
        return false;
    }

    /**
     * Whether this user represents a custom entity.
     * <p>
     * Players should always return false.
     * <p>
     * The functionality of users representing custom entities may be limited.
     *
     * @return true if this user is a custom entity
     */
    default boolean isCustom() {
        return false;
    }
}
