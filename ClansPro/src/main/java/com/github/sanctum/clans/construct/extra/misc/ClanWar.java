package com.github.sanctum.clans.construct.extra.misc;

import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.construct.extra.cooldown.CooldownArena;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.library.HFEncoded;
import com.github.sanctum.labyrinth.library.StringUtils;
import com.github.sanctum.labyrinth.task.Schedule;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ClanWar {

	private final Clan clan;
	private Clan targeted;
	private int points;
	private int time;
	private boolean isRed;
	private final LinkedList<Player> participants = new LinkedList<>();
	public static final Map<Player, Location> backTrack = new HashMap<>();

	public ClanWar(Clan clan) {
		this.clan = clan;
	}

	public CooldownArena getArenaTimer() {
		return DefaultClan.action.arena;
	}

	/**
	 * Check if the war is active. (If the configuration file for the current war exists)
	 *
	 * @return Whether or not the war is active
	 */
	public boolean warActive() {
		return ClansAPI.getData().arenaFile().exists();
	}

	/**
	 * @return true if the clans war team is red other was blue for false.
	 */
	public boolean isRed() {
		return isRed;
	}

	/**
	 * Get whether the clans team is red or blue as a string.
	 *
	 * @return The clans war team label.
	 */
	public String getTeam() {
		return isRed() ? "Red" : "Blue";
	}

	public void setMatchLength(int time) {
		this.time = time;
	}

	public void request(Clan target, int time) {
		if (target instanceof DefaultClan) {
			if (!clan.getWarInvites().contains(target)) {
				ClanWar action = new ClanWar(target);
				action.setTargeted(clan);
				action.setMatchLength(time);
				((DefaultClan) target).setCurrentWar(action);
				this.targeted = target;
				this.time = time;
				clan.getWarInvites().add(target);
			}
		}
	}

	public void deny(Clan target) {
		if (target instanceof DefaultClan) {
			if (target.getWarInvites().contains(clan)) {
				((DefaultClan) target).setCurrentWar(null);
				target.getWarInvites().removeIf(c -> c.equals(clan));
				((DefaultClan) clan).setCurrentWar(null);
			} else {
				// not invited
				this.clan.broadcast("&c&oWe cannot deny a war we have not been included in.");
			}
		}
	}

	public void setTargeted(Clan targeted) {
		this.targeted = targeted;
	}

	public void accept(Clan target) {
		Clan clan = this.clan;
		if (warActive()) {
			target.broadcast("&c&oA war is currently active.");
			clan.broadcast("&c&oA war is currently active.");
			return;
		}
		if (target.getWarInvites().contains(clan)) {
			target.getWarInvites().clear();
			clan.getWarInvites().clear();
			((DefaultClan) target).getCurrentWar().setTargeted(clan);
			CooldownArena c = getArenaTimer();
			c.save();
			int seconds = ClansAPI.getData().getInt("Clans.war.wait-time-seconds");
			long minutes = TimeUnit.SECONDS.toMinutes(seconds);
			clan.broadcast("&6&oOur clan war is starting in &f" + minutes + " &6&ominute(s) & &f" + seconds + " &6&osecond(s).");
			for (String id : clan.getMembersList()) {
				UUID user = UUID.fromString(id);
				OfflinePlayer p = Bukkit.getOfflinePlayer(user);
				if (p.isOnline()) {
					p.getPlayer().sendTitle(StringUtils.use("&3&lPREP FOR WAR!").translate(), StringUtils.use("&7A war involving your clan is starting!").translate(), 30, 100, 30);
				}
			}
			target.broadcast("&6&oOur clan war is starting in &f" + minutes + " &6&ominute(s) & &f" + seconds + " &6&osecond(s).");
			for (String id : target.getMembersList()) {
				UUID user = UUID.fromString(id);
				OfflinePlayer p = Bukkit.getOfflinePlayer(user);
				if (p.isOnline()) {
					p.getPlayer().sendTitle(StringUtils.use("&3&lPREP FOR WAR!").translate(), StringUtils.use("&7A war involving your clan is starting!").translate(), 30, 100, 30);
				}
			}
			Schedule.sync(() -> Schedule.sync(() -> {
				Schedule.sync(() -> {
					Schedule.sync(() -> {
						Schedule.sync(() -> {
							Schedule.sync(() -> {
								Schedule.sync(() -> {
									for (Player p : Bukkit.getOnlinePlayers()) {
										DefaultClan.action.sendMessage(p, "&4&oWar &6between &4" + clan.getName() + "&6 and &4" + target.getName() + " &a&lSTARTING");
										p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 5);
									}
									c.setTime(time);
									c.setCooldown();
									// teleport to red
									try {
										this.isRed = true;
										Location red = (Location) new HFEncoded(ClansAPI.getData().arenaRedTeamFile().getConfig().getString("spawn")).deserialized();
										for (String id : clan.getMembersList()) {
											OfflinePlayer mem = Bukkit.getOfflinePlayer(UUID.fromString(id));
											if (mem.isOnline()) {
												backTrack.put(mem.getPlayer(), mem.getPlayer().getLocation());
												mem.getPlayer().teleport(red);
												mem.getPlayer().setHealth(20.00);
												mem.getPlayer().setFoodLevel(20);
												participants.add(mem.getPlayer());
											}
										}
									} catch (IOException | ClassNotFoundException e) {
										e.printStackTrace();
									}
									// teleport to blue
									try {
										Location blue = (Location) new HFEncoded(ClansAPI.getData().arenaBlueTeamFile().getConfig().getString("spawn")).deserialized();
										for (String id : target.getMembersList()) {
											OfflinePlayer mem = Bukkit.getOfflinePlayer(UUID.fromString(id));
											if (mem.isOnline()) {
												backTrack.put(mem.getPlayer(), mem.getPlayer().getLocation());
												mem.getPlayer().teleport(blue);
												mem.getPlayer().setHealth(20.00);
												mem.getPlayer().setFoodLevel(20);
												((DefaultClan) target).getCurrentWar().getParticipants().add(mem.getPlayer());
											}
										}
									} catch (IOException | ClassNotFoundException e) {
										e.printStackTrace();
									}
								}).wait(20);
								for (Player p : Bukkit.getOnlinePlayers()) {
									DefaultClan.action.sendMessage(p, "&4&oWar &6between &4" + clan.getName() + "&6 and &4" + target.getName() + " &6in &l1");
									p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 3);
								}
							}).wait(20);
							for (Player p : Bukkit.getOnlinePlayers()) {
								DefaultClan.action.sendMessage(p, "&4&oWar &6between &4" + clan.getName() + "&6 and &4" + target.getName() + " &6in &l2");
								p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 3);
							}
						}).wait(20);
						for (Player p : Bukkit.getOnlinePlayers()) {
							DefaultClan.action.sendMessage(p, "&4&oWar &6between &4" + clan.getName() + "&6 and &4" + target.getName() + " &6in &l3");
							p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 3);
						}
					}).wait(20);
					for (Player p : Bukkit.getOnlinePlayers()) {
						DefaultClan.action.sendMessage(p, "&4&oWar &6between &4" + clan.getName() + "&6 and &4" + target.getName() + " &6in &l4");
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 3);
					}
				}).wait(20);
				for (Player p : Bukkit.getOnlinePlayers()) {
					DefaultClan.action.sendMessage(p, "&4&oWar &6between &4" + clan.getName() + "&6 and &4" + target.getName() + " &6in &l5");
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 10, 3);
				}
			}).wait(20)).wait(20 * seconds);
		} else {
			// not invited
			clan.broadcast("&c&oWe cannot start a war we are not included in.");
		}
	}

	public void addPoint() {
		this.points++;
	}

	public void takePoint() {
		if (this.points != 0) {
			this.points--;
		}
	}

	public int getPoints() {
		return this.points;
	}

	public List<Player> getParticipants() {
		return this.participants;
	}

	public void conclude() {
		// get participants
		for (Player par : participants) {
			par.teleport(backTrack.get(par));
		}
		for (Player par : ((DefaultClan) getTargeted()).getCurrentWar().getParticipants()) {
			par.teleport(backTrack.get(par));
		}
		FileManager arena = ClansAPI.getData().arenaFile();
		Clan other = getTargeted();
		Clan clan = this.clan;
		clan.broadcast("&a&oWar over.");
		if (other != null) {
			other.broadcast("&a&oWar over.");
			((DefaultClan) other).setCurrentWar(null);
		}
		backTrack.clear();
		arena.delete();
		((DefaultClan) other).setCurrentWar(null);
	}

	public Clan getTargeted() {
		return this.targeted;
	}


}
