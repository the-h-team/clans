package com.github.sanctum.clans.util;

import com.github.sanctum.labyrinth.library.SpigotResourceCheck;
import org.bukkit.plugin.Plugin;

public class ClansUpdate extends SpigotResourceCheck {

    private static final long serialVersionUID = -7156849826532257063L;

    public ClansUpdate(Plugin plugin) {
        super(plugin, 87515);
    }

    @Override
    public String getAuthor() {
        return "Hempfest";
    }

    @Override
    public int getId() {
        return 87515;
    }
}
