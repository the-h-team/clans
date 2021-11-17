package com.github.sanctum.clans.bridge.internal.map.event;

import com.github.sanctum.clans.bridge.internal.map.structure.MapPoint;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;

public class AsyncMapFormatEvent extends MapEvent {

	private final MapPoint[][] mapPoints;
	private final Set<String> clanIds;
	private List<String> addedLinesTop = null;
	private List<String> addedLinesBottom = null;

	public AsyncMapFormatEvent(Player player, MapPoint[][] mapPoints, Set<String> clanIds) {
		super(player.getUniqueId(), true);
		this.mapPoints = mapPoints;
		this.clanIds = clanIds;
	}

	public MapPoint[][] getMapPoints() {
		return mapPoints;
	}

	public Set<String> getClanIds() {
		return clanIds;
    }

    public void setAddedLinesTop(List<String> linesToAdd) {
        addedLinesTop = linesToAdd;
    }

    public List<String> getAddedLinesTop() {
        return addedLinesTop;
    }

    public void setAddedLinesBottom(List<String> linesToAdd) {
        addedLinesBottom = linesToAdd;
    }

    public List<String> getAddedLinesBottom() {
        return addedLinesBottom;
    }


}
