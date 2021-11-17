package com.github.sanctum.clans.bridge.internal.map.event;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ChangeChunkInWorldEvent extends MapEvent {

	protected final Chunk fromChunk;
	protected final Chunk toChunk;
	protected final World world;

	public ChangeChunkInWorldEvent(Player player, Chunk fromChunk, Chunk toChunk, World world) {
		super(player.getUniqueId(), false);
		this.fromChunk = fromChunk;
		this.toChunk = toChunk;
		this.world = world;
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

}
