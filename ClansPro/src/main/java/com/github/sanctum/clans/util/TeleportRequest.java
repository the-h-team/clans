package com.github.sanctum.clans.util;

import com.github.sanctum.clans.construct.ClanAssociate;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.library.Message;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.labyrinth.task.Schedule;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public abstract class TeleportRequest {

	private static final Set<TeleportRequest> REQUESTS = new HashSet<>();

	private final Player target;

	private final Date date;

	private Date accepted;

	private State state;

	private final ClanAssociate associate;

	protected TeleportRequest(ClanAssociate teleporter, Player target) {
		this.target = target;
		this.associate = teleporter;
		this.date = new Date();
		this.state = State.INITIALIZED;
		REQUESTS.add(this);
	}

	public ClanAssociate getAssociate() {
		return associate;
	}

	public TimeWatch.Recording getRecording() {
		return TimeWatch.Recording.subtract(date.getTime());
	}

	public TimeWatch.Recording getAccepted() {
		return TimeWatch.Recording.subtract(accepted.getTime());
	}

	public Player getTarget() {
		return target;
	}

	public void setState(State state) {
		this.state = state;
	}

	public State getState() {
		return this.state;
	}

	public void teleport() {
		Message.form(getAssociate().getPlayer().getPlayer()).setPrefix(ClansAPI.getInstance().getPrefix().joined()).send("&aTeleporting in 10 seconds, don't move.");
		Message.form(getTarget()).setPrefix(ClansAPI.getInstance().getPrefix().joined()).send("&a" + associate.getPlayer().getName() + " is teleporting to you.");
		this.state = State.TELEPORTING;
		this.accepted = new Date();
		Schedule.sync(() -> {

		}).cancelAfter(associate.getPlayer().getPlayer()).cancelAfter(task -> {

			if (getState() == TeleportRequest.State.TELEPORTING) {
				associate.getPlayer().getPlayer().teleport(getTarget());
				cancel();
				task.cancel();
				associate.getPlayer().getPlayer().getWorld().playSound(associate.getPlayer().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
			} else {
				task.cancel();
				cancel();
			}

		}).waitReal(20 * 10);
	}

	public void cancel() {
		REQUESTS.remove(this);
	}

	public static TeleportRequest get(ClanAssociate associate) {
		return REQUESTS.stream().filter(r -> r.getAssociate().equals(associate)).findFirst().orElse(null);
	}

	public static class Impl extends TeleportRequest {
		public Impl(ClanAssociate teleporter, Player target) {
			super(teleporter, target);
		}
	}

	public enum State {
		INITIALIZED, TELEPORTING, EXPIRED;
	}

}
