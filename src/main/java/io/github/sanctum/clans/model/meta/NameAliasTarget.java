package io.github.sanctum.clans.model.meta;

import io.github.sanctum.clans.interfacing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object that can have a separate name alias.
 * <p>
 * Name aliases are nicknames/display names.
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
    @Nullable String getNameAlias();

    /**
     * A name alias editing utility.
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
         * @param newNameAlias the nickname, display name or null
         * @return this edit util
         */
        @NotNull Edits setNameAlias(@Nullable Value.OrNull<String> newNameAlias);
    }

    /**
     * A staged name alias update.
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
        @Nullable Value.OrNull<String> getProposedNameAlias();
    }
}
