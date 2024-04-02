package io.github.sanctum.clans.model;

import io.github.sanctum.clans.interfacing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An object that can have a description (or "bio").
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface DescriptionTarget extends MutableEntity {
    /**
     * Gets the description or bio of the object.
     *
     * @return the description, bio or null
     */
    @Nullable String getDescription();

    /**
     * A description or bio editing utility.
     *
     * @since 1.6.1
     */
    interface Edits extends MutableEntityEdits, DescriptionTarget {
        @Override
        @NotNull DescriptionTarget getMutating();

        /**
         * Sets the new description/bio for the object.
         * <p>
         * Use {@code null} to no-op.
         *
         * @param newDescription a new description, bio or null
         * @return this edit util
         */
        @NotNull Edits setDescription(@Nullable Value.OrNull<String> newDescription);
    }

    /**
     * A staged description update.
     *
     * @since 1.6.1
     */
    interface Update extends StagedUpdate {
        @Override
        @NotNull DescriptionTarget getReferenceObject();

        /**
         * Gets the description/bio proposed by this element.
         *
         * @return the proposed description, bio or null
         */
        @Nullable Value.OrNull<String> getProposedDescription();
    }
}
