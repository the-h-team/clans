package io.github.sanctum.clans.model.identity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a read-only, base API for identifiable elements.
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface Entity {
    /**
     * Gets the most stable identifier for the entity.
     * <p>
     * Some entity types offer a {@linkplain ResolutionService resolution
     * service} whereby this value can be used somewhat reliably later on to
     * resolve live objects.
     *
     * @return the most stable identifier for this element
     */
    @NotNull String getUniqueIdentifier();

    /**
     * Gets the name commonly used by the entity.
     * <p>
     * The value returned by this method does not have to be as stable as
     * {@link #getUniqueIdentifier()} but delegates to it by default.
     *
     * @return the common name for this element
     */
    @NotNull String getName();

    /**
     * Gets the resolution service for the entity type (if available).
     *
     * @return a resolution service or null
     */
    default @Nullable ResolutionService<? extends Entity> getResolutionService() {
        return null;
    }
}
