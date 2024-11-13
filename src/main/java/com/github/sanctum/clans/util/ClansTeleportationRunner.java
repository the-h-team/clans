package com.github.sanctum.clans.util;

import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.labyrinth.interfacing.Identifiable;
import com.github.sanctum.labyrinth.library.Teleport;

public class ClansTeleportationRunner extends Teleport.Runner {
    @Override
    public void run(Identifiable identifiable) {
        Teleport teleport = Teleport.get(identifiable);
        if (teleport != null) {
            if (identifiable.getAsPlayer().getLocation().distance(teleport.getInitialLocation().getAsLocation()) > 0) {
                teleport.setState(Teleport.State.EXPIRED);
                Clan.ACTION.sendMessage(identifiable.getAsPlayer(), ClansAPI.getDataInstance().getMessageResponse("teleport-cancelled"));
                teleport.flush();
            }
        }
    }
}
