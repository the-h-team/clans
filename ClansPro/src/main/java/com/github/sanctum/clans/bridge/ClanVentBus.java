package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.event.ClanEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.event.Subscribe;
import com.github.sanctum.panther.event.Vent;
import com.github.sanctum.panther.event.VentMap;
import com.github.sanctum.panther.util.TypeAdapter;
import java.util.function.Consumer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * A class used to subscribe to and call {@link ClanEvent} specific {@link Vent}'s
 */
public abstract class ClanVentBus {

	public static final ClanVentBus LOW_PRIORITY = new ClanVentBus(Vent.Priority.LOW) {
	};
	public static final ClanVentBus MEDIUM_PRIORITY = new ClanVentBus(Vent.Priority.MEDIUM) {
	};
	public static final ClanVentBus HIGH_PRIORITY = new ClanVentBus(Vent.Priority.HIGH) {
	};
	public static final ClanVentBus HIGHEST_PRIORITY = new ClanVentBus(Vent.Priority.HIGHEST) {
	};
	public static final ClanVentBus READ_ONLY_PRIORITY = new ClanVentBus(Vent.Priority.READ_ONLY) {
	};

	private Vent.Runtime runtime;

	public ClanVentBus(@NotNull Vent.Runtime runtime) {
		this.runtime = runtime;
	}

	private Vent.Priority priority = Vent.Priority.MEDIUM;

	public ClanVentBus(@NotNull Vent.Priority priority) {
		this.priority = priority;
	}

	public <T extends ClanEvent> DeployableClanAction<T> deployFrom(T event) {
		return new DeployableClanAction<>(event, ev -> new ClanVentCall<>(ev).schedule());
	}

	public <T extends ClanEvent> DeployableClanAction<T> deployFrom(T event, Consumer<T> consumer) {
		return new DeployableClanAction<>(event, consumer);
	}

	public <T extends ClanEvent> DeployableClanAction<Void> subscribeTo(TypeAdapter<T> flag, Subscribe.Consumer<T> callable) {
		return new DeployableClanAction<>(null, unused -> subscribe(flag.getType(), this.priority, callable));
	}

	public <T extends ClanEvent> DeployableClanAction<Void> subscribeTo(TypeAdapter<T> flag, String key, Subscribe.Consumer<T> callable) {
		return new DeployableClanAction<>(null, unused -> new Vent.Subscription<>(flag.getType(), key, ((Vent.Host) ClansAPI.getInstance().getPlugin()), this.priority, callable));
	}

	public <T extends ClanEvent> DeployableClanAction<Void> unsubscribeFrom(TypeAdapter<T> flag, String key) {
		return new DeployableClanAction<>(null, unused -> LabyrinthProvider.getInstance().getEventMap().unsubscribe(flag.getType(), key));
	}

	public <T extends ClanEvent> DeployableClanAction<Void> subscribeTo(Class<T> flag, Subscribe.Consumer<T> callable) {
		return new DeployableClanAction<>(null, unused -> subscribe(flag, this.priority, callable));
	}

	public <T extends ClanEvent> DeployableClanAction<Void> subscribeTo(Class<T> flag, String key, Subscribe.Consumer<T> callable) {
		return new DeployableClanAction<>(null, unused -> new Vent.Subscription<>(flag, key, ((Vent.Host) ClansAPI.getInstance().getPlugin()), this.priority, callable));
	}

	public <T extends ClanEvent> DeployableClanAction<Void> unsubscribeFrom(Class<T> flag, String key) {
		return new DeployableClanAction<>(null, unused -> LabyrinthProvider.getInstance().getEventMap().unsubscribe(flag, key));
	}

	public <T extends ClanEvent> Vent.Subscription<T> retrieve(TypeAdapter<T> flag, String key) {
		return VentMap.getInstance().getSubscription(flag.getType(), key);
	}

	public <T extends ClanEvent> Vent.Subscription<T> retrieve(Class<T> flag, String key) {
		return VentMap.getInstance().getSubscription(flag, key);
	}

	@Deprecated
	public static <T extends ClanEvent> void subscribe(Class<T> event, Vent.Priority priority, Plugin host, Subscribe.Consumer<T> callable) {
		VentMap.getInstance().subscribe(new Vent.Subscription<>(event, ((Vent.Host) host), priority, callable));
	}

	@Note("For internal use only!!")
	@Deprecated
	public static <T extends ClanEvent> void subscribe(Class<T> event, Vent.Priority priority, Subscribe.Consumer<T> callable) {
		VentMap.getInstance().subscribe(new Vent.Subscription<>(event, ((Vent.Host) ClansAPI.getInstance().getPlugin()), priority, callable));
	}

	public static <T extends ClanEvent> ClanVentCall<T> plan(T event) {
		return new ClanVentCall<>(event);
	}

	public static <T extends ClanEvent> T call(T event) {
		ClanVentCall<T> call = plan(event);
		return call.schedule();
	}


}
