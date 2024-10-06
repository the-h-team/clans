package com.github.sanctum.clans.model.addon.map.event;

import com.github.sanctum.clans.model.addon.map.structure.ChunkPosition;
import java.util.Map;
import java.util.Set;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class AsyncMapDrawEvent extends MapEvent {

	private final ChunkPosition chunk;
	private final BlockFace compassDirection;
	private final Map<ChunkPosition, String> clanChunks;
	private final Set<String> clanIdStrings;

	public AsyncMapDrawEvent(Player player, ChunkPosition chunk, BlockFace compassDirection, Map<ChunkPosition, String> clanChunks, Set<String> clanIdStrings) {
		super(player.getUniqueId(), true);
		this.chunk = chunk;
		this.compassDirection = compassDirection;
		this.clanChunks = clanChunks;
		this.clanIdStrings = clanIdStrings;
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

}
