package com.github.sanctum.clans.bridge.internal.borders.task;

import com.github.sanctum.clans.bridge.internal.borders.BorderListener;
import com.github.sanctum.clans.bridge.internal.borders.event.BorderTaskEvent;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.labyrinth.task.Synchronous;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BorderTask {

	public static Synchronous run(final Player p) {
		Synchronous sync = Schedule.sync(() -> {
			if (BorderListener.toggled.containsKey(p.getUniqueId()) && BorderListener.toggled.get(p.getUniqueId())) {
				BorderTaskEvent event = new BorderTaskEvent(p);
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					event.perform();
				}
			}
		}).cancelAbsence(BorderListener.toggled, p.getUniqueId()).cancelAfter(p);
		if (ClansAPI.getData().isTrue("Formatting.console-debug")) {
			return sync.debug();
		} else
			return sync;
	}
}