package com.github.sanctum.clans.bridge.internal.borders.task;

import com.github.sanctum.clans.bridge.internal.borders.BorderListener;
import com.github.sanctum.clans.bridge.internal.borders.event.BorderTaskEvent;
import com.github.sanctum.labyrinth.task.BukkitTaskPredicate;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BorderTask {

	public static void run(final Player p) {
		TaskScheduler.of(() -> {
			if (BorderListener.toggled.containsKey(p.getUniqueId()) && BorderListener.toggled.get(p.getUniqueId())) {
				BorderTaskEvent event = new BorderTaskEvent(p);
				Bukkit.getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
					event.perform();
				}
			}
		}).scheduleTimer("BORDERTASK;" + p.getUniqueId().toString(), 1, 40, BukkitTaskPredicate.cancelAfter(t -> {
			if (!BorderListener.toggled.containsKey(p.getUniqueId())) {
				t.cancel();
				return false;
			}
			return true;
		}), BukkitTaskPredicate.cancelAfter(p));
	}

}