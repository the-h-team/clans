package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.api.TaskService;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.annotation.Note;
import com.github.sanctum.panther.annotation.Ordinal;
import com.github.sanctum.panther.container.PantherCollection;
import com.github.sanctum.panther.container.PantherSet;
import com.github.sanctum.panther.util.Applicable;
import com.github.sanctum.panther.util.Task;
import com.github.sanctum.panther.util.TaskChain;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;

public final class AsynchronousLoanableTask {

	public static final String KEY = "ClansPro:CONCURRENT";
	private final PantherCollection<Player> players = new PantherSet<>();
	private final Logic logic;

	public AsynchronousLoanableTask(Logic logic) {
		this.logic = logic;
	}


	Task getTask() {
		return LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).get(KEY);
	}

	public void join(Player player) {
		if (getTask() == null) {
			TimeUnit unit = TimeUnit.valueOf(ClansAPI.getDataInstance().getConfigString("Clans.timer.threshold"));
			int size = ClansAPI.getDataInstance().getConfigInt("Clans.timer.time-span");
			Task t = new Task(KEY, TaskChain.getAsynchronous()) {
				private static final long serialVersionUID = 7009836600799217888L;

				@Ordinal
				public void onRun() {
					try {
						players.forEach(p -> logic.accept(p, AsynchronousLoanableTask.this));
					} catch (Exception fail) {
						ClansAPI.getInstance().getPlugin().getLogger().severe("- The task failed to pass logic @ an unknown location.");
					}
				}

			};
			LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).repeat(t, unit.toMillis(size), unit.toMillis(size));
		}
		players.add(player);
	}

	public void leave(Player player) {
		players.remove(player);
		if (players.size() == 0) {
			if (getTask() != null) {
				getTask().cancel();
			}
		}
	}

	public void stop() {
		getTask().cancel();
		players.clear();
	}

	@Note("Meant to be used WITHIN your task logic")
	public void synchronize(Applicable data) {
		TaskScheduler.of(data).schedule();
	}

	@FunctionalInterface
	public interface Logic {

		void accept(Player target, AsynchronousLoanableTask task);

	}


}
