package io.github.sanctum.clans.model.combat;

import io.github.sanctum.clans.interfacing.*;
import io.github.sanctum.clans.model.association.GroupingLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object that can direct friendly fire within groups.
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface FriendlyFireScope extends MutableEntity, GroupingLike {
    /**
     * Gets the friendly fire state of the scope.
     * <p>
     * This flag controls whether entities in the scope are able to damage each
     * other by default. A state of true allows damage; false disallows.
     *
     * @return true if this scope allows friendly fire
     */
    boolean isFriendlyFire();

    /**
     * A friendly fire status editing utility.
     *
     * @since 1.6.1
     */
    interface Edits extends MutableEntityEdits, FriendlyFireScope {
        @Override
        @NotNull FriendlyFireScope getMutating();

        /**
         * Sets the new friendly fire state for the scope.
         * <p>
         * Use {@code null} to no-op.
         *
         * @param newFriendlyFire the desired friendly fire state or null
         * @return this edit util
         */
        @NotNull Edits setFriendlyFire(@Nullable Value.Required<Boolean> newFriendlyFire);
    }

    /**
     * A staged friendly fire status update.
     *
     * @since 1.6.1
     */
    interface Update extends StagedUpdate {
        @Override
        @NotNull FriendlyFireScope getReferenceObject();

        /**
         * Gets the friendly fire state proposed by this element.
         *
         * @return the proposed state or null
         */
        @Nullable Value.Required<Boolean> getProposedFriendlyFire();
    }
}
