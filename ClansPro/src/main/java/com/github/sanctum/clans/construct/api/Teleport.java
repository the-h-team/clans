package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.extra.TeleportationTarget;
import com.github.sanctum.clans.event.associate.AssociateTeleportEvent;
import com.github.sanctum.clans.event.player.PlayerTeleportEvent;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.labyrinth.task.Schedule;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public abstract class Teleport {

	public abstract State getState();

	public abstract void setState(State state);

	public abstract InvasiveEntity getEntity();

	public abstract TeleportationTarget getTarget();

	public abstract Location getLocationBeforeTeleport();

	public abstract TimeWatch.Recording getTimeAccepted();

	public abstract void teleport();

	public abstract void cancel();

	public static Teleport get(InvasiveEntity entity) {
		return Impl.REQUESTS.stream().filter(r -> r.getEntity().equals(entity)).findFirst().orElse(null);
	}

	public static Teleport get(Player player) {
		return get(InvasiveEntity.wrapNonAssociated(player));
	}

	public static class Impl extends Teleport {
		private static final Set<Teleport> REQUESTS = new HashSet<>();
		private final Player target;
		private final Location location;
		private Date accepted;
		private State state;
		private Location start;
		private final InvasiveEntity entity;

		public Impl(InvasiveEntity teleporter, Player target) {
			this.target = target;
			this.location = null;
			this.entity = teleporter;
			this.state = State.INITIALIZED;
			REQUESTS.add(this);
		}

		public Impl(InvasiveEntity teleporter, Location target) {
			this.location = target;
			this.target = null;
			this.entity = teleporter;
			this.state = State.INITIALIZED;
			REQUESTS.add(this);
		}

		public Location getLocationBeforeTeleport() {
			return start;
		}

		public InvasiveEntity getEntity() {
			return entity;
		}

		public TimeWatch.Recording getTimeAccepted() {
			return TimeWatch.Recording.subtract(accepted.getTime());
		}

		public TeleportationTarget getTarget() {
			return target != null ? new TeleportationTarget(target) : new TeleportationTarget(location);
		}

		public void setState(State state) {
			this.state = state;
		}

		public State getState() {
			return this.state;
		}

		public void teleport() {
			if (entity.isAssociate()) {
				start = entity.getAsAssociate().getUser().toBukkit().getPlayer().getLocation();
				if (this.target != null) {
					getEntity().getAsAssociate().getMailer().chat("&aTeleporting in 10 seconds, don't move.").deploy();
					Clan.ACTION.sendMessage(getTarget().getAsPlayer(), "&a" + entity.getAsAssociate().getUser().getName() + " is teleporting to you.");
					this.state = State.TELEPORTING;
					this.accepted = new Date();
					Schedule.sync(() -> {
						if (!REQUESTS.contains(this)) return;
						if (getState() == State.TELEPORTING) {
							AssociateTeleportEvent event = ClanVentBus.call(new AssociateTeleportEvent(getEntity().getAsAssociate(), new TeleportationTarget(this.target)));
							if (!event.isCancelled()) {
								entity.getAsAssociate().getUser().toBukkit().getPlayer().teleport(event.getTarget().getAsPlayer(), org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND);
								cancel();
								entity.getAsAssociate().getUser().toBukkit().getPlayer().getWorld().playSound(entity.getAsAssociate().getUser().toBukkit().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
							}
						} else {
							cancel();
						}
					}).waitReal(20 * 10);
				} else {
					getEntity().getAsAssociate().getMailer().chat("&aTeleporting in 10 seconds, don't move.").deploy();
					this.state = State.TELEPORTING;
					this.accepted = new Date();
					Schedule.sync(() -> {
						if (!REQUESTS.contains(this)) return;
						if (getState() == State.TELEPORTING) {
							AssociateTeleportEvent event = ClanVentBus.call(new AssociateTeleportEvent(getEntity().getAsAssociate(), new TeleportationTarget(this.location)));
							if (!event.isCancelled()) {
								entity.getAsAssociate().getUser().toBukkit().getPlayer().teleport(event.getTarget().getAsLocation(), org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND);
								cancel();
								entity.getAsAssociate().getUser().toBukkit().getPlayer().getWorld().playSound(entity.getAsAssociate().getUser().toBukkit().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
							}
						} else {
							cancel();
						}
					}).waitReal(20 * 10);
				}
			} else {
				if (entity.isClan()) {
					if (this.target != null) {
						getEntity().getAsClan().getMembers().forEach(a -> {
							if (!a.getUser().isOnline()) return;
							a.getMailer().chat("&aTeleporting in 10 seconds, don't move.").deploy();
							Clan.ACTION.sendMessage(getTarget().getAsPlayer(), "&a" + a.getUser().getName() + " is teleporting to you.");
							this.state = State.TELEPORTING;
							this.accepted = new Date();
							Schedule.sync(() -> {
								if (!REQUESTS.contains(this)) return;
								if (getState() == State.TELEPORTING) {
									AssociateTeleportEvent event = ClanVentBus.call(new AssociateTeleportEvent(a, new TeleportationTarget(this.target)));
									if (!event.isCancelled()) {
										a.getUser().toBukkit().getPlayer().teleport(event.getTarget().getAsPlayer(), org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND);
										cancel();
										a.getUser().toBukkit().getPlayer().getWorld().playSound(entity.getAsAssociate().getUser().toBukkit().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
									}
								} else {
									cancel();
								}
							}).waitReal(20 * 10);
						});
					} else {
						getEntity().getAsClan().getMembers().forEach(a -> {
							if (!a.getUser().isOnline()) return;
							a.getMailer().chat("&aTeleporting in 10 seconds, don't move.").deploy();
							this.state = State.TELEPORTING;
							this.accepted = new Date();
							Schedule.sync(() -> {
								if (!REQUESTS.contains(this)) return;
								if (getState() == State.TELEPORTING) {
									AssociateTeleportEvent event = ClanVentBus.call(new AssociateTeleportEvent(a, new TeleportationTarget(this.location)));
									if (!event.isCancelled()) {
										a.getUser().toBukkit().getPlayer().teleport(event.getTarget().getAsLocation());
										cancel();
										a.getUser().toBukkit().getPlayer().getWorld().playSound(entity.getAsAssociate().getUser().toBukkit().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
									}
								} else {
									cancel();
								}
							}).waitReal(20 * 10);
						});
					}
				}
				if (entity.isPlayer()) {
					start = getEntity().getAsPlayer().getPlayer().getLocation();
					if (this.target != null) {
						Clan.ACTION.sendMessage(getEntity().getAsPlayer().getPlayer(), "&aTeleporting in 10 seconds, don't move.");
						Clan.ACTION.sendMessage(getTarget().getAsPlayer(), "&a" + entity.getAsAssociate().getUser().getName() + " is teleporting to you.");
						this.state = State.TELEPORTING;
						this.accepted = new Date();
						Schedule.sync(() -> {
							if (!REQUESTS.contains(this)) return;
							if (getState() == State.TELEPORTING) {
								PlayerTeleportEvent event = ClanVentBus.call(new PlayerTeleportEvent(getEntity().getAsPlayer().getPlayer(), new TeleportationTarget(this.target)));
								if (!event.isCancelled()) {
									getEntity().getAsPlayer().getPlayer().teleport(event.getTarget().getAsPlayer());
									cancel();
									getEntity().getAsPlayer().getPlayer().getWorld().playSound(getEntity().getAsPlayer().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
								}
							} else {
								cancel();
							}
						}).waitReal(20 * 10);
					} else {
						Clan.ACTION.sendMessage(getEntity().getAsPlayer().getPlayer(), "&aTeleporting in 10 seconds, don't move.");
						this.state = State.TELEPORTING;
						this.accepted = new Date();
						Schedule.sync(() -> {
							if (!REQUESTS.contains(this)) return;
							if (getState() == State.TELEPORTING) {
								PlayerTeleportEvent event = ClanVentBus.call(new PlayerTeleportEvent(getEntity().getAsPlayer().getPlayer(), new TeleportationTarget(this.location)));
								if (!event.isCancelled()) {
									getEntity().getAsPlayer().getPlayer().teleport(event.getTarget().getAsLocation());
									cancel();
									getEntity().getAsPlayer().getPlayer().getWorld().playSound(getEntity().getAsPlayer().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
								}
							} else {
								cancel();
							}
						}).waitReal(20 * 10);
					}
				}
			}
		}

		public void cancel() {
			REQUESTS.remove(this);
		}
	}

	public enum State {
		INITIALIZED, TELEPORTING, EXPIRED
	}

}
