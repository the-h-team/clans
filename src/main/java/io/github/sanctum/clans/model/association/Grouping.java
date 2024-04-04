package io.github.sanctum.clans.model.association;

import io.github.sanctum.clans.interfacing.*;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a unified interface for managing entity groupings.
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface Grouping extends MutableEntity {
    interface Edits extends MutableEntityEdits, Grouping {
        /**
         * Kicks a specified target from the group.
         *
         * @param target the entity to kick
         * @return this edit util
         */
        @NotNull Edits kick(AssociableEntity target); // TODO convert to value
    }
}
