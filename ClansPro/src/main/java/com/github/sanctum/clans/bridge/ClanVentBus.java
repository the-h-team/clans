package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.ClanEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.event.custom.SubscriberCall;
import com.github.sanctum.labyrinth.event.custom.Vent;
import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.Plugin;

/**
 * A class used to subscribe to and call {@link ClanEvent} specific {@link Vent}'s
 */
public class ClanVentBus {

	public static <T extends ClanEvent> void subscribe(Class<T> event, Vent.Priority priority, Plugin host, SubscriberCall<T> callable) {
		LabyrinthProvider.getService(Service.VENT).subscribe(new Vent.Subscription<>(event, host, priority, callable));
	}

	@Note("For internal use only!!")
	public static <T extends ClanEvent> void subscribe(Class<T> event, Vent.Priority priority, SubscriberCall<T> callable) {
		LabyrinthProvider.getService(Service.VENT).subscribe(new Vent.Subscription<>(event, ClansAPI.getInstance().getPlugin(), priority, callable));
	}

	public static <T extends ClanEvent> Vent.Call<T> caller(T event) {
		return new Vent.Call<>(event);
	}

	public static <T extends ClanEvent> T call(T event) {
		Vent.Call<T> call = caller(event);
		return event.isAsynchronous() ? call.complete().join() : call.run();
	}

	public static <T extends ClanEvent> CompletableFuture<T> queue(T event) {
		return caller(event).complete();
	}


}
