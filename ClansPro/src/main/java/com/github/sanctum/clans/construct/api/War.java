package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.task.Schedule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface War extends Iterable<Clan.Associate> {

	/**
	 * @return The id for this war instance or "name" if you will.
	 */
	String getId();

	/**
	 * Start the arena! Record all current player locations and get ready for teleportation.
	 *
	 * @return true if started successfully.
	 */
	boolean start();

	/**
	 * @return
	 */
	boolean stop();

	/**
	 * @return
	 */
	boolean avoid();

	/**
	 *
	 */
	void reset();

	/**
	 * Assign each associate to their team's
	 */
	void populate();

	/**
	 * @return
	 */
	boolean isRunning();

	/**
	 * Get the recorded time started, if not started this method will instantiate itself.
	 *
	 * @return The recorded time started.
	 */
	long stamp();

	/**
	 * @return
	 */
	Map.Entry<Team, Integer> getMostPoints();

	/**
	 * @param team
	 * @return
	 */
	int getPoints(Team team);

	/**
	 * @param team
	 * @param amount
	 */
	void setPoints(Team team, int amount);

	Team getTeam(Clan c);

	Vote getVote();

	Vote getVote(Team team);

	Clan getClan(Team team);

	static Location getSpawn(Team team) {
		return team.getSpawn();
	}

	static void setSpawn(Team team, Location location) {
		FileManager file = ClansAPI.getInstance().getFileList().find("locations", "Configuration", FileType.JSON);
		switch (team) {
			case A:
				file.write(t -> t.set("War.team-a", location));
				break;
			case B:
				file.write(t -> t.set("War.team-b", location));
				break;
			case C:
				file.write(t -> t.set("War.team-c", location));
				break;
			case D:
				file.write(t -> t.set("War.team-d", location));
				break;
		}
	}

	Queue getQueue();

	Cooldown getTimer();

	class Queue implements Iterable<Clan.Associate> {

		private final Map<Clan.Associate, Location> pool = new HashMap<>();
		private final Map<Team, Clan> roster = new HashMap<>();

		public Map<Team, Clan> shuffle() {
			Clan[] a = teams();
			for (Clan c : a) {
				for (Team t : Team.values()) {
					if (!roster.containsValue(c) && !roster.containsKey(t)) {
						roster.put(t, c);
						break;
					}
				}
			}
			return roster;
		}

		public boolean que(Clan.Associate associate) {
			if (!pool.containsKey(associate)) {
				Player p = associate.getUser().toBukkit().getPlayer();
				if (p == null) return false;
				pool.put(associate, p.getLocation());
				return true;
			}
			return false;
		}

		public void updateReturnLocation(Clan.Associate associate) {
			if (!pool.containsKey(associate)) return;
			pool.put(associate, associate.getUser().toBukkit().getPlayer().getLocation());
		}

		public boolean unque(Clan.Associate associate) {
			if (pool.containsKey(associate)) {
				Location loc = pool.get(associate);
				Player p = associate.getUser().toBukkit().getPlayer();
				if (p == null) {
					Schedule.sync(() -> pool.remove(associate)).run();
					return false;
				}
				if (ClansAPI.getData().prefixedTagsAllowed()) {
					if (associate.getClan().getPalette().isGradient()) {
						Clan c = associate.getClan();
						ClanDisplayName.update(p, ClansAPI.getData().prefixedTag("", c.getPalette().toGradient().context(c.getName()).translate()));
					} else {
						ClanDisplayName.update(p, ClansAPI.getData().prefixedTag(associate.getClan().getPalette().toString(), associate.getClan().getName()));
					}
				} else {
					ClanDisplayName.remove(associate);
				}
				ClansAPI.getInstance().getArenaManager().show(associate);
				Clan.ACTION.sendMessage(p, "&aGoing back to previous location...");
				Schedule.sync(() -> p.teleport(loc)).applyAfter(() -> pool.remove(associate)).run();
				return true;
			}
			return false;
		}

		public boolean test(Clan.Associate associate) {
			if (pool.containsKey(associate)) return false;
			Clan t = Arrays.stream(teams()).filter(c -> c.equals(associate.getClan())).findFirst().orElse(null);
			return t != null;
		}

		public Clan[] teams() {
			List<String> teams = new ArrayList<>();
			forEach(a -> {
				if (!teams.contains(a.getClan().getId().toString())) {
					teams.add(a.getClan().getId().toString());
				}
			});
			ClansAPI api = ClansAPI.getInstance();
			return teams.stream().map(api::getClan).toArray(Clan[]::new);
		}

		public Clan.Associate[] associates() {
			return pool.keySet().toArray(new Clan.Associate[0]);
		}

		/**
		 * @return The amount of associates queued within a specific clan.
		 */
		public int count(Clan c) {
			return (int) pool.entrySet().stream().filter(a -> a.getKey().getClan().equals(c)).count();
		}

		public void clear() {
			pool.clear();
			roster.clear();
		}

		/**
		 * @return The total amount of players queued for the arena.
		 */
		public int size() {
			return this.pool.size();
		}

		@NotNull
		@Override
		public Iterator<Clan.Associate> iterator() {
			return pool.keySet().iterator();
		}

		@Override
		public void forEach(Consumer<? super Clan.Associate> action) {
			pool.keySet().forEach(action);
		}

		@Override
		public Spliterator<Clan.Associate> spliterator() {
			return pool.keySet().spliterator();
		}
	}

	enum Team {
		A(() -> {
			FileManager file = ClansAPI.getInstance().getFileList().find("locations", "Configuration", FileType.JSON);
			return file.read(c -> c.getNode("War").getNode("team-a").toBukkit().getLocation());
		}),
		B(() -> {
			FileManager file = ClansAPI.getInstance().getFileList().find("locations", "Configuration", FileType.JSON);
			return file.read(c -> c.getNode("War").getNode("team-b").toBukkit().getLocation());
		}),
		C(() -> {
			FileManager file = ClansAPI.getInstance().getFileList().find("locations", "Configuration", FileType.JSON);
			return file.read(c -> c.getNode("War").getNode("team-c").toBukkit().getLocation());
		}),
		D(() -> {
			FileManager file = ClansAPI.getInstance().getFileList().find("locations", "Configuration", FileType.JSON);
			return file.read(c -> c.getNode("War").getNode("team-d").toBukkit().getLocation());
		});

		private final Supplier<Location> spawn;

		Team(Supplier<Location> supplier) {
			this.spawn = supplier;
		}

		public @Nullable Location getSpawn() {
			return spawn.get();
		}

	}

	class Vote {

		public static final int YES = 1;
		public static final int NO = 2;
		private final List<Integer> numbers = new ArrayList<>();

		public void cast(@MagicConstant(intValues = {YES, NO}) int num) {
			numbers.add(num);
		}

		public int count(@MagicConstant(intValues = {YES, NO}) int num) {
			if (num == YES) {
				return (int) numbers.stream().filter(i -> i == 1).count();
			}
			if (num == NO) {
				return (int) numbers.stream().filter(i -> i == 2).count();
			}
			return -1;
		}

		public int getMajority() {
			int yes = (int) numbers.stream().filter(i -> i == 1).count();
			int no = (int) numbers.stream().filter(i -> i == 2).count();
			return Math.max(yes, no);
		}

		public boolean isUnanimous() {
			int yes = (int) numbers.stream().filter(i -> i == 1).count();
			int no = (int) numbers.stream().filter(i -> i == 2).count();
			return yes == no;
		}

		public void clear() {
			numbers.clear();
		}


	}

}
