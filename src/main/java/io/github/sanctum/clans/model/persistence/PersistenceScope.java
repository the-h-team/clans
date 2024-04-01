package io.github.sanctum.clans.model.persistence;

import io.github.sanctum.clans.interfacing.*;
import org.jetbrains.annotations.NotNull;

/**
 * An object that can store persisted data.
 * <p>
 * As a general rule, values returned should be immutable copies. Such a
 * restriction will avoid data race and long-lived objects.
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface PersistenceScope extends MutableEntity {
    // TODO persistence getter util
    interface Edits extends MutableEntityEdits {
        /**
         * Removes a value from the scope.
         * // TODO doc result map
         * @param key a key
         * @return this edit util
         */
        @NotNull Edits removeValue(String key);
    }
}
