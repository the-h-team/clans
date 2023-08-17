package com.github.sanctum.clans.construct.impl;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.api.Vote;
import com.github.sanctum.clans.construct.api.War;
import com.github.sanctum.clans.construct.util.AboveHeadDisplayName;
import com.github.sanctum.labyrinth.LabyrinthProvider;
import com.github.sanctum.labyrinth.library.Cooldown;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class DefaultArena implements War {

	final String id;
	final Queue queue;
	final Vote G = new Vote();
	final Vote A = new Vote();
	final Vote B = new Vote();
	final Vote C = new Vote();
	final Vote D = new Vote();
	long time;
	boolean pre;
	Cooldown timer;
	final Map<Clan, Team> roster;
	final Map<Team, Integer> pointMap;

	public DefaultArena(String id) {
		this.id = id;
		this.queue = new Queue();
		this.pointMap = new HashMap<>();
		this.roster = new HashMap<>();
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public boolean start() {
		if (pre) return false;
		if (getTimer() == null) {
			populate();
			for (Team t : Team.values()) {
				if (t.getSpawn() == null) {
					ClansAPI.getInstance().getPlugin().getLogger().severe("- Missing spawn location for team '" + t.name() + "'");
					return false;
				}
			}
			getQueue().forEach(a -> {
				Team t = roster.get(a.getClan());
				TaskScheduler.of(() -> {
					Player p = a.getTag().getPlayer().getPlayer();
					if (p != null) {
						getQueue().updateReturnLocation(a);
						AboveHeadDisplayName.set(a, "&7[&a" + t.name() + "&7]");
						Clan.ACTION.sendMessage(p, "&3BEGIN!");
						assert t.getSpawn() != null;
						p.teleport(t.getSpawn());
					} else {
						getQueue().unque(a);
					}
				}).schedule();
			});
			timer = new Cooldown() {

				private final long time;
				private final String id;

				{
					time = abv(ClansAPI.getDataInstance().getConfigInt("Clans.war.max-length"));
					this.id = DefaultArena.this.getId();
				}

				@Override
				public String getId() {
					return this.id;
				}

				@Override
				public long getCooldown() {
					return time;
				}
			};
			timer.save();
			ClansAPI.getInstance().getArenaManager().hideAll(this);
			return true;
		}

		return false;
	}

	@Override
	public void populate() {
		getQueue().shuffle().forEach((key, value) -> roster.put(value, key));
		roster.forEach((clan, team) -> {
			if (!pointMap.containsKey(team)) {
				pointMap.put(team, 1);
			}
		});
	}

	@Override
	public boolean stop() {
		if (getTimer() != null) {
			ClansAPI.getInstance().getArenaManager().showAll(this);
			LabyrinthProvider.getInstance().remove(getTimer());
			this.timer = null;
			return true;
		}
		return false;
	}

	@Override
	public boolean avoid() {
		if (isRunning()) return false;
		pre = true;
		return true;
	}

	@Override
	public void reset() {
		if (isRunning()) {
			ClansAPI.getInstance().getArenaManager().showAll(this);
			LabyrinthProvider.getInstance().remove(getTimer());
			this.timer = null;
		}
		this.time = 0;
		getQueue().clear();
		roster.clear();
		pointMap.clear();
		A.clear();
		B.clear();
		C.clear();
		D.clear();
		G.clear();
	}

	@Override
	public boolean isRunning() {
		return getTimer() != null;
	}

	@Override
	public long stamp() {
		if (this.time == 0) {
			this.time = System.currentTimeMillis();
		}
		return this.time;
	}

	@Override
	public Map.Entry<Team, Integer> getMostPoints() {
		return pointMap.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).orElse(null);
	}

	@Override
	public int getPoints(Team team) {
		return pointMap.computeIfAbsent(team, t -> 0);
	}

	@Override
	public void setPoints(Team team, int amount) {
		pointMap.put(team, amount);
		if (getPoints(team) >= ClansAPI.getDataInstance().getConfigInt("Clans.war.max-points")) {
			ClansAPI.getInstance().getArenaManager().conclude(this);
		}
	}

	@Override
	public Team getTeam(Clan c) {
		return roster.get(c);
	}

	@Override
	public Vote getVote() {
		return this.G;
	}

	@Override
	public Vote getVote(Team team) {
		switch (team) {
			case A:
				return this.A;
			case B:
				return this.B;
			case C:
				return this.C;
			case D:
				return this.D;
		}
		return null;
	}

	@Override
	public Clan getClan(Team team) {
		return roster.entrySet().stream().filter(e -> e.getValue() == team).map(Map.Entry::getKey).findFirst().orElse(null);
	}

	@Override
	public Queue getQueue() {
		return this.queue;
	}

	@Override
	public Cooldown getTimer() {
		return this.timer;
	}

	@NotNull
	@Override
	public Iterator<Clan.Associate> iterator() {
		return getQueue().iterator();
	}

	@Override
	public void forEach(Consumer<? super Clan.Associate> action) {
		getQueue().forEach(action);
	}

	@Override
	public Spliterator<Clan.Associate> spliterator() {
		return getQueue().spliterator();
	}
}
