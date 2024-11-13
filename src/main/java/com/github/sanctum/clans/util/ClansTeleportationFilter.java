package com.github.sanctum.clans.util;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.labyrinth.interfacing.Identifiable;
import com.github.sanctum.labyrinth.library.Teleport;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClansTeleportationFilter implements Teleport.RadiusFilter {
    final Teleportation teleportation;
    ClansTeleportationFilter(@NotNull Teleportation teleportation) {
        this.teleportation = teleportation;
    }

    @Override
    public boolean accept(Identifiable identifiable) {
        if (!teleportation.hasMultipleEntities()) {
            Identifiable i = teleportation.getEntity();
            if (i instanceof Clan.Associate) {
                Clan.Associate associate = (Clan.Associate) i;
                for (Entity e : i.getAsPlayer().getNearbyEntities(radius(), radius(), radius())) {
                    if (e instanceof Player && !associate.getClan().has(e.getUniqueId())) return false;
                }
            }
        } else {
            for (Identifiable i : teleportation.getEntities()) {
                if (i instanceof Clan.Associate) {
                    Clan.Associate associate = (Clan.Associate) i;
                    for (Entity e : i.getAsPlayer().getNearbyEntities(radius(), radius(), radius())) {
                        if (e instanceof Player && !associate.getClan().has(e.getUniqueId())) return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public int radius() {
        return 30;
    }
}
