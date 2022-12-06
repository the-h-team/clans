package com.github.sanctum.clans.bridge;

import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.api.TaskService;
import com.github.sanctum.panther.util.Deployable;
import com.github.sanctum.panther.util.DeployableMapping;
import com.github.sanctum.panther.util.HUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class DeployableClanAction<T> implements Deployable<T> {

	private final T t;
	private final Consumer<T> action;

	public DeployableClanAction(T t, Consumer<T> action) {
		this.t = t;
		this.action = action;
	}

	@Override
	public DeployableClanAction<T> deploy() {
		action.accept(t);
		return this;
	}

	@Override
	public DeployableClanAction<T> deploy(Consumer<? super T> consumer) {
		deploy();
		consumer.accept(t);
		return this;
	}

	@Override
	public DeployableClanAction<T> queue() {
		submit().thenAccept(action).join();
		return this;
	}

	@Override
	public DeployableClanAction<T> queue(Consumer<? super T> consumer, long timeout) {
		LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.SYNCHRONOUS).wait(() -> {
			queue();
			consumer.accept(t);
		}, HUID.randomID().toString(), timeout);
		return this;
	}

	@Override
	public <O> DeployableMapping<O> map(@NotNull Function<? super T, ? extends O> mapper) {
		return null;
	}

	@Override
	public DeployableClanAction<T> queue(long timeout) {
		LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.SYNCHRONOUS).wait(this::queue, HUID.randomID().toString(), timeout);
		return this;
	}

	@Override
	public CompletableFuture<T> submit() {
		return CompletableFuture.supplyAsync(() -> {
			deploy();
			return t;
		});
	}

	public T complete() {
		return submit().join();
	}

	public T get() {
		return t;
	}

}
