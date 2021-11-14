package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.extra.ClanDisplayName;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.HUID;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface War extends Iterable<Clan.Associate> {

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
	 * Try to stop the running war
	 *
	 * @return false if the war isn't running
	 */
	boolean stop();

	/**
	 * The war is about to start, the clock is running down "X" configured minutes until everyone gets teleported
	 * to their respective spawn location, cancel that by using this method.
	 *
	 * @return false if the war hasn't initiated yet
	 */
	boolean avoid();

	/**
	 * Clear the queue (teleport participants back to their previous location) and reset objectives.
	 */
	void reset();

	/**
	 * Assign each queued associate to their team's (Doesn't start anything)
	 *
	 * If the teams are already populated they will be re-scrambled.
	 */
	void populate();

	/**
	 * @return true if the war is currently running.
	 */
	boolean isRunning();

	/**
	 * Get the recorded time started, if not started this method will instantiate itself.
	 *
	 * @return The recorded time started.
	 */
	long stamp();

	/**
	 * Get the team with the most points so far in the war.
	 *
	 * @return The team with the most points
	 */
	Map.Entry<Team, Integer> getMostPoints();

	/**
	 * Get how many points a team has. (kills)
	 *
	 * @param team The team to check
	 * @return The amount of points
	 */
	int getPoints(Team team);

	/**
	 * Set how many points a team has
	 *
	 * @param team the team to modify
	 * @param amount the amount to set
	 */
	void setPoints(Team team, int amount);

	/**
	 * Get the team a clan resides in for this war.
	 *
	 * @param c The clan to check.
	 * @return the clans team or null if they're not in this war.
	 */
	Team getTeam(Clan c);

	/**
	 * Get the public voting instance used for when multiple teams need to vote on the same thing.
	 *
	 * @return The public voting instance.
	 */
	Vote getVote();

	/**
	 * Get a team private voting instance used for when individual clans need to vote on some thing.
	 *
	 * @param team The team to use
	 * @return The team private voting instance.
	 */
	Vote getVote(Team team);

	/**
	 * Get the clan a team belongs to
	 *
	 * @param team the team to check
	 * @return The clan the team belongs to or null if non populated
	 */
	Clan getClan(Team team);

	/**
	 * Get the queuing object for this war, responsible for keeping track of participant locations and times queued along with teams.
	 *
	 * @return The war queue.
	 */
	Queue getQueue();

	/**
	 * Get the running timer for the war, it will tell you the remaining time until the war is finished.
	 *
	 * @return The war's timer.
	 */
	Cooldown getTimer();

	/**
	 * A clan war queuing object responsible for keeping track of participant information and team allocation.
	 */
	class Queue implements Iterable<Clan.Associate> {

		private final Map<Clan.Associate, Location> pool = new HashMap<>();
		private final Map<Team, Clan> roster = new HashMap<>();

		/**
		 * Re-shuffle the teams, (Places clans with no team into one)
		 *
		 * @return The new shuffled team board.
		 */
		public Map<Team, Clan> shuffle() {
			Clan[] a = getTeams();
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

		/**
		 * Que an associate for this war.
		 *
		 * @param associate The associate to que
		 * @return false if the associate is already in que.
		 */
		public boolean que(Clan.Associate associate) {
			if (!pool.containsKey(associate)) {
				Player p = associate.getUser().toBukkit().getPlayer();
				if (p == null) return false;
				pool.put(associate, p.getLocation());
				return true;
			}
			return false;
		}

		/**
		 * Change a queued associates return location for when the war is finished.
		 *
		 * @param associate The associate to update
		 */
		public void updateReturnLocation(Clan.Associate associate) {
			if (!pool.containsKey(associate)) return;
			pool.put(associate, associate.getUser().toBukkit().getPlayer().getLocation());
		}

		/**
		 * Un-que an associate from their current war removing them from the battlefield and teleporting them
		 * back to their previous location.
		 *
		 * @param associate The associate to un-que
		 * @return false if the associate isn't currently in que
		 */
		public boolean unque(Clan.Associate associate) {
			if (pool.containsKey(associate)) {
				Location loc = pool.get(associate);
				Player p = associate.getUser().toBukkit().getPlayer();
				if (p == null) {
					Schedule.sync(() -> pool.remove(associate)).run();
					return false;
				}
				if (ClansAPI.getDataInstance().isDisplayTagsAllowed()) {
					if (associate.getClan().getPalette().isGradient()) {
						Clan c = associate.getClan();
						ClanDisplayName.update(p, ClansAPI.getDataInstance().formatDisplayTag("", c.getPalette().toString((c.getName()))));
					} else {
						ClanDisplayName.update(p, ClansAPI.getDataInstance().formatDisplayTag(associate.getClan().getPalette().toString(), associate.getClan().getName()));
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

		/**
		 * Check if a given associates clan has a team yet
		 *
		 * @param associate The associate to use
		 * @return true if the associate's clan has a team, false if the que needs to re-shuffle
		 */
		public boolean test(Clan.Associate associate) {
			if (pool.containsKey(associate)) return false;
			Clan t = Arrays.stream(getTeams()).filter(c -> c.equals(associate.getClan())).findFirst().orElse(null);
			return t != null;
		}

		/**
		 * @return an array of queued clan's for this war.
		 */
		public Clan[] getTeams() {
			List<String> teams = new ArrayList<>();
			forEach(a -> {
				if (!teams.contains(a.getClan().getId().toString())) {
					teams.add(a.getClan().getId().toString());
				}
			});
			ClansAPI api = ClansAPI.getInstance();
			return teams.stream().map(HUID::fromString).map(h -> api.getClanManager().getClan(h)).toArray(Clan[]::new);
		}

		/**
		 * @return an array of queued associates for this war.
		 */
		public Clan.Associate[] getAssociates() {
			return pool.keySet().toArray(new Clan.Associate[0]);
		}

		/**
		 * @return The amount of associates queued within a specific clan.
		 */
		public int count(Clan c) {
			return ((Number) pool.entrySet().stream().filter(a -> a.getKey().getClan().equals(c)).count()).intValue();
		}

		/**
		 * Clear the queue completely (Un-queues every contained associate)
		 */
		public void clear() {
			pool.forEach((associate, location) -> unque(associate));
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

	/**
	 *
	 */
	enum Team {
		A(() -> {
			FileManager file = ClansAPI.getInstance().getFileList().get("locations", "Configuration", FileType.JSON);
			return file.read(c -> c.getNode("War").getNode("team-a").toBukkit().getLocation());
		}),
		B(() -> {
			FileManager file = ClansAPI.getInstance().getFileList().get("locations", "Configuration", FileType.JSON);
			return file.read(c -> c.getNode("War").getNode("team-b").toBukkit().getLocation());
		}),
		C(() -> {
			FileManager file = ClansAPI.getInstance().getFileList().get("locations", "Configuration", FileType.JSON);
			return file.read(c -> c.getNode("War").getNode("team-c").toBukkit().getLocation());
		}),
		D(() -> {
			FileManager file = ClansAPI.getInstance().getFileList().get("locations", "Configuration", FileType.JSON);
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

}
