package com.github.sanctum.clans;

import com.github.sanctum.clans.model.ClanVentBus;
import com.github.sanctum.clans.model.Arena;
import com.github.sanctum.clans.model.Clan;
import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.impl.DefaultMapEntry;
import com.github.sanctum.clans.event.arena.ArenaStartingEvent;
import com.github.sanctum.clans.event.arena.ArenaWonEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.Mailer;
import com.github.sanctum.labyrinth.task.BukkitTaskPredicate;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import com.github.sanctum.panther.annotation.Note;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ArenaManager implements Iterable<Arena> {

	private final Set<Arena> arenas = new HashSet<>();
	private static final int MAX_TEAMS;
	private static final int TRIGGER;

	static {
		MAX_TEAMS = ClansAPI.getDataInstance().getConfigInt("Clans.arena.max-clans");
		TRIGGER = ClansAPI.getDataInstance().getConfigInt("Clans.arena.que-needed");
	}

	@Note("Load a new usable war instance into cache!")
	public boolean load(Arena arena) {
		return arenas.add(arena);
	}

	@Note("Remove a war instance from cache.")
	public boolean remove(Arena arena) {
		if (arena.isRunning()) {
			arena.stop();
			arena.reset();
		}
		return arenas.remove(arena);
	}

	@Note("Get a war arena by its id.")
	public Arena get(String id) {
		return arenas.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
	}

	@Note("If an associate is queued or currently playing get their arena.")
	public @Nullable Arena get(Clan.Associate associate) {
		return arenas.stream().filter(a -> Arrays.asList(a.getQueue().getAssociates()).contains(associate)).findFirst().orElse(null);
	}

	@Note("Attempt to queue an associate into a war.")
	public @Nullable Arena queue(Clan.Associate associate) {
		Arena free;
		// Try to join a running war with our clan mates.
		Arena running = arenas.stream().filter(a -> a.isRunning() && a.getTeam(associate.getClan()) != null).findFirst().orElse(null);
		if (running != null) {
			free = running;
		} else {
			// No mutual clan war found. Check if we can queue for a new one.
			free = arenas.stream().filter(a -> !a.isRunning()).findFirst().orElse(null);
		}

		if (free == null) return null;
		Arena.Queue q = free.getQueue();

		if (q.getTeams().length == MAX_TEAMS) {
			if (!q.test(associate)) {
				return null;
			}
		}
		if (!q.que(associate)) {
			return null;
		}
		boolean can = false;
		for (Clan c : q.getTeams()) {
			int count = q.count(c);
			can = count == TRIGGER;
		}
		if (!free.isRunning()) {
			if (can && q.getTeams().length == MAX_TEAMS) {
				Cooldown time = new Cooldown() {

					private final long time;

					{
						this.time = abv(LabyrinthProvider.getInstance().getLocalPrintManager().getPrint(ClansAPI.getInstance().getLocalPrintKey()).getNumber("war_start_time").intValue());
					}

					@Override
					public String getId() {
						return null;
					}

					@Override
					public long getCooldown() {
						return this.time;
					}
				};
				LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer().prefix().start(ClansAPI.getInstance().getPrefix().toString()).finish().announce(p -> true, "&3A new death-match between clans &b[" + Arrays.stream(q.getTeams()).map(Clan::getName).collect(Collectors.joining(",")) + "] &3starts in " + time.getMinutes() + " minute(s) & " + time.getSeconds() + " second(s)").deploy();
				free.stamp();
				new Cooldown() {

					private final long time;
					private final String id;

					{
						this.time = abv(LabyrinthProvider.getInstance().getLocalPrintManager().getPrint(ClansAPI.getInstance().getLocalPrintKey()).getNumber("war_start_time").intValue());
						this.id = "war-" + free.getId() + "-start";
					}

					@Override
					public String getId() {
						return this.id;
					}

					@Override
					public long getCooldown() {
						return this.time;
					}
				}.save();
				TaskScheduler.of(() -> {
				}).scheduleTimer(UUID.randomUUID().toString(), 0, 1, BukkitTaskPredicate.cancelAfter(t -> {
					ArenaStartingEvent e = ClanVentBus.call(new ArenaStartingEvent(free));
					if (e.getStatus() == ArenaStartingEvent.Status.STARTED || e.isCancelled()) {
						t.cancel();
						return false;
					}
					return true;
				}));
			}
		}
		return free;
	}

	@Note("Hide this associate completely from a given war instance")
	public void hide(Clan.Associate associate, Arena arena) {
		Plugin plugin = ClansAPI.getInstance().getPlugin();
		Player pla = associate.getTag().getPlayer().getPlayer();
		if (pla != null) {
			arena.forEach(ass -> {
				Player pl = ass.getTag().getPlayer().getPlayer();
				if (pl != null) {
					if (pl.canSee(pla)) {
						pl.hidePlayer(plugin, pla);
						pla.hidePlayer(plugin, pl);
					}
				}
			});
		}
	}

	@Note("Hide every war participant from other online players not in the same instance.")
	public void hideAll(Arena arena) {
		forEach(a -> {
			if (!a.getId().equals(arena.getId())) {
				a.forEach(as -> hide(as, arena));
			}
		});
	}

	@Note("Show this associate to anyone who can't already see them, visa versa.")
	public void show(Clan.Associate associate) {
		Plugin plugin = ClansAPI.getInstance().getPlugin();
		Player pl = associate.getTag().getPlayer().getPlayer();
		if (pl != null) {
			for (Player p : Bukkit.getOnlinePlayers()) {

				if (!pl.canSee(p)) {
					pl.showPlayer(plugin, p);
					p.showPlayer(plugin, pl);
				}

			}
		}

	}

	@Note("Show every war participant to hidden online players outside a war instance.")
	public void showAll(Arena arena) {
		if (!arena.isRunning()) return;
		Plugin plugin = ClansAPI.getInstance().getPlugin();
		for (Player p : Bukkit.getOnlinePlayers()) {
			arena.forEach(a -> {
				Player pl = a.getTag().getPlayer().getPlayer();
				if (pl != null) {
					if (!pl.canSee(p)) {
						Clan.Associate as = ClansAPI.getInstance().getAssociate(pl).orElse(null);
						if (as != null) {
							Arena w = get(as);
							if (w != null) {
								if (!w.getId().equals(arena.getId())) {
									if (!w.isRunning()) {
										pl.showPlayer(plugin, p);
										p.showPlayer(plugin, pl);
									}
								}
							} else {
								pl.showPlayer(plugin, p);
								p.showPlayer(plugin, pl);
							}
						} else {
							pl.showPlayer(plugin, p);
							p.showPlayer(plugin, pl);
						}
					}
				}
			});
		}
	}

	@Note("Force an associate to leave their current war.")
	public boolean leave(Clan.Associate associate) {
		Arena a = get(associate);
		if (a == null) return false;
		return a.getQueue().unque(associate);
	}

	@NotNull
	@Override
	public Iterator<Arena> iterator() {
		return arenas.iterator();
	}

	@Override
	public void forEach(Consumer<? super Arena> action) {
		arenas.forEach(action);
	}

	@Override
	public Spliterator<Arena> spliterator() {
		return arenas.spliterator();
	}

	public void conclude(Arena arena) {
		if (!arena.isRunning()) return;
		if (arena.stop()) {
			Arena.Team winner = arena.getMostPoints().getKey();
			int points = arena.getMostPoints().getValue();
			Clan w = arena.getClan(winner);
			w.giveWins(1);
			Map<Clan, Integer> map = new HashMap<>();
			for (Clan c : arena.getQueue().getTeams()) {
				if (!c.getName().equals(w.getName())) {
					Arena.Team t = arena.getTeam(c);
					map.put(c, arena.getPoints(t));
					c.takeWins(1);
				}
			}
			ArenaWonEvent e = ClanVentBus.call(new ArenaWonEvent(arena, new DefaultMapEntry<>(w, points), map));
			if (!e.isCancelled()) {
				Mailer msg = LabyrinthProvider.getService(Service.MESSENGER).getEmptyMailer().prefix().start(ClansAPI.getInstance().getPrefix().toString()).finish();
				Bukkit.broadcastMessage(" ");
				msg.announce(p -> true, "&3A death-match between clans &b[" + Arrays.stream(arena.getQueue().getTeams()).map(Clan::getName).collect(Collectors.joining(",")) + "]&3 in arena &7#&e" + arena.getId() + " &3concluded with winner &6&l" + w.getName() + " &f(&a" + points + "&f)");
				Bukkit.broadcastMessage(" ");
			}
			arena.reset();
		}
	}
}
