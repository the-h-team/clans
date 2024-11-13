package com.github.sanctum.clans.commands;

import com.github.sanctum.clans.model.ClanSubCommand;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.library.StringUtils;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class CommandVersion extends ClanSubCommand {
    public CommandVersion() {
        super("version");
    }

    @Override
    public boolean player(Player p, String label, String[] args) {

        if (args.length == 0) {

            CompletableFuture.runAsync(() -> {

                p.sendMessage(" ");
                String info = ClansAPI.getInstance().isUpdated() ? "is up to date." : "needs updated.";
                p.sendMessage(StringUtils.use("&b&oThis server is using clans version &r" + ClansAPI.getInstance().getPlugin().getDescription().getVersion() + " &b&owith &6Labyrinth &b&oversion &r" + LabyrinthProvider.getInstance().getPluginInstance().getDescription().getVersion() + " &b&oand &e" + info).translate());
                p.sendMessage(" ");

            }).join();

            return true;
        }


        return true;
    }
}
