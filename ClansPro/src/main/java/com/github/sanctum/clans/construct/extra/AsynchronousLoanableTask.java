package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.api.TaskService;
import com.github.sanctum.labyrinth.data.container.LabyrinthCollection;
import com.github.sanctum.labyrinth.data.container.LabyrinthSet;
import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.labyrinth.task.Schedule;
import com.github.sanctum.labyrinth.task.Task;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;

public final class AsynchronousLoanableTask {

	public static final String KEY = "ClansPro:CONCURRENT";
	private final LabyrinthCollection<Player> players = new LabyrinthSet<>();
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
			LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).repeat(task -> {
				try {
					players.forEach(p -> logic.accept(p, AsynchronousLoanableTask.this));
				} catch (Exception fail) {
					ClansAPI.getInstance().getPlugin().getLogger().severe("- The task failed to pass logic @ an unknown location.");
				}
			}, KEY, unit.toMillis(size), unit.toMillis(size));
		}
		players.add(player);
	}

	public void leave(Player player) {
		players.remove(player);
		if (players.size() == 0) {
			if (getTask() != null) {
				getTask().cancel();
				LabyrinthProvider.getService(Service.TASK).getScheduler(TaskService.ASYNCHRONOUS).purge();
			}
		}
	}

	public void stop() {
		getTask().cancel();
		players.clear();
	}

	@Note("Meant to be used WITHIN your task logic")
	public void synchronize(Applicable data) {
		Schedule.sync(data).run();
	}

	@FunctionalInterface
	public interface Logic {

		void accept(Player target, AsynchronousLoanableTask task);

	}


}
