package com.github.sanctum.clans.internal.map;

import com.github.sanctum.clans.internal.map.structure.MapPoint;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AsyncMapFormatEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final MapPoint[][] mapPoints;
    private final Set<String> clanIds;
    private List<String> addedLinesTop = null;
    private List<String> addedLinesBottom = null;

    protected AsyncMapFormatEvent(Player player, MapPoint[][] mapPoints, Set<String> clanIds) {
        super(true);
        this.player = player;
        this.mapPoints = mapPoints;
        this.clanIds = clanIds;
    }

    public Player getPlayer() {
        return player;
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

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
