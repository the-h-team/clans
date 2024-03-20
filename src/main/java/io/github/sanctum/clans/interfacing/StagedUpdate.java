package io.github.sanctum.clans.interfacing;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A staged update which may include required validation logic.
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface StagedUpdate {
    /**
     * Gets the object being updated.
     *
     * @return a reference object
     */
    @NotNull MutableEntity getReferenceObject();

    /**
     * Gets the edits object for this change.
     * <p>
     * The returned object is a copy.
     *
     * @return a copy of the staged edits
     */
    @Contract("-> new")
    @NotNull MutableEntityEdits staged();

    /**
     * Executes the update.
     * <p>
     * Some updates may yield a map output. The map may be empty but
     * must not be null.
     * <p>
     * If the update fails, the future will complete exceptionally.
     *
     * @return an update future
     */
    @NotNull CompletableFuture<@NotNull Map<String, Object>> process();
}
