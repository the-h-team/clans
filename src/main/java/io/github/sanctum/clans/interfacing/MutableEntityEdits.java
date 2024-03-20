package io.github.sanctum.clans.interfacing;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

/**
 * An editing utility object.
 *
 * @since 1.6.1
 * @author ms5984
 */
@ApiStatus.NonExtendable
public interface MutableEntityEdits {
    /**
     * Gets the object being mutated.
     *
     * @return the object to mutate
     */
    @NotNull MutableEntity getMutating();

    /**
     * Gets a map view of the edits in this utility.
     * <p>
     * Keys represent property names; values describe a desired mapping as
     * Suppliers. These may return null, but key-values should not be directly
     * assigned to null.
     * <p>
     * The map may be read-only.
     *
     * @return a map view of edits
     */
    @NotNull Map<String, Supplier<@Nullable Object>> getEdits();

    /**
     * Finalizes the edits in this utility into a staging object.
     * <p>
     * Updates are not processed until {@link StagedUpdate#process()}
     * is called.
     *
     * @return a staging object
     */
    @NotNull StagedUpdate stage();
}
