package com.github.sanctum.clans.bridge.internal.kingdoms;

import com.github.sanctum.clans.bridge.ClanAddon;
import com.github.sanctum.clans.bridge.internal.KingdomAddon;
import com.github.sanctum.clans.bridge.internal.kingdoms.achievement.KingdomAchievement;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.FileManager;
import com.github.sanctum.labyrinth.data.FileType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoundTable extends Progressable {

	private final String name;

	private final Map<UUID, Rank> USERS = new HashMap<>();

	private final Set<UUID> INVITES = new HashSet<>();

	private final List<KingdomAchievement> ACHIEVEMENTS = new LinkedList<>();

	public RoundTable(KingdomAddon cycle) {

		this.name = ClansAPI.getData().getConfigString("Addon.Kingdoms.roundtable.name");

		FileManager data = cycle.getFile(FileType.JSON, "achievements", "data");

		FileManager users = cycle.getFile(FileType.JSON, "users", "data");

		if (data.getRoot().exists()) {
			if (data.getRoot().isNode("memory.table")) {
				for (String name : data.getRoot().getNode("memory.table").getKeys(false)) {
					if (!loadAchievement(KingdomAchievement.newInstance(name, data.getRoot().getString("memory.table." + name + ".info"), data.getRoot().getDouble("memory.table." + name + ".progression"), data.getRoot().getDouble("memory.table." + name + ".requirement")))) {
						cycle.getLogger().log(Level.SEVERE, "- Unable to load achievement " + name);
					}
				}
			}
		}

		if (users.getRoot().exists()) {

			if (!users.getRoot().getKeys(false).isEmpty()) {
				for (String user : users.getRoot().getKeys(false)) {
					UUID id = UUID.fromString(user);
					this.USERS.put(id, Rank.valueOf(users.getRoot().getString(user + ".rank")));
				}
			}

		}

		if (ACHIEVEMENTS.isEmpty()) {
			loadAchievement(Kingdom.getDefaultAchievements());
		}

		PROGRESSABLES.add(this);
	}

	public enum Permission {

		INVITE(3),
		TAG(2),
		PROMOTE(4),
		DEMOTE(4),
		KICK(4);

		private final int requirement;

		Permission(int requirement) {
			this.requirement = requirement;
		}

		public int getRequirement() {
			return this.requirement;
		}

		public boolean test(Clan.Associate associate) {
			return associate.getPriority().toInt() >= requirement;
		}

	}

	public enum Rank {

		LOW(1),
		HIGH(2),
		HIGHER(3),
		HIGHEST(4);

		private final int level;

		Rank(int level) {
			this.level = level;
		}

		public int getLevel() {
			return level;
		}
	}

	public boolean isMember(UUID target) {
		return USERS.containsKey(target);
	}

	public boolean isInvited(UUID target) {
		return this.INVITES.contains(target);
	}

	public boolean invite(UUID target) {
		if (isMember(target)) return false;
		if (this.INVITES.contains(target)) return false;
		return this.INVITES.add(target);
	}

	public boolean join(UUID target) {
		if (!this.INVITES.contains(target)) return false;

		if (isMember(target)) return false;

		take(target, Rank.LOW);
		this.INVITES.remove(target);

		return true;
	}

	public void take(UUID target, Rank rank) {
		this.USERS.put(target, rank);
	}

	public boolean leave(UUID target) {
		if (!isMember(target)) return false;
		USERS.remove(target);
		return true;
	}

	@Override
	public @NotNull String getName() {
		return this.name;
	}

	@Override
	public int getLevel() {
		int level = 1;
		for (KingdomAchievement achievement : ACHIEVEMENTS) {
			if (achievement.isComplete()) {
				level += 1;
			}
		}
		return level;
	}

	@Override
	public @Nullable KingdomAchievement getAchievement(String title) {
		return getAchievements().stream().filter(a -> a.getTitle().equalsIgnoreCase(title)).findFirst().orElse(null);
	}

	public Set<UUID> getUsers() {

		return this.USERS.keySet();

	}

	public Rank getRank(UUID user) {
		return this.USERS.get(user);
	}

	@Override
	public @NotNull List<KingdomAchievement> getAchievements() {
		return ACHIEVEMENTS;
	}

	public List<Claim> getLandPool() {
		List<Claim> list = new LinkedList<>();
		for (UUID id : this.USERS.keySet()) {
			Clan c = ClansAPI.getInstance().getClan(id);
			if (c != null) {
				list.addAll(Arrays.asList(c.getOwnedClaims()));
			} else {
				leave(id);
			}
		}
		return list;
	}

	public boolean isOurs(Claim c) {
		return getLandPool().contains(c);
	}

	@Override
	public void loadAchievement(KingdomAchievement... achievement) {

		for (KingdomAchievement achiev : achievement) {
			loadAchievement(achiev);
		}

	}

	@Override
	public boolean loadAchievement(KingdomAchievement achievement) {

		if (this.ACHIEVEMENTS.stream().noneMatch(a -> a.getTitle().equalsIgnoreCase(achievement.getTitle()))) {
			achievement.setParent(this);
			return this.ACHIEVEMENTS.add(achievement);
		}

		return false;

	}

	@Override
	public void save(ClanAddon cycle) {

		FileManager users = cycle.getFile(FileType.JSON, "users", "data");

		for (Map.Entry<UUID, Rank> entry : this.USERS.entrySet()) {
			users.getRoot().set(entry.getKey().toString() + ".rank", entry.getValue().name());
		}

		users.getRoot().save();

		for (KingdomAchievement achievement : getAchievements()) {
			achievement.saveProgress("memory.table");
		}

	}


}
