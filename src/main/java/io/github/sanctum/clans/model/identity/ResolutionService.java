package io.github.sanctum.clans.model.identity;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a typed mechanism for resolving live objects from identifiers.
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface ResolutionService<T extends Entity> {
    /**
     * Resolves an entity by its {@linkplain Entity#getUniqueIdentifier()
     * unique identifier}, if possible.
     *
     * @param uniqueIdentifier an entity's unique identifier
     * @return the live entity or null
     * @implNote This method tolerates nulls; {@code null} will be returned.
     */
    @Contract("null -> null")
    @Nullable T getEntityByUniqueIdentifier(String uniqueIdentifier);

    /**
     * Resolves an entity by its {@linkplain Entity#getName() common name},
     * if possible.
     *
     * @param name an entity's common name
     * @return the live entity or null
     * @implNote This method is stubbed to return {@code null} and does not
     * have to be supported by every implementation.
     */
    @Contract("null -> null")
    default @Nullable T getEntityByName(String name) {
        return null;
    }
}
