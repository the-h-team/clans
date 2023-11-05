package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.bridge.ClanVentBus;
import com.github.sanctum.clans.construct.util.TeleportationTarget;
import com.github.sanctum.clans.event.associate.AssociateTeleportEvent;
import com.github.sanctum.clans.event.player.PlayerTeleportEvent;
import com.github.sanctum.labyrinth.library.TimeWatch;
import com.github.sanctum.labyrinth.task.TaskScheduler;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class Teleport {

	protected final Set<SuccessOperator> successOperators = new HashSet<>();

	public abstract State getState();

	public abstract void setState(State state);

	public abstract InvasiveEntity getEntity();

	public abstract TeleportationTarget getTarget();

	public abstract Location getLocationBeforeTeleport();

	public abstract TimeWatch.Recording getTimeAccepted();

	public abstract void teleport();

	public abstract void cancel();

	public void register(@NotNull SuccessOperator operator) {
		this.successOperators.add(operator);
	}

	public static Teleport get(InvasiveEntity entity) {
		return Impl.REQUESTS.stream().filter(r -> r.getEntity().equals(entity)).findFirst().orElse(null);
	}

	public static Teleport get(Player player) {
		return get(InvasiveEntity.wrapNonAssociated(player));
	}

	@FunctionalInterface
	public interface SuccessOperator {

		void onTeleportSuccess(InvasiveEntity parent);

	}

	public static class Impl extends Teleport {
		private static final Set<Teleport> REQUESTS = new HashSet<>();
		private final InvasiveEntity entity;
		private final Player target;
		private final Location location;
		private final String teleportMsg;
		private final int seconds;
		private Date accepted;
		private State state;
		private Location start;

		public Impl(InvasiveEntity teleporter, TeleportationTarget target) {
			if (target.isLocation()) {
				this.target = null;
				this.location = target.getAsLocation();
			} else {
				this.location = null;
				this.target = target.getAsPlayer();
			}
			this.entity = teleporter;
			this.state = State.INITIALIZED;
			REQUESTS.add(this);
			this.teleportMsg = ClansAPI.getDataInstance().getMessageResponse("teleporting");
			this.seconds = ClansAPI.getDataInstance().getConfigInt("teleportation-time");
		}

		public Impl(InvasiveEntity teleporter, Player target) {
			this.target = target;
			this.location = null;
			this.entity = teleporter;
			this.state = State.INITIALIZED;
			REQUESTS.add(this);
			this.teleportMsg = ClansAPI.getDataInstance().getMessageResponse("teleporting");
			this.seconds = ClansAPI.getDataInstance().getConfigInt("teleportation-time");
		}

		public Impl(InvasiveEntity teleporter, Location target) {
			this.location = target;
			this.target = null;
			this.entity = teleporter;
			this.state = State.INITIALIZED;
			REQUESTS.add(this);
			this.teleportMsg = ClansAPI.getDataInstance().getMessageResponse("teleporting");
			this.seconds = ClansAPI.getDataInstance().getConfigInt("teleportation-time");
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
				start = entity.getAsAssociate().getTag().getPlayer().getPlayer().getLocation();
				if (this.target != null) {
					if (entity.getAsAssociate().getTag().getPlayer().getPlayer().getNearbyEntities(30, 0, 30).stream().noneMatch(e -> e instanceof Player && getEntity().getAsAssociate().getClan().getMember(m -> m.getName().equals(e.getName())) == null)) {
						this.state = State.TELEPORTING;
						this.accepted = new Date();
						if (!REQUESTS.contains(this)) return;
						Clan.ACTION.sendMessage(getTarget().getAsPlayer(), "&a" + entity.getAsAssociate().getName() + " is teleporting to you.");
						AssociateTeleportEvent event = ClanVentBus.call(new AssociateTeleportEvent(getEntity().getAsAssociate(), new TeleportationTarget(this.target)));
						if (!event.isCancelled()) {
							successOperators.forEach(operator -> operator.onTeleportSuccess(entity));
							entity.getAsAssociate().getTag().getPlayer().getPlayer().teleport(event.getTarget().getAsPlayer(), org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND);
							cancel();
							entity.getAsAssociate().getTag().getPlayer().getPlayer().getWorld().playSound(entity.getAsAssociate().getTag().getPlayer().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
						}
					} else {
						getEntity().getAsAssociate().getMailer().chat(MessageFormat.format(teleportMsg, seconds)).deploy();
						Clan.ACTION.sendMessage(getTarget().getAsPlayer(), "&a" + entity.getAsAssociate().getName() + " is teleporting to you.");
						this.state = State.TELEPORTING;
						this.accepted = new Date();
						TaskScheduler.of(() -> {
							if (!REQUESTS.contains(this)) return;
							if (getState() == State.TELEPORTING) {
								AssociateTeleportEvent event = ClanVentBus.call(new AssociateTeleportEvent(getEntity().getAsAssociate(), new TeleportationTarget(this.target)));
								if (!event.isCancelled()) {
									successOperators.forEach(operator -> operator.onTeleportSuccess(entity));
									entity.getAsAssociate().getTag().getPlayer().getPlayer().teleport(event.getTarget().getAsPlayer(), org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND);
									cancel();
									entity.getAsAssociate().getTag().getPlayer().getPlayer().getWorld().playSound(entity.getAsAssociate().getTag().getPlayer().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
								}
							} else {
								cancel();
							}
						}).scheduleLater(20 * 10);
					}
				} else {
					if (entity.getAsAssociate().getTag().getPlayer().getPlayer().getNearbyEntities(30, 0, 30).stream().noneMatch(e -> e instanceof Player && getEntity().getAsAssociate().getClan().getMember(m -> m.getName().equals(e.getName())) == null)) {
						this.state = State.TELEPORTING;
						this.accepted = new Date();
						AssociateTeleportEvent event = ClanVentBus.call(new AssociateTeleportEvent(getEntity().getAsAssociate(), new TeleportationTarget(this.location)));
						if (!event.isCancelled()) {
							successOperators.forEach(operator -> operator.onTeleportSuccess(entity));
							entity.getAsAssociate().getTag().getPlayer().getPlayer().teleport(event.getTarget().getAsLocation(), org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND);
							cancel();
							entity.getAsAssociate().getTag().getPlayer().getPlayer().getWorld().playSound(entity.getAsAssociate().getTag().getPlayer().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
						}
					} else {
						getEntity().getAsAssociate().getMailer().chat(MessageFormat.format(teleportMsg, seconds)).deploy();
						this.state = State.TELEPORTING;
						this.accepted = new Date();
						TaskScheduler.of(() -> {
							if (!REQUESTS.contains(this)) return;
							if (getState() == State.TELEPORTING) {
								AssociateTeleportEvent event = ClanVentBus.call(new AssociateTeleportEvent(getEntity().getAsAssociate(), new TeleportationTarget(this.location)));
								if (!event.isCancelled()) {
									successOperators.forEach(operator -> operator.onTeleportSuccess(entity));
									entity.getAsAssociate().getTag().getPlayer().getPlayer().teleport(event.getTarget().getAsLocation(), org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND);
									cancel();
									entity.getAsAssociate().getTag().getPlayer().getPlayer().getWorld().playSound(entity.getAsAssociate().getTag().getPlayer().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
								}
							} else {
								cancel();
							}
						}).scheduleLater(20 * 10);
					}
				}
			} else {
				if (entity.isClan()) {
					if (this.target != null) {
						getEntity().getAsClan().getMembers().forEach(a -> {
							if (a.getTag().isPlayer() && !a.getTag().getPlayer().isOnline()) return;
							a.getMailer().chat(MessageFormat.format(teleportMsg, seconds)).deploy();
							Clan.ACTION.sendMessage(getTarget().getAsPlayer(), "&a" + a.getName() + " is teleporting to you.");
							this.state = State.TELEPORTING;
							this.accepted = new Date();
							TaskScheduler.of(() -> {
								if (!REQUESTS.contains(this)) return;
								if (getState() == State.TELEPORTING) {
									AssociateTeleportEvent event = ClanVentBus.call(new AssociateTeleportEvent(a, new TeleportationTarget(this.target)));
									if (!event.isCancelled()) {
										successOperators.forEach(operator -> operator.onTeleportSuccess(a));
										a.getTag().getPlayer().getPlayer().teleport(event.getTarget().getAsPlayer(), org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.COMMAND);
										cancel();
										a.getTag().getPlayer().getPlayer().getWorld().playSound(entity.getAsAssociate().getTag().getPlayer().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
									}
								} else {
									cancel();
								}
							}).scheduleLater(20 * 10);
						});
					} else {
						getEntity().getAsClan().getMembers().forEach(a -> {
							if (a.getTag().isPlayer() && !a.getTag().getPlayer().isOnline()) return;
							a.getMailer().chat(MessageFormat.format(teleportMsg, seconds)).deploy();
							this.state = State.TELEPORTING;
							this.accepted = new Date();
							TaskScheduler.of(() -> {
								if (!REQUESTS.contains(this)) return;
								if (getState() == State.TELEPORTING) {
									AssociateTeleportEvent event = ClanVentBus.call(new AssociateTeleportEvent(a, new TeleportationTarget(this.location)));
									if (!event.isCancelled()) {
										successOperators.forEach(operator -> operator.onTeleportSuccess(a));
										a.getTag().getPlayer().getPlayer().teleport(event.getTarget().getAsLocation());
										cancel();
										a.getTag().getPlayer().getPlayer().getWorld().playSound(entity.getAsAssociate().getTag().getPlayer().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
									}
								} else {
									cancel();
								}
							}).scheduleLater(20 * 10);
						});
					}
				}
				if (entity.isPlayer()) {
					start = getEntity().getAsPlayer().getPlayer().getLocation();
					if (this.target != null) {
						Clan.ACTION.sendMessage(getEntity().getAsPlayer().getPlayer(), MessageFormat.format(teleportMsg, seconds));
						Clan.ACTION.sendMessage(getTarget().getAsPlayer(), "&a" + entity.getAsAssociate().getName() + " is teleporting to you.");
						this.state = State.TELEPORTING;
						this.accepted = new Date();
						TaskScheduler.of(() -> {
							if (!REQUESTS.contains(this)) return;
							if (getState() == State.TELEPORTING) {
								PlayerTeleportEvent event = ClanVentBus.call(new PlayerTeleportEvent(getEntity().getAsPlayer().getPlayer(), new TeleportationTarget(this.target)));
								if (!event.isCancelled()) {
									successOperators.forEach(operator -> operator.onTeleportSuccess(entity));
									getEntity().getAsPlayer().getPlayer().teleport(event.getTarget().getAsPlayer());
									cancel();
									getEntity().getAsPlayer().getPlayer().getWorld().playSound(getEntity().getAsPlayer().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
								}
							} else {
								cancel();
							}
						}).scheduleLater(20 * 10);
					} else {
						Clan.ACTION.sendMessage(getEntity().getAsPlayer().getPlayer(), MessageFormat.format(teleportMsg, seconds));
						this.state = State.TELEPORTING;
						this.accepted = new Date();
						TaskScheduler.of(() -> {
							if (!REQUESTS.contains(this)) return;
							if (getState() == State.TELEPORTING) {
								PlayerTeleportEvent event = ClanVentBus.call(new PlayerTeleportEvent(getEntity().getAsPlayer().getPlayer(), new TeleportationTarget(this.location)));
								if (!event.isCancelled()) {
									successOperators.forEach(operator -> operator.onTeleportSuccess(entity));
									getEntity().getAsPlayer().getPlayer().teleport(event.getTarget().getAsLocation());
									cancel();
									getEntity().getAsPlayer().getPlayer().getWorld().playSound(getEntity().getAsPlayer().getPlayer().getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 10, 1);
								}
							} else {
								cancel();
							}
						}).scheduleLater(20 * 10);
					}
				}
			}
		}

		public void cancel() {
			setState(State.EXPIRED);
			REQUESTS.remove(this);
		}
	}

	public enum State {
		INITIALIZED, TELEPORTING, EXPIRED
	}

}
