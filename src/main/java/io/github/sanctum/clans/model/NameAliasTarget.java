package io.github.sanctum.clans.model;

import io.github.sanctum.clans.interfacing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object that can have a separate alias.
 * <p>
 * Aliases are nicknames/display names.
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface NameAliasTarget extends MutableEntity {
    /**
     * Gets the nickname or alternate display name of this object.
     *
     * @return the nickname, display name or null
     */
    @Nullable String getAlias();

    /**
     * An alias editing utility.
     *
     * @since 1.6.1
     */
    interface Edits extends MutableEntityEdits, NameAliasTarget {
        @Override
        @NotNull NameAliasTarget getMutating();

        /**
         * Sets the new nickname/alternate display name for the object.
         * <p>
         * Use {@code null} to no-op.
         *
         * @param newName the nickname, display name or null
         * @return this edit util
         */
        @NotNull Edits setAlias(@Nullable Value.OrNull<String> newName);
    }

    /**
     * A staged alias update.
     *
     * @since 1.6.1
     */
    interface Update extends StagedUpdate {
        @Override
        @NotNull NameAliasTarget getReferenceObject();

        /**
         * Gets the nickname/alternative display proposed by this element.
         *
         * @return the proposed nickname, display name or null
         */
        @Nullable Value.OrNull<String> getProposedNickname(); // TODO match other signatures
    }
}
