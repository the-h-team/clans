package io.github.sanctum.clans.model.association;

import org.jetbrains.annotations.NotNull;

/**
 * An object that can be resolved to a grouping.
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface GroupingLike {
    /**
     * Resolves this object to its associated grouping.
     *
     * @return the associated grouping
     */
    @NotNull Grouping asGrouping();
}
