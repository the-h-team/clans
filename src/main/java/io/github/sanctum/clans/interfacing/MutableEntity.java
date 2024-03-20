package io.github.sanctum.clans.interfacing;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * An entity that may be edited using {@link MutableEntityEdits}.
 *
 * @since 1.6.1
 * @author ms5984
 */
@ApiStatus.OverrideOnly
public interface MutableEntity {
    /**
     * Gets an editing utility for this entity.
     *
     * @return a new editing utility
     */
    @Contract("-> new")
    @NotNull MutableEntityEdits toMutable();
}
