package io.github.sanctum.clans.model.world;

import io.github.sanctum.labyrinth.loci.chunk.WorldChunkReference;
import org.jetbrains.annotations.Contract;

/**
 * An object that can possess chunks.
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface ChunkHolder {
    /**
     * Checks if the object holds the provided chunk.
     *
     * @param chunk the chunk to check
     * @return true if this object holds the chunk
     */
    @Contract("null -> false")
    boolean holds(WorldChunkReference chunk); // FIXME make as future?
}
