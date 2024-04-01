package io.github.sanctum.clans.model.time;

import org.jetbrains.annotations.Contract;

/**
 * An object that maintains cooldowns.
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface CooldownScope {
    /**
     * Checks if this scope is tracking a cooldown with a given token.
     *
     * @param token the token to search for
     * @return true if this scope tracks the provided token
     */
    @Contract("null -> false")
    boolean hasCooldown(String token);
}
