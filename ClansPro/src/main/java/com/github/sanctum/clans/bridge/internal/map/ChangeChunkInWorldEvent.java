package com.github.sanctum.clans.bridge.internal.map;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ChangeChunkInWorldEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    protected final Player player;
    protected final Chunk fromChunk;
    protected final Chunk toChunk;
    protected final World world;

    public ChangeChunkInWorldEvent(Player player, Chunk fromChunk, Chunk toChunk, World world) {
        this.player = player;
        this.fromChunk = fromChunk;
        this.toChunk = toChunk;
        this.world = world;
    }

    public Player getPlayer() {
        return player;
    }

    public Chunk getFromChunk() {
        return fromChunk;
    }

    public Chunk getToChunk() {
        return toChunk;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
