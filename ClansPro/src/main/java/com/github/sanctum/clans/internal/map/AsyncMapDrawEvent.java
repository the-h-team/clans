package com.github.sanctum.clans.internal.map;

import com.github.sanctum.clans.internal.map.structure.ChunkPosition;
import java.util.Map;
import java.util.Set;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AsyncMapDrawEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ChunkPosition chunk;
    private final BlockFace compassDirection;
    private final Map<ChunkPosition, String> clanChunks;
    private final Set<String> clanIdStrings;

    protected AsyncMapDrawEvent(Player player, ChunkPosition chunk, BlockFace compassDirection, Map<ChunkPosition, String> clanChunks, Set<String> clanIdStrings) {
        super(true);
        this.player = player;
        this.chunk = chunk;
        this.compassDirection = compassDirection;
        this.clanChunks = clanChunks;
        this.clanIdStrings = clanIdStrings;
    }

    public Player getPlayer() {
        return player;
    }

    public ChunkPosition getPlayerChunkPosition() {
        return chunk;
    }

    public BlockFace getCompassDirection() {
        return compassDirection;
    }

    public Map<ChunkPosition, String> getClanChunks() {
        return clanChunks;
    }

    public Set<String> getClanIdStrings() {
        return clanIdStrings;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
