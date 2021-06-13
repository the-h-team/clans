package com.github.sanctum.link;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.util.events.AsyncClanEventBuilder;
import com.github.sanctum.clans.util.events.ClanEventBuilder;
import com.github.sanctum.labyrinth.event.custom.SubscriberCall;
import com.github.sanctum.labyrinth.event.custom.Vent;
import java.util.concurrent.CompletableFuture;

/**
 * A class used to subscribe to and call specific {@link ClanEventBuilder} or {@link AsyncClanEventBuilder} {@link Vent}'s
 */
public class ClanVentBus {

	public static <T extends ClanEventBuilder> void subscribe(Class<T> event, Vent.Priority priority, SubscriberCall<T> callable) {
		Vent.subscribe(new Vent.Subscription<>(event, ClansPro.getInstance(), priority, callable));
	}

	public static <T extends AsyncClanEventBuilder> void subscribeAsync(Class<T> event, Vent.Priority priority, SubscriberCall<T> callable) {
		Vent.subscribe(new Vent.Subscription<>(event, ClansPro.getInstance(), priority, callable));
	}

	public static <T extends ClanEventBuilder> T call(T event) {
		return new Vent.Call<>(Vent.Runtime.Synchronous, event).run();
	}

	public static <T extends AsyncClanEventBuilder> CompletableFuture<T> queue(T event) {
		return new Vent.Call<>(Vent.Runtime.Asynchronous, event).complete();
	}


}
