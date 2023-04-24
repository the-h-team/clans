package io.github.sanctum.tether.api;

import net.kyori.adventure.audience.ForwardingAudience;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents a group of users.
 * <p>
 * Groups can represent teams, factions, clans; any group of users!
 * Most groups will only want one owner, but since some groups would prefer the
 * option to designate multiple owners support is included.
 *
 * @since 0.0.1
 */
public interface Group extends ForwardingAudience {
    /**
     * Whether the group must have a single owner.
     * <p>
     * Due to the expected implementations of business logic in
     * single-owner-only group systems, a group which returns {@code true}
     * for this method must always have an owner.
     *
     * @return true if the group must have a single owner
     */
    default boolean isSingleOwner() {
        return true;
    }

    /**
     * Gets the owner(s) of the group.
     *
     * @return the owner(s) of the group
     * @implSpec If {@link #isSingleOwner()} returns {@code true} the returned
     * set must contain exactly one element.
     */
    @NotNull Set<User> getOwners();
}
