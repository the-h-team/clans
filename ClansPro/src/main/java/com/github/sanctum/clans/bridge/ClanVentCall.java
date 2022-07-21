package com.github.sanctum.clans.bridge;

import com.github.sanctum.clans.event.ClanEvent;
import com.github.sanctum.panther.event.Vent;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public final class ClanVentCall<T extends ClanEvent> extends Vent.Call<T> {
	public ClanVentCall(@NotNull T event) {
		super(event);
	}

	public T schedule() {
		if (type == Vent.Runtime.Asynchronous) {
			return CompletableFuture.supplyAsync(this::run).join();
		}
		return run();
	}

}
