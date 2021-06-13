package com.github.sanctum.borders;

import com.github.sanctum.clans.util.events.command.CommandHelpInsertEvent;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.link.ClanVentBus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class BorderListener implements Listener {

	public static final HashMap<UUID, Boolean> toggled = new HashMap<>();

	public static final List<UUID> baseLocate = new ArrayList<>();

	public static final List<UUID> playerLocate = new ArrayList<>();

	public static final List<UUID> spawnLocate = new ArrayList<>();

	public BorderListener() {
		ClanVentBus.subscribe(CommandHelpInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

			e.insert("&7|&e) &6/clan &fterritory &7| &8optional:&f-f &7<&8flag&7>");
			e.insert("&7|&e) &6/clan &fflags");

		});
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		toggled.remove(p.getUniqueId());
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		toggled.remove(e.getEntity().getUniqueId());
	}

	static TextComponent coToggle(String body, String highlight) {
		return TextLib.getInstance().textRunnable(body, highlight, "Click me to toggle", "c territory");
	}


}
