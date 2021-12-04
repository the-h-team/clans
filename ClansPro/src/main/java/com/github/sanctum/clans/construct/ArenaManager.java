package com.github.sanctum.clans.construct;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.impl.SimpleEntry;
import com.github.sanctum.clans.event.war.WarStartEvent;
import com.github.sanctum.clans.event.war.WarWonEvent;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.annotation.Note;
import com.github.sanctum.labyrinth.api.Service;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.task.Schedule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ArenaManager implements Iterable<War> {

	private final Set<War> arenas = new HashSet<>();
	private static final int MAX_TEAMS;
	private static final int TRIGGER;

	static {
		MAX_TEAMS = ClansAPI.getDataInstance().getConfigInt("Clans.war.max-clans");
		TRIGGER = ClansAPI.getDataInstance().getConfigInt("Clans.war.que-needed");
	}

	@Note("Load a new usable war instance into cache!")
	public boolean load(War arena) {
		return arenas.add(arena);
	}

	@Note("Remove a war instance from cache.")
	public boolean remove(War arena) {
		if (arena.isRunning()) {
			arena.stop();
			arena.reset();
		}
		return arenas.remove(arena);
	}

	@Note("Get a war arena by its id.")
	public War get(String id) {
		return arenas.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
	}

	@Note("If an associate is queued or currently playing get their arena.")
	public @Nullable War get(Clan.Associate associate) {
		return arenas.stream().filter(a -> Arrays.asList(a.getQueue().getAssociates()).contains(associate)).findFirst().orElse(null);
	}

	@Note("Attempt to queue an associate into a war.")
	public @Nullable War queue(Clan.Associate associate) {
		War free;
		// Try to join a running war with our clan mates.
		War running = arenas.stream().filter(a -> a.isRunning() && a.getTeam(associate.getClan()) != null).findFirst().orElse(null);
		if (running != null) {
			free = running;
		} else {
			// No mutual clan war found. Check if we can queue for a new one.
			free = arenas.stream().filter(a -> !a.isRunning()).findFirst().orElse(null);
		}

		if (free == null) return null;
		War.Queue q = free.getQueue();

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
				LabyrinthProvider.getService(Service.MESSENGER).getNewMessage().setPrefix(ClansAPI.getInstance().getPrefix().joined()).broadcast("&3A new clan war between clans &b[" + Arrays.stream(q.getTeams()).map(Clan::getName).collect(Collectors.joining(",")) + "] &3starts in " + time.getMinutesLeft() + " minute(s) & " + time.getSecondsLeft() + " second(s)");
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
				Schedule.sync(() -> {
				}).cancelAfter(t -> {
					WarStartEvent e = ClanVentBus.call(new WarStartEvent(free));
					if (e.isCancelled()) {
						t.cancel();
					}
				}).repeatReal(0, 1);
			}
		}
		return free;
	}

	@Note("Hide this associate completely from a given war instance")
	public void hide(Clan.Associate associate, War war) {
		Plugin plugin = ClansAPI.getInstance().getPlugin();
		Player pla = associate.getUser().toBukkit().getPlayer();
		if (pla != null) {
			war.forEach(ass -> {
				Player pl = ass.getUser().toBukkit().getPlayer();
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
	public void hideAll(War war) {
		forEach(a -> {
			if (!a.getId().equals(war.getId())) {
				a.forEach(as -> hide(as, war));
			}
		});
	}

	@Note("Show this associate to anyone who can't already see them, visa versa.")
	public void show(Clan.Associate associate) {
		Plugin plugin = ClansAPI.getInstance().getPlugin();
		Player pl = associate.getUser().toBukkit().getPlayer();
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
	public void showAll(War war) {
		if (!war.isRunning()) return;
		Plugin plugin = ClansAPI.getInstance().getPlugin();
		for (Player p : Bukkit.getOnlinePlayers()) {
			war.forEach(a -> {
				Player pl = a.getUser().toBukkit().getPlayer();
				if (pl != null) {
					if (!pl.canSee(p)) {
						Clan.Associate as = ClansAPI.getInstance().getAssociate(pl).orElse(null);
						if (as != null) {
							War w = get(as);
							if (w != null) {
								if (!w.getId().equals(war.getId())) {
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
		War a = get(associate);
		if (a == null) return false;
		return a.getQueue().unque(associate);
	}

	@NotNull
	@Override
	public Iterator<War> iterator() {
		return arenas.iterator();
	}

	@Override
	public void forEach(Consumer<? super War> action) {
		arenas.forEach(action);
	}

	@Override
	public Spliterator<War> spliterator() {
		return arenas.spliterator();
	}

	public void conclude(War war) {
		if (!war.isRunning()) return;
		if (war.stop()) {
			War.Team winner = war.getMostPoints().getKey();
			int points = war.getMostPoints().getValue();
			Clan w = war.getClan(winner);
			w.giveWins(1);
			Map<Clan, Integer> map = new HashMap<>();
			for (Clan c : war.getQueue().getTeams()) {
				if (!c.getName().equals(w.getName())) {
					War.Team t = war.getTeam(c);
					map.put(c, war.getPoints(t));
					c.takeWins(1);
				}
			}
			WarWonEvent e = ClanVentBus.call(new WarWonEvent(war, new SimpleEntry<>(w, points), map));
			if (!e.isCancelled()) {
				Message msg = LabyrinthProvider.getService(Service.MESSENGER).getNewMessage().setPrefix(ClansAPI.getInstance().getPrefix().joined());
				Bukkit.broadcastMessage(" ");
				msg.broadcast("&3A war between clans &b[" + Arrays.stream(war.getQueue().getTeams()).map(Clan::getName).collect(Collectors.joining(",")) + "]&3 in arena &7#&e" + war.getId() + " &3concluded with winner &6&l" + w.getName() + " &f(&a" + points + "&f)");
				Bukkit.broadcastMessage(" ");
			}
			war.reset();
		}
	}
}
