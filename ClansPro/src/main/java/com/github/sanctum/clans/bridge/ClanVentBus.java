package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.ClanEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.event.custom.SubscriberCall;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.library.TypeFlag;
import java.util.function.Consumer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * A class used to subscribe to and call {@link ClanEvent} specific {@link Vent}'s
 */
public abstract class ClanVentBus {

	public static final ClanVentBus LOW_PRIORITY = new ClanVentBus(Vent.Priority.LOW) {};
	public static final ClanVentBus MEDIUM_PRIORITY = new ClanVentBus(Vent.Priority.MEDIUM) {};
	public static final ClanVentBus HIGH_PRIORITY = new ClanVentBus(Vent.Priority.HIGH) {};
	public static final ClanVentBus HIGHEST_PRIORITY = new ClanVentBus(Vent.Priority.HIGHEST) {};
	public static final ClanVentBus READ_ONLY_PRIORITY = new ClanVentBus(Vent.Priority.READ_ONLY) {};

	private Vent.Runtime runtime;

	public ClanVentBus(@NotNull Vent.Runtime runtime) {
		this.runtime = runtime;
	}

	private Vent.Priority priority = Vent.Priority.MEDIUM;

	public ClanVentBus(@NotNull Vent.Priority priority) {
		this.priority = priority;
	}

	public <T extends ClanEvent> DeployableClanAction<T> deployFrom(T event) {
		return new DeployableClanAction<>(event, ev -> new Vent.Call<>(ev).run(this.runtime != null ? this.runtime : event.getRuntime()));
	}

	public <T extends ClanEvent> DeployableClanAction<T> deployFrom(T event, Consumer<T> consumer) {
		return new DeployableClanAction<>(event, consumer);
	}

	public <T extends ClanEvent> DeployableClanAction<Void> subscribeTo(TypeFlag<T> flag, SubscriberCall<T> callable) {
		return new DeployableClanAction<>(null, unused -> subscribe(flag.getType(), this.priority, callable));
	}

	public <T extends ClanEvent> DeployableClanAction<Void> subscribeTo(TypeFlag<T> flag, String key, SubscriberCall<T> callable) {
		return new DeployableClanAction<>(null, unused -> new Vent.Subscription<>(flag.getType(), key, ClansAPI.getInstance().getPlugin(), this.priority, callable));
	}

	public <T extends ClanEvent> DeployableClanAction<Void> unsubscribeFrom(TypeFlag<T> flag, String key) {
		return new DeployableClanAction<>(null, unused -> LabyrinthProvider.getInstance().getEventMap().unsubscribe(flag.getType(), key));
	}

	public <T extends ClanEvent> Vent.Subscription<T> retrieve(TypeFlag<T> flag, String key) {
		return LabyrinthProvider.getInstance().getEventMap().get(flag.getType(), key);
	}

	public static <T extends ClanEvent> void subscribe(Class<T> event, Vent.Priority priority, Plugin host, SubscriberCall<T> callable) {
		LabyrinthProvider.getService(Service.VENT).subscribe(new Vent.Subscription<>(event, host, priority, callable));
	}

	@Note("For internal use only!!")
	public static <T extends ClanEvent> void subscribe(Class<T> event, Vent.Priority priority, SubscriberCall<T> callable) {
		LabyrinthProvider.getService(Service.VENT).subscribe(new Vent.Subscription<>(event, ClansAPI.getInstance().getPlugin(), priority, callable));
	}

	public static <T extends ClanEvent> Vent.Call<T> plan(T event) {
		return new Vent.Call<>(event);
	}

	public static <T extends ClanEvent> T call(T event) {
		Vent.Call<T> call = plan(event);
		return event.isAsynchronous() ? call.complete().join() : call.run();
	}


}
