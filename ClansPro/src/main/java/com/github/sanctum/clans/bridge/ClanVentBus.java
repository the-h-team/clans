package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.events.AsyncClanEventBuilder;
import com.github.sanctum.clans.events.ClanEventBuilder;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.event.custom.SubscriberCall;
import com.github.sanctum.labyrinth.event.custom.Vent;
import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.Plugin;

/**
 * A class used to subscribe to and check specific {@link ClanEventBuilder} or {@link AsyncClanEventBuilder} {@link Vent}'s
 */
public class ClanVentBus {

	public static <T extends ClanEventBuilder> void subscribe(Class<T> event, Vent.Priority priority, Plugin host, SubscriberCall<T> callable) {
		LabyrinthProvider.getService(Service.VENT).subscribe(new Vent.Subscription<>(event, host, priority, callable));
	}

	public static <T extends AsyncClanEventBuilder> void subscribeAsync(Class<T> event, Vent.Priority priority, Plugin host, SubscriberCall<T> callable) {
		LabyrinthProvider.getService(Service.VENT).subscribe(new Vent.Subscription<>(event, host, priority, callable));
	}

	@Note("For internal use only!!")
	public static <T extends ClanEventBuilder> void subscribe(Class<T> event, Vent.Priority priority, SubscriberCall<T> callable) {
		LabyrinthProvider.getService(Service.VENT).subscribe(new Vent.Subscription<>(event, ClansAPI.getInstance().getPlugin(), priority, callable));
	}

	@Note("For internal use only!!")
	public static <T extends AsyncClanEventBuilder> void subscribeAsync(Class<T> event, Vent.Priority priority, SubscriberCall<T> callable) {
		LabyrinthProvider.getService(Service.VENT).subscribe(new Vent.Subscription<>(event, ClansAPI.getInstance().getPlugin(), priority, callable));
	}

	public static <T extends ClanEventBuilder> T call(T event) {
		return new Vent.Call<>(Vent.Runtime.Synchronous, event).run();
	}

	public static <T extends AsyncClanEventBuilder> CompletableFuture<T> queue(T event) {
		return new Vent.Call<>(Vent.Runtime.Asynchronous, event).complete();
	}


}
