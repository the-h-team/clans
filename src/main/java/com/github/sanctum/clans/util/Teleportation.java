package com.github.sanctum.clans.util;

import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.labyrinth.interfacing.Identifiable;
import com.github.sanctum.labyrinth.library.SimpleTeleport;

public class Teleportation extends SimpleTeleport {
    public Teleportation(Identifiable entity, Location targetLocation) {
        super(entity, targetLocation);
        setRadiusFilter(new ClansTeleportationFilter(this));
        getRadiusFilterOptions().setDelay(ClansAPI.getDataInstance().getConfigInt("teleportation-time"));
        getRadiusFilterOptions().setDelayMessage(ClansAPI.getDataInstance().getMessageResponse("teleporting"));
        setTeleportedMessage(ClansAPI.getDataInstance().getMessageResponse("teleported"));
    }

    public Teleportation(Location targetLocation, Identifiable... entities) {
        super(targetLocation, entities);
        setRadiusFilter(new ClansTeleportationFilter(this));
        getRadiusFilterOptions().setDelay(ClansAPI.getDataInstance().getConfigInt("teleportation-time"));
        getRadiusFilterOptions().setDelayMessage(ClansAPI.getDataInstance().getMessageResponse("teleporting"));
        setTeleportedMessage(ClansAPI.getDataInstance().getMessageResponse("teleported"));
    }
}
